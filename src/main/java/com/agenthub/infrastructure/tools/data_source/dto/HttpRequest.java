package com.agenthub.infrastructure.tools.data_source.dto;

public class HttpRequest {

    private String method;
    private String path;
    private String body;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
