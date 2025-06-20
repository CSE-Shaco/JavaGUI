package shared.dto;

import java.io.Serializable;

public abstract class ServerResponse implements Serializable {

    protected final String senderId;
    protected final String sender;
    protected final String roomId;

    protected ServerResponse(String senderId, String sender, String roomId) {
        this.senderId = senderId;
        this.sender = sender;
        this.roomId = roomId;
    }

    public String getSender() {
        return sender;
    }

    public String getSenderId() {
        return senderId;
    }
}
