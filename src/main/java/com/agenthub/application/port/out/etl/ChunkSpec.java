package com.agenthub.application.port.out.etl;

/**
 * 文档分块请求参数。
 */
public final class ChunkSpec {
    private final String documentId;
    private final String kbId;
    private final String content;
    private final int chunkSize;
    private final int overlap;

    public ChunkSpec(String documentId, String kbId, String content, int chunkSize, int overlap) {
        this.documentId = documentId;
        this.kbId = kbId;
        this.content = content;
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getKbId() {
        return kbId;
    }

    public String getContent() {
        return content;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getOverlap() {
        return overlap;
    }
}
