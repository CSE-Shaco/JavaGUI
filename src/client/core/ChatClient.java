package client.core;

import client.handler.FileReceiverThread;
import client.handler.MessageReceiverThread;
import client.util.FileSender;
import client.util.MessageSender;
import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.FileResponse;
import shared.dto.MessageResponse;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Core client-side chat communication manager.
 * Manages sockets, senders, and receiving threads for message and file communication.
 */
public class ChatClient {

    private final User user;               // Logged-in user
    private final String roomId;           // Chat room identifier
    private final Socket messageSocket;    // Socket for message communication
    private final Socket fileSocket;       // Socket for file communication
    private final MessageSender sender;    // Utility for sending message requests
    private final FileSender fileSender;   // Utility for sending file requests

    /**
     * Initializes the ChatClient by connecting to server and starting I/O threads.
     *
     * @param host            Server host
     * @param msgPort         Base port (message port)
     * @param user            User info
     * @param roomId          Room ID to join
     * @param messageConsumer Callback for received messages
     * @param fileConsumer    Callback for received files
     */
    public ChatClient(String host, int msgPort, User user, String roomId, Consumer<MessageResponse> messageConsumer, Consumer<FileResponse> fileConsumer) {
        this.user = user;
        this.roomId = roomId;
        try {
            // Establish separate sockets for message and file communication
            this.messageSocket = new Socket(host, msgPort);
            this.fileSocket = new Socket(host, msgPort + 1);

            // Set up message streams
            ObjectOutputStream msgOut = new ObjectOutputStream(messageSocket.getOutputStream());
            ObjectInputStream msgIn = new ObjectInputStream(messageSocket.getInputStream());

            // Set up file streams
            ObjectOutputStream fileOut = new ObjectOutputStream(fileSocket.getOutputStream());
            ObjectInputStream fileIn = new ObjectInputStream(fileSocket.getInputStream());

            // Create sender utilities
            sender = new MessageSender(msgOut);
            fileSender = new FileSender(fileOut);

            // Start message receiving thread
            MessageReceiverThread receiverThread = new MessageReceiverThread(msgIn, messageConsumer);
            receiverThread.start();

            // Start file receiving thread
            FileReceiverThread fileReceiverThread = new FileReceiverThread(fileIn, fileConsumer);
            fileReceiverThread.start();

            // Send initial join request to server
            ClientRequest joinRequest = new ClientRequest("join", user.getUsername() + " has joined the chat.", roomId, user, null);
            sender.sendRequest(joinRequest);

            // Sleep briefly to ensure join is processed before file init
            Thread.sleep(100);

            // Request to initialize file handler on server
            ClientRequest fileInitRequest = new ClientRequest("fileInit", "", roomId, user, null);
            fileSender.sendRequest(fileInitRequest);
        } catch (Exception e) {
            // Show connection failure message and propagate exception
            JOptionPane.showMessageDialog(null, "Failed to connect to server: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public MessageSender getSender() {
        return sender;
    }

    public FileSender getFileSender() {
        return fileSender;
    }

    /**
     * Sends quit request to server and closes sockets.
     */
    public void disconnect() {
        try {
            ClientRequest quitRequest = new ClientRequest("quit", "", roomId, user, null);
            sender.sendRequest(quitRequest);
            messageSocket.close();
            fileSocket.close();
        } catch (Exception ignored) {
            // Ignore disconnection exceptions silently
        }
    }
}
