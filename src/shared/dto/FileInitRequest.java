package shared.dto;

import shared.domain.User;

import java.io.Serializable;

public class FileInitRequest implements Serializable {

    private final User user;
    private final String roomId;

    public FileInitRequest(User user, String roomId) {
        this.user = user;
        this.roomId = roomId;
    }

    public User getUser() {
        return user;
    }

    public String getRoomId() {
        return roomId;
    }
}