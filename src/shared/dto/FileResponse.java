package shared.dto;

import shared.domain.FileInfo;

public class FileResponse extends ServerResponse {

    private final FileInfo fileInfo;

    public FileResponse(String senderId, String sender, String roomId, FileInfo fileInfo) {
        super(senderId, sender, roomId);
        this.fileInfo = fileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }
}
