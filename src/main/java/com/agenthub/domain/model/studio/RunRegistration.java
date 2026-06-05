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
     * 工厂方法所需字段快照。
     */
    public static final class CreationSpec {
        private final String id;
        private final String project;
        private final String name;
        private final Instant timestamp;
        private final Integer pid;
        private final String status;
        private final String runDir;

        public CreationSpec(String id, String project, String name,
                               Instant timestamp, Integer pid, String status, String runDir) {
            this.id = id;
            this.project = project;
            this.name = name;
            this.timestamp = timestamp;
            this.pid = pid;
            this.status = status;
            this.runDir = runDir;
        }
    }

    /**
     * 创建Run注册.
     */
    public static RunRegistration create(CreationSpec spec) {
        RunRegistration registration = new RunRegistration();
        registration.id = spec.id;
        registration.project = spec.project;
        registration.name = spec.name;
        registration.timestamp = spec.timestamp;
        registration.pid = spec.pid;
        registration.status = spec.status;
        registration.runDir = spec.runDir;
        registration.createdAt = Instant.now();
        return registration;
    }
}
