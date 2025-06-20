package server.service;

import server.domain.ChatRoom;
import server.session.ClientSession;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class ChatService {

    private final Map<String, ChatRoom> roomMap = new ConcurrentHashMap<>();
    private final Queue<ClientSession> waitingQueue = new LinkedBlockingQueue<>();

    public ChatRoom getOrCreateRoom(String roomId) {
        return roomMap.computeIfAbsent(roomId, ChatRoom::new);
    }

    public ChatRoom getRoomById(String roomId) {
        return roomMap.get(roomId);
    }

    public void removeSession(ClientSession session) {
        for (ChatRoom room : roomMap.values()) {
            if (room.getSessions().contains(session)) {
                room.removeSession(session);
                if (room.getParticipantCount() == 0) {
                    roomMap.remove(room.getRoomId());
                }
                break;
            }
        }
    }

    public Map<String, Integer> getRoomList() {
        return roomMap.entrySet().stream().filter(entry -> !entry.getValue().isAnonymous()) // 익명 방 제외
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getParticipantCount()));
    }


    public synchronized ChatRoom startRandomChat(ClientSession requester) {
        if (waitingQueue.contains(requester)) {
            return null; // 이미 대기 중이면 아무것도 안 함
        }

        if (!waitingQueue.isEmpty()) {
            ClientSession partner = waitingQueue.poll();

            // 이 시점에서 방 생성
            String roomId = "anonymous-" + System.currentTimeMillis();
            ChatRoom room = new ChatRoom(roomId, true);
            room.addSession(partner);
            room.addSession(requester);
            roomMap.put(roomId, room);

            return room;
        } else {
            // 아무도 대기 중이 없으면 큐에 추가만 함
            waitingQueue.offer(requester);
            return null;
        }
    }

    public synchronized void cancelWaiting(ClientSession session) {
        waitingQueue.remove(session);
    }
}
