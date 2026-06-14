package com.agenthub.infrastructure.tools.data_tools;

import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.model.DataFieldMetadata;
import com.agenthub.domain.model.DataModelMetadata;
import com.agenthub.infrastructure.store.db.entity.AgentEntity;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据模型元数据扫描器。
 * <p>
 * 启动时扫描所有带有 {@link AgentDataModel} 注解的类，
 * 生成元数据并缓存到内存中供运行时使用。
 * </p>
 */
@Slf4j
@Component
public class DataModelScanner {

    private static final Set<String> SYSTEM_HIDDEN_FIELDS = Set.of(
        "createdAt", "updatedAt", "tenantId", "workspaceId"
    );

    private final ApplicationContext applicationContext;

    private static final Map<String, DataModelMetadata> MODEL_METADATA = new ConcurrentHashMap<>();
    private static final Map<String, String> CLASS_TO_MODEL = new ConcurrentHashMap<>();
    private static final Map<String, String> MAPPER_TO_MODEL = new ConcurrentHashMap<>();

    public DataModelScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 启动时扫描所有 @AgentDataModel 注解的类
     */
    @PostConstruct
    public void init() {
        registerEntitiesByPackage();
        log.info("扫描到 {} 个 Agent 数据模型: {}", MODEL_METADATA.size(), MODEL_METADATA.keySet());
    }

    /**
     * 如果有注解则注册模型
     */
    private void registerEntitiesByBean() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(AgentDataModel.class);
        beans.values().forEach(bean -> {
            Class<?> clazz = bean.getClass();
            AgentDataModel annotation = clazz.getAnnotation(AgentDataModel.class);
            if (annotation != null) {
                registerModel(clazz, annotation);
            }
        });
    }

    private void registerEntitiesByPackage() {
        Reflections reflections = new Reflections(AgentEntity.class.getPackageName());
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(AgentDataModel.class);
        for (Class<?> clazz : classes) {
            AgentDataModel annotation = clazz.getAnnotation(AgentDataModel.class);
            if (annotation != null) {
                registerModel(clazz, annotation);
            }
        }
    }

    /**
     * 注册数据模型
     */
    private void registerModel(Class<?> clazz, AgentDataModel annotation) {
        DataModelMetadata metadata = buildMetadata(clazz, annotation);
        MODEL_METADATA.put(annotation.name(), metadata);
        CLASS_TO_MODEL.put(clazz.getName(), annotation.name());
        MAPPER_TO_MODEL.put(annotation.mapper().getName(), annotation.name());
        log.debug("注册数据模型: {} -> {}", annotation.name(), clazz.getSimpleName());
    }

    /**
     * 构建元数据
     */
    private DataModelMetadata buildMetadata(Class<?> clazz, AgentDataModel annotation) {
        return DataModelMetadata.fromAnnotation(annotation, clazz, scanFields(clazz));
    }

    /**
     * 扫描实体类的字段元数据
     */
    private List<DataFieldMetadata> scanFields(Class<?> clazz) {
        List<DataFieldMetadata> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            fields.add(buildFieldMetadata(field));
        }
        return fields;
    }

    /**
     * 构建字段元数据，系统字段自动隐藏
     */
    private DataFieldMetadata buildFieldMetadata(Field field) {
        AgentDataField annotation = field.getAnnotation(AgentDataField.class);
        DataFieldMetadata metadata = DataFieldMetadata.fromField(field, annotation);
        if (SYSTEM_HIDDEN_FIELDS.contains(field.getName())) {
            metadata.setHidden(true);
        }
        return metadata;
    }

    /**
     * 根据模型名称获取元数据
     */
    public Optional<DataModelMetadata> getModel(String modelName) {
        return Optional.ofNullable(MODEL_METADATA.get(modelName));
    }

    /**
     * 获取所有模型元数据
     */
    public Collection<DataModelMetadata> getAllModels() {
        return MODEL_METADATA.values();
    }

    /**
     * 根据实体类名获取模型名称
     */
    public Optional<String> getModelNameByClass(String className) {
        return Optional.ofNullable(CLASS_TO_MODEL.get(className));
    }

    /**
     * 根据 Mapper 类名获取模型名称
     */
    public Optional<String> getModelNameByMapper(String mapperClassName) {
        return Optional.ofNullable(MAPPER_TO_MODEL.get(mapperClassName));
    }

    /**
     * 获取所有模型名称
     */
    public Set<String> getAllModelNames() {
        return MODEL_METADATA.keySet();
    }
}
