package com.agenthub.infrastructure.tools.data_source.dto;

public class MongoQueryRequest {

    private String collection;
    private String filterJson;
    private int limit;

    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
    public String getFilterJson() { return filterJson; }
    public void setFilterJson(String filterJson) { this.filterJson = filterJson; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
