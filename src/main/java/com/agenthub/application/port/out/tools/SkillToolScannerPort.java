package com.agenthub.application.port.out.tools;

import com.agenthub.domain.model.Skill;

import java.util.List;

/**
 * @author huangdayu
 */
public interface SkillToolScannerPort {

    List<Skill> scanSkills(String skillsPath);

}
