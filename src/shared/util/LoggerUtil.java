package shared.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {

    private static final Logger logger = Logger.getLogger("ChatLogger");

    static {
        logger.setUseParentHandlers(false); // 기본 콘솔 출력 방지

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);

        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    public static void log(String message) {
        logger.info(message);
    }

    public static void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
}