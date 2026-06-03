package com.agenthub.application.port.out.tools;

import com.agenthub.domain.model.skill.Skill;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author huangdayu
 */
public interface SkillToolScannerPort {

    List<Skill> scanSkills(String skillsPath);

    Optional<Skill> loadSkillFromPath(Path skillPath);
}
