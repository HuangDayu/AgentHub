package com.agenthub.application.command;

/**
 * 保存入库文档的命令。
 */
public final class SaveDocumentCommand {
    private final String kbId;
    private final String jobId;
    private final org.springframework.web.multipart.MultipartFile file;
    private final String documentId;
    private final String objectKey;

    public SaveDocumentCommand(String kbId, String jobId,
                                org.springframework.web.multipart.MultipartFile file,
                                String documentId, String objectKey) {
        this.kbId = kbId;
        this.jobId = jobId;
        this.file = file;
        this.documentId = documentId;
        this.objectKey = objectKey;
    }

    public String getKbId() {
        return kbId;
    }

    public String getJobId() {
        return jobId;
    }

    public org.springframework.web.multipart.MultipartFile getFile() {
        return file;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getObjectKey() {
        return objectKey;
    }
}
