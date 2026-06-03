package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.skill.SkillFile;
import com.agenthub.domain.model.skill.SkillFileStats;

import java.util.List;
import java.util.Optional;

/**
 * 技能文件仓储接口。
 */
public interface SkillFileRepository {

    SkillFile saveOrUpdate(SkillFile file);

    List<SkillFile> saveAll(List<SkillFile> files);

    Optional<SkillFile> findById(String id);

    Optional<SkillFile> findBySkillIdAndFileId(String skillId, String fileId);

    List<SkillFile> findBySkillId(String skillId);

    List<SkillFile> findBySkillIdAndExt(String skillId, String ext);

    List<SkillFile> findBySkillIdAndFileExt(String skillId, String ext);

    void deleteById(String id);

    void deleteBySkillId(String skillId);

    void deleteBySkillIdAndFileId(String skillId, String filePath);

    SkillFileStats getStats(String skillId);

}
