package server.session;

import server.domain.ChatRoom;
import server.handler.ClientHandler;
import server.handler.FileHandler;
import shared.domain.FileInfo;
import shared.domain.User;

public class ClientSession {

    private final ClientHandler clientHandler;
    private final FileHandler fileHandler;
    private User user;
    private ChatRoom chatRoom; // ✅ 추가

    public ClientSession(ClientHandler clientHandler, FileHandler fileHandler) {
        this.clientHandler = clientHandler;
        this.fileHandler = fileHandler;
        this.clientHandler.setSession(this);
        if (this.fileHandler != null) {
            this.fileHandler.setSession(this);
        }
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

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void sendFile(FileInfo fileInfo) {
        // FileHandler는 수신 전용, 실제 전송은 메시지용 ClientHandler 사용
        clientHandler.sendMessage(fileInfo);
    }
}