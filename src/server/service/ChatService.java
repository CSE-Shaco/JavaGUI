package server.service;

import server.domain.ChatRoom;
import server.session.ClientSession;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Service class managing chat rooms and random chat matching.
 */
public class ChatService {

    private final Map<String, ChatRoom> roomMap = new ConcurrentHashMap<>();
    private final Queue<ClientSession> waitingQueue = new LinkedBlockingQueue<>();

    /**
     * Returns an existing room or creates one if not found.
     */
    public ChatRoom getOrCreateRoom(String roomId) {
        return roomMap.computeIfAbsent(roomId, ChatRoom::new);
    }

    /**
     * Returns a room by its ID, or null if not found.
     */
    public ChatRoom getRoomById(String roomId) {
        return roomMap.get(roomId);
    }

    /**
     * Removes the session from any room it belongs to, and deletes empty rooms.
     */
    public void removeSession(ClientSession session) {
        for (ChatRoom room : roomMap.values()) {
            if (room.getSessions().contains(session)) {
                room.removeSession(session);
                if (room.getParticipantCount() == 0) {
                    roomMap.remove(room.getRoomId());
                }
                break;
            }
        }
    }

    /**
     * Returns a map of non-anonymous room IDs to participant counts.
     */
    public Map<String, Integer> getRoomList() {
        return roomMap.entrySet().stream()
                .filter(entry -> !entry.getValue().isAnonymous()) // exclude anonymous rooms
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getParticipantCount()
                ));
    }

    /**
     * Starts a random chat if a match is found; otherwise, adds to waiting queue.
     */
    public synchronized ChatRoom startRandomChat(ClientSession requester) {
        if (waitingQueue.contains(requester)) {
            return null; // do nothing if already waiting
        }

        if (!waitingQueue.isEmpty()) {
            ClientSession partner = waitingQueue.poll();

            // create a new room at this point
            String roomId = "anonymous-" + System.currentTimeMillis();
            ChatRoom room = new ChatRoom(roomId, true);
            room.addSession(partner);
            room.addSession(requester);
            roomMap.put(roomId, room);

            return room;
        } else {
            // no one is waiting, so add to queue
            waitingQueue.offer(requester);
            return null;
        }
    }

    /**
     * Removes a session from the random chat waiting queue.
     */
    public synchronized void cancelWaiting(ClientSession session) {
        waitingQueue.remove(session);
    }
}
