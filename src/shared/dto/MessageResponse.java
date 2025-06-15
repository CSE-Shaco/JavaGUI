package shared.dto;

public class MessageResponse extends ServerResponse {

    private final String message;
    private final boolean system;

    public MessageResponse(String sender, String roomId, String message, boolean system) {
        super(sender, roomId);
        this.message = message;
        this.system = system;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSystemMessage() {
        return system;
    }
}
