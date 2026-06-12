package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequest {
    private String method;
    private String url;
    private String body;
}
