package client.handler;

import shared.dto.ServerResponse;

import java.io.ObjectInputStream;
import java.util.function.Consumer;

public class MessageReceiverThread extends Thread {

    private final ObjectInputStream in;
    private final Consumer<String> messageConsumer;

    public MessageReceiverThread(ObjectInputStream in, Consumer<String> messageConsumer) {
        this.in = in;
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof ServerResponse response) {
                    String sender = response.getSender();
                    String content = response.getContent();
                    String message = response.isSystemMessage() ? "[System] " + content : sender + " : " + content;
                    messageConsumer.accept(message);
                }
            }
        } catch (Exception e) {
            messageConsumer.accept("[System] 서버 연결이 끊겼습니다.");
        }
    }
}
