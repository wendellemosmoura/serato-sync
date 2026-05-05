package main.java.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SeratoPathUtils {

    private static final String SERATO_DIR = "_Serato_";
    private static final String SUBCRATES  = "Subcrates";

    /**
     * Automatically detects the system's default _Serato_ folder.
     * Mac:     ~/Music/_Serato_
     * Windows: C:\_Serato_  (fallback to %USERPROFILE%\Music\_Serato_)
     */
    public static Path detectSeratoLibraryPath() {
        String home = System.getProperty("user.home");

        if (OsUtils.isMac()) {
            return Paths.get(home, "Music", SERATO_DIR);
        }

        // Windows - try the root of drive C first, then the user's Music folder
        Path windowsRoot = Paths.get("C:\\", SERATO_DIR);
        if (windowsRoot.toFile().exists()) return windowsRoot;

        return Paths.get(home, "Music", SERATO_DIR);
    }

    public static Path getSubcratesPath(Path seratoLibraryPath) {
        return seratoLibraryPath.resolve(SUBCRATES);
    }

    public static void ensureSubcratesExists(Path seratoLibraryPath) {
        File subcrates = getSubcratesPath(seratoLibraryPath).toFile();
        if (!subcrates.exists()) subcrates.mkdirs();
    }
}