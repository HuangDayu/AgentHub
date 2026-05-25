package com.agenthub.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class RuntimeRunOutput {
    private String id;
    private String agentId;
    private String project;
    private String name;
    private Instant timestamp;
    private Integer pid;
    private String status;
    private String runDir;
}
