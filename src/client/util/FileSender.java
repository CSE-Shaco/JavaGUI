package client.util;

import shared.domain.FileInfo;
import shared.domain.User;
import shared.dto.ClientRequest;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class FileSender {

    private final ObjectOutputStream out;

    public FileSender(OutputStream outputStream) throws Exception {
        this.out = new ObjectOutputStream(outputStream);
    }

    public void sendFile(FileInfo fileInfo, User user, String roomId) {
        new Thread(() -> {
            try {
                ClientRequest request = new ClientRequest("sendFile", "", roomId, user, fileInfo);
                synchronized (out) {
                    out.writeObject(request);
                    out.flush();
                }
            } catch (Exception e) {
                System.err.println("파일 전송 실패: " + e.getMessage());
            }
        }).start();
    }
}