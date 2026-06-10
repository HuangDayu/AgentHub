package com.agenthub.infrastructure.tools.data_source.dto;

public class FileWriteRequest {

    private String filePath;
    private String content;

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
