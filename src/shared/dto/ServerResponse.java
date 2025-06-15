package shared.dto;

import java.io.Serial;
import java.io.Serializable;

public abstract class ServerResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    protected final String sender;
    protected final String roomId;

    protected ServerResponse(String sender, String roomId) {
        this.sender = sender;
        this.roomId = roomId;
    }

    public String getSender() {
        return sender;
    }
}
