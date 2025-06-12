package server;

import server.handler.ClientHandler;
import server.handler.FileHandler;
import server.handler.RoomListHandler;
import server.service.ChatService;
import shared.util.LoggerUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int MSG_PORT = 12345;
    private static final int FILE_PORT = 12346;
    private static final int ROOM_PORT = 12347;

    // ✅ 공용 ChatService 인스턴스
    private static final ChatService chatService = new ChatService();

    public static void main(String[] args) {
        LoggerUtil.log("Starting server...");
        Thread msgThread = getMessageThread(); // 이름도 명확하게 변경
        Thread fileThread = getFileThread();
        Thread roomThread = getRoomThread();

        msgThread.start();
        fileThread.start();
        roomThread.start();

        LoggerUtil.log("Server started successfully.");
    }

    private static Thread getMessageThread() {
        return new Thread(() -> {
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
    }

    private static Thread getFileThread() {
        return new Thread(() -> {
            try (ServerSocket fileServerSocket = new ServerSocket(FILE_PORT)) {
                LoggerUtil.log("File Server listening on port " + FILE_PORT);
                while (true) {
                    Socket fileSocket = fileServerSocket.accept();
                    LoggerUtil.log("New file client connected: " + fileSocket.getRemoteSocketAddress());
                    new FileHandler(fileSocket, chatService).start(); // chatService 전달
                }
            } catch (IOException e) {
                LoggerUtil.error("File server error", e);
            }
        });
    }

    private static Thread getRoomThread() {
        return new Thread(() -> {
            try (ServerSocket roomSocket = new ServerSocket(ROOM_PORT)) {
                LoggerUtil.log("RoomList Server listening on port " + ROOM_PORT);
                while (true) {
                    Socket socket = roomSocket.accept();
                    new RoomListHandler(socket, chatService).start();
                }
            } catch (IOException e) {
                LoggerUtil.error("RoomList Server 예외", e);
            }
        });
    }
}