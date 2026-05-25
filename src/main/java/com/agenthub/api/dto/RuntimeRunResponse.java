package com.agenthub.api.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class RuntimeRunResponse {
    private String id;
    private String agentId;
    private String project;
    private String name;
    private Instant timestamp;
    private Integer pid;
    private String status;
    private String runDir;
}
