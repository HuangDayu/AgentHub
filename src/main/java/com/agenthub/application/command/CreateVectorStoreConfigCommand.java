package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVectorStoreConfigCommand {
    private String tenantId;
    private String name;
    private String type;
    private String host;
    private Integer port;
    private String apiKey;
    private String collectionName;
}
