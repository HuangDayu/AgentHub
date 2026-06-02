package com.agenthub.application.dto;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 技能文件输出。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillFileOutput {
    private String id;
    private String skillId;
    private String tenantId;
    private String workspaceId;
    private String filePath;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private String fileType;
    private String encoding;
    private String storagePath;
    private String checksum;
    private boolean isDirectory;
    private String metadata;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;
}
