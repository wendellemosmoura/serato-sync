package main.java.model;

import java.io.File;

/**
 * @author Wendel Lemos Moura
 */

public class SyncPlaylist {
    private final File file;

    public SyncPlaylist(File file) {
        this.file = file;
    }

    public SyncPlaylist(String path) {
        this.file = new File(path);
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getName();
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyncPlaylist)) return false;
        return file.getAbsolutePath().equals(((SyncPlaylist) o).file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return file.getAbsolutePath().hashCode();
    }
}
