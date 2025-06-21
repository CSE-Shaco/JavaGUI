package shared.dto;

/**
 * Response sent from server to clients when a text message is transferred.
 * Can be a normal user message or a system message.
 */
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

    public boolean isSystemMessage() {
        return system;
    }
}
