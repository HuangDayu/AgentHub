package com.agenthub.api.dto;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpToolInvokeViewResponse {
    private String toolId;
    private String status;
    private Map<String, Object> output;
}
