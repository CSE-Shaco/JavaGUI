package client;

import client.core.FileListenerThread;
import client.core.FileSender;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Console-based client for sending messages and files to the server.
 */
public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public static void main(String[] args) {
        // Start file receiving thread (dynamic port)
        FileListenerThread fileListener = new FileListenerThread(0); // 0 means OS will assign port
        fileListener.start();

        int fileReceivePort = fileListener.getPort(); // get assigned port

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             Scanner scanner = new Scanner(System.in)) {

            logger.info("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);

            // 서버에 파일 수신 포트 번호 전달
            out.write("FILE_PORT:" + fileReceivePort);
            out.newLine();
            out.flush();

            // Receiving thread
            Thread receiveThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Disconnected from server", e);
                }
            });
            receiveThread.start();

            // Sending input
            while (true) {
                String input = scanner.nextLine();

                if (input.startsWith("!sendfile ")) {
                    String filePath = input.substring("!sendfile ".length()).trim();
                    FileSender.sendFile(filePath);
                    continue;
                }

                out.write(input);
                out.newLine();
                out.flush();
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Connection error", e);
        }
    }
}
