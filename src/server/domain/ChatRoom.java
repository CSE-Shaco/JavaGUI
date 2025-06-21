package server.domain;

import server.session.ClientSession;
import shared.dto.ServerResponse;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a chat room that holds multiple client sessions.
 */
public class ChatRoom {

    private final String roomId;
    private final boolean anonymous;
    private final Set<ClientSession> sessions = ConcurrentHashMap.newKeySet();

    public ChatRoom(String roomId) {
        this(roomId, false);
    }

    public ChatRoom(String roomId, boolean anonymous) {
        this.roomId = roomId;
        this.anonymous = anonymous;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void addSession(ClientSession session) {
        sessions.add(session);
    }

    public void removeSession(ClientSession session) {
        sessions.remove(session);
    }

    public void removeSessionByUsername(String username) {
        sessions.removeIf(session -> session.getUsername().equals(username));
    }

    public int getParticipantCount() {
        return sessions.size();
    }

    public ClientSession findSessionByUsername(String username) {
        return sessions.stream()
                .filter(session -> session.getUser() != null && session.getUser().getUserId().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Broadcasts a generic message object to all connected sessions.
     *
     * @param message The message to broadcast
     */
    public void broadcastMessage(Object message) {
        for (ClientSession session : sessions) {
            System.out.println("broadcast message: " + message); // For debugging; replace with logger if needed
            if (session.getClientHandler() != null) {
                session.getClientHandler().sendMessage(message);
            }
        }
    }

    /**
     * Broadcasts a file response to all connected sessions.
     *
     * @param response The ServerResponse containing file data
     */
    public void broadcastFile(ServerResponse response) {
        for (ClientSession session : sessions) {
            session.getFileHandler().sendFile(response);
        }
    }

    public Set<ClientSession> getSessions() {
        return sessions;
    }
}
