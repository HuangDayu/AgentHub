package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.agenthub.application.port.out.repositories.AlertRepository;
import com.agenthub.domain.model.monitor.Alert;
import com.agenthub.infrastructure.store.db.entity.AlertEntity;
import com.agenthub.infrastructure.store.db.mapper.AlertMybatisMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Optional;

import static org.springframework.ai.util.json.JsonParser.fromJson;
import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * Alert Repository 实现.
 */
@Repository
@RequiredArgsConstructor
public class MybatisAlertRepository implements AlertRepository {
    private final AlertMybatisMapper mapper;

    @Override
    public Alert save(Alert alert) {
        AlertEntity entity = toEntity(alert);
        mapper.insert(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Alert> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id))
                .map(this::toDomain);
    }

    @Override
    public List<Alert> findByRunId(String runId) {
        LambdaQueryWrapper<AlertEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertEntity::getRunId, runId);
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Alert> findByResolved(boolean resolved) {
        LambdaQueryWrapper<AlertEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertEntity::getResolved, resolved);
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Alert> findAll() {
        return mapper.selectList(null).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private AlertEntity toEntity(Alert alert) {
        AlertEntity alertEntity = new AlertEntity();
        BeanUtil.copyProperties(alert, alertEntity, metadataCopyOptions());
        alertEntity.setMetadata(toJson(alert.getMetadata()));
        return alertEntity;
    }

    private Alert toDomain(AlertEntity entity) {
        Alert alert = new Alert();
        BeanUtil.copyProperties(entity, alert, metadataCopyOptions());
        alert.setMetadata(fromJson(entity.getMetadata(), new TypeReference<>() {
        }));
        return alert;
    }

    private CopyOptions metadataCopyOptions() {
        return CopyOptions.create().setIgnoreProperties("metadata");
    }
}
