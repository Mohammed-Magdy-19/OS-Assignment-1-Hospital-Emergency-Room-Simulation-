package hospital.util;

/**
 * Logger
 */
public class Logger {

    private static final boolean DEBUG = false;  // set true only while testing

    private Logger () {}                         // utility class, no instances

    /** Prints one line, or several lines as one atomic block (nothing can be printed in between). */
    public static synchronized void log(String... lines) {
        for (String line : lines) {
            if (DEBUG) {
                System.out.println("[" + Thread.currentThread().getName() + "] " + line);
            } else {
                System.out.println(line);
            }
        }
    }
}