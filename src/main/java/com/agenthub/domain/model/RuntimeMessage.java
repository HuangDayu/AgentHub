package com.agenthub.domain.model;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeMessage {
    private String id;
    private String sessionId;
    private String role;
    private String content;
    private Instant createdAt;
}
