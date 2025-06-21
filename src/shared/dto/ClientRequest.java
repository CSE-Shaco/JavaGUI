package shared.dto;

import shared.domain.FileInfo;
import shared.domain.User;

import java.io.Serializable;

/**
 * Represents a request sent from client to server.
 * Can contain a message, file, or control action.
 */
public class ClientRequest implements Serializable {

    private final String action;     // e.g., "sendMessage", "join", "quit"
    private final String content;    // message content (optional)
    private final String roomId;     // target room ID
    private final User user;         // sender user
    private final FileInfo fileInfo; // file data (optional)

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
        return "ClientRequest{" +
                "action='" + action + '\'' +
                ", content='" + content + '\'' +
                ", roomId='" + roomId + '\'' +
                ", user=" + user +
                '}';
    }
}
