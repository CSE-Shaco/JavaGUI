package server.handler;

import server.domain.ChatRoom;
import server.service.ChatService;
import server.session.ClientSession;
import shared.domain.FileInfo;
import shared.util.LoggerUtil;

import java.io.ObjectInputStream;
import java.net.Socket;

public class FileHandler extends Thread {

    private final Socket socket;
    private ClientSession session;

    public FileHandler(Socket socket) {
        this.socket = socket;
    }

    public void setSession(ClientSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof FileInfo fileInfo) {
                    ChatRoom room = session.getChatRoom();
                    if (room != null) {
                        room.broadcastFile(fileInfo);
                        LoggerUtil.log("파일 수신 및 브로드캐스트 완료: " + fileInfo.getFileName());
                    }
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