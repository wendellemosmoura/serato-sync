package main.java.io;

import main.java.util.OsUtils;
import main.java.util.SeratoPathUtils;

import java.nio.file.Path;
import java.util.StringJoiner;

/**
 * @author Wendel Lemos Moura
 */

public class CratePathResolver {

    /**
     * Converts a directory hierarchy into the filename .crate.
     *
     * Example:
     *   rootFolder  = /Music/Electronic
     *   targetFolder = /Music/Electronic/House/House 00's
     *   result   = "Electronic%%House%%House 00's"
     *
     * @param rootFolder   root folder that the user added in the UI.
     * @param targetFolder pasta that is being transformed into a crate
     * @return filename .crate (without extension)
     */
    public String resolveCrateName(Path rootFolder, Path targetFolder) {
        Path base = rootFolder.getParent();
        if (base == null) base = rootFolder.getRoot();

        Path relative = base.relativize(targetFolder);

        StringJoiner joiner = new StringJoiner("%%");
        for (Path segment : relative) {
            joiner.add(segment.toString());
        }

        return joiner.toString();
    }

    /**
     * Returns the complete destination file for the .crate.
     *
     * @param seratoLibraryPath path to the Serato library (where is _Serato_ located)
     * @param crateName         Name resolved by resolveCrateName()
     */
    public Path resolveCrateFile(Path seratoLibraryPath, String crateName) {
        Path subcrates = SeratoPathUtils.getSubcratesPath(seratoLibraryPath);
        return subcrates.resolve(crateName + ".crate");
    }

    /**
     * Normalizes the absolute path of a track to the format that Serato
     * expects within the ptrk field (relative, no drive letter, forward slashes '/').
     * Windows: "D:/Music/House/track.mp3"  →  "Music/House/track.mp3"
     * Mac:     "/Volumes/HD/Music/track.mp3" →  "Music/track.mp3"
     *          "/Users/dj/Music/track.mp3"  →  "Users/dj/Music/track.mp3"
     */
    public String normalizeTrackPath(Path absoluteTrackPath) {
        String path = absoluteTrackPath.toString();

        path = path.replace("\\", "/");

        if (OsUtils.isWindows()) {
            path = path.replaceAll("^[A-Za-z]:/", "");
        } else {
            path = path.replaceAll("^/Volumes/[^/]+/", "");
            path = path.replaceAll("^/", "");
        }

        return path;
    }
}
