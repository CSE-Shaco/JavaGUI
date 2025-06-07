package shared.dto;

import java.io.Serializable;
import java.util.Map;

public class RoomListResponse implements Serializable {

    private final Map<String, Integer> rooms;

    public RoomListResponse(Map<String, Integer> rooms) {
        this.rooms = rooms;
    }

    public Map<String, Integer> getRooms() {
        return rooms;
    }
}