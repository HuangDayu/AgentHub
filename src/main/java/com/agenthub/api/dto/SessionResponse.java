package com.agenthub.api.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String id;
    private String agentId;
    private String name;
    private Instant createdAt;
}
