package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.skill.SkillFile;

import java.util.List;
import java.util.Optional;

/**
 * 技能文件仓储接口。
 */
public interface SkillFileRepository {

    SkillFile saveOrUpdate(SkillFile file);

    List<SkillFile> saveAll(List<SkillFile> files);

    Optional<SkillFile> findById(String id);

    Optional<SkillFile> findBySkillIdAndPath(String skillId, String filePath);

    Optional<SkillFile> findBySkillIdAndFilePath(String skillId, String filePath);

    List<SkillFile> findBySkillId(String skillId);

    List<SkillFile> findBySkillIdAndExt(String skillId, String ext);

    List<SkillFile> findBySkillIdAndFileExt(String skillId, String ext);

    void deleteById(String id);

    void deleteBySkillId(String skillId);

    void deleteBySkillIdAndPath(String skillId, String filePath);

    void deleteBySkillIdAndFilePath(String skillId, String filePath);

    FileStats getStats(String skillId);

    record FileStats(int fileCount, long totalSize) {}
}
