package server.core;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class FileReceiver {

    private static final String SAVE_DIR = "uploads/";
    private static final Logger logger = Logger.getLogger(FileReceiver.class.getName());

    // 비동기 실행을 위한 스레드 풀
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void receiveFile(DataInputStream dis, Consumer<File> onComplete) throws IOException {
        String fileName = dis.readUTF().substring("FILE:".length());
        long fileSize = dis.readLong();

        File dir = new File(SAVE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            logger.warning("Failed to create upload directory: " + dir.getAbsolutePath());
        }

        File outFile = new File(SAVE_DIR + fileName + ".gz");
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            while (remaining > 0) {
                int read = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1) break;
                fos.write(buffer, 0, read);
                remaining -= read;
            }
            logger.info("[Server] File received: " + outFile.getName());
        }

        // 파일 저장이 끝난 후 비동기로 후속 작업 실행
        executor.submit(() -> onComplete.accept(outFile));
    }
}
