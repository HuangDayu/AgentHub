package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfig {
    private String id;
    private String name;
    private ModelType type;
    private ModelSupplier supplier;
    private String apiKey;
    private String baseUrl;
    private String model;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
