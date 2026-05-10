package main.java.fs;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author Wendel Lemos Moura
 */

public class FileScanner {
    private static final Set<String> AUDIO_EXTENSIONS;

    static {
        Set<String> s = new HashSet<>(Arrays.asList(
                "mp3", "wl.mp3", "flac", "aif", "aiff", "wav", "ogg", "m4a", "aac", "wma", "alac", "mp4", "mov", "avi"
        ));
        AUDIO_EXTENSIONS = Collections.unmodifiableSet(s);
    }

    /**
     * Recursively scans the root folder.
     *
     * @param rootFolder user added folder
     * @return sorted map of [folder] → [list of absolute track paths]
     * Only folders that contain at least one audio track are included.
     */
    public Map<Path, List<Path>> scan(Path rootFolder) throws IOException {
        Map<Path, List<Path>> result = new LinkedHashMap<>();

        Files.walkFileTree(rootFolder, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (isAudioFile(file)) {
                    result
                            .computeIfAbsent(file.getParent(), k -> new ArrayList<>())
                            .add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.println("Aviso: não foi possível ler " + file + " — " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });

        return result;
    }

    private boolean isAudioFile(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0) return false;
        return AUDIO_EXTENSIONS.contains(name.substring(dot + 1));
    }
}
