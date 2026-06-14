package com.agenthub.infrastructure.tools.data_tools;

import com.agenthub.domain.model.DataFieldMetadata;
import com.agenthub.domain.model.DataModelMetadata;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据模型通用执行器。
 * <p>
 * 直接调用 MyBatis-Plus 的 {@link BaseMapper} 接口，
 * 提供通用的 CRUD 操作实现。
 * </p>
 */
@Slf4j
@Component
public class DataModelInvoker {

    private final ApplicationContext applicationContext;

    public DataModelInvoker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 查询数据列表
     */
    public Map<String, Object> query(DataModelMetadata metadata,
                                     Map<String, Object> filters,
                                     QueryParams params) {
        BaseMapper<Object> mapper = getMapper(metadata);
        QueryWrapper<Object> wrapper = buildWrapper(metadata, filters, params);
        Map<String, Object> result = executeQuery(mapper, wrapper, params);
        maskItemList(result, metadata);
        return result;
    }

    /**
     * 对结果中的 items 进行脱敏
     */
    private void maskItemList(Map<String, Object> result, DataModelMetadata metadata) {
        List<Object> items = (List<Object>) result.get("items");
        if (items != null) {
            result.put("items", maskListSensitiveFields(items, metadata));
        }
    }

    /**
     * 根据 ID 查询单条数据
     */
    public Object findById(DataModelMetadata metadata, String id) {
        Object entity = getMapper(metadata).selectById(id);
        if (entity == null) {
            return null;
        }
        Map<String, DataFieldMetadata> fieldMap = buildFieldMap(metadata);
        return maskSensitiveFields(entity, fieldMap);
    }

    /**
     * 创建数据
     */
    public Object create(DataModelMetadata metadata, Map<String, Object> data) {
        BaseMapper<Object> mapper = getMapper(metadata);
        Object entity = convertToEntity(data, metadata);
        mapper.insert(entity);
        return entity;
    }

    /**
     * 更新数据
     */
    public Object update(DataModelMetadata metadata, String id, Map<String, Object> data) {
        BaseMapper<Object> mapper = getMapper(metadata);
        Object existing = findExistingEntity(mapper, id);
        updateFields(existing, data, metadata);
        mapper.updateById(existing);
        return existing;
    }

    /**
     * 删除数据
     */
    public boolean delete(DataModelMetadata metadata, String id) {
        return getMapper(metadata).deleteById(id) > 0;
    }

    public int batchDelete(DataModelMetadata metadata, List<String> ids) {
        return getMapper(metadata).deleteByIds(ids);
    }

    /**
     * 获取 Mapper 实例
     */
    @SuppressWarnings("unchecked")
    private BaseMapper<Object> getMapper(DataModelMetadata metadata) {
        Class<?> mapperClass = loadClass(metadata.getMapperClassName());
        return (BaseMapper<Object>) applicationContext.getBean(mapperClass);
    }

    /**
     * 加载类
     */
    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("类不存在: " + className, e);
        }
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<Object> buildWrapper(DataModelMetadata metadata,
                                              Map<String, Object> filters,
                                              QueryParams params) {
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        FilterContext filterContext = buildFilterContext(metadata, filters, params);
        addAllFilters(wrapper, filterContext);
        wrapper.orderByDesc("created_at");
        return wrapper;
    }

    /**
     * 构建过滤上下文
     */
    private FilterContext buildFilterContext(DataModelMetadata metadata,
                                             Map<String, Object> filters,
                                             QueryParams params) {
        return FilterContext.builder()
                .metadata(metadata)
                .filters(filters)
                .tenantId(params.getTenantId())
                .workspaceId(params.getWorkspaceId())
                .build();
    }

    /**
     * 添加所有过滤条件
     */
    private void addAllFilters(QueryWrapper<Object> wrapper, FilterContext filterContext) {
        addTenantFilter(wrapper, filterContext);
        addWorkspaceFilter(wrapper, filterContext);
        addUserFilters(wrapper, filterContext);
    }

    /**
     * 添加租户过滤条件
     */
    private void addTenantFilter(QueryWrapper<Object> wrapper, FilterContext filterContext) {
        String tenantId = filterContext.getTenantId();
        String field = filterContext.getMetadata().getTenantField();
        if (tenantId != null && filterContext.getMetadata().isHasTenantField() && field != null && !field.isEmpty()) {
            wrapper.eq(StringUtils.camelToUnderline(field), tenantId);
        }
    }

    /**
     * 添加工作空间过滤条件
     */
    private void addWorkspaceFilter(QueryWrapper<Object> wrapper, FilterContext filterContext) {
        String workspaceId = filterContext.getWorkspaceId();
        String field = filterContext.getMetadata().getWorkspaceField();
        if (workspaceId != null && filterContext.getMetadata().isHasWorkspaceField() && field != null && !field.isEmpty()) {
            wrapper.eq(StringUtils.camelToUnderline(field), workspaceId);
        }
    }

    /**
     * 添加用户自定义过滤条件
     */
    private void addUserFilters(QueryWrapper<Object> wrapper, FilterContext filterContext) {
        Map<String, Object> filters = filterContext.getFilters();
        if (filters == null) return;
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            addFilterIfValid(wrapper, entry, filterContext.getMetadata());
        }
    }

    /**
     * 添加单个过滤条件
     */
    private void addFilterIfValid(QueryWrapper<Object> wrapper,
                                  Map.Entry<String, Object> entry,
                                  DataModelMetadata metadata) {
        String fieldName = entry.getKey();
        Object value = entry.getValue();
        if (isValidFilter(value, fieldName, metadata)) {
            wrapper.eq(StringUtils.camelToUnderline(fieldName), value);
        }
    }

    /**
     * 检查是否为有效过滤条件
     */
    private boolean isValidFilter(Object value, String fieldName, DataModelMetadata metadata) {
        return value != null && isFieldFilterable(fieldName, metadata);
    }

    /**
     * 执行分页查询
     */
    private Map<String, Object> executeQuery(BaseMapper<Object> mapper,
                                             QueryWrapper<Object> wrapper,
                                             QueryParams params) {
        Page<Object> pageParam = new Page<>(params.getPage(), params.getSize());
        Page<Object> result = mapper.selectPage(pageParam, wrapper);
        return buildPageResult(result, params);
    }

    /**
     * 构建分页结果
     */
    private Map<String, Object> buildPageResult(Page<Object> result, QueryParams params) {
        Map<String, Object> response = new HashMap<>();
        response.put("items", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", params.getPage());
        response.put("size", params.getSize());
        return response;
    }

    /**
     * 脱敏列表中的敏感字段
     */
    private List<Object> maskListSensitiveFields(List<Object> items, DataModelMetadata metadata) {
        Map<String, DataFieldMetadata> fieldMap = buildFieldMap(metadata);
        return items.stream()
                .map(r -> maskSensitiveFields(r, fieldMap))
                .toList();
    }

    /**
     * 脱敏单条记录的敏感字段
     */
    private Object maskSensitiveFields(Object entity, Map<String, DataFieldMetadata> fieldMap) {
        Map<String, Object> data = convertToMap(entity);
        applyMasks(data, fieldMap);
        return data;
    }

    /**
     * 构建字段映射
     */
    private Map<String, DataFieldMetadata> buildFieldMap(DataModelMetadata metadata) {
        Map<String, DataFieldMetadata> map = new HashMap<>();
        for (DataFieldMetadata f : metadata.getFields()) {
            map.put(f.getName(), f);
        }
        return map;
    }

    /**
     * 将对象转为 Map
     */
    private Map<String, Object> convertToMap(Object entity) {
        Map<String, Object> map = new HashMap<>();
        for (Field f : entity.getClass().getDeclaredFields()) {
            copyFieldToMap(f, entity, map);
        }
        return map;
    }

    /**
     * 复制单个字段到 Map
     */
    private void copyFieldToMap(Field field, Object entity, Map<String, Object> map) {
        field.setAccessible(true);
        try {
            map.put(field.getName(), field.get(entity));
        } catch (IllegalAccessException e) {
            log.warn("字段访问失败: {}", field.getName());
        }
    }

    /**
     * 应用脱敏
     */
    private void applyMasks(Map<String, Object> data, Map<String, DataFieldMetadata> fieldMap) {
        for (Map.Entry<String, DataFieldMetadata> entry : fieldMap.entrySet()) {
            if (entry.getValue().isSensitive()) {
                data.put(entry.getKey(), "***");
            }
        }
    }

    /**
     * 检查字段是否可过滤
     */
    private boolean isFieldFilterable(String fieldName, DataModelMetadata metadata) {
        return metadata.getFields().stream()
                .filter(f -> f.getName().equals(fieldName))
                .findFirst()
                .map(DataFieldMetadata::isFilterable)
                .orElse(false);
    }

    /**
     * 将 Map 转换为实体对象
     */
    private Object convertToEntity(Map<String, Object> data, DataModelMetadata metadata) {
        try {
            Class<?> entityClass = loadClass(metadata.getEntityClassName());
            Object entity = entityClass.getDeclaredConstructor().newInstance();
            updateFields(entity, data, metadata);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("创建实体对象失败", e);
        }
    }

    /**
     * 查找已存在的实体
     */
    private Object findExistingEntity(BaseMapper<Object> mapper, String id) {
        Object existing = mapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("数据不存在: " + id);
        }
        return existing;
    }

    /**
     * 更新实体字段
     */
    private void updateFields(Object entity, Map<String, Object> data, DataModelMetadata metadata) {
        Class<?> entityClass = loadClass(metadata.getEntityClassName());
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            setFieldIfExist(entity, entry, entityClass);
        }
    }

    /**
     * 设置字段值（如果字段存在）
     */
    private void setFieldIfExist(Object entity, Map.Entry<String, Object> entry, Class<?> entityClass) {
        try {
            setFieldValue(entity, entry, entityClass);
        } catch (NoSuchFieldException e) {
            log.warn("字段不存在，跳过: {}", entry.getKey());
        } catch (IllegalAccessException e) {
            log.warn("字段访问失败: {}", entry.getKey(), e);
        }
    }

    /**
     * 设置字段值
     */
    private void setFieldValue(Object entity, Map.Entry<String, Object> entry, Class<?> entityClass)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = entityClass.getDeclaredField(entry.getKey());
        field.setAccessible(true);
        field.set(entity, convertType(entry.getValue(), field.getType()));
    }

    /**
     * 类型转换
     */
    private Object convertType(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        return parseValue(value.toString(), targetType);
    }

    /**
     * 解析值为指定类型
     */
    private Object parseValue(String valueStr, Class<?> targetType) {
        if (targetType == String.class) return valueStr;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(valueStr);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(valueStr);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(valueStr);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(valueStr);
        if (targetType == Float.class || targetType == float.class) return Float.parseFloat(valueStr);
        return valueStr;
    }


}
