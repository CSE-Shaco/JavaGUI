package client.core;

import client.handler.FileReceiverThread;
import client.handler.MessageReceiverThread;
import client.util.FileSender;
import client.util.MessageSender;
import shared.domain.User;
import shared.dto.ClientRequest;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ChatClient {

    private final User user;
    private final String roomId;
    private final Socket messageSocket;
    private final Socket fileSocket;
    private final MessageSender sender;
    private final FileSender fileSender;

    public ChatClient(String host, int msgPort, User user, String roomId, JTextArea chatArea) {
        this.user = user;
        this.roomId = roomId;
        try {
            this.messageSocket = new Socket(host, msgPort);
            this.fileSocket = new Socket(host, msgPort + 1);

            ObjectOutputStream msgOut = new ObjectOutputStream(messageSocket.getOutputStream());
            ObjectInputStream msgIn = new ObjectInputStream(messageSocket.getInputStream());
            OutputStream fileOut = fileSocket.getOutputStream();
            InputStream fileIn = fileSocket.getInputStream();

            this.sender = new MessageSender(msgOut);
            this.fileSender = new FileSender(fileOut);

            MessageReceiverThread receiverThread = new MessageReceiverThread(msgIn, chatArea);
            receiverThread.start();

            FileReceiverThread fileReceiverThread = new FileReceiverThread(fileIn, fileInfo -> {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("수신된 파일: " + fileInfo.getFileName() + "\n");
                });
            });
            fileReceiverThread.start();

            ClientRequest joinRequest = new ClientRequest("join", user.getDisplayName() + "님이 입장하셨습니다.", roomId, user, null);
            sender.sendRequest(joinRequest);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "서버 연결 실패: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public MessageSender getSender() {
        return sender;
    }

    public FileSender getFileSender() {
        return fileSender;
    }

    public void disconnect() {
        try {
            ClientRequest quitRequest = new ClientRequest("quit", "__quit__", roomId, user, null);
            sender.sendRequest(quitRequest);
            messageSocket.close();
            fileSocket.close();
        } catch (Exception ignored) {
        }
    }

    public User getUser() {
        return user;
    }

    public String getRoomId() {
        return roomId;
    }
}