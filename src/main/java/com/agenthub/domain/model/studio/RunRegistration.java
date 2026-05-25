package com.agenthub.domain.model.studio;

import lombok.Data;

import java.time.Instant;

/**
 * Run注册领域模型.
 */
@Data
public class RunRegistration {
    private String id;
    private String project;
    private String name;
    private Instant timestamp;
    private Integer pid;
    private String status;
    private String runDir;
    private Instant createdAt;

    /**
     * 创建Run注册.
     */
    public static RunRegistration create(
        String id,
        String project,
        String name,
        Instant timestamp,
        Integer pid,
        String status,
        String runDir
    ) {
        RunRegistration registration = new RunRegistration();
        registration.setId(id);
        registration.setProject(project);
        registration.setName(name);
        registration.setTimestamp(timestamp);
        registration.setPid(pid);
        registration.setStatus(status);
        registration.setRunDir(runDir);
        registration.setCreatedAt(Instant.now());
        return registration;
    }
}
