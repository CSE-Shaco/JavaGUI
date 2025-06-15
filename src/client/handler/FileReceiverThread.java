package client.handler;

import shared.dto.FileResponse;

import java.io.ObjectInputStream;
import java.util.function.Consumer;

public class FileReceiverThread extends Thread {

    private final ObjectInputStream in;
    private final Consumer<FileResponse> fileHandler;

    public FileReceiverThread(ObjectInputStream in, Consumer<FileResponse> fileHandler) throws Exception {
        this.in = in;
        this.fileHandler = fileHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof FileResponse response) {
                    fileHandler.accept(response);
                    System.out.println(response.getFileInfo().getFileName());
                }
            }
        } catch (Exception e) {
            System.err.println("파일 수신 오류: " + e.getMessage());
        }
    }
}