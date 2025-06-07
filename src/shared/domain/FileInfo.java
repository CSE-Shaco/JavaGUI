package shared.domain;

import java.io.Serializable;

public class FileInfo implements Serializable {
    private final String fileName;
    private final byte[] data;
    private final boolean isImage;

    public FileInfo(String fileName, byte[] data, boolean isImage) {
        this.fileName = fileName;
        this.data = data;
        this.isImage = isImage;
    }

    public String getFileName() { return fileName; }
    public byte[] getData() { return data; }
    public boolean isImage() { return isImage; }
}