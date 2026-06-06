package com.agenthub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceColumnResponse {
    private String id;
    private String name;
    private String type;
    private boolean nullable;
    private boolean isPrimary;
    private String defaultValue;
    private String description;
    private boolean isPii;
    private String piiType;
    private int columnOrder;
}
