package com.agenthub.application.dto.workflow;

import lombok.Data;
import java.util.Map;

@Data
public class DagNodeOutput {
    private String id;
    private String type;
    private String name;
    private Map<String, Object> position;
    private Map<String, Object> config;
    private String status;
}
