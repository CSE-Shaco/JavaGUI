package shared.dto;

public class MessageResponse extends ServerResponse {

    private final String message;
    private final boolean system;

    public MessageResponse(String senderId, String sender, String roomId, String message, boolean system) {
        super(senderId, sender, roomId);
        this.message = message;
        this.system = system;
    }

    public String getMessage() {
        return message;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isSystemMessage() {
        return system;
    }
}
