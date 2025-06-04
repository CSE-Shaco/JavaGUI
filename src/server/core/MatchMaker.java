package server.core;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles random 1:1 chat room matchmaking between anonymous users.
 */
public class MatchMaker {

    private static final Logger logger = Logger.getLogger(MatchMaker.class.getName());
    private final Queue<String> waitingQueue = new ConcurrentLinkedQueue<>();
    private final ChatRoomManager chatRoomManager;

    public MatchMaker(ChatRoomManager chatRoomManager) {
        this.chatRoomManager = chatRoomManager;
    }

    public synchronized ChatRoom requestMatch(String username) {
        if (!waitingQueue.isEmpty()) {
            String partner = waitingQueue.poll();
            Set<String> participants = new HashSet<>();
            participants.add(username);
            participants.add(partner);

            ChatRoom room = chatRoomManager.createRoom(participants);
            logger.info("Matched " + username + " with " + partner + " in room: " + room.getRoomId());
            return room;
        } else {
            waitingQueue.offer(username);
            logger.info(username + " added to waiting queue");
            return null; // 대기 상태
        }
    }

    public synchronized void cancelMatchRequest(String username) {
        waitingQueue.remove(username);
        logger.info(username + " removed from waiting queue");
    }
}
