package server.core;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages multi-user chat rooms using a set of participants as the key.
 */
public class ChatRoomManager {

    private final Map<String, ChatRoom> roomMap;
    private static final String LOG_DIR = "logs";

    static {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (!created) {
                Logger.getLogger(ChatRoomManager.class.getName()).warning("Failed to create log directory.");
            }
        }
    }

    public ChatRoomManager() {
        this.roomMap = new ConcurrentHashMap<>();
    }

    public ChatRoom createRoom(Collection<String> participants) {
        String roomId = generateRoomId(participants);
        ChatRoom room = new ChatRoom(roomId, participants);
        roomMap.put(roomId, room);
        return room;
    }

    public ChatRoom getRoom(Collection<String> participants) {
        return roomMap.get(generateRoomId(participants));
    }

    public boolean hasRoom(Collection<String> participants) {
        return roomMap.containsKey(generateRoomId(participants));
    }

    public Collection<ChatRoom> getAllRooms() {
        return roomMap.values();
    }

    private String generateRoomId(Collection<String> participants) {
        List<String> sorted = new ArrayList<>(participants);
        Collections.sort(sorted);
        return String.join("_", sorted); // ex: alice_bob_charlie
    }
}
