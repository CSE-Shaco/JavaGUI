package server.core;

import server.handler.ClientHandler;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileReceiverServer implements Runnable {

    private static final int FILE_PORT = 23456;
    private static final Logger logger = Logger.getLogger(FileReceiverServer.class.getName());

    @Override
    public void run() {
        try (ServerSocket fileServerSocket = new ServerSocket(FILE_PORT)) {
            logger.info("FileReceiverServer started on port " + FILE_PORT);

            while (true) {
                Socket fileSocket = fileServerSocket.accept();

                new Thread(() -> {
                    try (DataInputStream dis = new DataInputStream(fileSocket.getInputStream())) {

                        FileReceiver.receiveFile(dis, receivedFile -> {
                            logger.info("File received and saved: " + receivedFile.getAbsolutePath());

                            String fileName = receivedFile.getName();

                            ClientHandler recipient = ClientHandlerMap.getRecipientFor(fileName);
                            if (recipient != null) {
                                recipient.sendFileToClient(receivedFile); // 클라이언트에게 GZIP 전송
                            } else {
                                logger.warning("No recipient found for file: " + fileName);
                            }
                        });


                    } catch (IOException e) {
                        logger.log(Level.WARNING, "File reception failed", e);
                    } finally {
                        try {
                            fileSocket.close();
                        } catch (IOException e) {
                            logger.log(Level.WARNING, "Socket close failed", e);
                        }
                    }
                }).start();
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "FileReceiverServer failed", e);
        }
    }
}
