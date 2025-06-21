package server.session;

import server.domain.ChatRoom;
import server.handler.ClientHandler;
import server.handler.FileHandler;
import shared.domain.User;
import shared.dto.ServerResponse;
import shared.util.LoggerUtil;

import java.io.ObjectOutputStream;

/**
 * Represents a session between a client and the server.
 * Holds reference to user, handlers, and output stream for file transfers.
 */
public class ClientSession {

    private final ClientHandler clientHandler;
    private final Object fileLock = new Object();
    private FileHandler fileHandler;
    private User user;
    private ObjectOutputStream fileOut;

    public ClientSession(ClientHandler clientHandler, FileHandler fileHandler) {
        this.clientHandler = clientHandler;
        setFileHandler(fileHandler);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public FileHandler getFileHandler() {
        return fileHandler;
    }

    public void setFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        if (fileHandler != null) {
            this.fileHandler.setSession(this);
        }
    }

    public void setFileOutputStream(ObjectOutputStream out) {
        this.fileOut = out;
    }

    /**
     * Sends a file-related server response through the output stream.
     */
    public void sendFile(ServerResponse response) {
        try {
            synchronized (fileLock) {
                fileOut.writeObject(response);
                fileOut.flush();
            }
        } catch (Exception e) {
            LoggerUtil.error("Failed to send file", e);
        }
    }

    public String getUsername() {
        return user.getUserId();
    }
}
