package client.core;

import client.handler.FileReceiverThread;
import client.handler.MessageReceiverThread;
import client.util.FileSender;
import client.util.MessageSender;
import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.FileInitRequest;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

            ObjectOutputStream fileOut = new ObjectOutputStream(fileSocket.getOutputStream());
            ObjectInputStream fileIn = new ObjectInputStream(fileSocket.getInputStream());

            this.sender = new MessageSender(msgOut);
            this.fileSender = new FileSender(fileOut);

// ✅ 파일 핸들러 초기화 요청 보내기 (반드시 ObjectStream 생성 직후)
            fileOut.writeObject(new FileInitRequest(user, roomId));
            fileOut.flush();

            MessageReceiverThread receiverThread = new MessageReceiverThread(msgIn, chatArea);
            receiverThread.start();

            try {
                FileReceiverThread fileReceiverThread = new FileReceiverThread(fileIn, fileInfo -> SwingUtilities.invokeLater(() -> chatArea.append("수신된 파일: " + fileInfo.getFileName() + "\n")));
                fileReceiverThread.start();
            } catch (Exception ex) {
                System.err.println("fileReceiverThread 예외 발생: " + ex.getMessage());
                ex.printStackTrace();
            }

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