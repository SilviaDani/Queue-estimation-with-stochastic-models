package Utils;

public class Logger {
    public static boolean debugMode = true;

    public static void debug(String message) {
        if (debugMode) {
            System.out.println(message);
        }
    }
}