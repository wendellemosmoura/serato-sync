package main.java.model;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SyncFolder {
    private final Path path;

    public SyncFolder(Path path) { this.path = path; }
    public SyncFolder(String path) { this.path = Paths.get(path); }

    public Path getPath()         { return path; }

    @Override
    public String toString()      { return path.toString(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyncFolder)) return false;
        return path.equals(((SyncFolder) o).path);
    }

    @Override
    public int hashCode() { return path.hashCode(); }
}