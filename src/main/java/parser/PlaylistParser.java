package main.java.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Wendel Lemos Moura
 */

public interface PlaylistParser {
    List<String> parse(File file) throws IOException;
}