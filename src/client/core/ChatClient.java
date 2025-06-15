package client.core;

import client.handler.FileReceiverThread;
import client.handler.MessageReceiverThread;
import client.util.FileSender;
import client.util.MessageSender;
import shared.domain.FileInfo;
import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.FileResponse;
import shared.dto.MessageResponse;
import shared.dto.ServerResponse;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {

    private final User user;
    private final String roomId;
    private final Socket messageSocket;
    private final Socket fileSocket;
    private final MessageSender sender;
    private final FileSender fileSender;

    public ChatClient(String host, int msgPort, User user, String roomId, Consumer<MessageResponse> messageConsumer, Consumer<FileResponse> fileConsumer) {
        this.user = user;
        this.roomId = roomId;
        try {
            this.messageSocket = new Socket(host, msgPort);
            this.fileSocket = new Socket(host, msgPort + 1);

            ObjectOutputStream msgOut = new ObjectOutputStream(messageSocket.getOutputStream());
            ObjectInputStream msgIn = new ObjectInputStream(messageSocket.getInputStream());

            ObjectOutputStream fileOut = new ObjectOutputStream(fileSocket.getOutputStream());
            ObjectInputStream fileIn = new ObjectInputStream(fileSocket.getInputStream());

            sender = new MessageSender(msgOut);
            fileSender = new FileSender(fileOut);

            // 메시지 수신 스레드
            MessageReceiverThread receiverThread = new MessageReceiverThread(msgIn, messageConsumer);
            receiverThread.start();

            // 파일 수신 스레드
            FileReceiverThread fileReceiverThread = new FileReceiverThread(fileIn, fileConsumer);
            fileReceiverThread.start();

            // 초기 입장 및 파일 핸들러 초기화 요청
            ClientRequest joinRequest = new ClientRequest("join", user.getDisplayName() + "님이 입장하셨습니다.", roomId, user, null);
            sender.sendRequest(joinRequest);
            Thread.sleep(100);
            ClientRequest fileInitRequest = new ClientRequest("fileInit", "", roomId, user, null);
            fileSender.sendRequest(fileInitRequest);

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
