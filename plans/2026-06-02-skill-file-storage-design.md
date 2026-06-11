# Skill 文件存储方案（完整版）

## 1. 需求分析

### 1.1 技能类型

| 类型 | 说明 | 来源 | 同步方式 |
|------|------|------|----------|
| **SYNCED** | 同步技能 | 本地文件夹 | 定时/手动扫描同步 |
| **UPLOADED** | 上传技能 | ZIP 包（上传/URL） | 创建时解压存储 |

### 1.2 存储策略

| 数据 | 存储位置 | 说明 |
|------|----------|------|
| Skill 元数据 | 数据库 `skill` 表 | id, name, type, source 等 |
| 技能配置 | 数据库 `skill_config` 表 | 本地路径、同步设置等 |
| 文件元数据 | 数据库 `skill_file` 表 | 路径、大小、类型、storagePath |
| 文件内容 | MinIO | **所有技能类型的文件都存储到 MinIO** |
| ZIP 原包 | MinIO | 仅 UPLOADED 类型，路径: `agenthub/skills/{skillCode}/_package.zip` |

### 1.3 各类型技能的存储方式

| 技能类型 | 元数据 | 文件内容 | ZIP 原包 | 说明 |
|----------|--------|----------|----------|------|
| **SYNCED** | 数据库 | MinIO | 无 | 本地文件夹同步时，文件上传到 MinIO |
| **UPLOADED** | 数据库 | MinIO | MinIO | ZIP 解压后文件上传到 MinIO，原包也保存 |

### 1.4 MinIO 存储路径规范

```
agenthub/skills/{skillCode}/              # 技能文件根目录
├── SKILL.md                              # 技能定义文件
├── scripts/                              # 脚本目录
│   └── helper.py
├── config.json                           # 配置文件
└── _package.zip                          # ZIP 原包（仅 UPLOADED 类型）
```

### 1.3 功能需求

1. **创建 SYNCED 技能**: 从本地文件夹扫描，同步元数据和文件
2. **创建 UPLOADED 技能**: 
   - 支持上传 ZIP 文件
   - 支持提交网络链接 ZIP
   - 解压 ZIP 到临时目录
   - 扫描解压后的文件
   - 保存 ZIP 原包到 MinIO
   - 保存解压后的文件到 MinIO
   - 保存元数据到数据库
3. **技能配置管理**: 
   - 配置本地技能路径（支持多个）
   - 配置同步策略
   - 配置启用/禁用

---

## 2. 数据库设计

### 2.1 修改表: `skill`

```sql
-- 修改 skill 表
ALTER TABLE skill ADD COLUMN IF NOT EXISTS skill_type varchar(32) NOT NULL DEFAULT 'SYNCED';
ALTER TABLE skill ADD COLUMN IF NOT EXISTS source varchar(32);           -- LOCAL, URL, UPLOAD
ALTER TABLE skill ADD COLUMN IF NOT EXISTS source_path text;             -- 本地路径或 URL
ALTER TABLE skill ADD COLUMN IF NOT EXISTS zip_storage_path text;        -- ZIP 原包 MinIO 路径
ALTER TABLE skill ADD COLUMN IF NOT EXISTS file_count integer DEFAULT 0;
ALTER TABLE skill ADD COLUMN IF NOT EXISTS total_size bigint DEFAULT 0;
ALTER TABLE skill ADD COLUMN IF NOT EXISTS last_sync_at timestamptz;
ALTER TABLE skill ADD COLUMN IF NOT EXISTS config_id varchar(64);        -- 关联 skill_config
```

### 2.2 新增表: `skill_config`

```sql
-- Table: skill_config
CREATE TABLE IF NOT EXISTS skill_config
(
    id              varchar(64) NOT NULL,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    name            varchar(255) NOT NULL,          -- 配置名称
    description     text,                           -- 配置描述
    skill_paths     text NOT NULL,                  -- 本地技能路径列表 (JSON 数组)
    sync_enabled    boolean DEFAULT true,           -- 是否启用同步
    sync_interval   integer DEFAULT 3600,           -- 同步间隔（秒）
    auto_sync       boolean DEFAULT false,          -- 是否自动同步
    enabled         boolean DEFAULT true,
    created_at      timestamptz,
    updated_at      timestamptz,
    PRIMARY KEY (id)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_skill_config_tenant ON skill_config(tenant_id);
CREATE INDEX IF NOT EXISTS idx_skill_config_workspace ON skill_config(workspace_id);
```

### 2.3 新增表: `skill_file`

```sql
-- Table: skill_file
CREATE TABLE IF NOT EXISTS skill_file
(
    id              varchar(64) NOT NULL,
    skill_id        varchar(64) NOT NULL,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    file_path       varchar(1024) NOT NULL,         -- 相对路径
    file_name       varchar(255) NOT NULL,
    file_ext        varchar(64),
    file_size       bigint,
    file_type       varchar(32),
    encoding        varchar(32),
    storage_path    varchar(1024) NOT NULL,         -- MinIO 存储路径
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
```

### 2.4 字段说明

#### skill 表
| 字段 | 类型 | 说明 |
|------|------|------|
| `skill_type` | varchar(32) | SYNCED 或 UPLOADED |
| `source` | varchar(32) | 来源: LOCAL, URL, UPLOAD |
| `source_path` | text | 本地路径或 URL |
| `zip_storage_path` | text | ZIP 原包 MinIO 路径 |
| `file_count` | integer | 文件数量 |
| `total_size` | bigint | 文件总大小 |
| `last_sync_at` | timestamptz | 最后同步时间 |
| `config_id` | varchar(64) | 关联 skill_config |

#### skill_config 表
| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | varchar(255) | 配置名称 |
| `skill_paths` | text | 本地技能路径列表 (JSON 数组) |
| `sync_enabled` | boolean | 是否启用同步 |
| `sync_interval` | integer | 同步间隔（秒） |
| `auto_sync` | boolean | 是否自动同步 |

#### skill_file 表
| 字段 | 类型 | 说明 |
|------|------|------|
| `storage_path` | varchar(1024) | MinIO 存储路径 |
| `file_path` | varchar(1024) | 相对路径 |
| `file_size` | bigint | 文件大小 |

---

## 3. 领域模型设计

### 3.1 修改: `Skill` 领域模型

```java
package com.agenthub.domain.model.skill;

import java.time.Instant;

/**
 * 技能聚合根。
 */
public class Skill {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String skillCode;
    private String name;
    private String description;
    private SkillType skillType;            // SYNCED, UPLOADED
    private SkillSource source;             // LOCAL, URL, UPLOAD
    private String sourcePath;              // 本地路径或 URL
    private String skillPath;               // 本地文件路径（SYNCED 类型）
    private String skillFilesTree;          // 文件树 JSON
    private String zipStoragePath;          // ZIP 原包 MinIO 路径
    private String configId;                // 关联 skill_config
    private int fileCount;
    private long totalSize;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastSyncAt;

    /**
     * 技能类型枚举。
     */
    public enum SkillType {
        SYNCED,     // 同步技能（从本地文件夹）
        UPLOADED    // 上传技能（从 ZIP 包）
    }

    /**
     * 技能源枚举。
     */
    public enum SkillSource {
        LOCAL,      // 本地文件夹
        URL,        // 网络链接
        UPLOAD      // 上传 ZIP
    }

    /**
     * 创建同步技能。
     */
    public static Skill createSynced(String tenantId, String workspaceId,
                                      String skillCode, String name, String description,
                                      String skillPath) {
        Skill skill = new Skill();
        skill.id = randomId();
        skill.tenantId = tenantId;
        skill.workspaceId = workspaceId;
        skill.skillCode = skillCode;
        skill.name = name;
        skill.description = description;
        skill.skillType = SkillType.SYNCED;
        skill.source = SkillSource.LOCAL;
        skill.sourcePath = skillPath;
        skill.skillPath = skillPath;
        skill.enabled = true;
        skill.createdAt = Instant.now();
        skill.updatedAt = Instant.now();
        return skill;
    }

    /**
     * 创建上传技能（从 URL）。
     */
    public static Skill createFromUrl(String tenantId, String workspaceId,
                                       String skillCode, String name, String description,
                                       String url) {
        Skill skill = new Skill();
        skill.id = randomId();
        skill.tenantId = tenantId;
        skill.workspaceId = workspaceId;
        skill.skillCode = skillCode;
        skill.name = name;
        skill.description = description;
        skill.skillType = SkillType.UPLOADED;
        skill.source = SkillSource.URL;
        skill.sourcePath = url;
        skill.enabled = true;
        skill.createdAt = Instant.now();
        skill.updatedAt = Instant.now();
        return skill;
    }

    /**
     * 创建上传技能（从文件上传）。
     */
    public static Skill createFromUpload(String tenantId, String workspaceId,
                                          String skillCode, String name, String description) {
        Skill skill = new Skill();
        skill.id = randomId();
        skill.tenantId = tenantId;
        skill.workspaceId = workspaceId;
        skill.skillCode = skillCode;
        skill.name = name;
        skill.description = description;
        skill.skillType = SkillType.UPLOADED;
        skill.source = SkillSource.UPLOAD;
        skill.enabled = true;
        skill.createdAt = Instant.now();
        skill.updatedAt = Instant.now();
        return skill;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getSkillCode() { return skillCode; }
    public void setSkillCode(String skillCode) { this.skillCode = skillCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SkillType getSkillType() { return skillType; }
    public void setSkillType(SkillType skillType) { this.skillType = skillType; }
    public SkillSource getSource() { return source; }
    public void setSource(SkillSource source) { this.source = source; }
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }
    public String getSkillPath() { return skillPath; }
    public void setSkillPath(String skillPath) { this.skillPath = skillPath; }
    public String getSkillFilesTree() { return skillFilesTree; }
    public void setSkillFilesTree(String skillFilesTree) { this.skillFilesTree = skillFilesTree; }
    public String getZipStoragePath() { return zipStoragePath; }
    public void setZipStoragePath(String zipStoragePath) { this.zipStoragePath = zipStoragePath; }
    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
    public long getTotalSize() { return totalSize; }
    public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    /**
     * 更新文件统计。
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
}
```

### 3.2 新增: `SkillConfig` 领域模型

```java
package com.agenthub.domain.model.skill;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 技能配置，管理技能同步路径和策略。
 */
public class SkillConfig {
    private String id;
    private String tenantId;
    private String workspaceId;
    private String name;
    private String description;
    private List<String> skillPaths;         // 本地技能路径列表
    private boolean syncEnabled;             // 是否启用同步
    private int syncInterval;                // 同步间隔（秒）
    private boolean autoSync;                // 是否自动同步
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public SkillConfig() {
        this.skillPaths = new ArrayList<>();
    }

    /**
     * 创建配置。
     */
    public static SkillConfig create(String tenantId, String workspaceId,
                                      String name, List<String> skillPaths) {
        SkillConfig config = new SkillConfig();
        config.id = randomId();
        config.tenantId = tenantId;
        config.workspaceId = workspaceId;
        config.name = name;
        config.skillPaths = skillPaths != null ? new ArrayList<>(skillPaths) : new ArrayList<>();
        config.syncEnabled = true;
        config.syncInterval = 3600;
        config.autoSync = false;
        config.enabled = true;
        config.createdAt = Instant.now();
        config.updatedAt = Instant.now();
        return config;
    }

    /**
     * 添加技能路径。
     */
    public void addSkillPath(String path) {
        if (!this.skillPaths.contains(path)) {
            this.skillPaths.add(path);
            this.updatedAt = Instant.now();
        }
    }

    /**
     * 移除技能路径。
     */
    public void removeSkillPath(String path) {
        if (this.skillPaths.remove(path)) {
            this.updatedAt = Instant.now();
        }
    }

    /**
     * 更新配置。
     */
    public void update(String name, String description, List<String> skillPaths,
                       boolean syncEnabled, int syncInterval, boolean autoSync) {
        this.name = name;
        this.description = description;
        this.skillPaths = skillPaths != null ? new ArrayList<>(skillPaths) : this.skillPaths;
        this.syncEnabled = syncEnabled;
        this.syncInterval = syncInterval;
        this.autoSync = autoSync;
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getSkillPaths() { return skillPaths; }
    public void setSkillPaths(List<String> skillPaths) { this.skillPaths = skillPaths; }
    public boolean isSyncEnabled() { return syncEnabled; }
    public void setSyncEnabled(boolean syncEnabled) { this.syncEnabled = syncEnabled; }
    public int getSyncInterval() { return syncInterval; }
    public void setSyncInterval(int syncInterval) { this.syncInterval = syncInterval; }
    public boolean isAutoSync() { return autoSync; }
    public void setAutoSync(boolean autoSync) { this.autoSync = autoSync; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
```

### 3.3 新增: `SkillFile` 领域模型

```java
package com.agenthub.domain.model.skill;

import java.time.Instant;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 技能文件元数据。
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
    private String storagePath;             // MinIO 存储路径
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

    /**
     * 创建文件元数据。
     */
    public static SkillFile create(String skillId, String tenantId,
                                    String workspaceId, String filePath,
                                    long fileSize, String encoding) {
        SkillFile file = new SkillFile();
        file.id = randomId();
        file.skillId = skillId;
        file.tenantId = tenantId;
        file.workspaceId = workspaceId;
        file.filePath = filePath;
        file.fileName = extractFileName(filePath);
        file.fileExt = extractExtension(filePath);
        file.fileSize = fileSize;
        file.fileType = detectFileType(file.fileExt);
        file.encoding = encoding;
        file.storagePath = buildStoragePath(skillId, filePath);
        file.version = 1;
        file.createdAt = Instant.now();
        file.updatedAt = Instant.now();
        return file;
    }

    /**
     * 构建 MinIO 存储路径。
     */
    private static String buildStoragePath(String skillId, String filePath) {
        return String.format("agenthub/skills/%s/%s", skillId, filePath);
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
    public String getStoragePath() { return storagePath; }
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
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public void setDirectory(boolean directory) { isDirectory = directory; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setVersion(int version) { this.version = version; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
```

---

## 4. Repository 接口设计

### 4.1 新增: `SkillConfigRepository`

```java
package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.skill.SkillConfig;
import java.util.List;
import java.util.Optional;

/**
 * 技能配置仓储接口。
 */
public interface SkillConfigRepository {

    SkillConfig saveOrUpdate(SkillConfig config);

    Optional<SkillConfig> findById(String id);

    List<SkillConfig> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    List<SkillConfig> findAll();

    void deleteById(String id);
}
```

### 4.2 新增: `SkillFileRepository`

```java
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

    List<SkillFile> findBySkillId(String skillId);

    List<SkillFile> findBySkillIdAndExt(String skillId, String ext);

    void deleteById(String id);

    void deleteBySkillId(String skillId);

    void deleteBySkillIdAndPath(String skillId, String filePath);

    FileStats getStats(String skillId);

    record FileStats(int fileCount, long totalSize) {}
}
```

### 4.3 修改: `SkillRepository`

```java
// 新增方法
void updateFileStats(String skillId, int fileCount, long totalSize);
void updateSyncTime(String skillId);
```

---

## 5. Infrastructure 实现

### 5.1 新增: `SkillConfigEntity`

```java
package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.Instant;

@Data
@TableName("skill_config")
public class SkillConfigEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    private String name;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String description;

    @TableField(value = "skill_paths", jdbcType = JdbcType.LONGVARCHAR)
    private String skillPaths;           // JSON 数组

    @TableField(value = "sync_enabled")
    private Boolean syncEnabled;

    @TableField(value = "sync_interval")
    private Integer syncInterval;

    @TableField(value = "auto_sync")
    private Boolean autoSync;

    private Boolean enabled;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
```

### 5.2 新增: `SkillFileEntity`

```java
package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.Instant;

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

    @TableField(value = "storage_path")
    private String storagePath;

    private String checksum;

    @TableField(value = "is_directory")
    private Boolean isDirectory;

    @TableField(jdbcType = JdbcType.LONGVARCHAR)
    private String metadata;

    private Integer version;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
```

### 5.3 修改: `SkillEntity`

```java
// 新增字段
@TableField(value = "skill_type")
private String skillType;

@TableField(value = "source")
private String source;

@TableField(value = "source_path", jdbcType = JdbcType.LONGVARCHAR)
private String sourcePath;

@TableField(value = "zip_storage_path", jdbcType = JdbcType.LONGVARCHAR)
private String zipStoragePath;

@TableField(value = "file_count")
private Integer fileCount;

@TableField(value = "total_size")
private Long totalSize;

@TableField(value = "last_sync_at")
private Instant lastSyncAt;

@TableField(value = "config_id")
private String configId;
```

---

## 6. UseCase 设计

### 6.1 新增: `SkillConfigUseCase`

```java
package com.agenthub.application.usecase;

import com.agenthub.application.port.out.repositories.SkillConfigRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.skill.SkillConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 技能配置用例。
 */
@Component
@RequiredArgsConstructor
public class SkillConfigUseCase {

    private final SkillConfigRepository repository;

    /**
     * 创建配置。
     */
    @Transactional
    public SkillConfig create(SkillConfig config) {
        return repository.saveOrUpdate(config);
    }

    /**
     * 获取配置。
     */
    @Transactional(readOnly = true)
    public SkillConfig get(String configId) {
        return repository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Skill config not found: " + configId));
    }

    /**
     * 列出工作空间的所有配置。
     */
    @Transactional(readOnly = true)
    public List<SkillConfig> list(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId);
    }

    /**
     * 更新配置。
     */
    @Transactional
    public SkillConfig update(String configId, SkillConfig updated) {
        SkillConfig existing = get(configId);
        existing.update(updated.getName(), updated.getDescription(),
                updated.getSkillPaths(), updated.isSyncEnabled(),
                updated.getSyncInterval(), updated.isAutoSync());
        return repository.saveOrUpdate(existing);
    }

    /**
     * 添加技能路径。
     */
    @Transactional
    public SkillConfig addSkillPath(String configId, String path) {
        SkillConfig config = get(configId);
        config.addSkillPath(path);
        return repository.saveOrUpdate(config);
    }

    /**
     * 移除技能路径。
     */
    @Transactional
    public SkillConfig removeSkillPath(String configId, String path) {
        SkillConfig config = get(configId);
        config.removeSkillPath(path);
        return repository.saveOrUpdate(config);
    }

    /**
     * 删除配置。
     */
    @Transactional
    public void delete(String configId) {
        repository.deleteById(configId);
    }
}
```

### 6.2 修改: `SkillUseCase`

```java
package com.agenthub.application.usecase;

import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.repositories.SkillConfigRepository;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.application.port.out.tools.SkillToolScannerPort;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.skill.Skill;
import com.agenthub.domain.model.skill.SkillConfig;
import com.agenthub.domain.model.skill.SkillFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 技能用例。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillUseCase {

    private final SkillRepository skillRepository;
    private final SkillFileRepository skillFileRepository;
    private final SkillConfigRepository skillConfigRepository;
    private final SkillToolScannerPort skillToolScannerPort;
    private final DocumentFileStoragePort documentFileStoragePort;

    /**
     * 创建同步技能。
     */
    @Transactional
    public Skill createSynced(Skill skill, String configId) {
        // 验证配置存在
        SkillConfig config = skillConfigRepository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Skill config not found: " + configId));
        
        skill.setSkillType(Skill.SkillType.SYNCED);
        skill.setSource(Skill.SkillSource.LOCAL);
        skill.setSourcePath(skill.getSkillPath());
        skill.setConfigId(configId);
        
        Skill saved = skillRepository.saveOrUpdate(skill);
        
        // 扫描并保存文件
        List<SkillFile> files = scanAndStoreSkillFiles(saved);
        skillFileRepository.saveAll(files);
        
        // 更新统计
        SkillFileRepository.FileStats stats = skillFileRepository.getStats(saved.getId());
        skillRepository.updateFileStats(saved.getId(), stats.fileCount(), stats.totalSize());
        
        return saved;
    }

    /**
     * 从 URL 创建上传技能。
     */
    @Transactional
    public Skill createFromUrl(Skill skill, String zipUrl) {
        skill.setSkillType(Skill.SkillType.UPLOADED);
        skill.setSource(Skill.SkillSource.URL);
        skill.setSourcePath(zipUrl);
        
        Skill saved = skillRepository.saveOrUpdate(skill);
        
        // 下载并处理 ZIP
        processZipFromUrl(saved, zipUrl);
        
        return saved;
    }

    /**
     * 从上传创建技能。
     */
    @Transactional
    public Skill createFromUpload(Skill skill, InputStream zipStream, long zipSize) {
        skill.setSkillType(Skill.SkillType.UPLOADED);
        skill.setSource(Skill.SkillSource.UPLOAD);
        
        Skill saved = skillRepository.saveOrUpdate(skill);
        
        // 处理上传的 ZIP
        processUploadedZip(saved, zipStream, zipSize);
        
        return saved;
    }

    /**
     * 处理 URL ZIP。
     */
    private void processZipFromUrl(Skill skill, String zipUrl) {
        try {
            // 1. 保存 ZIP 原包到 MinIO
            String zipStoragePath = String.format("agenthub/skills/%s/_package.zip", skill.getSkillCode());
            
            // 2. 下载 ZIP 到临时目录
            Path tempDir = Files.createTempDirectory("skill-zip-");
            Path zipPath = tempDir.resolve("skill.zip");
            
            try (InputStream in = new java.net.URL(zipUrl).openStream()) {
                Files.copy(in, zipPath);
            }
            
            // 3. 上传 ZIP 到 MinIO
            try (InputStream zipIn = Files.newInputStream(zipPath)) {
                documentFileStoragePort.store(zipStoragePath, zipIn, Files.size(zipPath));
            }
            skill.setZipStoragePath(zipStoragePath);
            
            // 4. 解压并处理
            extractAndProcess(skill, zipPath);
            
            // 5. 清理临时目录
            deleteDirectory(tempDir);
            
        } catch (Exception e) {
            log.error("Failed to process ZIP from URL: {}", zipUrl, e);
            throw new RuntimeException("Failed to process ZIP", e);
        }
    }

    /**
     * 处理上传的 ZIP。
     */
    private void processUploadedZip(Skill skill, InputStream zipStream, long zipSize) {
        try {
            // 1. 保存 ZIP 原包到 MinIO
            String zipStoragePath = String.format("agenthub/skills/%s/_package.zip", skill.getSkillCode());
            documentFileStoragePort.store(zipStoragePath, zipStream, zipSize);
            skill.setZipStoragePath(zipStoragePath);
            
            // 2. 解压到临时目录
            Path tempDir = Files.createTempDirectory("skill-zip-");
            Path zipPath = tempDir.resolve("skill.zip");
            
            // 重新读取流（已经消费了，需要重新获取）
            try (InputStream zipIn = documentFileStoragePort.retrieve(zipStoragePath)) {
                Files.copy(zipIn, zipPath);
            }
            
            // 3. 解压并处理
            extractAndProcess(skill, zipPath);
            
            // 4. 清理
            deleteDirectory(tempDir);
            
        } catch (Exception e) {
            log.error("Failed to process uploaded ZIP", e);
            throw new RuntimeException("Failed to process ZIP", e);
        }
    }

    /**
     * 解压 ZIP 并处理文件。
     */
    private void extractAndProcess(Skill skill, Path zipPath) throws Exception {
        Path extractDir = Files.createTempDirectory("skill-extract-");
        
        // 1. 解压 ZIP
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = extractDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath);
                }
                zis.closeEntry();
            }
        }
        
        // 2. 扫描解压后的文件
        List<SkillFile> files = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(extractDir)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    String relativePath = extractDir.relativize(path).toString();
                    long size = Files.size(path);
                    
                    // 上传到 MinIO
                    String storagePath = String.format("agenthub/skills/%s/%s", 
                            skill.getSkillCode(), relativePath);
                    try (InputStream content = Files.newInputStream(path)) {
                        documentFileStoragePort.store(storagePath, content, size);
                    }
                    
                    // 创建元数据
                    SkillFile file = SkillFile.create(skill.getId(), skill.getTenantId(),
                            skill.getWorkspaceId(), relativePath, size, "UTF-8");
                    files.add(file);
                } catch (Exception e) {
                    log.error("Failed to process file: {}", path, e);
                }
            });
        }
        
        // 3. 保存文件元数据
        skillFileRepository.saveAll(files);
        
        // 4. 更新统计
        SkillFileRepository.FileStats stats = skillFileRepository.getStats(skill.getId());
        skillRepository.updateFileStats(skill.getId(), stats.fileCount(), stats.totalSize());
        
        // 5. 清理
        deleteDirectory(extractDir);
    }

    /**
     * 扫描并存储技能文件（SYNCED 类型）。
     * 本地文件同步到 MinIO。
     */
    private List<SkillFile> scanAndStoreSkillFiles(Skill skill) {
        Path skillPath = Path.of(skill.getSkillPath());
        List<SkillFile> files = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(skillPath)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    String relativePath = skillPath.relativize(path).toString();
                    long size = Files.size(path);
                    
                    // 上传到 MinIO
                    String storagePath = String.format("agenthub/skills/%s/%s", 
                            skill.getSkillCode(), relativePath);
                    try (InputStream content = Files.newInputStream(path)) {
                        documentFileStoragePort.store(storagePath, content, size);
                    }
                    
                    // 创建元数据
                    SkillFile file = SkillFile.create(skill.getId(), skill.getTenantId(),
                            skill.getWorkspaceId(), relativePath, size, "UTF-8");
                    files.add(file);
                    log.debug("Synced file to MinIO: {} -> {}", relativePath, storagePath);
                } catch (Exception e) {
                    log.error("Failed to process file: {}", path, e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to scan skill files: {}", skill.getSkillPath(), e);
        }
        
        return files;
    }

    /**
     * 同步所有配置的技能。
     */
    @Transactional
    public void syncAll() {
        List<SkillConfig> configs = skillConfigRepository.findAll();
        for (SkillConfig config : configs) {
            if (config.isSyncEnabled()) {
                syncByConfig(config);
            }
        }
    }

    /**
     * 按配置同步技能。
     */
    @Transactional
    public void syncByConfig(SkillConfig config) {
        for (String path : config.getSkillPaths()) {
            syncFromPath(path, config.getWorkspaceId());
        }
    }

    /**
     * 从路径同步技能（SYNCED 类型）。
     * 扫描本地文件夹，将文件同步到 MinIO。
     */
    private void syncFromPath(String path, String workspaceId) {
        List<Skill> skills = skillToolScannerPort.scanSkills(path);
        for (Skill skill : skills) {
            skill.setWorkspaceId(workspaceId);
            skill.setSkillType(Skill.SkillType.SYNCED);
            skill.setSource(Skill.SkillSource.LOCAL);
            skill.setSourcePath(path);
            
            Skill saved = skillRepository.saveOrUpdate(skill);
            
            // 扫描本地文件并上传到 MinIO
            List<SkillFile> files = scanAndStoreSkillFiles(saved);
            
            // 删除旧文件元数据，保存新的
            skillFileRepository.deleteBySkillId(saved.getId());
            skillFileRepository.saveAll(files);
            
            // 更新统计
            SkillFileRepository.FileStats stats = skillFileRepository.getStats(saved.getId());
            skillRepository.updateFileStats(saved.getId(), stats.fileCount(), stats.totalSize());
            skillRepository.updateSyncTime(saved.getId());
            
            log.info("Synced skill {} with {} files to MinIO", saved.getSkillCode(), files.size());
        }
    }

    /**
     * 删除临时目录。
     */
    private void deleteDirectory(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); } 
                        catch (Exception e) { log.warn("Failed to delete: {}", p); }
                    });
        } catch (Exception e) {
            log.warn("Failed to delete directory: {}", dir);
        }
    }
}
```

---

## 7. API 设计

### 7.1 新增: `SkillConfigController`

```java
package com.agenthub.api.controller;

import com.agenthub.application.usecase.SkillConfigUseCase;
import com.agenthub.domain.model.skill.SkillConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能配置控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skill-configs")
@RequiredArgsConstructor
public class SkillConfigController {

    private final SkillConfigUseCase useCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillConfig create(@PathVariable String workspaceId,
                              @RequestBody SkillConfig config) {
        config.setWorkspaceId(workspaceId);
        return useCase.create(config);
    }

    @GetMapping
    public List<SkillConfig> list(@PathVariable String workspaceId,
                                   @RequestParam String tenantId) {
        return useCase.list(tenantId, workspaceId);
    }

    @GetMapping("/{configId}")
    public SkillConfig get(@PathVariable String workspaceId,
                           @PathVariable String configId) {
        return useCase.get(configId);
    }

    @PutMapping("/{configId}")
    public SkillConfig update(@PathVariable String workspaceId,
                              @PathVariable String configId,
                              @RequestBody SkillConfig config) {
        return useCase.update(configId, config);
    }

    @PostMapping("/{configId}/paths")
    public SkillConfig addPath(@PathVariable String workspaceId,
                               @PathVariable String configId,
                               @RequestBody AddPathRequest request) {
        return useCase.addSkillPath(configId, request.getPath());
    }

    @DeleteMapping("/{configId}/paths")
    public SkillConfig removePath(@PathVariable String workspaceId,
                                  @PathVariable String configId,
                                  @RequestParam String path) {
        return useCase.removeSkillPath(configId, path);
    }

    @DeleteMapping("/{configId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId,
                       @PathVariable String configId) {
        useCase.delete(configId);
    }

    @Data
    static class AddPathRequest {
        private String path;
    }
}
```

### 7.2 修改: `SkillController`

```java
// 新增端点

/**
 * 从 URL 创建上传技能。
 */
@PostMapping("/from-url")
@ResponseStatus(HttpStatus.CREATED)
public SkillResponse createFromUrl(@PathVariable String workspaceId,
                                    @RequestBody CreateSkillFromUrlRequest request) {
    Skill skill = Skill.createFromUrl(request.getTenantId(), workspaceId,
            request.getSkillCode(), request.getName(), request.getDescription(),
            request.getZipUrl());
    Skill result = useCase.createFromUrl(skill, request.getZipUrl());
    return toResponse(result);
}

/**
 * 上传 ZIP 创建技能。
 */
@PostMapping("/from-upload")
@ResponseStatus(HttpStatus.CREATED)
public SkillResponse createFromUpload(@PathVariable String workspaceId,
                                       @RequestParam String tenantId,
                                       @RequestParam String skillCode,
                                       @RequestParam String name,
                                       @RequestParam String description,
                                       @RequestParam("file") MultipartFile file) throws Exception {
    Skill skill = Skill.createFromUpload(tenantId, workspaceId, skillCode, name, description);
    Skill result = useCase.createFromUpload(skill, file.getInputStream(), file.getSize());
    return toResponse(result);
}

/**
 * 同步所有技能。
 */
@PostMapping("/sync-all")
public void syncAll() {
    useCase.syncAll();
}
```

### 7.3 新增: `SkillFileController`

```java
package com.agenthub.api.controller;

import com.agenthub.application.usecase.SkillFileUseCase;
import com.agenthub.domain.model.skill.SkillFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;

/**
 * 技能文件控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skills/{skillId}/files")
@RequiredArgsConstructor
public class SkillFileController {

    private final SkillFileUseCase skillFileUseCase;

    @GetMapping
    public ResponseEntity<List<SkillFile>> getFiles(
            @PathVariable String workspaceId,
            @PathVariable String skillId) {
        return ResponseEntity.ok(skillFileUseCase.getSkillFiles(skillId));
    }

    @GetMapping("/{filePath}")
    public ResponseEntity<SkillFile> getFile(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String filePath) {
        return skillFileUseCase.getFile(skillId, filePath)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{filePath}/content")
    public ResponseEntity<InputStream> getFileContent(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String filePath) {
        return ResponseEntity.ok(skillFileUseCase.getFileContent(skillId, filePath));
    }

    @DeleteMapping("/{filePath}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String filePath) {
        skillFileUseCase.deleteFile(skillId, filePath);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ext/{ext}")
    public ResponseEntity<List<SkillFile>> getFilesByExt(
            @PathVariable String workspaceId,
            @PathVariable String skillId,
            @PathVariable String ext) {
        return ResponseEntity.ok(skillFileUseCase.getFilesByExt(skillId, ext));
    }

    @GetMapping("/stats")
    public ResponseEntity<SkillFileRepository.FileStats> getStats(
            @PathVariable String workspaceId,
            @PathVariable String skillId) {
        return ResponseEntity.ok(skillFileUseCase.getStats(skillId));
    }
}
```

### 7.4 新增: `SkillFileUseCase`

```java
package com.agenthub.application.usecase;

import com.agenthub.application.port.out.DocumentFileStoragePort;
import com.agenthub.application.port.out.repositories.SkillFileRepository;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.skill.Skill;
import com.agenthub.domain.model.skill.SkillFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
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
    private final DocumentFileStoragePort documentFileStoragePort;

    @Transactional(readOnly = true)
    public List<SkillFile> getSkillFiles(String skillId) {
        verifySkillExists(skillId);
        return skillFileRepository.findBySkillId(skillId);
    }

    @Transactional(readOnly = true)
    public Optional<SkillFile> getFile(String skillId, String filePath) {
        verifySkillExists(skillId);
        return skillFileRepository.findBySkillIdAndPath(skillId, filePath);
    }

    @Transactional(readOnly = true)
    public InputStream getFileContent(String skillId, String filePath) {
        SkillFile file = skillFileRepository.findBySkillIdAndPath(skillId, filePath)
                .orElseThrow(() -> new NotFoundException("File not found: " + filePath));
        return documentFileStoragePort.retrieve(file.getStoragePath());
    }

    @Transactional
    public void deleteFile(String skillId, String filePath) {
        SkillFile file = skillFileRepository.findBySkillIdAndPath(skillId, filePath)
                .orElse(null);
        if (file != null) {
            documentFileStoragePort.delete(file.getStoragePath());
            skillFileRepository.deleteBySkillIdAndPath(skillId, filePath);
            updateSkillStats(skillId);
        }
    }

    @Transactional(readOnly = true)
    public List<SkillFile> getFilesByExt(String skillId, String ext) {
        verifySkillExists(skillId);
        return skillFileRepository.findBySkillIdAndExt(skillId, ext);
    }

    @Transactional(readOnly = true)
    public SkillFileRepository.FileStats getStats(String skillId) {
        verifySkillExists(skillId);
        return skillFileRepository.getStats(skillId);
    }

    private Skill verifySkillExists(String skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
    }

    private void updateSkillStats(String skillId) {
        SkillFileRepository.FileStats stats = skillFileRepository.getStats(skillId);
        skillRepository.updateFileStats(skillId, stats.fileCount(), stats.totalSize());
    }
}
```

---

## 8. API 接口汇总

### 8.1 技能配置 API
```
POST   /api/v1/workspaces/{workspaceId}/skill-configs           - 创建配置
GET    /api/v1/workspaces/{workspaceId}/skill-configs           - 列出配置
GET    /api/v1/workspaces/{workspaceId}/skill-configs/{id}      - 获取配置
PUT    /api/v1/workspaces/{workspaceId}/skill-configs/{id}      - 更新配置
POST   /api/v1/workspaces/{workspaceId}/skill-configs/{id}/paths - 添加路径
DELETE /api/v1/workspaces/{workspaceId}/skill-configs/{id}/paths - 移除路径
DELETE /api/v1/workspaces/{workspaceId}/skill-configs/{id}      - 删除配置
```

### 8.2 技能 API
```
POST   /api/v1/workspaces/{workspaceId}/skills                  - 创建技能
POST   /api/v1/workspaces/{workspaceId}/skills/from-url         - 从 URL 创建
POST   /api/v1/workspaces/{workspaceId}/skills/from-upload      - 上传 ZIP 创建
POST   /api/v1/workspaces/{workspaceId}/skills/sync-all         - 同步所有
GET    /api/v1/workspaces/{workspaceId}/skills                  - 列出技能
GET    /api/v1/workspaces/{workspaceId}/skills/{id}             - 获取技能
PUT    /api/v1/workspaces/{workspaceId}/skills/{id}             - 更新技能
DELETE /api/v1/workspaces/{workspaceId}/skills/{id}             - 删除技能
```

### 8.3 技能文件 API
```
GET    /api/v1/workspaces/{workspaceId}/skills/{id}/files           - 列出文件
GET    /api/v1/workspaces/{workspaceId}/skills/{id}/files/{path}    - 获取文件元数据
GET    /api/v1/workspaces/{workspaceId}/skills/{id}/files/{path}/content - 下载文件
DELETE /api/v1/workspaces/{workspaceId}/skills/{id}/files/{path}    - 删除文件
GET    /api/v1/workspaces/{workspaceId}/skills/{id}/files/ext/{ext} - 按扩展名查找
GET    /api/v1/workspaces/{workspaceId}/skills/{id}/files/stats     - 文件统计
```

---

## 9. 数据流

### 9.1 创建 SYNCED 技能
```
1. 用户调用 POST /skills (skillType=SYNCED, skillPath=xxx)
2. SkillUseCase.createSynced()
3. 验证 SkillConfig 存在
4. 保存 Skill 元数据到数据库
5. 扫描 skillPath 下的所有文件
6. 上传文件到 MinIO (agenthub/skills/{skillCode}/{path})
7. 保存 SkillFile 元数据到数据库
8. 更新 Skill 文件统计
```

### 9.2 创建 UPLOADED 技能（URL）
```
1. 用户调用 POST /skills/from-url (zipUrl=xxx)
2. SkillUseCase.createFromUrl()
3. 保存 Skill 元数据到数据库
4. 下载 ZIP 到临时目录
5. 上传 ZIP 原包到 MinIO (agenthub/skills/{skillCode}/_package.zip)
6. 解压 ZIP 到临时目录
7. 扫描解压后的文件
8. 上传文件到 MinIO
9. 保存 SkillFile 元数据到数据库
10. 更新 Skill 文件统计
11. 清理临时目录
```

### 9.3 创建 UPLOADED 技能（上传）
```
1. 用户调用 POST /skills/from-upload (multipart file)
2. SkillUseCase.createFromUpload()
3. 保存 Skill 元数据到数据库
4. 上传 ZIP 原包到 MinIO
5. 解压 ZIP 到临时目录
6. 扫描解压后的文件
7. 上传文件到 MinIO
8. 保存 SkillFile 元数据到数据库
9. 更新 Skill 文件统计
10. 清理临时目录
```

### 9.4 同步技能（SYNCED 类型）
```
1. 用户调用 POST /skills/sync-all
2. SkillUseCase.syncAll()
3. 查询所有 SkillConfig
4. 对每个配置，遍历 skillPaths
5. 扫描每个路径下的技能
6. 对每个 SYNCED 技能:
   a. 更新/新增技能元数据到数据库
   b. 扫描本地文件夹中的所有文件
   c. 上传文件到 MinIO (agenthub/skills/{skillCode}/{path})
   d. 删除旧文件元数据，重新保存
   e. 更新统计和同步时间
```

### 9.5 文件同步策略

| 场景 | 策略 |
|------|------|
| 首次创建 | 全量上传所有文件到 MinIO |
| 定时同步 | 基于 checksum 增量更新 |
| 手动同步 | 全量替换（删除旧文件，重新上传） |
| 删除技能 | 同时删除 MinIO 中的文件和数据库记录 |

### 9.6 SYNCED 技能文件同步详情

```
本地文件夹: ~/.agents/skills/test-skill-001/
├── SKILL.md
├── scripts/
│   └── helper.py
└── config.json

                    ↓ 同步过程

MinIO 存储: agenthub/skills/test-skill-001/
├── SKILL.md
├── scripts/
│   └── helper.py
└── config.json

数据库:
├── skill 表: 元数据 (skillCode, name, fileCount=3, totalSize=...)
└── skill_file 表: 文件元数据 (filePath, storagePath, fileSize...)
```

### 9.7 为什么 SYNCED 技能也要同步到 MinIO？

1. **统一访问方式**: 所有技能文件都通过相同的 API 访问，无需区分来源
2. **支持远程访问**: Agent 可以在任何地方访问技能文件，不仅限于本地
3. **备份与恢复**: MinIO 提供可靠的存储，避免本地文件丢失
4. **版本控制**: 可以基于 checksum 追踪文件变化
5. **性能优化**: 减少本地磁盘 I/O，通过 MinIO 缓存加速

---

## 10. 前端设计

### 10.1 技能类型定义更新

**文件**: `src/main/web/src/types/memory.ts`

```typescript
export interface Skill {
  id: string
  tenantId: string
  workspaceId: string
  skillCode: string
  name: string
  description: string
  skillType: 'SYNCED' | 'UPLOADED'           // 技能类型
  source: 'LOCAL' | 'URL' | 'UPLOAD'         // 来源
  sourcePath?: string                         // 本地路径或 URL
  zipStoragePath?: string                     // ZIP 原包路径
  configId?: string                           // 关联配置
  fileCount: number                           // 文件数量
  totalSize: number                           // 文件总大小
  enabled: boolean
  createdAt: string
  updatedAt: string
  lastSyncAt?: string
}

export interface SkillConfig {
  id: string
  tenantId: string
  workspaceId: string
  name: string
  description?: string
  skillPaths: string[]                        // 本地技能路径列表
  syncEnabled: boolean
  syncInterval: number                        // 同步间隔（秒）
  autoSync: boolean
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface SkillFile {
  id: string
  skillId: string
  filePath: string
  fileName: string
  fileExt: string
  fileSize: number
  fileType: string
  encoding?: string
  storagePath: string
  isDirectory: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export interface SkillFileTreeNode {
  name: string
  path: string
  isDirectory: boolean
  children?: SkillFileTreeNode[]
}
```

### 10.2 API 函数更新

**文件**: `src/main/web/src/api/skill-api.ts`

```typescript
import { requestJson } from './http'
import type { Skill, SkillConfig, SkillFile, SkillFileTreeNode } from '@/types/memory'

const baseUrl = runtimeConfig.agentApiBase

// ========== 技能配置 API ==========

export async function listSkillConfigs(selection: { tenantId: string; workspaceId: string }) {
  return requestJson<SkillConfig[]>({
    method: 'GET',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skill-configs?tenantId=${selection.tenantId}`
  })
}

export async function createSkillConfig(selection: { tenantId: string; workspaceId: string }, config: Partial<SkillConfig>) {
  return requestJson<SkillConfig>({
    method: 'POST',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skill-configs`,
    headers: {
      'Content-Type': 'application/json',
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    },
    body: JSON.stringify(config)
  })
}

export async function updateSkillConfig(selection: { tenantId: string; workspaceId: string }, configId: string, config: Partial<SkillConfig>) {
  return requestJson<SkillConfig>({
    method: 'PUT',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skill-configs/${configId}`,
    headers: {
      'Content-Type': 'application/json',
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    },
    body: JSON.stringify(config)
  })
}

export async function deleteSkillConfig(selection: { tenantId: string; workspaceId: string }, configId: string) {
  return requestJson<void>({
    method: 'DELETE',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skill-configs/${configId}`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

// ========== 技能 API ==========

export async function listSkills(selection: { tenantId: string; workspaceId: string }) {
  return requestJson<Skill[]>({
    method: 'GET',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

export async function getSkill(selection: { tenantId: string; workspaceId: string }, skillId: string) {
  return requestJson<Skill>({
    method: 'GET',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

export async function createSkillFromUpload(
  selection: { tenantId: string; workspaceId: string },
  params: { skillCode: string; name: string; description: string },
  file: File
) {
  const formData = new FormData()
  formData.append('file', file)
  
  return requestJson<Skill>({
    method: 'POST',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/from-upload?tenantId=${selection.tenantId}&skillCode=${params.skillCode}&name=${encodeURIComponent(params.name)}&description=${encodeURIComponent(params.description)}`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    },
    body: formData
  })
}

export async function createSkillFromUrl(
  selection: { tenantId: string; workspaceId: string },
  params: { skillCode: string; name: string; description: string; zipUrl: string }
) {
  return requestJson<Skill>({
    method: 'POST',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/from-url`,
    headers: {
      'Content-Type': 'application/json',
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    },
    body: JSON.stringify({
      tenantId: selection.tenantId,
      ...params
    })
  })
}

export async function deleteSkill(selection: { tenantId: string; workspaceId: string }, skillId: string) {
  return requestJson<void>({
    method: 'DELETE',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

export async function enableSkill(selection: { tenantId: string; workspaceId: string }, skillId: string) {
  return requestJson<Skill>({
    method: 'POST',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/enable`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

export async function disableSkill(selection: { tenantId: string; workspaceId: string }, skillId: string) {
  return requestJson<Skill>({
    method: 'POST',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/disable`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

export async function syncAllSkills(selection: { tenantId: string; workspaceId: string }) {
  return requestJson<void>({
    method: 'POST',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/sync-all`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

// ========== 技能文件 API ==========

export async function listSkillFiles(selection: { tenantId: string; workspaceId: string }, skillId: string) {
  return requestJson<SkillFile[]>({
    method: 'GET',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/files`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

export async function getSkillFileContent(selection: { tenantId: string; workspaceId: string }, skillId: string, filePath: string) {
  return requestJson<string>({
    method: 'GET',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/files/${encodeURIComponent(filePath)}/content`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

export async function getSkillFileTree(selection: { tenantId: string; workspaceId: string }, skillId: string) {
  return requestJson<SkillFileTreeNode>({
    method: 'GET',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/files/tree`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}

export async function getSkillFileStats(selection: { tenantId: string; workspaceId: string }, skillId: string) {
  return requestJson<{ fileCount: number; totalSize: number }>({
    method: 'GET',
    url: `${baseUrl}/api/v1/workspaces/${selection.workspaceId}/skills/${skillId}/files/stats`,
    headers: {
      'X-Tenant-Id': selection.tenantId,
      'X-Workspace-Id': selection.workspaceId
    }
  })
}
```

### 10.3 技能管理页面重构

**文件**: `src/main/web/src/views/agenthub/SkillManagementView.vue`

#### 页面布局
```
┌─────────────────────────────────────────────────────────────────┐
│  工具栏: [创建技能] [同步全部] [搜索...]                          │
├─────────────────────────────────────────────────────────────────┤
│  技能列表表格                                                    │
│  ┌──────┬──────┬──────┬──────┬──────┬──────┬──────┐            │
│  │ 编码 │ 名称 │ 类型 │ 来源 │ 文件数│ 大小 │ 操作 │            │
│  ├──────┼──────┼──────┼──────┼──────┼──────┼──────┤            │
│  │ ...  │ ...  │ ...  │ ...  │ ...  │ ...  │ 查看 │            │
│  │      │      │      │      │      │      │ 删除 │            │
│  └──────┴──────┴──────┴──────┴──────┴──────┴──────┘            │
└─────────────────────────────────────────────────────────────────┘
```

#### 创建技能对话框
```
┌─────────────────────────────────────────────────────────────────┐
│  创建技能                                                   [X]│
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  技能编码: [________________]                                    │
│  技能名称: [________________]                                    │
│  描    述: [________________]                                    │
│                                                                 │
│  ─────────────────────────────────────────────────────────────  │
│                                                                 │
│  创建方式:                                                      │
│  ○ 上传 ZIP 包                                                  │
│    [拖拽文件到此处或点击上传]                                      │
│    支持 .zip 格式                                                 │
│                                                                 │
│  ○ 远程 ZIP 链接                                                │
│    [________________] 请输入 ZIP 文件 URL                        │
│                                                                 │
│  ─────────────────────────────────────────────────────────────  │
│                                                                 │
│                              [取消]  [创建]                      │
└─────────────────────────────────────────────────────────────────┘
```

#### 技能详情对话框
```
┌─────────────────────────────────────────────────────────────────┐
│  技能详情: test-skill-001                                   [X]│
├─────────────────────────────────────────────────────────────────┤
│  基本信息                                                       │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ 编码: test-skill-001    名称: Test Skill                  │ │
│  │ 类型: SYNCED            来源: LOCAL                       │ │
│  │ 文件数: 5              大小: 12.5 KB                      │ │
│  │ 创建时间: 2026-06-02    更新时间: 2026-06-02              │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  文件内容                                                       │
│  ┌──────────────────┬───────────────────────────────────────┐ │
│  │  📁 目录树        │  📄 文件内容                           │ │
│  │                   │                                       │ │
│  │  ▼ 📁 scripts     │  # Test Skill                        │ │
│  │    📄 helper.py   │                                       │ │
│  │    📄 utils.py    │  ## Description                      │ │
│  │  📄 SKILL.md     │                                       │ │
│  │  📄 config.json  │  This is a test skill...              │ │
│  │                   │                                       │ │
│  └──────────────────┴───────────────────────────────────────┘ │
│                                                                 │
│                              [关闭]                             │
└─────────────────────────────────────────────────────────────────┘
```

### 10.4 新增组件

#### SkillDetailModal.vue

```vue
<template>
  <ModalDialog
    v-model:visible="visible"
    :title="`技能详情: ${skill?.skillCode}`"
    size="xlarge"
    :show-footer="false"
    @close="handleClose"
  >
    <div class="skill-detail">
      <!-- 基本信息 -->
      <div class="skill-info">
        <div class="info-row">
          <span class="label">编码:</span>
          <span class="value">{{ skill?.skillCode }}</span>
        </div>
        <div class="info-row">
          <span class="label">名称:</span>
          <span class="value">{{ skill?.name }}</span>
        </div>
        <div class="info-row">
          <span class="label">类型:</span>
          <span class="value">{{ skill?.skillType === 'SYNCED' ? '同步技能' : '上传技能' }}</span>
        </div>
        <div class="info-row">
          <span class="label">来源:</span>
          <span class="value">{{ getSourceLabel(skill?.source) }}</span>
        </div>
        <div class="info-row">
          <span class="label">文件数:</span>
          <span class="value">{{ skill?.fileCount }}</span>
        </div>
        <div class="info-row">
          <span class="label">大小:</span>
          <span class="value">{{ formatSize(skill?.totalSize) }}</span>
        </div>
      </div>

      <!-- 文件浏览器 -->
      <div class="file-browser">
        <!-- 左侧目录树 -->
        <div class="file-tree">
          <div class="tree-header">文件目录</div>
          <div class="tree-content">
            <FileTreeNode
              v-for="node in fileTree?.children"
              :key="node.path"
              :node="node"
              :selected-path="selectedFilePath"
              @select="handleFileSelect"
            />
          </div>
        </div>

        <!-- 右侧文件内容 -->
        <div class="file-content">
          <div class="content-header">
            <span v-if="selectedFilePath">{{ selectedFilePath }}</span>
            <span v-else class="placeholder">请选择文件</span>
          </div>
          <div class="content-body">
            <pre v-if="fileContent" class="code-block">{{ fileContent }}</pre>
            <div v-else class="empty-state">选择左侧文件查看内容</div>
          </div>
        </div>
      </div>
    </div>
  </ModalDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import type { Skill, SkillFileTreeNode } from '@/types/memory'
import { getSkillFileTree, getSkillFileContent } from '@/api/skill-api'
import { useWorkspaceStore } from '@/store/workspace-store'

const props = defineProps<{
  visible: boolean
  skill: Skill | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const workspaceStore = useWorkspaceStore()

const fileTree = ref<SkillFileTreeNode | null>(null)
const selectedFilePath = ref<string>('')
const fileContent = ref<string>('')

watch(() => props.skill, async (newSkill) => {
  if (newSkill && props.visible) {
    await loadFileTree(newSkill.id)
  }
})

watch(() => props.visible, async (isVisible) => {
  if (isVisible && props.skill) {
    await loadFileTree(props.skill.id)
  } else {
    fileTree.value = null
    selectedFilePath.value = ''
    fileContent.value = ''
  }
})

async function loadFileTree(skillId: string) {
  try {
    fileTree.value = await getSkillFileTree(workspaceStore.currentSelection, skillId)
  } catch (error) {
    console.error('Failed to load file tree:', error)
  }
}

async function handleFileSelect(filePath: string) {
  if (!props.skill) return
  
  selectedFilePath.value = filePath
  try {
    fileContent.value = await getSkillFileContent(
      workspaceStore.currentSelection,
      props.skill.id,
      filePath
    )
  } catch (error) {
    console.error('Failed to load file content:', error)
    fileContent.value = ''
  }
}

function getSourceLabel(source?: string) {
  switch (source) {
    case 'LOCAL': return '本地文件夹'
    case 'URL': return '远程链接'
    case 'UPLOAD': return '上传 ZIP'
    default: return '-'
  }
}

function formatSize(bytes?: number) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

function handleClose() {
  emit('update:visible', false)
}
</script>

<style scoped>
.skill-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skill-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.info-row {
  display: flex;
  gap: 8px;
}

.info-row .label {
  color: var(--text-secondary);
}

.info-row .value {
  color: var(--text-primary);
  font-weight: 500;
}

.file-browser {
  display: flex;
  height: 400px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.file-tree {
  width: 240px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.tree-header {
  padding: 8px 12px;
  background: var(--bg-secondary);
  font-weight: 500;
  border-bottom: 1px solid var(--border-color);
}

.tree-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.file-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-header {
  padding: 8px 12px;
  background: var(--bg-secondary);
  font-family: monospace;
  font-size: 12px;
  border-bottom: 1px solid var(--border-color);
}

.content-header .placeholder {
  color: var(--text-secondary);
}

.content-body {
  flex: 1;
  overflow: auto;
  padding: 12px;
}

.code-block {
  margin: 0;
  font-family: monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-secondary);
}
</style>
```

#### FileTreeNode.vue

```vue
<template>
  <div class="tree-node">
    <div
      class="node-item"
      :class="{ selected: node.path === selectedPath, directory: node.isDirectory }"
      @click="handleClick"
    >
      <span class="node-icon">
        {{ node.isDirectory ? (isExpanded ? '📂' : '📁') : getFileIcon(node.name) }}
      </span>
      <span class="node-name">{{ node.name }}</span>
    </div>
    <div v-if="node.isDirectory && isExpanded" class="node-children">
      <FileTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :selected-path="selectedPath"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { SkillFileTreeNode } from '@/types/memory'

const props = defineProps<{
  node: SkillFileTreeNode
  selectedPath?: string
}>()

const emit = defineEmits<{
  (e: 'select', path: string): void
}>()

const isExpanded = ref(props.node.isDirectory)

function handleClick() {
  if (props.node.isDirectory) {
    isExpanded.value = !isExpanded.value
  } else {
    emit('select', props.node.path)
  }
}

function getFileIcon(fileName: string) {
  const ext = fileName.split('.').pop()?.toLowerCase()
  switch (ext) {
    case 'md': return '📝'
    case 'json': return '📋'
    case 'py': return '🐍'
    case 'js': case 'ts': return '📜'
    case 'jpg': case 'jpeg': case 'png': case 'gif': return '🖼️'
    default: return '📄'
  }
}
</script>

<style scoped>
.tree-node {
  user-select: none;
}

.node-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
}

.node-item:hover {
  background: var(--bg-hover);
}

.node-item.selected {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.node-item.directory {
  font-weight: 500;
}

.node-icon {
  font-size: 14px;
}

.node-children {
  padding-left: 16px;
}
</style>
```

### 10.5 修改现有组件

#### SkillManagementView.vue 主要变更

1. **表格列更新**:
   - 添加"类型"列（SYNCED/UPLOADED）
   - 添加"来源"列（LOCAL/URL/UPLOAD）
   - 添加"文件数"和"大小"列
   - 操作列添加"查看"按钮

2. **创建对话框重构**:
   - 移除旧的表单字段
   - 添加 ZIP 上传/URL 两种创建方式
   - 使用 Radio 切换创建方式
   - ZIP 上传支持拖拽

3. **新增详情对话框**:
   - 点击"查看"打开 SkillDetailModal
   - 左侧显示文件目录树
   - 右侧显示文件内容

### 10.6 交互流程

#### 创建技能流程
```
1. 用户点击"创建技能"按钮
2. 弹出创建对话框
3. 用户填写编码、名称、描述
4. 用户选择创建方式:
   - 上传 ZIP: 拖拽或选择文件
   - 远程链接: 输入 URL
5. 用户点击"创建"
6. 前端调用 API:
   - 上传 ZIP: POST /skills/from-upload (multipart)
   - 远程链接: POST /skills/from-url (JSON)
7. 后端处理 ZIP 并返回技能信息
8. 前端刷新列表
```

#### 查看技能详情流程
```
1. 用户点击"查看"按钮
2. 弹出详情对话框
3. 加载技能基本信息
4. 加载文件目录树
5. 用户点击文件节点
6. 加载文件内容到右侧显示
7. 支持浏览多个文件
```

---

## 11. 文件清单

### 后端新增文件
1. `domain/model/skill/SkillConfig.java` - 技能配置领域模型
2. `domain/model/skill/SkillFile.java` - 技能文件领域模型
3. `application/port/out/repositories/SkillConfigRepository.java` - 配置仓储接口
4. `application/port/out/repositories/SkillFileRepository.java` - 文件仓储接口
5. `application/usecase/SkillConfigUseCase.java` - 配置用例
6. `application/usecase/SkillFileUseCase.java` - 文件用例
7. `infrastructure/store/db/entity/SkillConfigEntity.java` - 配置实体
8. `infrastructure/store/db/entity/SkillFileEntity.java` - 文件实体
9. `infrastructure/store/db/mapper/SkillConfigMybatisMapper.java` - 配置 Mapper
10. `infrastructure/store/db/mapper/SkillFileMybatisMapper.java` - 文件 Mapper
11. `infrastructure/store/db/repository/MybatisSkillConfigRepository.java` - 配置仓储实现
12. `infrastructure/store/db/repository/MybatisSkillFileRepository.java` - 文件仓储实现
13. `api/controller/SkillConfigController.java` - 配置控制器
14. `api/controller/SkillFileController.java` - 文件控制器
15. `api/dto/CreateSkillFromUrlRequest.java` - 从 URL 创建请求

### 后端修改文件
1. `domain/model/skill/Skill.java` - 添加新字段和工厂方法
2. `infrastructure/store/db/entity/SkillEntity.java` - 添加新字段
3. `application/usecase/SkillUseCase.java` - 添加 ZIP 处理逻辑
4. `api/controller/SkillController.java` - 添加新端点
5. `api/dto/SkillResponse.java` - 添加新字段
6. `sql/schema.sql` - 添加新表和字段

### 前端新增文件
1. `src/main/web/src/views/agenthub/components/SkillDetailModal.vue` - 技能详情对话框
2. `src/main/web/src/views/agenthub/components/FileTreeNode.vue` - 文件树节点组件

### 前端修改文件
1. `src/main/web/src/types/memory.ts` - 更新 Skill 类型定义，添加 SkillConfig、SkillFile 类型
2. `src/main/web/src/api/skill-api.ts` - 更新 API 函数，添加配置和文件 API
3. `src/main/web/src/views/agenthub/SkillManagementView.vue` - 重构页面，更新创建流程和详情查看
