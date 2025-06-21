package client.util;

import shared.domain.User;
import shared.dto.ClientRequest;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Utility class for sending text and control messages to the server.
 */
public class MessageSender {

    private final ObjectOutputStream out;

    public MessageSender(ObjectOutputStream out) {
        this.out = out;
    }

    /**
     * Sends a plain text message to the server.
     *
     * @param content Message content
     * @param sender  User who sends the message
     * @param roomId  Target room ID
     * @throws IOException if sending fails
     */
    public void sendText(String content, User sender, String roomId) throws IOException {
        ClientRequest request = new ClientRequest("sendMessage", content, roomId, sender, null);
        out.writeObject(request);
        out.flush();
    }

    /**
     * Sends a generic client request.
     *
     * @param request The ClientRequest to send
     * @throws IOException if sending fails
     */
    public void sendRequest(ClientRequest request) throws IOException {
        out.writeObject(request);
        out.flush();
    }
}
