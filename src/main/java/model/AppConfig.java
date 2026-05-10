package main.java.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Wendel Lemos Moura
 */

public class AppConfig {
    private final List<SyncFolder> syncFolders = new ArrayList<>();
    private Path seratoLibraryPath;
    private boolean darkMode = false;

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
    }

    public void addFolder(SyncFolder folder) {
        if (!syncFolders.contains(folder)) syncFolders.add(folder);
    }

    public void removeFolder(SyncFolder folder) {
        syncFolders.remove(folder);
    }

    public List<SyncFolder> getSyncFolders() {
        return Collections.unmodifiableList(syncFolders);
    }

    public Path getSeratoLibraryPath() {
        return seratoLibraryPath;
    }

    public void setSeratoLibraryPath(Path path) {
        this.seratoLibraryPath = path;
    }
}