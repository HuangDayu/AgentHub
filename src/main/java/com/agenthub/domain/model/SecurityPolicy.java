package com.agenthub.domain.model;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 安全策略聚合根，管理Agent的安全控制策略。
 */
public class SecurityPolicy {
    private final String id;
    private final String tenantId;
    private final String workspaceId;
    private String name;
    private String description;
    private boolean inputValidation;
    private boolean outputFiltering;
    private boolean rateLimitEnabled;
    private int rateLimitPerMinute;
    private boolean contentModeration;
    private boolean piiDetection;
    private String allowedDomains;
    private String blockedPatterns;
    private Instant createdAt;
    private Instant updatedAt;

    public SecurityPolicy(String id, String tenantId, String workspaceId) {
        this.id = id;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
    }

    private SecurityPolicy(String id, String tenantId, String workspaceId, String name,
                           String description, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.inputValidation = true;
        this.outputFiltering = true;
        this.rateLimitEnabled = false;
        this.rateLimitPerMinute = 60;
        this.contentModeration = false;
        this.piiDetection = false;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static SecurityPolicy create(String tenantId, String workspaceId,
                                        String name, String description) {
        return new SecurityPolicy(randomId(), tenantId, workspaceId,
                name, description, Instant.now());
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void configureSecurity(boolean inputValidation, boolean outputFiltering,
                                  boolean rateLimitEnabled, int rateLimitPerMinute,
                                  boolean contentModeration, boolean piiDetection) {
        this.inputValidation = inputValidation;
        this.outputFiltering = outputFiltering;
        this.rateLimitEnabled = rateLimitEnabled;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.contentModeration = contentModeration;
        this.piiDetection = piiDetection;
        this.updatedAt = Instant.now();
    }

    public void setAllowedDomains(String allowedDomains) {
        this.allowedDomains = allowedDomains;
        this.updatedAt = Instant.now();
    }

    public void setBlockedPatterns(String blockedPatterns) {
        this.blockedPatterns = blockedPatterns;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInputValidation() {
        return inputValidation;
    }

    public boolean isOutputFiltering() {
        return outputFiltering;
    }

    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public boolean isContentModeration() {
        return contentModeration;
    }

    public boolean isPiiDetection() {
        return piiDetection;
    }

    public String getAllowedDomains() {
        return allowedDomains;
    }

    public String getBlockedPatterns() {
        return blockedPatterns;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInputValidation(boolean inputValidation) {
        this.inputValidation = inputValidation;
    }

    public void setOutputFiltering(boolean outputFiltering) {
        this.outputFiltering = outputFiltering;
    }

    public void setRateLimitEnabled(boolean rateLimitEnabled) {
        this.rateLimitEnabled = rateLimitEnabled;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public void setContentModeration(boolean contentModeration) {
        this.contentModeration = contentModeration;
    }

    public void setPiiDetection(boolean piiDetection) {
        this.piiDetection = piiDetection;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
