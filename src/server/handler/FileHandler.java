package server.handler;

import server.domain.ChatRoom;
import server.service.ChatService;
import server.session.ClientSession;
import shared.domain.FileInfo;
import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.FileResponse;
import shared.dto.ServerResponse;
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
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof ClientRequest request)) {
                    LoggerUtil.error("FileHandler: 잘못된 요청 수신", new Exception("유효하지 않은 객체"));
                    continue;
                }

                String action = request.getAction();
                String roomId = request.getRoomId();
                User user = request.getUser();

                switch (action) {
                    case "fileInit" -> {
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
                    }

                    case "sendFile" -> {
                        FileInfo fileInfo = request.getFileInfo();
                        if (session == null) {
                            LoggerUtil.error("FileHandler: 초기화되지 않은 세션에서 파일 전송 시도", new Exception("초기화 필요"));
                            return;
                        }

                        ChatRoom room = chatService.getRoomById(roomId);
                        if (room == null) return;

                        FileResponse response = new FileResponse(user.getDisplayName(), roomId, fileInfo);

                        room.broadcastFile(response);
                        LoggerUtil.log("파일 수신 및 브로드캐스트 완료: " + fileInfo.getFileName());
                    }

                    default -> LoggerUtil.error("FileHandler: 지원하지 않는 액션: " + action, new Exception("알 수 없는 액션"));
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("파일 처리 실패", e);
        }
    }

    public void sendFile(ServerResponse response) {
        try {
            session.sendFile(response);
        } catch (Exception e) {
            LoggerUtil.error("파일 전송 실패", e);
        }
    }
}
