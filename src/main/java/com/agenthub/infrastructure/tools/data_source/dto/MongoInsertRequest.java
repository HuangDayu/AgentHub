package com.agenthub.infrastructure.tools.data_source.dto;

public class MongoInsertRequest {

    private String collection;
    private String documentJson;

    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
    public String getDocumentJson() { return documentJson; }
    public void setDocumentJson(String documentJson) { this.documentJson = documentJson; }
}
