package com.agenthub.application.command;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVectorStoreConfigCommand {
    private String id;
    private String name;
    private String host;
    private Integer port;
    private String apiKey;
    private String collectionName;
    private String extraParams;
    private Boolean enabled;
}
