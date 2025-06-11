package server.handler;

import server.domain.ChatRoom;
import server.service.ChatService;
import server.session.ClientSession;
import shared.domain.FileInfo;
import shared.domain.User;
import shared.dto.FileInitRequest;
import shared.util.LoggerUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class FileHandler extends Thread {

    private final Socket socket;
    private final ChatService chatService;
    private ClientSession session;

    public FileHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;
    }

    public void setSession(ClientSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Object obj = in.readObject();
            if (!(obj instanceof FileInitRequest initRequest)) {
                LoggerUtil.error("FileHandler 초기 요청이 잘못되었습니다.", new Exception("논리적 예외"));
                return;
            }

            String roomId = initRequest.getRoomId();
            User user = initRequest.getUser();

            ChatRoom room = chatService.getRoomById(roomId);
            if (room == null) {
                LoggerUtil.error("FileHandler: 존재하지 않는 방 " + roomId, new Exception("논리적 예외"));
                return;
            }

            ClientSession foundSession = room.findSessionByUsername(user.getUsername());
            if (foundSession == null) {
                LoggerUtil.error("FileHandler: 해당 사용자의 세션을 찾을 수 없음", new Exception("논리적 예외"));
                return;
            }

            this.session = foundSession;
            this.session.setFileHandler(this);
            this.session.setFileOutputStream(out);

            LoggerUtil.log("FileHandler 연결 완료: " + user.getUsername());

            // 실제 파일 수신 처리
            while (true) {
                Object data = in.readObject();
                if (data instanceof FileInfo fileInfo) {
                    room.broadcastFile(fileInfo);
                    LoggerUtil.log("파일 수신 및 브로드캐스트 완료: " + fileInfo.getFileName());
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("파일 처리 실패", e);
        }
    }

    public void sendFile(FileInfo fileInfo) {
        try {
            session.sendFile(fileInfo);
        } catch (Exception e) {
            LoggerUtil.error("파일 전송 실패", e);
        }
    }
}