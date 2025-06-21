package shared.dto;

import java.io.Serializable;

/**
 * Request object sent from the client to the server
 * to retrieve the current list of available chat rooms.
 * <p>
 * This class does not contain any fields because the
 * request does not require any parameters.
 * <p>
 * Once the server receives this request, it will respond
 * with a {@link RoomListResponse} containing a map of
 * room IDs and their current participant counts.
 */
public class RoomListRequest implements Serializable {
    // No fields required. The presence of this object
    // signals a request for room list.
}
