package server.session;

import server.domain.ChatRoom;
import server.handler.ClientHandler;
import server.handler.FileHandler;
import shared.domain.FileInfo;
import shared.domain.User;
import shared.util.LoggerUtil;

import java.io.ObjectOutputStream;

public class ClientSession {

    private final ClientHandler clientHandler;
    private final Object fileLock = new Object();
    private FileHandler fileHandler;
    private User user;
    private ChatRoom chatRoom; // ✅ 추가
    private ObjectOutputStream fileOut;

    public ClientSession(ClientHandler clientHandler, FileHandler fileHandler) {
        this.clientHandler = clientHandler;
        setFileHandler(fileHandler);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public FileHandler getFileHandler() {
        return fileHandler;
    }

    public void setFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        if (fileHandler != null) {
            this.fileHandler.setSession(this);
        }
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void setFileOutputStream(ObjectOutputStream out) {
        this.fileOut = out;
    }

    public void sendFile(FileInfo fileInfo) {
        try {
            synchronized (fileLock) {
                fileOut.writeObject(fileInfo);
                fileOut.flush();
            }
        } catch (Exception e) {
            LoggerUtil.error("파일 전송 실패", e);
        }
    }
}