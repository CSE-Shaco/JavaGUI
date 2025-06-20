package server.domain;

import server.session.ClientSession;
import shared.dto.ServerResponse;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        return sessions.stream().filter(session -> session.getUser() != null && session.getUser().getUserId().equals(username)).findFirst().orElse(null);
    }

    public void broadcastMessage(Object message) {
        for (ClientSession session : sessions) {
            System.out.println("broadcast message: " + message);
            if (session.getClientHandler() != null) {
                session.getClientHandler().sendMessage(message);
            }
        }
    }

    public void broadcastFile(ServerResponse response) {
        for (ClientSession session : sessions) {
            session.getFileHandler().sendFile(response);
        }
    }

    public Set<ClientSession> getSessions() {
        return sessions;
    }
}