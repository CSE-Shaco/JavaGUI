package client.handler;

import shared.dto.RoomListResponse;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Thread that fetches the room list from the server.
 */
public class RoomListFetcher extends Thread {

    private final Socket socket;
    private final ObjectInputStream in;
    private final Consumer<Map<String, Integer>> callback;

    public RoomListFetcher(Socket socket, ObjectInputStream in, Consumer<Map<String, Integer>> callback) {
        this.socket = socket;
        this.in = in;
        this.callback = callback;
    }

    @Override
    public void run() {
        try (socket; in) {

            Object obj = in.readObject();
            if (obj instanceof RoomListResponse response) {
                callback.accept(response.getRooms());  // Received Map<String, Integer>
            }

        } catch (Exception e) {
            System.err.println("Failed to receive room list: " + e.getMessage());
        }
    }
}
