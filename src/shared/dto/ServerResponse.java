package shared.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ServerResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String sender;       // 보낸 사람
    private final String content;      // 메시지 내용
    private final String roomId;       // 채팅방 ID
    private final LocalDateTime time;  // 전송 시각

    private final boolean systemMessage;

    public ServerResponse(String sender, String content, String roomId, boolean systemMessage) {
        this.sender = sender;
        this.content = content;
        this.roomId = roomId;
        this.time = LocalDateTime.now(); // 생성 시 자동 시간 기록
        this.systemMessage = systemMessage;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getRoomId() {
        return roomId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public boolean isSystemMessage() {
        return systemMessage;
    }

    @Override
    public String toString() {
        return "[" + time + "] " + sender + ": " + content;
    }
}