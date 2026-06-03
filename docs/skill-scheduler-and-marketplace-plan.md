# 技能定时同步 & 多市场技能发现安装 — 设计方案

## 一、现状分析

### 已有能力
- `SkillConfig` 领域模型已有 `autoSync`、`syncInterval`、`syncEnabled` 字段
- `SkillUseCase.sync(skillPath)` / `syncWithConfig(configId)` 已实现本地路径扫描 + MinIO 上传
- `ScheduledTaskScheduler` 基于 `ThreadPoolTaskScheduler` + `ScheduledFuture` 实现了 cron 定时任务（用于 Agent prompt 执行）
- `SkillFileManager` 已实现 ZIP 下载解压 (`downloadAndExtract`)、目录扫描、SKILL.md 解析
- `SkillController` 已支持三种创建方式：本地路径 / URL下载 / 文件上传
- `SkillTools` 已有 7 个 @Tool 方法供 Agent 调用

### 缺失部分
1. **定时同步**：`SkillConfig.autoSync=true` 时，没有后台调度器定期触发 `syncWithConfig`
2. **技能市场**：没有第三方市场抽象层，不支持搜索/浏览/安装市场技能
3. **SkillTools 不完善**：文件读取依赖本地路径 `skill.getSkillPath()`，但技能文件已存 MinIO；搜索用内存过滤而非 DB 查询

---

## 二、功能一：技能配置定时同步

### 2.1 设计思路

不复用 `ScheduledTaskScheduler`（那是给 Agent prompt 执行用的），而是为技能同步创建独立的调度器。原因：
- 职责不同：Agent 任务是发 prompt 给 LLM，技能同步是文件系统扫描
- 生命周期不同：技能同步跟随 `SkillConfig` CRUD 动态增减，Agent 任务跟随 ScheduledTask
- 避免耦合：技能调度器不需要 Agent/Chat 基础设施

### 2.2 架构

```
SkillConfig (autoSync=true, syncInterval=3600)
        │
        ▼
SkillSyncScheduler ──── @PostConstruct: 从 DB 加载所有 autoSync 配置
        │                 注册到 ThreadPoolTaskScheduler
        │
        ├── 配置启用时 → schedule(config) → 计算 nextRun = now + interval
        ├── 配置禁用时 → unschedule(configId)
        ├── 配置更新时 → reschedule(config)
        └── 定时触发   → SkillUseCase.syncWithConfig(configId)
                       → 更新 SkillConfig.lastSyncAt
```

### 2.3 新增文件

| 层 | 文件 | 说明 |
|---|---|---|
| Infrastructure | `SkillSyncScheduler.java` | 技能同步调度器，管理定时任务生命周期 |

### 2.4 修改文件

| 文件 | 改动 |
|---|---|
| `SkillConfigRepository.java` | 新增 `List<SkillConfig> findAllEnabledAutoSync()` |
| `MybatisSkillConfigRepository.java` | 实现该方法 |
| `SkillConfigUseCase.java` | `create`/`update`/`enable`/`disable` 后通知调度器 reschedule |
| `SkillUseCase.java` | `syncWithConfig` 执行后更新 `lastSyncAt` |
| `SkillConfig.java` | 新增 `lastSyncAt` 字段 |
| `SkillConfigEntity.java` | 新增 `lastSyncAt` 列 |
| `schema.sql` | skill_config 表新增 `last_sync_at` 列 |

### 2.5 核心实现：SkillSyncScheduler

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillSyncScheduler {

    private final SkillConfigRepository skillConfigRepository;
    private final SkillUseCase skillUseCase;
    private ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.setThreadNamePrefix("skill-sync-");
        taskScheduler.initialize();
        loadAndScheduleAll();
    }

    /** 启动时加载所有 autoSync=true 的配置并注册定时任务 */
    private void loadAndScheduleAll() {
        skillConfigRepository.findAllEnabledAutoSync()
            .forEach(this::schedule);
    }

    /** 注册定时任务：fixedRate = syncInterval * 1000 */
    public void schedule(SkillConfig config) {
        unschedule(config.getId());
        if (!config.isAutoSync() || !config.isSyncEnabled()) return;
        ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(
            () -> executeSync(config.getId()),
            Duration.ofSeconds(config.getSyncInterval())
        );
        futures.put(config.getId(), future);
        log.info("Scheduled skill sync: {} every {}s", config.getId(), config.getSyncInterval());
    }

    public void unschedule(String configId) {
        ScheduledFuture<?> future = futures.remove(configId);
        if (future != null) future.cancel(false);
    }

    public void reschedule(SkillConfig config) {
        schedule(config);
    }

    private void executeSync(String configId) {
        try {
            skillUseCase.syncWithConfig(configId);
            log.info("Skill sync completed: {}", configId);
        } catch (Exception e) {
            log.error("Skill sync failed: {}", configId, e);
        }
    }

    @PreDestroy
    void shutdown() {
        futures.values().forEach(f -> f.cancel(false));
        if (taskScheduler != null) taskScheduler.shutdown();
    }
}
```

### 2.6 SkillConfigRepository 新增方法

```java
List<SkillConfig> findAllEnabledAutoSync();
```

实现：
```java
@Override
public List<SkillConfig> findAllEnabledAutoSync() {
    LambdaQueryWrapper<SkillConfigEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SkillConfigEntity::getAutoSync, true)
           .eq(SkillConfigEntity::getSyncEnabled, true)
           .eq(SkillConfigEntity::getEnabled, true);
    return mapper.selectList(wrapper).stream().map(this::toDomain).toList();
}
```

### 2.7 SkillConfigUseCase 与调度器集成

`SkillConfigUseCase` 注入 `SkillSyncScheduler`，在配置变更后通知调度器：

```java
private final SkillSyncScheduler skillSyncScheduler;

public SkillOutput create(CreateSkillConfigCommand command) {
    // ... 创建逻辑 ...
    skillSyncScheduler.schedule(savedConfig);
    return toOutput(saved);
}

public SkillOutput update(String configId, ...) {
    // ... 更新逻辑 ...
    skillSyncScheduler.reschedule(updatedConfig);
    return toOutput(updated);
}

public void delete(String configId) {
    skillSyncScheduler.unschedule(configId);
    // ... 删除逻辑 ...
}
```

### 2.8 SkillConfig 领域模型新增 lastSyncAt

```java
private Instant lastSyncAt;

public void markSynced() {
    this.lastSyncAt = Instant.now();
}
```

---

## 三、功能二：多市场技能发现与安装

### 3.1 核心思路

**市场只负责搜索和提供下载 URL，安装复用现有 `SkillUseCase.createFromUrl()`。**

```
用户搜索 → 市场适配器返回技能列表（含 downloadUrl）
用户点击安装 → 后端拿到 downloadUrl → 调用 createFromUrl → 完成
```

这样市场适配器只需要实现 `search` 和 `getDetail`，不需要处理 ZIP 流。安装链路完全复用已有的 `downloadZip → extractAndProcess → saveZipToMinio` 流程。

### 3.2 架构

```
前端                       后端
┌──────────┐              ┌──────────────────────────────────┐
│ 市场搜索  │──GET──▶     │ SkillMarketController            │
│ (选市场)  │              │   GET /{marketId}/search         │
│          │              │   GET /{marketId}/skills/{id}    │
│ 点击安装  │──POST──▶    │   POST /{marketId}/install       │
└──────────┘              │        │                         │
                          │        ▼                         │
                          │   SkillMarketUseCase             │
                          │     install(marketId, skillId)   │
                          │        │                         │
                          │        ▼                         │
                          │   SkillMarketPort.getDetail()    │
                          │     → 返回 downloadUrl           │
                          │        │                         │
                          │        ▼                         │
                          │   SkillUseCase.createFromUrl()   │
                          │     → 下载ZIP → 解压 → 入库      │
                          └──────────────────────────────────┘
```

### 3.3 核心接口：SkillMarketPort

```java
/**
 * 技能市场端口，定义市场搜索能力。
 * 安装逻辑复用 SkillUseCase.createFromUrl，市场只需提供下载地址。
 */
public interface SkillMarketPort {

    /** 市场唯一标识 */
    String getMarketId();

    /** 市场显示名称 */
    String getMarketName();

    /** 是否可用（API key、网络等） */
    boolean isAvailable();

    /** 搜索技能 */
    List<MarketSkillSummary> search(MarketSearchQuery query);

    /** 获取技能详情（包含 downloadUrl） */
    MarketSkillDetail getDetail(String skillId);
}
```

### 3.4 领域模型

#### MarketSearchQuery — 搜索条件

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSearchQuery {
    private String keyword;
    private String category;    // 可选：tools / agents / retrievals
    private String sortBy;      // relevance / stars / updated
    private int page = 1;
    private int pageSize = 20;
}
```

#### MarketSkillSummary — 搜索结果摘要

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSkillSummary {
    private String marketId;       // 来源市场
    private String skillId;        // 市场内技能 ID
    private String skillCode;      // 技能编码
    private String name;
    private String description;
    private String author;
    private String version;
    private int downloadCount;
    private int starCount;
    private String thumbnailUrl;
    private Instant updatedAt;
}
```

#### MarketSkillDetail — 技能详情

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSkillDetail {
    private String marketId;
    private String skillId;
    private String skillCode;
    private String name;
    private String description;
    private String author;
    private String version;
    private String license;
    private String homepage;
    private String downloadUrl;     // ZIP 下载地址 → 传给 createFromUrl
    private List<String> tags;
    private int downloadCount;
    private int starCount;
    private Instant updatedAt;
    private String readmeContent;   // SKILL.md 内容预览
}
```

### 3.5 市场适配器实现

#### 3.5.1 ClawHubAdapter

```java
@Component
@ConditionalOnProperty(name = "agenthub.skill-market.clawhub.enabled", havingValue = "true")
public class ClawHubAdapter implements SkillMarketPort {

    private final WebClient webClient;

    @Override
    public String getMarketId() { return "clawhub"; }

    @Override
    public List<MarketSkillSummary> search(MarketSearchQuery query) {
        // GET https://clawhub.com/api/skills/search?q={keyword}&page={page}
        // 解析 JSON 响应
    }

    @Override
    public MarketSkillDetail getDetail(String skillId) {
        // GET https://clawhub.com/api/skills/{skillId}
        // downloadUrl = https://clawhub.com/api/skills/{skillId}/download
    }
}
```

#### 3.5.2 GitHubAdapter

```java
@Component
@ConditionalOnProperty(name = "agenthub.skill-market.github.enabled", havingValue = "true")
public class GitHubAdapter implements SkillMarketPort {

    private final WebClient webClient;

    @Override
    public String getMarketId() { return "github"; }

    @Override
    public List<MarketSkillSummary> search(MarketSearchQuery query) {
        // GET https://api.github.com/search/repositories?q={keyword}+topic:agent-skill
        // 每个 repo 视为一个技能
    }

    @Override
    public MarketSkillDetail getDetail(String skillId) {
        // GET https://api.github.com/repos/{owner}/{repo}
        // downloadUrl = https://api.github.com/repos/{owner}/{repo}/zipball/{default_branch}
    }
}
```

#### 3.5.3 OpenCLIHubAdapter

```java
@Component
@ConditionalOnProperty(name = "agenthub.skill-market.opencli.enabled", havingValue = "true")
public class OpenCLIHubAdapter implements SkillMarketPort {
    // 类似实现
}
```

### 3.6 UseCase 层

#### SkillMarketUseCase

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillMarketUseCase {

    private final List<SkillMarketPort> marketPorts;
    private final SkillUseCase skillUseCase;

    /** 搜索指定市场 */
    public List<MarketSkillSummary> search(String marketId, MarketSearchQuery query) {
        SkillMarketPort port = findPort(marketId);
        return port.search(query);
    }

    /** 获取技能详情 */
    public MarketSkillDetail getDetail(String marketId, String skillId) {
        SkillMarketPort port = findPort(marketId);
        return port.getDetail(skillId);
    }

    /** 从市场安装技能 — 复用 createFromUrl */
    @Transactional
    public SkillOutput install(String marketId, String skillId,
                                String tenantId, String workspaceId) {
        SkillMarketPort port = findPort(marketId);
        MarketSkillDetail detail = port.getDetail(skillId);

        CreateSkillCommand command = new CreateSkillCommand();
        command.setTenantId(tenantId);
        command.setWorkspaceId(workspaceId);
        command.setSkillCode(detail.getSkillCode());
        command.setName(detail.getName());
        command.setDescription(detail.getDescription());
        command.setSkillType("UPLOADED");
        command.setSource(marketId.toUpperCase());
        command.setSourcePath(detail.getDownloadUrl());
        command.setZipUrl(detail.getDownloadUrl());

        // 复用已有的 createFromUrl：下载ZIP → 解压 → 扫描 → 入MinIO
        return skillUseCase.createFromUrl(command);
    }

    /** 列出所有可用市场 */
    public List<MarketInfo> listMarkets() {
        return marketPorts.stream()
            .filter(SkillMarketPort::isAvailable)
            .map(port -> new MarketInfo(port.getMarketId(), port.getMarketName()))
            .toList();
    }

    private SkillMarketPort findPort(String marketId) {
        return marketPorts.stream()
            .filter(p -> p.getMarketId().equals(marketId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Market not found: " + marketId));
    }
}
```

### 3.7 API 层

#### SkillMarketController

```java
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skill-markets")
@RequiredArgsConstructor
public class SkillMarketController {

    private final SkillMarketUseCase useCase;

    /** 列出所有可用市场 */
    @GetMapping
    public List<MarketInfoResponse> listMarkets(@PathVariable String workspaceId) {
        return useCase.listMarkets().stream().map(this::toResponse).toList();
    }

    /** 搜索市场技能 */
    @GetMapping("/{marketId}/search")
    public List<MarketSkillSummaryResponse> search(
            @PathVariable String workspaceId,
            @PathVariable String marketId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        MarketSearchQuery query = new MarketSearchQuery(keyword, null, "relevance", page, pageSize);
        return useCase.search(marketId, query).stream().map(this::toSummaryResponse).toList();
    }

    /** 获取技能详情 */
    @GetMapping("/{marketId}/skills/{skillId}")
    public MarketSkillDetailResponse getDetail(
            @PathVariable String workspaceId,
            @PathVariable String marketId,
            @PathVariable String skillId) {
        return toDetailResponse(useCase.getDetail(marketId, skillId));
    }

    /** 安装市场技能 — 复用 createFromUrl */
    @PostMapping("/{marketId}/install")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse install(
            @PathVariable String workspaceId,
            @PathVariable String marketId,
            @RequestBody InstallMarketSkillRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        SkillOutput result = useCase.install(marketId, request.getSkillId(), tenantId, workspaceId);
        return toSkillResponse(result);
    }
}
```

#### DTO 文件

| 文件 | 说明 |
|---|---|
| `MarketInfoResponse.java` | 市场信息（marketId, marketName） |
| `MarketSkillSummaryResponse.java` | 搜索结果摘要 |
| `MarketSkillDetailResponse.java` | 技能详情（含 downloadUrl） |
| `InstallMarketSkillRequest.java` | 安装请求（skillId） |

### 3.8 前端设计

#### 3.8.1 浮动按钮：技能市场入口

在同步按钮**上方**新增「技能市场」浮动按钮，位置计算：

```
Settings (24px)
  ↑
Effect/Theme (80px)
  ↑
SkillConfig (136px) — 条件显示
  ↑
Home (192px) — 条件显示
  ↑
Market (248px) — 条件显示（skill 页面时显示） ← 新增
  ↑
Sync (304px) — 条件显示
  ↑
Add (360px) — 条件显示
```

新增文件：`FloatingMarketButton.vue`

```vue
<template>
  <div class="floating-market-button-container" :style="{ bottom: `${bottom}px` }">
    <button class="floating-market-btn" @click="showDialog = true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"/>
        <path d="M3 6h18"/>
        <path d="M16 10a4 4 0 0 1-8 0"/>
      </svg>
    </button>
  </div>
  <SkillMarketModal v-model:visible="showDialog" @close="showDialog = false" />
</template>
```

图标用购物袋（marketplace），与同步按钮的循环箭头图标区分。

修改 `AgentHubLayout.vue`：
- 新增 `showMarketButton` computed：`route.path.includes('skill')`
- 新增 `positions.market`，在 `sync` 之前计算
- 模板中添加 `<FloatingMarketButton v-if="showMarketButton" :bottom="buttonPositions.market" />`

#### 3.8.2 技能市场弹窗：SkillMarketModal

新建 `SkillMarketModal.vue`，从浮动按钮打开：

```
┌─────────────────────────────────────────────────────────┐
│  技能市场                                        [✕]    │
├─────────────────────────────────────────────────────────┤
│  市场: [ClawHub ▾] [GitHub] [OpenCLI Hub]              │
├─────────────────────────────────────────────────────────┤
│  🔍 搜索技能名称、作者、标签...                          │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐              │
│  │ 🧩 weather      │  │ 🧩 git-helper   │              │
│  │ 天气查询助手     │  │ Git 辅助工具     │              │
│  │ 作者: xxx       │  │ 作者: yyy       │              │
│  │ ⭐ 128  📥 1.2k │  │ ⭐ 256  📥 2.5k │              │
│  │ v1.2.0         │  │ v2.0.1         │              │
│  │     [安装]     │  │     [安装]     │              │
│  └─────────────────┘  └─────────────────┘              │
│                                                         │
│  加载中...                                              │
└─────────────────────────────────────────────────────────┘
```

点击「安装」→ 调用 `POST /{marketId}/install` → 后端复用 `createFromUrl` → 安装完成刷新列表。

#### 3.8.3 新增 API 函数

```typescript
// skill-api.ts

export async function listSkillMarkets(selection: Selection): Promise<MarketInfo[]> {
  return requestJson<MarketInfo[]>(`/api/v1/workspaces/${selection.workspaceId}/skill-markets`, {
    baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: buildHeaders(selection),
  })
}

export async function searchMarketSkills(
  selection: Selection, marketId: string, keyword: string, page?: number
): Promise<MarketSkillSummary[]> {
  return requestJson<MarketSkillSummary[]>(
    `/api/v1/workspaces/${selection.workspaceId}/skill-markets/${marketId}/search`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: buildHeaders(selection), query: { keyword, page: page || 1 } }
  )
}

export async function getMarketSkillDetail(
  selection: Selection, marketId: string, skillId: string
): Promise<MarketSkillDetail> {
  return requestJson<MarketSkillDetail>(
    `/api/v1/workspaces/${selection.workspaceId}/skill-markets/${marketId}/skills/${skillId}`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'GET', headers: buildHeaders(selection) }
  )
}

export async function installMarketSkill(
  selection: Selection, marketId: string, skillId: string
): Promise<Skill> {
  return requestJson<Skill>(
    `/api/v1/workspaces/${selection.workspaceId}/skill-markets/${marketId}/install`,
    { baseUrl: runtimeConfig.agentApiBase, method: 'POST', headers: buildHeaders(selection), bodyJson: { skillId } }
  )
}
```

#### 3.8.4 新增类型

```typescript
// memory.ts

export interface MarketInfo {
  marketId: string
  marketName: string
}

export interface MarketSkillSummary {
  marketId: string
  skillId: string
  skillCode: string
  name: string
  description: string
  author: string
  version: string
  downloadCount: number
  starCount: number
  thumbnailUrl: string
  updatedAt: string
}

export interface MarketSkillDetail extends MarketSkillSummary {
  license: string
  homepage: string
  downloadUrl: string
  tags: string[]
  readmeContent: string
}
```

### 3.9 配置项

```yaml
agenthub:
  skill-market:
    clawhub:
      enabled: true
      base-url: https://clawhub.com/api
    github:
      enabled: true
      token: ${GITHUB_TOKEN:}    # 可选，提升 rate limit
    opencli:
      enabled: false
      base-url: https://opencli.com/api
```

---

## 四、功能三：SkillTools 完善

### 4.1 问题分析

当前 `SkillTools` 存在的问题：

| 方法 | 问题 | 修复方案 |
|---|---|---|
| `readSkillFile` | 从本地 `skill.getSkillPath()` 读文件，但文件已存 MinIO | 改用 `DocumentFileStoragePort.retrieve()` 读取 |
| `readSkillDocumentation` | 同上 | 同上 |
| `listSkillFiles` | 从本地目录遍历，但文件已存 MinIO + DB | 改用 `SkillFileRepository.findBySkillId()` 查询 |
| `searchSkills` | 内存过滤 `matchesKeyword`，效率低 | 改用 `SkillRepository.search()` 数据库查询 |
| `getSkillDetail` | 缺少文件统计信息 | 新增 `fileCount`、`totalSize` 字段 |
| `getSkills` | 缺少文件统计和同步状态 | 补充 `fileCount`、`totalSize`、`lastSyncAt` |

### 4.2 修改文件

| 文件 | 改动 |
|---|---|
| `SkillTools.java` | 注入 `DocumentFileStoragePort` + `SkillFileRepository`，改造文件读取和搜索逻辑 |
| `AgentSkillDTO.java` | 新增 `fileCount`、`totalSize` 字段 |
| `SkillDetailDTO.java` | 新增 `fileCount`、`totalSize`、`lastSyncAt`、`files`（文件列表）字段 |

### 4.3 改造后的 SkillTools

```java
@RequiredArgsConstructor
@AgentTools(name = "SkillTools", description = "技能数据工具，提供技能信息查询、详情获取和文件读取功能")
public class SkillTools {

    private final SkillRepository skillRepository;
    private final SkillFileRepository skillFileRepository;
    private final DocumentFileStoragePort documentFileStoragePort;
    private final SkillRunner skillRunner;

    @Tool(description = "获取当前工作空间下Agent可用的技能列表")
    public List<AgentSkillDTO> getSkills(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return skillRepository.findByWorkspaceId(ctx.getWorkspace().getWorkspace().getId())
                .stream().map(this::toBasicDto).collect(Collectors.toList());
    }

    @Tool(description = "获取技能详情，包含文件统计和文件树结构")
    public SkillDetailDTO getSkillDetail(
            @ToolParam(description = "技能ID") String skillId) {
        Skill skill = findSkill(skillId);
        return toDetailDto(skill);
    }

    @Tool(description = "读取技能的SKILL.md文件内容，了解技能的使用说明")
    public String readSkillDocumentation(
            @ToolParam(description = "技能ID") String skillId) {
        Skill skill = findSkill(skillId);
        return readSkillFileFromMinio(skill, "SKILL.md");
    }

    @Tool(description = "读取技能中的指定文件（从MinIO存储读取）")
    public String readSkillFile(
            @ToolParam(description = "技能ID") String skillId,
            @ToolParam(description = "文件路径（相对于技能目录）") String filePath) {
        Skill skill = findSkill(skillId);
        return readSkillFileFromMinio(skill, filePath);
    }

    @Tool(description = "列出技能下的所有文件（从数据库查询）")
    public String listSkillFiles(
            @ToolParam(description = "技能ID") String skillId) {
        Skill skill = findSkill(skillId);
        List<SkillFile> files = skillFileRepository.findBySkillId(skillId);
        if (files.isEmpty()) return "技能没有关联的文件";
        StringBuilder sb = new StringBuilder();
        files.forEach(f -> sb.append(f.getFilePath()).append(" (").append(f.getFileSize()).append(" bytes)\n"));
        return sb.toString();
    }

    @Tool(description = "搜索技能名称或描述中包含关键词的技能")
    public List<AgentSkillDTO> searchSkills(
            @ToolParam(description = "搜索关键词") String keyword,
            ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return skillRepository.search(keyword,
                    ctx.getTenant().getTenant().getId(),
                    ctx.getWorkspace().getWorkspace().getId())
                .stream().map(this::toBasicDto).collect(Collectors.toList());
    }

    @Tool(description = "执行技能，解析SKILL.md中的步骤并自动调用工具")
    public SkillExecutionResult executeSkill(
            @ToolParam(description = "技能ID") String skillId,
            @ToolParam(description = "执行参数（JSON格式）") String parameters) {
        Skill skill = findSkill(skillId);
        return skillRunner.run(skill, parameters);
    }

    /** 从 MinIO 读取技能文件内容 */
    private String readSkillFileFromMinio(Skill skill, String filePath) {
        String storagePath = String.format("skills/%s/%s", skill.getSkillCode(), filePath);
        try (InputStream is = documentFileStoragePort.retrieve(storagePath)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "读取文件失败: " + e.getMessage();
        }
    }

    // ... findSkill, toBasicDto, toDetailDto 等辅助方法
}
```

### 4.4 DTO 改造

#### AgentSkillDTO 新增字段

```java
@Data
public class AgentSkillDTO {
    private String id;
    private String name;
    private String description;
    private String skillType;
    private boolean enabled;
    private int fileCount;      // 新增
    private long totalSize;     // 新增
}
```

#### SkillDetailDTO 新增字段

```java
@Data
public class SkillDetailDTO {
    private String id;
    private String name;
    private String description;
    private String skillType;
    private String skillPath;
    private String skillFilesTree;
    private boolean enabled;
    private int fileCount;          // 新增
    private long totalSize;         // 新增
    private Instant lastSyncAt;     // 新增
    private List<SkillFileInfo> files;  // 新增：文件列表
}

@Data
@AllArgsConstructor
public class SkillFileInfo {
    private String filePath;
    private long fileSize;
    private String fileType;
}
```

---

## 五、实施计划

### Phase 1：技能定时同步（预计 2-3 天）

| 步骤 | 内容 | 文件 |
|---|---|---|
| 1 | SkillConfig 新增 `lastSyncAt` 字段 | `SkillConfig.java`, `SkillConfigEntity.java`, `schema.sql` |
| 2 | SkillConfigRepository 新增 `findAllEnabledAutoSync` | `SkillConfigRepository.java`, `MybatisSkillConfigRepository.java` |
| 3 | 创建 `SkillSyncScheduler` | `SkillSyncScheduler.java` (新建) |
| 4 | SkillConfigUseCase 集成调度器 | `SkillConfigUseCase.java` |
| 5 | SkillUseCase.syncWithConfig 执行后更新 lastSyncAt | `SkillUseCase.java` |
| 6 | 集成测试 | `SkillConfigControllerIntegrationTest.java` |
| 7 | 前端显示 lastSyncAt | `SkillConfigModal.vue` |

### Phase 2：SkillTools 完善（预计 1-2 天）

| 步骤 | 内容 | 文件 |
|---|---|---|
| 1 | SkillTools 注入新依赖，改造文件读取 | `SkillTools.java` |
| 2 | AgentSkillDTO / SkillDetailDTO 新增字段 | `AgentSkillDTO.java`, `SkillDetailDTO.java` |
| 3 | 新增 `SkillFileInfo` DTO | `SkillFileInfo.java` (新建) |
| 4 | SkillTools.toBasicDto / toDetailDto 适配新字段 | `SkillTools.java` |

### Phase 3：多市场技能发现安装（预计 3-5 天）

| 步骤 | 内容 | 文件 |
|---|---|---|
| 1 | 定义领域模型 | `MarketSearchQuery.java`, `MarketSkillSummary.java`, `MarketSkillDetail.java` (新建) |
| 2 | 定义 `SkillMarketPort` 接口 | `SkillMarketPort.java` (新建) |
| 3 | 实现 `ClawHubAdapter` | `ClawHubAdapter.java` (新建) |
| 4 | 实现 `GitHubAdapter` | `GitHubAdapter.java` (新建) |
| 5 | 实现 `OpenCLIHubAdapter` | `OpenCLIHubAdapter.java` (新建) |
| 6 | 创建 `SkillMarketUseCase` | `SkillMarketUseCase.java` (新建) |
| 7 | 创建 `SkillMarketController` | `SkillMarketController.java` (新建) |
| 8 | 创建 API DTO | `MarketInfoResponse.java`, `MarketSkillSummaryResponse.java`, `MarketSkillDetailResponse.java`, `InstallMarketSkillRequest.java` (新建) |
| 9 | 配置文件 | `application.yml` |
| 10 | 前端：新建 `FloatingMarketButton.vue` | (新建) |
| 11 | 前端：新建 `SkillMarketModal.vue` | (新建) |
| 12 | 前端：`AgentHubLayout.vue` 新增市场按钮 | `AgentHubLayout.vue` |
| 13 | 前端：市场 API 函数 + 类型 | `skill-api.ts`, `memory.ts` |
| 14 | 集成测试 | `SkillMarketControllerIntegrationTest.java` (新建) |

### Phase 4：优化（可选）

| 步骤 | 内容 |
|---|---|
| 1 | 市场技能缓存（避免重复请求 GitHub API） |
| 2 | 技能版本管理（升级/回滚） |
| 3 | 市场技能自动更新检查 |
| 4 | 私有市场支持（企业内部技能仓库） |

---

## 六、关键设计决策

| 决策 | 选择 | 理由 |
|---|---|---|
| 定时调度器 | 独立于 ScheduledTaskScheduler | 职责分离，技能同步不需要 Agent/Chat 基础设施 |
| 调度方式 | fixedRate（固定间隔） | 技能同步是周期性任务，不需要 cron 表达式的灵活性 |
| 市场抽象 | Strategy 模式 + `@ConditionalOnProperty` | 新增市场只需加一个 Adapter 类，零侵入 |
| 安装方式 | 复用 `SkillUseCase.createFromUrl` | 市场只提供 downloadUrl，安装链路完全复用已有逻辑 |
| SkillTools 文件读取 | 改用 `DocumentFileStoragePort.retrieve()` | 技能文件已存 MinIO，不再依赖本地路径 |
| SkillTools 搜索 | 改用 `SkillRepository.search()` DB 查询 | 已有 DB 索引，比内存过滤高效 |
| 市场按钮位置 | 同步按钮上方 | 市场是高频操作，放在显眼位置 |
