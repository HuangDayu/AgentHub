package com.agenthub.infrastructure.agents.aliyun.filesystem;

import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystemWithShell;
import io.agentscope.harness.agent.filesystem.spec.LocalFilesystemSpec;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * AgentScope 文件系统工厂。
 */
@Component
public class FilesystemFactory {

    public AbstractFilesystem createLocalFilesystem(Path workspacePath) {
        return new LocalFilesystemWithShell(workspacePath);
    }

    public LocalFilesystemSpec createLocalFilesystemSpec() {
        return new LocalFilesystemSpec();
    }

    public LocalFilesystemSpec createLocalFilesystemSpec(int executeTimeoutSeconds) {
        return new LocalFilesystemSpec().executeTimeoutSeconds(executeTimeoutSeconds);
    }
}
