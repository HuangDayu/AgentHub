package com.agenthub.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AddNodeRequest {
    private String type;
    private String name;
    private Map<String, Object> position;
    private Map<String, Object> config;
}
