package server.domain;

import server.session.ClientSession;
import shared.domain.FileInfo;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {

    private final String roomId;
    private final Set<ClientSession> sessions = ConcurrentHashMap.newKeySet();

    public ChatRoom(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void addSession(ClientSession session) {
        sessions.add(session);
    }

    public void removeSession(ClientSession session) {
        sessions.remove(session);
    }

    public int getParticipantCount() {
        return sessions.size();
    }

    public void broadcastMessage(Object message) {
        System.out.println("broadcast : " + message);

        for (ClientSession session : sessions) {
            System.out.println("broadcast message: " + message);
            if (session.getClientHandler() != null) {
                session.getClientHandler().sendMessage(message);
            }
        }
    }

    public void broadcastFile(FileInfo fileInfo) {
        for (ClientSession session : sessions) {
            session.getFileHandler().sendFile(fileInfo);
        }
    }
}