package com.agenthub.infrastructure.tools;

import java.util.Set;
import java.util.Map;

public interface ToolSecurityPolicy {
    
    boolean isToolAllowed(String toolName, String agentId, Map<String, Object> context);
    
    boolean isParameterAllowed(String toolName, String paramName, Object value, Map<String, Object> context);
    
    Set<String> getAllowedTags(String agentId);
    
    String getSecurityLevel(String toolName);
    
    boolean requiresAuthentication(String toolName);
    
    void validateExecution(String toolName, String agentId, Map<String, Object> params) throws SecurityException;
}
