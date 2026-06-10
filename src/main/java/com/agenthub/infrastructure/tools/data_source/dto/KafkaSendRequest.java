package com.agenthub.infrastructure.tools.data_source.dto;

public class KafkaSendRequest {

    private String topic;
    private String message;
    private String key;

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
