package com.agenthub.domain.model.agent;

import com.agenthub.domain.model.Workspace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * @author huangdayu
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ReActAgentWorkspace {

    private Workspace workspace;

    private Path rootPath;

    private Path skillsPath;

    private Path shareSkillsPath;

    private Path agentsPath;

    private Path cronPath;

    private Path logsPath;

    private Path configsPath;

    private Path sessionsPath;

}
