package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionStrategyRequest {
    private String id;
    private String name;
    private String description;
    private Set<String> allowedRoles;
    private Set<String> allowedOperations;
    private Set<String> protocolBlocklist;
    private Boolean dangerousSqlBlock;
    private Set<String> requireApprovalFor;
    private Map<String, Set<String>> tablePermissions;
    private Integer rateLimitPerMinute;
    private Integer rateLimitPerHour;
    private Boolean piiMaskingOnResult;
}
