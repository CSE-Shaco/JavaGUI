package shared.dto;

import shared.domain.FileInfo;
import shared.domain.User;

import java.io.Serial;
import java.io.Serializable;

public class ClientRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String action;   // 예: "sendMessage", "join", "quit"
    private final String content;  // 메시지 내용
    private final String roomId;   // 방 ID
    private final User user;       // 사용자 정보
    private final FileInfo fileInfo;

    public ClientRequest(String action, String content, String roomId, User user, FileInfo fileInfo) {
        this.action = action;
        this.content = content;
        this.roomId = roomId;
        this.user = user;
        this.fileInfo = fileInfo;
    }

    public String getAction() {
        return action;
    }

    public String getContent() {
        return content;
    }

    public String getRoomId() {
        return roomId;
    }

    public User getUser() {
        return user;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    @Override
    public String toString() {
        return "ClientRequest{" + "action='" + action + '\'' + ", content='" + content + '\'' + ", roomId='" + roomId + '\'' + ", user=" + user + '}';
    }
}