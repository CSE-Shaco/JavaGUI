package client.handler;

import shared.dto.MessageResponse;
import shared.util.LoggerUtil;

import java.io.ObjectInputStream;
import java.util.function.Consumer;

/**
 * Thread responsible for receiving message responses from the server.
 */
public class MessageReceiverThread extends Thread {

    private final ObjectInputStream in;
    private final Consumer<MessageResponse> messageConsumer;

    public MessageReceiverThread(ObjectInputStream in, Consumer<MessageResponse> messageConsumer) {
        this.in = in;
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof MessageResponse response) {
                    messageConsumer.accept(response);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Connection to server has been closed.", e);
        }
    }
}
