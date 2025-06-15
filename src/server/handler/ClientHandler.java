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
            // 반드시 ObjectOutputStream 먼저 생성
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            LoggerUtil.log("Client connected: " + socket);

            while (true) {
                Object obj = in.readObject();

                // 일반 클라이언트 요청
                if (obj instanceof ClientRequest request) {
                    handleClientRequest(request);
                    System.out.println("Client request Sent");
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Connection Lost", e);
        } finally {
            cleanup();
        }
    }

    private void handleClientRequest(ClientRequest request) {
        String roomId = request.getRoomId();
        String action = request.getAction();
        User user = request.getUser();

        // 최초 요청이면 세션 생성 및 방 등록
        if (session == null) {
            ChatRoom room = chatService.getOrCreateRoom(roomId);
            session = new ClientSession(this, null); // FileHandler는 나중에 set
            session.setUser(user);
            room.addSession(session);
            LoggerUtil.log(user.getDisplayName() + " joined room " + roomId);
        }

        ChatRoom room = chatService.getRoomById(roomId);
        if (room == null || session == null) return;

        switch (action) {
            case "join" -> {
                String msg = user.getDisplayName() + "님이 입장하셨습니다.";
                System.out.println(msg);
                ServerResponse response = new MessageResponse("", roomId, msg, true);
                room.broadcastMessage(response);
            }
            case "sendMessage" -> {
                String msg = request.getContent();
                ServerResponse response = new MessageResponse(user.getDisplayName(), roomId, msg, false);
                room.broadcastMessage(response);
            }
            case "quit" -> {
                String msg = user.getDisplayName() + "님이 퇴장하셨습니다.";
                ServerResponse response = new MessageResponse("", roomId, msg, true);
                room.broadcastMessage(response);
                chatService.removeSession(session);
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
            LoggerUtil.error("메시지 전송 실패", e);
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
