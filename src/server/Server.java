package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Server {
    private static final int PORT = 12345;
    private static final Queue<Socket> waitingQueue = new LinkedList<>();

    public static void main(String[] args) {
        System.out.println("Random Chat Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected.");

                synchronized (waitingQueue) {
                    if (waitingQueue.isEmpty()) {
                        waitingQueue.add(socket);
                        System.out.println("Waiting for a match...");
                    } else {
                        Socket partnerSocket = waitingQueue.poll();
                        System.out.println("Match found! Starting 1:1 chat.");
                        new Thread(new ClientHandler(socket, partnerSocket)).start();
                        new Thread(new ClientHandler(partnerSocket, socket)).start();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
