package com.agenthub.api.dto;

import com.agenthub.domain.model.VectorStoreType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreConfigRequest {
    private String name;
    private VectorStoreType type;
    private String host;
    private Integer port;
    private String apiKey;
    private String collectionName;
    private String extraParams;
    private Boolean enabled;
}
