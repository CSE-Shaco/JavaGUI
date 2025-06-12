package client.util;

import shared.domain.FileInfo;
import shared.domain.User;
import shared.dto.ClientRequest;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class FileSender {

    private final ObjectOutputStream out;

    public FileSender(ObjectOutputStream out) {
        this.out = out;
    }

    public void sendFile(FileInfo fileInfo, User user, String roomId) {
        ClientRequest request = new ClientRequest("sendFile", "", roomId, user, fileInfo);
        sendRequest(request);
    }

    public void sendRequest(ClientRequest request) {
        new Thread(() -> {
            try {
                synchronized (out) {
                    out.writeObject(request);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("파일 전송 실패: " + e.getMessage());
            }
        }).start();
    }
}
