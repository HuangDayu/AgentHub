package com.agenthub.api.dto;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigResponse {
    private String id;
    private String name;
    private String type;
    private String supplier;
    private String apiKey;
    private String baseUrl;
    private String model;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
