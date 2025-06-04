package client.core;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class FileSender {
    private static final Logger logger = Logger.getLogger(FileSender.class.getName());
    private static final String FILE_HOST = "localhost";
    private static final int FILE_PORT = 23456;

    public static void sendFile(String filePath) {
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            logger.warning("File not found: " + filePath);
            return;
        }

        try (Socket socket = new Socket(FILE_HOST, FILE_PORT);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            // 압축 데이터를 메모리에 임시 저장
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
                 FileInputStream fis = new FileInputStream(file)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    gzipOut.write(buffer, 0, read);
                }
                gzipOut.finish(); // 압축 마무리
            }

            byte[] compressedData = baos.toByteArray();

            // 전송
            dos.writeUTF("FILE:" + file.getName());
            dos.writeLong(compressedData.length); // 압축된 데이터 길이
            dos.write(compressedData);            // 압축된 데이터 전송
            dos.flush();

            logger.info("[Client] File sent (GZIP): " + file.getName());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "File transfer failed", e);
        }
    }
}
