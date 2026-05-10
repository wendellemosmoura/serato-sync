package main.java.io;

import main.java.model.AppConfig;
import main.java.model.SyncFolder;
import main.java.util.SeratoPathUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * @author Wendel Lemos Moura
 */

public class ConfigRepository {

    private static final String CONFIG_DIR  = ".serato-sync";
    private static final String CONFIG_FILE = "config.properties";

    private static final String KEY_DARK_MODE = "ui.dark.mode";
    private static final String KEY_SERATO_PATH   = "serato.library.path";
    private static final String KEY_FOLDERS_COUNT = "sync.folders.count";
    private static final String KEY_FOLDER_PREFIX = "sync.folder.";

    private final Path configFilePath;

    public ConfigRepository() {
        String home = System.getProperty("user.home");
        this.configFilePath = Paths.get(home, CONFIG_DIR, CONFIG_FILE);
    }

    public void save(AppConfig config) throws IOException {
        Files.createDirectories(configFilePath.getParent());

        Properties props = new Properties();

        props.setProperty(KEY_DARK_MODE, String.valueOf(config.isDarkMode()));

        if (config.getSeratoLibraryPath() != null) {
            props.setProperty(KEY_SERATO_PATH, config.getSeratoLibraryPath().toString());
        }

        List<SyncFolder> folders = config.getSyncFolders();
        props.setProperty(KEY_FOLDERS_COUNT, String.valueOf(folders.size()));
        for (int i = 0; i < folders.size(); i++) {
            props.setProperty(KEY_FOLDER_PREFIX + i, folders.get(i).toString());
        }

        try (OutputStream out = new FileOutputStream(configFilePath.toFile())) {
            props.store(out, "serato-sync configuration");
        }
    }

    public AppConfig load() throws IOException {
        AppConfig config = new AppConfig();

        if (!Files.exists(configFilePath)) {
            config.setSeratoLibraryPath(SeratoPathUtils.detectSeratoLibraryPath());
            return config;
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configFilePath.toFile())) {
            props.load(in);
        }

        config.setDarkMode(Boolean.parseBoolean(props.getProperty(KEY_DARK_MODE, "false")));

        String seratoPath = props.getProperty(KEY_SERATO_PATH);
        config.setSeratoLibraryPath(
                seratoPath != null
                        ? Paths.get(seratoPath)
                        : SeratoPathUtils.detectSeratoLibraryPath()
        );

        int count = Integer.parseInt(props.getProperty(KEY_FOLDERS_COUNT, "0"));
        for (int i = 0; i < count; i++) {
            String folderPath = props.getProperty(KEY_FOLDER_PREFIX + i);
            if (folderPath != null && !folderPath.trim().isEmpty()) {
                config.addFolder(new SyncFolder(folderPath));
            }
        }

        return config;
    }
}