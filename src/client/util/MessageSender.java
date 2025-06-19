package client.util;

import shared.domain.User;
import shared.dto.ClientRequest;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class MessageSender {

    private final ObjectOutputStream out;

    public MessageSender(ObjectOutputStream out) {
        this.out = out;
    }

    public void sendText(String content, User sender, String roomId) throws IOException {
        ClientRequest request = new ClientRequest("sendMessage", content, roomId, sender, null);
        out.writeObject(request);
        out.flush();
    }

    public void sendRequest(ClientRequest request) throws IOException {
        out.writeObject(request);
        out.flush();
    }

    public void sendQuit(User user, String roomId) throws IOException {
        ClientRequest request = new ClientRequest("quit", "", roomId, user, null);
        out.writeObject(request);
        out.flush();
    }
}