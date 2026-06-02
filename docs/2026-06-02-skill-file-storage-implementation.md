# Skill 文件存储实现计划

## 项目信息
- **项目**: AgentHub
- **分支**: main
- **编码规范**: Java 21, Spring Boot 4.1, Clean Architecture

---

## 实施步骤

### Phase 1: 数据库与领域模型

#### 步骤 1.1: 创建 skill_file 表
**文件**: `sql/schema.sql`

```sql
-- 在 skill 表定义后添加
CREATE TABLE IF NOT EXISTS skill_file
(
    id              varchar(64) NOT NULL,
    skill_id        varchar(64) NOT NULL,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    file_path       varchar(1024) NOT NULL,
    file_name       varchar(255) NOT NULL,
    file_ext        varchar(64),
    file_size       bigint,
    file_type       varchar(32),
    encoding        varchar(32),
    content_text    text,
    content_binary  bytea,
    checksum        varchar(64),
    is_directory    boolean DEFAULT false,
    metadata        text,
    version         integer DEFAULT 1,
    created_at      timestamptz,
    updated_at      timestamptz,
    PRIMARY KEY (id),
    CONSTRAINT fk_skill_file_skill FOREIGN KEY (skill_id) 
        REFERENCES skill(id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_skill_file_skill_id ON skill_file(skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_file_path ON skill_file(file_path);
CREATE INDEX IF NOT EXISTS idx_skill_file_ext ON skill_file(file_ext);

-- 修改 skill 表添加新字段
ALTER TABLE skill ADD COLUMN IF NOT EXISTS file_count integer DEFAULT 0;
ALTER TABLE skill ADD COLUMN IF NOT EXISTS total_size bigint DEFAULT 0;
ALTER TABLE skill ADD COLUMN IF NOT EXISTS last_sync_at timestamptz;
```

---

#### 步骤 1.2: 创建 SkillFile 领域模型
**文件**: `src/main/java/com/agenthub/domain/model/skill/SkillFile.java`

```java
package com.agenthub.domain.model.skill;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 技能文件，管理单个文件的元数据和内容。
 */
public class SkillFile {
    private String id;
    private String skillId;
    private String tenantId;
    private String workspaceId;
    private String filePath;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private FileType fileType;
    private String encoding;
    private String contentText;
    private byte[] contentBinary;
    private String checksum;
    private boolean isDirectory;
    private String metadata;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 文件类型枚举。
     */
    public enum FileType {
        TEXT, BINARY, JSON, YAML, MD, IMAGE, UNKNOWN
    }

    public SkillFile() {
    }

    /**
     * 创建文本文件。
     */
    public static SkillFile createText(String skillId, String tenantId,
                                        String workspaceId, String filePath,
                                        String content, String encoding) {
        SkillFile file = new SkillFile();
        file.id = randomId();
        file.skillId = skillId;
        file.tenantId = tenantId;
        file.workspaceId = workspaceId;
        file.filePath = filePath;
        file.fileName = extractFileName(filePath);
        file.fileExt = extractExtension(filePath);
        file.fileSize = content != null ? (long) content.length() : 0L;
        file.fileType = detectFileType(file.fileExt);
        file.encoding = encoding;
        file.contentText = content;
        file.version = 1;
        file.createdAt = Instant.now();
        file.updatedAt = Instant.now();
        return file;
    }

    /**
     * 创建二进制文件。
     */
    public static SkillFile createBinary(String skillId, String tenantId,
                                          String workspaceId, String filePath,
                                          byte[] content) {
        SkillFile file = new SkillFile();
        file.id = randomId();
        file.skillId = skillId;
        file.tenantId = tenantId;
        file.workspaceId = workspaceId;
        file.filePath = filePath;
        file.fileName = extractFileName(filePath);
        file.fileExt = extractExtension(filePath);
        file.fileSize = content != null ? (long) content.length : 0L;
        file.fileType = FileType.BINARY;
        file.contentBinary = content;
        file.version = 1;
        file.createdAt = Instant.now();
        file.updatedAt = Instant.now();
        return file;
    }

    /**
     * 更新文件内容。
     */
    public void updateContent(String content, String encoding) {
        this.contentText = content;
        this.encoding = encoding;
        this.fileSize = content != null ? (long) content.length() : 0L;
        this.checksum = calculateChecksum(content);
        this.version++;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新二进制内容。
     */
    public void updateContent(byte[] content) {
        this.contentBinary = content;
        this.fileSize = content != null ? (long) content.length : 0L;
        this.version++;
        this.updatedAt = Instant.now();
    }

    /**
     * 计算校验和。
     */
    private String calculateChecksum(String content) {
        if (content == null) return null;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 提取文件名。
     */
    private static String extractFileName(String filePath) {
        if (filePath == null) return "";
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * 提取扩展名。
     */
    private static String extractExtension(String filePath) {
        String fileName = extractFileName(filePath);
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot).toLowerCase() : "";
    }

    /**
     * 检测文件类型。
     */
    private static FileType detectFileType(String ext) {
        if (ext == null) return FileType.UNKNOWN;
        return switch (ext.toLowerCase()) {
            case ".md", ".txt", ".java", ".py", ".js", ".ts", ".html", ".css", ".xml" -> FileType.TEXT;
            case ".json" -> FileType.JSON;
            case ".yaml", ".yml" -> FileType.YAML;
            case ".jpg", ".jpeg", ".png", ".gif", ".svg", ".webp" -> FileType.IMAGE;
            default -> FileType.UNKNOWN;
        };
    }

    // Getters
    public String getId() { return id; }
    public String getSkillId() { return skillId; }
    public String getTenantId() { return tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public String getFileExt() { return fileExt; }
    public Long getFileSize() { return fileSize; }
    public FileType getFileType() { return fileType; }
    public String getEncoding() { return encoding; }
    public String getContentText() { return contentText; }
    public byte[] getContentBinary() { return contentBinary; }
    public String getChecksum() { return checksum; }
    public boolean isDirectory() { return isDirectory; }
    public String getMetadata() { return metadata; }
    public int getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileExt(String fileExt) { this.fileExt = fileExt; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public void setFileType(FileType fileType) { this.fileType = fileType; }
    public void setEncoding(String encoding) { this.encoding = encoding; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public void setContentBinary(byte[] contentBinary) { this.contentBinary = contentBinary; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public void setDirectory(boolean directory) { isDirectory = directory; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setVersion(int version) { this.version = version; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
```

---

#### 步骤 1.3: 修改 Skill 领域模型
**文件**: `src/main/java/com/agenthub/domain/model/skill/Skill.java`

**新增字段**:
```java
private int fileCount;
private long totalSize;
private Instant lastSyncAt;
```

**新增方法**:
```java
/**
 * 更新文件统计信息。
 */
public void updateFileStats(int fileCount, long totalSize) {
    this.fileCount = fileCount;
    this.totalSize = totalSize;
    this.updatedAt = Instant.now();
}

/**
 * 标记同步时间。
 */
public void markSynced() {
    this.lastSyncAt = Instant.now();
    this.updatedAt = Instant.now();
}
```

**修改构造函数和工厂方法** 添加新字段。

---

#### 步骤 1.4: 创建 SkillFileRepository 接口
**文件**: `src/main/java/com/agenthub/application/port/out/repositories/SkillFileRepository.java`

```java
package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.skill.SkillFile;
import java.util.List;
import java.util.Optional;

/**
 * 技能文件仓储接口。
 */
public interface SkillFileRepository {

    /**
     * 保存或更新文件。
     */
    SkillFile saveOrUpdate(SkillFile file);

    /**
     * 批量保存。
     */
    List<SkillFile> saveAll(List<SkillFile> files);

    /**
     * 根据 ID 查找。
     */
    Optional<SkillFile> findById(String id);

    /**
     * 根据 skillId 和路径查找。
     */
    Optional<SkillFile> findBySkillIdAndPath(String skillId, String filePath);

    /**
     * 查找 skill 下的所有文件。
     */
    List<SkillFile> findBySkillId(String skillId);

    /**
     * 查找 skill 下的文本文件。
     */
    List<SkillFile> findTextFilesBySkillId(String skillId);

    /**
     * 根据扩展名查找。
     */
    List<SkillFile> findBySkillIdAndExt(String skillId, String ext);

    /**
     * 按内容搜索。
     */
    List<SkillFile> searchByContent(String skillId, String keyword);

    /**
     * 删除文件。
     */
    void deleteById(String id);

    /**
     * 删除 skill 下的所有文件。
     */
    void deleteBySkillId(String skillId);

    /**
     * 根据路径删除。
     */
    void deleteBySkillIdAndPath(String skillId, String filePath);

    /**
     * 统计 skill 的文件数量和总大小。
     */
    FileStats getStats(String skillId);

    /**
     * 文件统计信息。
     */
    record FileStats(int fileCount, long totalSize) {}
}
```

---

### Phase 2: Infrastructure 层

#### 步骤 2.1: 创建 SkillFileEntity
**文件**: `src/main/java/com/agenthub/infrastructure/store/db/entity/SkillFileEntity.java`

```java
package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;

/**
 * 技能文件实体。
 */
@Data
@TableName("skill_file")
public class SkillFileEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "skill_id")
    private String skillId;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @TableField(value = "file_path")
    private String filePath;

    @TableField(value = "file_name")
    private String fileName;

    @TableField(value = "file_ext")
    private String fileExt;

    @TableField(value = "file_size")
    private Long fileSize;

    @TableField(value = "file_type")
    private String fileType;

    private String encoding;

    @TableField(value = "content_text", jdbcType = JdbcType.LONGVARCHAR)
    private String contentText;

    @TableField(value = "content_binary", jdbcType = JdbcType.BLOB)
    private byte[] contentBinary;

    private String checksum;

    @TableField(value = "is_directory")
    private boolean isDirectory;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String metadata;

    private Integer version;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
```

---

#### 步骤 2.2: 创建 SkillFileMybatisMapper
**文件**: `src/main/java/com/agenthub/infrastructure/store/db/mapper/SkillFileMybatisMapper.java`

```java
package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.SkillFileEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 技能文件 Mapper。
 */
@Mapper
public interface SkillFileMybatisMapper extends BaseMapper<SkillFileEntity> {

    /**
     * 根据 skillId 和路径查找。
     */
    @Select("SELECT * FROM skill_file WHERE skill_id = #{skillId} AND file_path = #{filePath}")
    SkillFileEntity selectBySkillIdAndPath(@Param("skillId") String skillId,
                                           @Param("filePath") String filePath);

    /**
     * 根据扩展名查找。
     */
    @Select("SELECT * FROM skill_file WHERE skill_id = #{skillId} AND file_ext = #{ext}")
    List<SkillFileEntity> selectBySkillIdAndExt(@Param("skillId") String skillId,
                                                 @Param("ext") String ext);

    /**
     * 搜索文件内容。
     */
    @Select("SELECT * FROM skill_file WHERE skill_id = #{skillId} " +
            "AND content_text ILIKE CONCAT('%', #{keyword}, '%')")
    List<SkillFileEntity> searchByContent(@Param("skillId") String skillId,
                                          @Param("keyword") String keyword);

    /**
     * 获取文件统计。
     */
    @Select("SELECT COUNT(*) as file_count, COALESCE(SUM(file_size), 0) as total_size " +
            "FROM skill_file WHERE skill_id = #{skillId} AND is_directory = false")
    Object[] selectStats(@Param("skillId") String skillId);
}
```

---

#### 步骤 2.3: 创建 MybatisSkillFileRepository
**文件**: `src/main/java/com/agenthub/infrastructure/store/db/repository/MybatisSkillFileRepository.java`

```java
package com.agenthub.infrastructure.store.db.repository;

import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.infrastructure.store.db.entity.SkillFileEntity;
import com.agenthub.infrastructure.store.db.mapper.SkillFileMybatisMapper;
import com.agenthub.domain.model.skill.SkillFile;
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
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
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
    public Optional<SkillFile> findBySkillIdAndPath(String skillId, String filePath) {
        return Optional.ofNullable(mapper.selectBySkillIdAndPath(skillId, filePath))
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
    public List<SkillFile> findTextFilesBySkillId(String skillId) {
        return mapper.selectList(
                new LambdaQueryWrapper<SkillFileEntity>()
                        .eq(SkillFileEntity::getSkillId, skillId)
                        .eq(SkillFileEntity::getIsDirectory, false)
                        .isNotNull(SkillFileEntity::getContentText)
                        .orderByAsc(SkillFileEntity::getFilePath))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<SkillFile> findBySkillIdAndExt(String skillId, String ext) {
        return mapper.selectBySkillIdAndExt(skillId, ext)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<SkillFile> searchByContent(String skillId, String keyword) {
        return mapper.searchByContent(skillId, keyword)
                .stream().map(this::toDomain).toList();
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
    public void deleteBySkillIdAndPath(String skillId, String filePath) {
        mapper.delete(
                new LambdaQueryWrapper<SkillFileEntity>()
                        .eq(SkillFileEntity::getSkillId, skillId)
                        .eq(SkillFileEntity::getFilePath, filePath));
    }

    @Override
    public FileStats getStats(String skillId) {
        Object[] stats = mapper.selectStats(skillId);
        if (stats != null && stats.length >= 2) {
            return new FileStats(
                    stats[0] != null ? ((Number) stats[0]).intValue() : 0,
                    stats[1] != null ? ((Number) stats[1]).longValue() : 0L
            );
        }
        return new FileStats(0, 0L);
    }

    /**
     * 转换为实体。
     */
    private SkillFileEntity toEntity(SkillFile domain) {
        SkillFileEntity entity = new SkillFileEntity();
        entity.setId(domain.getId());
        entity.setSkillId(domain.getSkillId());
        entity.setTenantId(domain.getTenantId());
        entity.setWorkspaceId(domain.getWorkspaceId());
        entity.setFilePath(domain.getFilePath());
        entity.setFileName(domain.getFileName());
        entity.setFileExt(domain.getFileExt());
        entity.setFileSize(domain.getFileSize());
        entity.setFileType(domain.getFileType() != null ? domain.getFileType().name() : null);
        entity.setEncoding(domain.getEncoding());
        entity.setContentText(domain.getContentText());
        entity.setContentBinary(domain.getContentBinary());
        entity.setChecksum(domain.getChecksum());
        entity.setDirectory(domain.isDirectory());
        entity.setMetadata(domain.getMetadata());
        entity.setVersion(domain.getVersion());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    /**
     * 转换为领域模型。
     */
    private SkillFile toDomain(SkillFileEntity entity) {
        SkillFile domain = new SkillFile();
        domain.setId(entity.getId());
        domain.setSkillId(entity.getSkillId());
        domain.setTenantId(entity.getTenantId());
        domain.setWorkspaceId(entity.getWorkspaceId());
        domain.setFilePath(entity.getFilePath());
        domain.setFileName(entity.getFileName());
        domain.setFileExt(entity.getFileExt());
        domain.setFileSize(entity.getFileSize());
        domain.setFileType(entity.getFileType() != null ?
                SkillFile.FileType.valueOf(entity.getFileType()) : null);
        domain.setEncoding(entity.getEncoding());
        domain.setContentText(entity.getContentText());
        domain.setContentBinary(entity.getContentBinary());
        domain.setChecksum(entity.getChecksum());
        domain.setDirectory(entity.isDirectory());
        domain.setMetadata(entity.getMetadata());
        domain.setVersion(entity.getVersion() != null ? entity.getVersion() : 1);
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
```

---

#### 步骤 2.4: 修改 SkillEntity
**文件**: `src/main/java/com/agenthub/infrastructure/store/db/entity/SkillEntity.java`

**新增字段**:
```java
@TableField(value = "file_count")
private Integer fileCount;

@TableField(value = "total_size")
private Long totalSize;

@TableField(value = "last_sync_at")
private Instant lastSyncAt;
```

---

#### 步骤 2.5: 修改 MybatisSkillRepository
**文件**: `src/main/java/com/agenthub/infrastructure/store/db/repository/MybatisSkillRepository.java`

**新增方法**:
```java
@Override
public void updateFileStats(String skillId, int fileCount, long totalSize) {
    SkillEntity entity = new SkillEntity();
    entity.setId(skillId);
    entity.setFileCount(fileCount);
    entity.setTotalSize(totalSize);
    mapper.updateById(entity);
}

@Override
public void updateSyncTime(String skillId) {
    SkillEntity entity = new SkillEntity();
    entity.setId(skillId);
    entity.setLastSyncAt(Instant.now());
    mapper.updateById(entity);
}
```

---

### Phase 3: UseCase 层

#### 步骤 3.1: 创建 SkillFileUseCase
**文件**: `src/main/java/com/agenthub/application/usecase/SkillFileUseCase.java`

```java
package com.agenthub.application.usecase;

import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.skill.Skill;
import com.agenthub.domain.model.skill.SkillFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 技能文件用例。
 */
@Component
@RequiredArgsConstructor
public class SkillFileUseCase {

    private final SkillFileRepository skillFileRepository;
    private final SkillRepository skillRepository;

    /**
     * 获取 skill 的所有文件。
     */
    @Transactional(readOnly = true)
    public List<SkillFile> getSkillFiles(String skillId) {
        verifySkillExists(skillId);
        return skillFileRepository.findBySkillId(skillId);
    }

    /**
     * 获取单个文件内容。
     */
    @Transactional(readOnly = true)
    public Optional<SkillFile> getFile(String skillId, String filePath) {
        verifySkillExists(skillId);
        return skillFileRepository.findBySkillIdAndPath(skillId, filePath);
    }

    /**
     * 保存文件。
     */
    @Transactional
    public SkillFile saveFile(String skillId, SkillFile file) {
        Skill skill = verifySkillExists(skillId);
        file.setSkillId(skillId);
        file.setTenantId(skill.getTenantId());
        file.setWorkspaceId(skill.getWorkspaceId());
        SkillFile saved = skillFileRepository.saveOrUpdate(file);
        updateSkillStats(skillId);
        return saved;
    }

    /**
     * 批量保存文件。
     */
    @Transactional
    public List<SkillFile> saveFiles(String skillId, List<SkillFile> files) {
        Skill skill = verifySkillExists(skillId);
        files.forEach(file -> {
            file.setSkillId(skillId);
            file.setTenantId(skill.getTenantId());
            file.setWorkspaceId(skill.getWorkspaceId());
        });
        List<SkillFile> saved = skillFileRepository.saveAll(files);
        updateSkillStats(skillId);
        return saved;
    }

    /**
     * 删除文件。
     */
    @Transactional
    public void deleteFile(String skillId, String filePath) {
        verifySkillExists(skillId);
        skillFileRepository.deleteBySkillIdAndPath(skillId, filePath);
        updateSkillStats(skillId);
    }

    /**
     * 删除 skill 的所有文件。
     */
    @Transactional
    public void deleteAllFiles(String skillId) {
        verifySkillExists(skillId);
        skillFileRepository.deleteBySkillId(skillId);
        updateSkillStats(skillId);
    }

    /**
     * 搜索文件内容。
     */
    @Transactional(readOnly = true)
    public List<SkillFile> searchContent(String skillId, String keyword) {
        verifySkillExists(skillId);
        return skillFileRepository.searchByContent(skillId, keyword);
    }

    /**
     * 按扩展名查找文件。
     */
    @Transactional(readOnly = true)
    public List<SkillFile> getFilesByExt(String skillId, String ext) {
        verifySkillExists(skillId);
        return skillFileRepository.findBySkillIdAndExt(skillId, ext);
    }

    /**
     * 获取文件统计。
     */
    @Transactional(readOnly = true)
    public SkillFileRepository.FileStats getStats(String skillId) {
        verifySkillExists(skillId);
        return skillFileRepository.getStats(skillId);
    }

    /**
     * 验证 skill 存在。
     */
    private Skill verifySkillExists(String skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
    }

    /**
     * 更新 skill 文件统计。
     */
    private void updateSkillStats(String skillId) {
        SkillFileRepository.FileStats stats = skillFileRepository.getStats(skillId);
        skillRepository.updateFileStats(skillId, stats.fileCount(), stats.totalSize());
    }
}
```

---

#### 步骤 3.2: 修改 SkillUseCase
**文件**: `src/main/java/com/agenthub/application/usecase/SkillUseCase.java`

**新增依赖**:
```java
private final SkillFileRepository skillFileRepository;
```

**新增方法**:
```java
/**
 * 同步技能文件到数据库。
 */
@Transactional
public void syncFiles() {
    List<Skill> skills = skillToolScannerPort.scanSkills(skillSharePath);
    for (Skill skill : skills) {
        Skill savedSkill = repository.saveOrUpdate(skill);
        List<SkillFile> files = scanSkillFiles(savedSkill);
        skillFileRepository.deleteBySkillId(savedSkill.getId());
        skillFileRepository.saveAll(files);
        SkillFileRepository.FileStats stats = skillFileRepository.getStats(savedSkill.getId());
        repository.updateFileStats(savedSkill.getId(), stats.fileCount(), stats.totalSize());
        repository.updateSyncTime(savedSkill.getId());
    }
}

/**
 * 扫描 skill 目录下的所有文件。
 */
private List<SkillFile> scanSkillFiles(Skill skill) {
    Path skillPath = Path.of(skill.getSkillPath());
    List<SkillFile> files = new ArrayList<>();
    try (Stream<Path> paths = java.nio.file.Files.walk(skillPath)) {
        paths.filter(java.nio.file.Files::isRegularFile).forEach(path -> {
            String relativePath = skillPath.relativize(path).toString();
            SkillFile file = createSkillFile(skill, relativePath, path);
            if (file != null) {
                files.add(file);
            }
        });
    } catch (Exception e) {
        // 日志记录
    }
    return files;
}

/**
 * 创建 SkillFile 对象。
 */
private SkillFile createSkillFile(Skill skill, String relativePath, Path filePath) {
    try {
        String content = java.nio.file.Files.readString(filePath);
        return SkillFile.createText(skill.getId(), skill.getTenantId(),
                skill.getWorkspaceId(), relativePath, content, "UTF-8");
    } catch (Exception e) {
        return null;
    }
}
```

---

### Phase 4: API 层

#### 步骤 4.1: 创建 SkillFileController
**文件**: `src/main/java/com/agenthub/api/controller/SkillFileController.java`

```java
package com.agenthub.api.controller;

import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.application.usecase.SkillFileUseCase;
import com.agenthub.domain.model.skill.SkillFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能文件控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skills/{skillId}/files")
@RequiredArgsConstructor
public class SkillFileController {

    private final SkillFileUseCase skillFileUseCase;

    /**
     * 获取 skill 的所有文件。
     */
    @GetMapping
    public ResponseEntity<List<SkillFile>> getFiles(
            @PathVariable String workspaceId,
            @PathVariable String skillId) {
        return ResponseEntity.ok(skillFileUseCase.getSkillFiles(skillId));
    }

    /**
     * 获取单个文件内容。
     */
    @GetMapping("/{filePath}")
    public ResponseEntity<SkillFile> getFile(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String filePath) {
        return skillFileUseCase.getFile(skillId, filePath)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 保存文件。
     */
    @PostMapping
    public ResponseEntity<SkillFile> saveFile(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @RequestBody SkillFile file) {
        return ResponseEntity.ok(skillFileUseCase.saveFile(skillId, file));
    }

    /**
     * 删除文件。
     */
    @DeleteMapping("/{filePath}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String filePath) {
        skillFileUseCase.deleteFile(skillId, filePath);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索文件内容。
     */
    @GetMapping("/search")
    public ResponseEntity<List<SkillFile>> searchContent(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(skillFileUseCase.searchContent(skillId, keyword));
    }

    /**
     * 按扩展名查找文件。
     */
    @GetMapping("/ext/{ext}")
    public ResponseEntity<List<SkillFile>> getFilesByExt(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String ext) {
        return ResponseEntity.ok(skillFileUseCase.getFilesByExt(skillId, ext));
    }

    /**
     * 获取文件统计。
     */
    @GetMapping("/stats")
    public ResponseEntity<SkillFileRepository.FileStats> getStats(
            @PathVariable String workspaceId,
            @PathVariable String skillId) {
        return ResponseEntity.ok(skillFileUseCase.getStats(skillId));
    }
}
```

---

#### 步骤 4.2: 更新 SkillResponse
**文件**: `src/main/java/com/agenthub/api/dto/SkillResponse.java`

**新增字段**:
```java
private int fileCount;
private long totalSize;
private Instant lastSyncAt;
```

---

### Phase 5: 测试与验证

#### 步骤 5.1: 编译验证
```bash
gradle compileJava
```

#### 步骤 5.2: 运行架构测试
```bash
gradle test --tests "*ArchTest*"
```

#### 步骤 5.3: 运行 Skill 相关测试
```bash
gradle test --tests "*Skill*"
```

---

## 文件清单

### 新增文件 (8个)
1. `src/main/java/com/agenthub/domain/model/skill/SkillFile.java`
2. `src/main/java/com/agenthub/application/port/out/repositories/SkillFileRepository.java`
3. `src/main/java/com/agenthub/infrastructure/store/db/entity/SkillFileEntity.java`
4. `src/main/java/com/agenthub/infrastructure/store/db/mapper/SkillFileMybatisMapper.java`
5. `src/main/java/com/agenthub/infrastructure/store/db/repository/MybatisSkillFileRepository.java`
6. `src/main/java/com/agenthub/application/usecase/SkillFileUseCase.java`
7. `src/main/java/com/agenthub/api/controller/SkillFileController.java`
8. `docs/design/skill-file-storage-design.md`

### 修改文件 (6个)
1. `sql/schema.sql` - 添加 skill_file 表和 skill 表新字段
2. `src/main/java/com/agenthub/domain/model/skill/Skill.java` - 添加 fileCount, totalSize, lastSyncAt
3. `src/main/java/com/agenthub/infrastructure/store/db/entity/SkillEntity.java` - 添加新字段
4. `src/main/java/com/agenthub/infrastructure/store/db/repository/MybatisSkillRepository.java` - 添加 updateFileStats, updateSyncTime
5. `src/main/java/com/agenthub/application/usecase/SkillUseCase.java` - 添加 syncFiles()
6. `src/main/java/com/agenthub/api/dto/SkillResponse.java` - 添加新字段

---

## 验证检查点

- [ ] `gradle compileJava` 编译通过
- [ ] `gradle test --tests "*ArchTest*"` 架构测试通过
- [ ] `gradle test --tests "*Skill*"` Skill 相关测试通过
- [ ] 手动测试: 调用 `/api/v1/workspaces/{id}/skills/{id}/files` 接口
- [ ] 手动测试: 调用 `/api/v1/workspaces/{id}/skills/sync` 同步文件
