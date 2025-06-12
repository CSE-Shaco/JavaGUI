package client.handler;

import shared.dto.RoomListResponse;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;
import java.util.function.Consumer;

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
                callback.accept(response.getRooms());  // Map<String, Integer> 형태
            }

        } catch (Exception e) {
            System.err.println("Room list 수신 실패: " + e.getMessage());
        }
    }
}