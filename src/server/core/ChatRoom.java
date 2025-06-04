package server.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A chat room for one or more participants.
 */
public class ChatRoom {

    private final String roomId;
    private final Set<String> participants;
    private final List<String> messageLog;

    public ChatRoom(String roomId, Collection<String> initialParticipants) {
        this.roomId = roomId;
        this.participants = new HashSet<>(initialParticipants);
        this.messageLog = new ArrayList<>();
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isParticipant(String username) {
        return participants.contains(username);
    }

    public void addParticipant(String username) {
        participants.add(username);
    }

    public void removeParticipant(String username) {
        participants.remove(username);
    }

    public Set<String> getParticipants() {
        return Collections.unmodifiableSet(participants);
    }

    public void addMessage(String message) {
        messageLog.add(message);
        saveMessageToFile(message);  // 파일에 저장
    }


    public List<String> getMessageLog() {
        return Collections.unmodifiableList(messageLog);
    }

    public void saveMessageToFile(String message) {
        String fileName = "logs/" + String.join("_", participants) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            Logger.getLogger(ChatRoom.class.getName()).log(Level.WARNING, "Log file write failed", e);
        }
    }

}
