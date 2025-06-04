package server.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatLogger {
    private static final Logger logger = Logger.getLogger(ChatLogger.class.getName());
    private static final String LOG_DIR = "logs";

    public static void log(String hashedRoomId, String message) {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists() && !logDir.mkdirs()) {
            logger.warning("Failed to create log directory");
            return;
        }

        File logFile = new File(logDir, hashedRoomId + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to write log", e);
        }
    }
}