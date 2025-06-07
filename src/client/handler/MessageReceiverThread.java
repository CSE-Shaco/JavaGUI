package client.handler;

import shared.dto.RoomListResponse;
import shared.dto.ServerResponse;

import javax.swing.*;
import java.io.ObjectInputStream;
import java.util.List;

public class MessageReceiverThread extends Thread {

    private final ObjectInputStream in;
    private final JTextArea chatArea;

    public MessageReceiverThread(ObjectInputStream in, JTextArea chatArea) {
        this.in = in;
        this.chatArea = chatArea;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof ServerResponse response) {
                    String sender = response.getSender();
                    String content = response.getContent();
                    if (response.isSystemMessage()) {
                        chatArea.append("[System] " + content + "\n");
                    } else {
                        chatArea.append(sender + " : " + content + "\n");
                    }
                }
            }
        } catch (Exception e) {
            chatArea.append("[System] 서버 연결이 끊겼습니다.\n");
        }
    }
}