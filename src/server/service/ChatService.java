package server.service;

import server.domain.ChatRoom;
import server.session.ClientSession;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatService {

    private final Map<String, ChatRoom> roomMap = new ConcurrentHashMap<>();

    public ChatRoom getOrCreateRoom(String roomId) {
        return roomMap.computeIfAbsent(roomId, ChatRoom::new);
    }

    public ChatRoom getRoomById(String roomId) {
        return roomMap.get(roomId);
    }

    public void removeSession(ClientSession session) {
        for (ChatRoom room : roomMap.values()) {
            room.removeSession(session);
            if (room.getParticipantCount() == 0) {
                roomMap.remove(room.getRoomId());
                break;
            }
        }
    }

    public Map<String, Integer> getRoomList() {
        return roomMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getParticipantCount()));
    }
}