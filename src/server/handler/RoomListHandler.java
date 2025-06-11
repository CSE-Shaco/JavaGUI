package server.handler;

import server.service.ChatService;
import shared.dto.RoomListRequest;
import shared.dto.RoomListResponse;
import shared.util.LoggerUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

// 별도의 RoomListHandler 생성 (한 번 받고 응답하고 종료)
public class RoomListHandler extends Thread {

    private final Socket socket;
    private final ChatService chatService;

    public RoomListHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Object obj = in.readObject();
            if (obj instanceof RoomListRequest) {
                Map<String, Integer> roomMap = chatService.getRoomList();
                out.writeObject(new RoomListResponse(roomMap));
                out.flush();
            }
        } catch (Exception e) {
            LoggerUtil.error("RoomListHandler 예외", e);
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }
}