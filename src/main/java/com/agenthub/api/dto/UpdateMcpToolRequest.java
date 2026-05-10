package com.agenthub.api.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMcpToolRequest {
    private String name;
    private String description;
    private String serverUrl;
    private String serverType;
    private String command;
    private List<String> args;
    private Map<String, String> env;
    private Boolean async;
    private Boolean enabled;
}
