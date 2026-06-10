package com.agenthub.infrastructure.tools.data_source.dto;

public class JmsSendRequest {

    private String destination;
    private String message;

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
