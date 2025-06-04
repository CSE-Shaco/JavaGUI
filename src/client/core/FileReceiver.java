package client.core;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class FileReceiver {
    private static final String SAVE_DIR = "client/uploads/";
    private static final Logger logger = Logger.getLogger(FileReceiver.class.getName());

    public static void receiveFile(DataInputStream dis) throws IOException {
        String header = dis.readUTF();
        if (!header.startsWith("FILE:")) return;

        String fileName = header.substring("FILE:".length());
        long fileSize = dis.readLong();

        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();

        File outFile = new File(SAVE_DIR + fileName);

        try (FileOutputStream fos = new FileOutputStream(outFile); GZIPInputStream gzipIn = new GZIPInputStream(dis)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = gzipIn.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }

            logger.info("[Client] File received and saved to: " + outFile.getPath());
        }
    }
}
