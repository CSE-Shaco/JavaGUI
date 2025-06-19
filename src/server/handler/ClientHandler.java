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
import java.util.Random;

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
        ChatRoom room;

        if (session == null) {
            session = new ClientSession(this, null);
            session.setUser(user);
            if (!request.getAction().equals("start_random") && !request.getAction().equals("cancel_waiting")) {
                room = chatService.getOrCreateRoom(roomId);
                room.addSession(session);
            }
        }


        room = chatService.getRoomById(roomId);
        if ((room == null || session == null) && !request.getAction().equals("start_random") && !request.getAction().equals("cancel_waiting"))
            return;

        switch (action) {
            case "join" -> {
                String msg = user.getDisplayName() + "님이 입장하셨습니다.";
                System.out.println(msg);
                ServerResponse response = new MessageResponse("", roomId, msg, true);
                room.broadcastMessage(response);
            }
            case "sendMessage" -> {
                String msg = request.getContent();
                String displayName = room.isAnonymous() ? ("unknown#" + String.format("%04d", new Random().nextInt(10000))) : user.getDisplayName();
                ServerResponse response = new MessageResponse(displayName, roomId, msg, false);
                room.broadcastMessage(response);
            }
            case "quit" -> {
                String msg = user.getDisplayName() + "님이 퇴장하셨습니다.";
                ServerResponse response = new MessageResponse("", roomId, msg, true);
                room.broadcastMessage(response);
                chatService.removeSession(session);
            }
            case "start_random" -> {
                ChatRoom matchedRoom = chatService.startRandomChat(session);
                if (matchedRoom != null) {
                    ServerResponse systemMsg = new MessageResponse("", matchedRoom.getRoomId(), "matched", true);
                    matchedRoom.broadcastMessage(systemMsg);
                    LoggerUtil.log("Anonymous chat started: " + matchedRoom.getRoomId());
                }
            }
            case "cancel_waiting" -> {
                chatService.cancelWaiting(session);
                LoggerUtil.log(user.getDisplayName() + " 대기열에서 취소됨");
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
