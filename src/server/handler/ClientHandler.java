package server.handler;

import server.domain.ChatRoom;
import server.service.ChatService;
import server.session.ClientSession;
import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.RoomListRequest;
import shared.dto.RoomListResponse;
import shared.dto.ServerResponse;
import shared.util.LoggerUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final ChatService chatService;
    private ClientSession session;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;
    }

    public void setSession(ClientSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object obj = in.readObject();

                if (obj instanceof RoomListRequest) {
                    Map<String, Integer> roomMap = chatService.getRoomList();
                    out.writeObject(new RoomListResponse(roomMap));
                    out.flush();
                }

                if (obj instanceof ClientRequest request) {
                    User user = request.getUser();
                    String roomId = request.getRoomId();

                    if (session == null) {
                        ChatRoom room = chatService.getOrCreateRoom(roomId);
                        session = new ClientSession(this, null); // FileHandler는 나중에 set
                        session.setUser(user);
                        room.addSession(session);
                        LoggerUtil.log(user.getDisplayName() + " joined room " + roomId);
                    }

                    handleRequest(request);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Client disconnected", e);
        } finally {
            if (session != null) {
                chatService.removeSession(session);
            }
            try {
                socket.close();
            } catch (Exception ignore) {}
        }
    }

    private void handleRequest(ClientRequest request) {
        String action = request.getAction();
        ChatRoom room = chatService.getRoomById(request.getRoomId());

        if (room == null || session == null) return;

        User user = session.getUser();

        switch (action) {
            case "sendMessage" -> {
                String content = request.getContent();
                LoggerUtil.log("[" + room.getRoomId() + "] " + user.getDisplayName() + ": " + content);
                ServerResponse response = new ServerResponse(user.getDisplayName(), content, room.getRoomId(), false);
                room.broadcastMessage(response);
            }
            case "listRooms" -> {
                try {
                    Map<String, Integer> rooms = chatService.getRoomList();
                    sendMessage(new RoomListResponse(rooms));
                } catch (Exception e) {
                    LoggerUtil.error("Failed to list rooms", e);
                }
            }
            case "join" -> {
                String msg = user.getDisplayName() + "님이 입장하셨습니다.";
                ServerResponse response = new ServerResponse("server", msg, room.getRoomId(), true);
                room.broadcastMessage(response);
            }
            case "quit" -> {
                String msg = user.getDisplayName() + "님이 퇴장하셨습니다.";
                ServerResponse response = new ServerResponse("server", msg, room.getRoomId(), true);
                room.broadcastMessage(response);
            }
        }
    }

    public void sendMessage(Object obj) {
        try {
            out.writeObject(obj);
            out.flush();
        } catch (Exception e) {
            LoggerUtil.error("메시지 전송 실패", e);
        }
    }
}