package server.handler;

import server.domain.ChatRoom;
import server.service.ChatService;
import server.session.ClientSession;
import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.MessageResponse;
import shared.dto.ServerResponse;
import shared.util.LoggerUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles a connected client and routes requests to ChatService.
 */
public class ClientHandler extends Thread {

    private final Socket socket;
    private final ChatService chatService;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ClientSession session;

    public ClientHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;
    }

    public ClientSession getSession() {
        return session;
    }

    public void setSession(ClientSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            // Always initialize ObjectOutputStream before ObjectInputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            LoggerUtil.log("Client connected: " + socket);

            while (true) {
                Object obj = in.readObject();

                // Process client request
                if (obj instanceof ClientRequest request) {
                    handleClientRequest(request);
                    System.out.println("Client request received");
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Connection lost", e);
        } finally {
            cleanup();
        }
    }

    private void handleClientRequest(ClientRequest request) {
        String roomId = request.getRoomId();
        String action = request.getAction();
        User user = request.getUser();
        ChatRoom room;

        if (session == null) {
            session = new ClientSession(this, null);
            session.setUser(user);
            if (!action.equals("start_random") && !action.equals("cancel_waiting")) {
                room = chatService.getOrCreateRoom(roomId);
                if (room.findSessionByUsername(user.getUserId()) != null) {
                    room.removeSessionByUsername(user.getUserId());
                }
                room.addSession(session);
            }
        }

        room = chatService.getRoomById(roomId);
        if ((room == null || session == null) && !action.equals("start_random") && !action.equals("cancel_waiting"))
            return;

        switch (action) {
            case "join" -> {
                String msg = room.isAnonymous()
                        ? "A user has joined the anonymous chat."
                        : user.getUsername() + " has entered the room.";
                System.out.println(msg);
                ServerResponse response = new MessageResponse("", "", roomId, msg, true);
                room.broadcastMessage(response);
            }
            case "sendMessage" -> {
                String msg = request.getContent();
                String displayName = room.isAnonymous() ? "unknown" : user.getUsername();
                ServerResponse response = new MessageResponse(user.getUserId(), displayName, roomId, msg, false);
                room.broadcastMessage(response);
            }
            case "quit" -> {
                String msg = (room.isAnonymous() ? "unknown" : user.getUsername()) + " has left the room.";
                ServerResponse response = new MessageResponse("", "", roomId, msg, true);
                room.broadcastMessage(response);
                chatService.removeSession(session);
            }
            case "start_random" -> {
                ChatRoom matchedRoom = chatService.startRandomChat(session);
                if (matchedRoom != null) {
                    ServerResponse systemMsg = new MessageResponse("", "", matchedRoom.getRoomId(), "matched", true);
                    matchedRoom.broadcastMessage(systemMsg);
                    LoggerUtil.log("Anonymous chat started: " + matchedRoom.getRoomId());
                }
            }
            case "cancel_waiting" -> {
                chatService.cancelWaiting(session);
                LoggerUtil.log(user.getUsername() + " cancelled from waiting queue.");
            }
        }
    }

    public void sendMessage(Object obj) {
        try {
            synchronized (out) {
                out.writeObject(obj);
                out.flush();
            }
        } catch (Exception e) {
            LoggerUtil.error("Failed to send message", e);
        }
    }

    private void cleanup() {
        if (session != null) {
            chatService.removeSession(session);
        }
        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }
}
