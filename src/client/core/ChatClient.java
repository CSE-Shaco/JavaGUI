package client.core;

import client.handler.FileReceiverThread;
import client.handler.MessageReceiverThread;
import client.util.FileSender;
import client.util.MessageSender;
import shared.domain.User;
import shared.dto.ClientRequest;

import javax.swing.*;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
            System.out.println("1. messageSocket 생성 시도");
            this.messageSocket = new Socket(host, msgPort);

            System.out.println("2. fileSocket 생성 시도");
            this.fileSocket = new Socket(host, msgPort + 1);

            System.out.println("3. message OutputStream 생성 시도");
            ObjectOutputStream msgOut = new ObjectOutputStream(messageSocket.getOutputStream());

            System.out.println("4. message InputStream 생성 시도");
            ObjectInputStream msgIn = new ObjectInputStream(messageSocket.getInputStream());

            System.out.println("5. file OutputStream 생성 시도");
            ObjectOutputStream fileOut = new ObjectOutputStream(fileSocket.getOutputStream());

            System.out.println("6. file InputStream 생성 시도");
            ObjectInputStream fileIn = new ObjectInputStream(fileSocket.getInputStream());

            System.out.println("7. Sender 객체 생성 시도");
            this.sender = new MessageSender(msgOut);
            this.fileSender = new FileSender(fileOut);

            MessageReceiverThread receiverThread = new MessageReceiverThread(msgIn, chatArea);
            receiverThread.start();
            System.out.println("[Client] Sockets created. Streams opened.");


            try {
                FileReceiverThread fileReceiverThread = new FileReceiverThread(fileIn, fileInfo -> SwingUtilities.invokeLater(() -> chatArea.append("수신된 파일: " + fileInfo.getFileName() + "\n")));
                fileReceiverThread.start();
                System.out.println("뭐야 얘가 문제인거야?");
            } catch (Exception ex) {
                System.err.println("fileReceiverThread 예외 발생: " + ex.getMessage());
                ex.printStackTrace();
            }


            System.out.println("join request 생성");
            ClientRequest joinRequest = new ClientRequest("join", user.getDisplayName() + "님이 입장하셨습니다.", roomId, user, null);
            System.out.println("join request 발송");
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