package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket fromClient;
    private final Socket toClient;

    public ClientHandler(Socket fromClient, Socket toClient) {
        this.fromClient = fromClient;
        this.toClient = toClient;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(fromClient.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(toClient.getOutputStream()))
        ) {
            String message;
            while ((message = reader.readLine()) != null) {
                writer.write(message);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println("Connection closed.");
        } finally {
            try {
                fromClient.close();
            } catch (IOException ignored) {}
            try {
                toClient.close();
            } catch (IOException ignored) {}
        }
    }
}
