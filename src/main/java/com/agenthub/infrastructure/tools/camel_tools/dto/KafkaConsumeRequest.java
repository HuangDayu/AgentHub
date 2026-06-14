package com.agenthub.infrastructure.tools.camel_tools.dto;

public class KafkaConsumeRequest {

    private String topic;
    private int maxMessages;

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public int getMaxMessages() { return maxMessages; }
    public void setMaxMessages(int maxMessages) { this.maxMessages = maxMessages; }
}
