package client.util;

import shared.domain.FileInfo;
import shared.domain.User;
import shared.dto.ClientRequest;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Utility class for sending file-related requests to the server.
 */
public class FileSender {

    private final ObjectOutputStream out;

    public FileSender(ObjectOutputStream out) {
        this.out = out;
    }

    /**
     * Sends a file along with user and room information.
     */
    public void sendFile(FileInfo fileInfo, User user, String roomId) {
        ClientRequest request = new ClientRequest("sendFile", "", roomId, user, fileInfo);
        sendRequest(request);
    }

    /**
     * Sends a generic client request using the output stream.
     */
    public void sendRequest(ClientRequest request) {
        new Thread(() -> {
            try {
                synchronized (out) {
                    out.writeObject(request);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Failed to send file: " + e.getMessage());
            }
        }).start();
    }
}
