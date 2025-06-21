package server.handler;

import server.domain.ChatRoom;
import server.service.ChatService;
import server.session.ClientSession;
import shared.domain.FileInfo;
import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.FileResponse;
import shared.dto.ServerResponse;
import shared.util.LoggerUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles incoming file-related requests from a client in a separate thread.
 */
public class FileHandler extends Thread {

    private final Socket socket;
    private final ChatService chatService;
    private ClientSession session;

    public FileHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;
    }

    public void setSession(ClientSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof ClientRequest request)) {
                    LoggerUtil.error("FileHandler: Invalid request received", new Exception("Invalid object type"));
                    continue;
                }

                String action = request.getAction();
                String roomId = request.getRoomId();
                User user = request.getUser();

                switch (action) {
                    case "fileInit" -> {
                        ChatRoom room = chatService.getRoomById(roomId);
                        if (room == null) {
                            LoggerUtil.error("FileHandler: Room not found - " + roomId, new Exception("Logical error"));
                            return;
                        }

                        ClientSession foundSession = room.findSessionByUsername(user.getUserId());
                        if (foundSession == null) {
                            LoggerUtil.error("FileHandler: Session not found for user", new Exception("Logical error"));
                            return;
                        }

                        this.session = foundSession;
                        this.session.setFileHandler(this);
                        this.session.setFileOutputStream(out);
                        LoggerUtil.log("FileHandler connected: " + user.getUserId());
                    }

                    case "sendFile" -> {
                        FileInfo fileInfo = request.getFileInfo();
                        if (session == null) {
                            LoggerUtil.error("FileHandler: File sent before session was initialized", new Exception("Session not initialized"));
                            return;
                        }

                        ChatRoom room = chatService.getRoomById(roomId);
                        if (room == null) return;

                        FileResponse response = new FileResponse(user.getUserId(), user.getUsername(), roomId, fileInfo);
                        room.broadcastFile(response);
                        LoggerUtil.log("File received and broadcasted: " + fileInfo.getFileName());
                    }

                    default -> LoggerUtil.error("FileHandler: Unsupported action - " + action, new Exception("Unknown action"));
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("FileHandler: Failed to process file request", e);
        }
    }

    /**
     * Sends a file response back to the client session.
     */
    public void sendFile(ServerResponse response) {
        try {
            session.sendFile(response);
        } catch (Exception e) {
            LoggerUtil.error("FileHandler: Failed to send file", e);
        }
    }
}
