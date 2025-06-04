package server.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Saves chat logs of a ChatRoom to a text file.
 */
public class MessageLogger {

    private static final Logger logger = Logger.getLogger(MessageLogger.class.getName());
    private static final String LOG_DIR = "logs/";

    public static void saveChatLog(ChatRoom room) {
        String filename = generateLogFileName(room.getRoomId());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_DIR + filename, true))) {
            for (String message : room.getMessageLog()) {
                writer.write(message);
                writer.newLine();
            }
            logger.info("Saved chat log to " + filename);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save chat log for room: " + room.getRoomId(), e);
        }
    }

    private static String generateLogFileName(String roomId) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "chat_" + roomId + "_" + timestamp + ".txt";
    }
}
