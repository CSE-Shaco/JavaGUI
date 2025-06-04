package server;

import server.core.FileReceiverServer;
import server.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server {

    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 100;
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {
        new Thread(new FileReceiverServer()).start();
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server encountered an error", e);
        } finally {
            threadPool.shutdown();
        }
    }
}
