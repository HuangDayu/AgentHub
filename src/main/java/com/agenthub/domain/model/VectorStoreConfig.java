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
public class VectorStoreConfig {
    private String id;
    private String tenantId;
    private String name;
    private VectorStoreType type;
    private String host;
    private Integer port;
    private String apiKey;
    private String collectionName;
    private String extraParams;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
