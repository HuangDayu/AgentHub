package com.agenthub.application.command;

/**
 * 上传入库文档的命令。
 */
public final class UploadDocumentCommand {
    private final String kbId;
    private final String fileName;
    private final String contentType;
    private final long size;
    private final String storagePath;

    public UploadDocumentCommand(String kbId, String fileName, String contentType,
                                  long size, String storagePath) {
        this.kbId = kbId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.storagePath = storagePath;
    }

    public String getKbId() {
        return kbId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public String getStoragePath() {
        return storagePath;
    }
}
