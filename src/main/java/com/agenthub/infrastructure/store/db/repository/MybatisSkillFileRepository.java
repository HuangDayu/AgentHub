package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.domain.model.skill.SkillFile;
import com.agenthub.domain.model.skill.SkillFileStats;
import com.agenthub.infrastructure.store.db.entity.SkillFileEntity;
import com.agenthub.infrastructure.store.db.mapper.SkillFileMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 技能文件仓储实现。
 */
@Component
@RequiredArgsConstructor
public class MybatisSkillFileRepository implements SkillFileRepository {

    private final SkillFileMybatisMapper mapper;

    @Override
    public SkillFile saveOrUpdate(SkillFile file) {
        SkillFileEntity entity = toEntity(file);
        SkillFileEntity existing = mapper.selectById(entity.getId());
        if (existing != null) {
            entity.setId(existing.getId());
        }
        mapper.insertOrUpdate(entity);
        return toDomain(entity);
    }

    @Override
    public List<SkillFile> saveAll(List<SkillFile> files) {
        return files.stream().map(this::saveOrUpdate).toList();
    }

    @Override
    public Optional<SkillFile> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public Optional<SkillFile> findBySkillIdAndFileId(String skillId, String fileId) {
        return Optional.ofNullable(mapper.selectById(fileId))
                .map(this::toDomain);
    }

    @Override
    public List<SkillFile> findBySkillId(String skillId) {
        return mapper.selectList(
                        new LambdaQueryWrapper<SkillFileEntity>()
                                .eq(SkillFileEntity::getSkillId, skillId)
                                .orderByAsc(SkillFileEntity::getFilePath))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<SkillFile> findBySkillIdAndExt(String skillId, String ext) {
        return mapper.selectBySkillIdAndExt(skillId, ext)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<SkillFile> findBySkillIdAndFileExt(String skillId, String ext) {
        return findBySkillIdAndExt(skillId, ext);
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    @Override
    public void deleteBySkillId(String skillId) {
        mapper.delete(
                new LambdaQueryWrapper<SkillFileEntity>()
                        .eq(SkillFileEntity::getSkillId, skillId));
    }

    @Override
    public void deleteBySkillIdAndFileId(String skillId, String filePath) {
        mapper.delete(
                new LambdaQueryWrapper<SkillFileEntity>()
                        .eq(SkillFileEntity::getSkillId, skillId)
                        .eq(SkillFileEntity::getFilePath, filePath));
    }


    @Override
    public SkillFileStats getStats(String skillId) {
        return mapper.selectStats(skillId);
    }

    /**
     * 转换为实体。
     */
    private SkillFileEntity toEntity(SkillFile domain) {
        SkillFileEntity entity = new SkillFileEntity();
        BeanUtil.copyProperties(domain, entity, CopyOptions.create().setIgnoreProperties("fileType"));
        entity.setFileType(domain.getFileType() != null ? domain.getFileType().name() : null);
        return entity;
    }

    /**
     * 转换为领域模型。
     */
    private SkillFile toDomain(SkillFileEntity entity) {
        SkillFile domain = new SkillFile();
        BeanUtil.copyProperties(entity, domain, CopyOptions.create().setIgnoreProperties("fileType", "version"));
        domain.setFileType(entity.getFileType() != null ? SkillFile.FileType.valueOf(entity.getFileType()) : null);
        domain.setVersion(entity.getVersion() != null ? entity.getVersion() : 1);
        return domain;
    }
}
