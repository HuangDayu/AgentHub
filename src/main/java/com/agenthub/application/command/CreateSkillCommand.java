package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSkillCommand {
    private String tenantId;
    private String workspaceId;
    private String skillCode;
    private String name;
    private String description;
    private String skillType;
    private String skillPath;
    private String source;
    private String sourcePath;
    private String zipUrl;
    private InputStream zipStream;
    private long zipSize;
}
