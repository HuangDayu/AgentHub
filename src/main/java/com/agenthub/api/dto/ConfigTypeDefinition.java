package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigTypeDefinition {
    private String category;
    private String displayName;
    private String description;
    private List<TypeInfo> types;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeInfo {
        private String type;
        private String displayName;
        private String description;
    }
}
