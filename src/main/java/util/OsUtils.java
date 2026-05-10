package main.java.util;

/**
 * @author Wendel Lemos Moura
 */

public class OsUtils {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }
}