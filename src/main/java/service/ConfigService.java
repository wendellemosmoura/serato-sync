package main.java.service;

import main.java.io.ConfigRepository;
import main.java.model.AppConfig;
import main.java.model.SyncFolder;
import main.java.util.SeratoPathUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Wendel Lemos Moura
 */

public class ConfigService {

    private final ConfigRepository repository = new ConfigRepository();
    private final AppConfig config;

    public ConfigService() {
        AppConfig loaded;
        try {
            loaded = repository.load();
        } catch (IOException e) {
            System.err.println("Aviso: não foi possível carregar config, usando padrões. " + e.getMessage());
            loaded = new AppConfig();
            loaded.setSeratoLibraryPath(SeratoPathUtils.detectSeratoLibraryPath());
        }
        this.config = loaded;
    }

    public AppConfig getConfig() {
        return config;
    }

    public void setDarkMode(boolean dark) {
        config.setDarkMode(dark);
        persist();
    }

    public void addFolder(SyncFolder folder) {
        config.addFolder(folder);
        persist();
    }

    public void removeFolder(SyncFolder folder) {
        config.removeFolder(folder);
        persist();
    }

    public void setSeratoLibraryPath(Path path) {
        config.setSeratoLibraryPath(path);
        persist();
    }

    private void persist() {
        try {
            repository.save(config);
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração: " + e.getMessage());
        }
    }
}
