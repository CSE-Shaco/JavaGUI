package client;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        final String SERVER_HOST = "localhost";
        final int SERVER_PORT = 12345;

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter serverWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            System.out.println("Connected to random chat server.");

            new Thread(() -> {
                String response;
                try {
                    while ((response = serverReader.readLine()) != null) {
                        System.out.println("Stranger: " + response);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected.");
                }
            }).start();

            String input;
            while ((input = consoleReader.readLine()) != null) {
                serverWriter.write(input);
                serverWriter.newLine();
                serverWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
