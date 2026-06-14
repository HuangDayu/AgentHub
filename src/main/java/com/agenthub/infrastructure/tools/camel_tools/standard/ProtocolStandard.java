package com.agenthub.infrastructure.tools.camel_tools.standard;

import com.agenthub.domain.enums.AgentDataSourceProtocol;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProtocolStandard {

    private AgentDataSourceProtocol protocol;
    private String displayName;
    private String description;
    private String syntaxGuidelines;
    private String allowedOperations;
    private String examples;
    private String errorHandling;
    private String securityNotes;
    private String bestPractices;
    private List<ProtocolParam> parameters;

    @Data
    @AllArgsConstructor
    public static class ProtocolParam {
        private String name;
        private String type;
        private boolean required;
        private String description;
    }
}
