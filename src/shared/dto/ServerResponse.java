package shared.dto;

import java.io.Serializable;

/**
 * Abstract base class for all server-to-client response types.
 * Contains common fields: senderId, sender (display name), and roomId.
 */
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

    public String getRoomId() {
        return roomId;
    }
}
