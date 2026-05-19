package main.java.parser;

import java.io.File;

/**
 * @author Wendel Lemos Moura
 */

public class PlaylistParserFactory {

    public static PlaylistParser getParser(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".m3u") || name.endsWith(".m3u8")) {
            return new M3uParser();
        }
        if (name.endsWith(".wpl")) {
            return new WplParser();
        }

        throw new IllegalArgumentException(
                "Unsupported playlist format: " + file.getName()
        );
    }

    public static boolean isSupported(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".m3u")
                || name.endsWith(".m3u8")
                || name.endsWith(".wpl");
    }
}
