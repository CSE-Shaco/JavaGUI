package client.core;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens for incoming file transfers and handles them.
 */
public class FileListenerThread extends Thread {
    private int port;
    private static final Logger logger = Logger.getLogger(FileListenerThread.class.getName());

    public FileListenerThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.port = serverSocket.getLocalPort();
            logger.info("[Client] FileListenerThread started on port " + this.port);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                        FileReceiver.receiveFile(dis);
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "File reception failed", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not start FileListenerThread", e);
        }
    }

    public int getPort() {
        return this.port;
    }
}
