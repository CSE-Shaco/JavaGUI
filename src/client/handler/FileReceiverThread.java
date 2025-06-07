package client.handler;

import shared.domain.FileInfo;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.function.Consumer;

public class FileReceiverThread extends Thread {

    private final ObjectInputStream in;
    private final Consumer<FileInfo> fileHandler;

    public FileReceiverThread(InputStream inputStream, Consumer<FileInfo> fileHandler) throws Exception {
        this.in = new ObjectInputStream(inputStream);
        this.fileHandler = fileHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof FileInfo fileInfo) {
                    fileHandler.accept(fileInfo);
                }
            }
        } catch (Exception e) {
            System.err.println("파일 수신 오류: " + e.getMessage());
        }
    }
}