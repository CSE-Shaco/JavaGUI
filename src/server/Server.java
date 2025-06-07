package server;

import server.handler.ClientHandler;
import server.handler.FileHandler;
import server.service.ChatService;
import shared.util.LoggerUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int MSG_PORT = 12345;
    private static final int FILE_PORT = 12346;

    public static void main(String[] args) {
        LoggerUtil.log("Starting server...");
        ChatService chatService = new ChatService();

        // 메시지 수신용 서버소켓
        Thread msgThread = new Thread(() -> {
            try (ServerSocket msgServerSocket = new ServerSocket(MSG_PORT)) {
                LoggerUtil.log("Message Server listening on port " + MSG_PORT);
                while (true) {
                    Socket msgSocket = msgServerSocket.accept();
                    LoggerUtil.log("New message client connected: " + msgSocket.getRemoteSocketAddress());
                    new ClientHandler(msgSocket, chatService).start();
                }
            } catch (IOException e) {
                LoggerUtil.error("Message server error", e);
            }
        });

        // 파일 수신용 서버소켓
        Thread fileThread = new Thread(() -> {
            try (ServerSocket fileServerSocket = new ServerSocket(FILE_PORT)) {
                LoggerUtil.log("File Server listening on port " + FILE_PORT);
                while (true) {
                    Socket fileSocket = fileServerSocket.accept();
                    LoggerUtil.log("New file client connected: " + fileSocket.getRemoteSocketAddress());
                    new FileHandler(fileSocket).start();
                }
            } catch (IOException e) {
                LoggerUtil.error("File server error", e);
            }
        });

        msgThread.start();
        fileThread.start();

        LoggerUtil.log("Server started successfully.");
    }
}