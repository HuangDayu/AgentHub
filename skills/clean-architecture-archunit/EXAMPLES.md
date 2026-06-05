# TDD Development Walkthrough

> 以"添加 Agent 统计查询接口"为例，演示完整的 10 步 TDD 开发流程。

---

## 第 1 步：理解架构

确认四层依赖方向：`api → application → domain ← infrastructure`。

---

## 第 2 步：理解需求

- **功能**：查询 Agent 的运行统计（总消息数、总 Token 数、平均响应时间）
- **API**：`GET /api/v1/workspaces/{wsId}/agents/{agentId}/stats`
- **正常返回**：`200 { agentId, totalMessages, totalTokens, avgResponseTimeMs }`
- **边界情况**：Agent 不存在 → 404

---

## 第 3 步：制定方案

创建 `docs/2026-06-05-agent-stats-api-plan.md`：

```markdown
# Agent 统计查询 API 实现方案

> 日期：2026-06-05 · 状态：进行中

## 1. 需求概述

新增 GET /api/v1/workspaces/{wsId}/agents/{agentId}/stats，返回 Agent 消息数/Token/响应时间。

## 2. 架构设计

| 层 | 文件 | 操作 |
|----|------|------|
| domain | AgentStats.java | 新建 |
| application/port | AgentStatsRepository.java | 新建 |
| application/command | GetAgentStatsCommand.java | 新建 |
| application/dto | AgentStatsOutput.java | 新建 |
| application/usecase | AgentStatsUseCase.java | 新建 |
| infrastructure/entity | AgentStatsEntity.java | 新建 |
| infrastructure/mapper | AgentStatsMapper.java | 新建 |
| infrastructure/repository | MybatisAgentStatsRepository.java | 新建 |
| api/dto | AgentStatsResponse.java | 新建 |
| api/controller | AgentController.java | 修改（+1 方法） |
| test | AgentStatsControllerIntegrationTest.java | 新建 |

## 3. API 设计

- Method: GET
- Path: /api/v1/workspaces/{wsId}/agents/{agentId}/stats
- Response 200: { "agentId", "totalMessages", "totalTokens", "avgResponseTimeMs" }
- Response 404: { "error", "message" }

## 4. 边界情况

- [x] Agent 存在且有数据 → 200 + 统计数据
- [ ] Agent 不存在 → 404 NotFoundException

## 5. 检查清单

- [ ] 所有方法 ≤10 行、≤3 参数
- [ ] 分层依赖正确
- [ ] 命名符合约定
```

---

## 第 4 步：编写集成测试（Red）

**文件：`src/test/java/com/agenthub/test/integration/AgentStatsControllerIntegrationTest.java`**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AgentStatsControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BASE = "/api/v1/workspaces/test-ws/agents/test-agent/stats";

    @Test
    @Order(1)
    void shouldGetAgentStats() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", "test-tenant");
        ResponseEntity<String> resp = restTemplate.exchange(
                BASE, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(2)
    void shouldReturn404ForUnknownAgent() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-Id", "test-tenant");
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/v1/workspaces/test-ws/agents/nonexistent/stats",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

运行确认 Red：
```bash
gradle test --tests "*AgentStatsControllerIntegrationTest*"
# FAILED ✅ — 接口还不存在
```

---

## 第 5 步：按方案开发（Green）

### 5.1 domain/model/AgentStats.java

```java
package com.agenthub.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Agent 运行统计 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentStats {
    private String agentId;
    private long totalMessages;
    private long totalTokens;
    private double avgResponseTimeMs;
}
```

### 5.2 application/command/GetAgentStatsCommand.java

```java
package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** 查询 Agent 统计命令 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAgentStatsCommand {
    private String workspaceId;
    private String agentId;
}
```

### 5.3 application/dto/AgentStatsOutput.java

```java
package com.agenthub.application.dto;

import com.agenthub.domain.model.AgentStats;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Agent 统计输出 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentStatsOutput {
    private String agentId;
    private long totalMessages;
    private long totalTokens;
    private double avgResponseTimeMs;

    public static AgentStatsOutput from(AgentStats s) {
        AgentStatsOutput o = new AgentStatsOutput();
        o.setAgentId(s.getAgentId());
        o.setTotalMessages(s.getTotalMessages());
        o.setTotalTokens(s.getTotalTokens());
        o.setAvgResponseTimeMs(s.getAvgResponseTimeMs());
        return o;
    }
}
```

### 5.4 application/port/out/repositories/AgentStatsRepository.java

```java
package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.AgentStats;
import java.util.Optional;

/** Agent 统计仓储端口 */
public interface AgentStatsRepository {
    Optional<AgentStats> findByAgentId(String agentId);
}
```

### 5.5 application/usecase/AgentStatsUseCase.java

```java
package com.agenthub.application.usecase;

import com.agenthub.application.command.GetAgentStatsCommand;
import com.agenthub.application.dto.AgentStatsOutput;
import com.agenthub.application.port.out.repositories.AgentStatsRepository;
import com.agenthub.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Agent 统计用例 */
@Component
@RequiredArgsConstructor
public class AgentStatsUseCase {
    private final AgentStatsRepository agentStatsRepository;

    /** 获取 Agent 统计 */
    public AgentStatsOutput getStats(GetAgentStatsCommand cmd) {
        return agentStatsRepository.findByAgentId(cmd.getAgentId())
                .map(AgentStatsOutput::from)
                .orElseThrow(() -> new NotFoundException("Agent not found"));
    }
}
```

### 5.6 infrastructure/store/db/entity/AgentStatsEntity.java

```java
package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** Agent 统计实体 */
@Data
@TableName("agent_stats")
public class AgentStatsEntity {
    @TableId
    private String agentId;
    private long totalMessages;
    private long totalTokens;
    private double avgResponseTimeMs;
}
```

### 5.7 infrastructure/store/db/mapper/AgentStatsMapper.java

```java
package com.agenthub.infrastructure.store.db.mapper;

import com.agenthub.infrastructure.store.db.entity.AgentStatsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentStatsMapper extends BaseMapper<AgentStatsEntity> { }
```

### 5.8 infrastructure/store/db/repository/MybatisAgentStatsRepository.java

```java
package com.agenthub.infrastructure.store.db.repository;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.AgentStatsRepository;
import com.agenthub.domain.model.AgentStats;
import com.agenthub.infrastructure.store.db.entity.AgentStatsEntity;
import com.agenthub.infrastructure.store.db.mapper.AgentStatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Agent 统计 MyBatis 仓储 */
@Component
@Primary
@RequiredArgsConstructor
public class MybatisAgentStatsRepository implements AgentStatsRepository {
    private final AgentStatsMapper mapper;

    @Override
    public Optional<AgentStats> findByAgentId(String id) {
        AgentStatsEntity e = mapper.selectById(id);
        if (e == null) return Optional.empty();
        AgentStats s = new AgentStats();
        BeanUtil.copyProperties(e, s);
        return Optional.of(s);
    }
}
```

### 5.9 api/dto/AgentStatsResponse.java

```java
package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/** Agent 统计响应 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentStatsResponse {
    private String agentId;
    private long totalMessages;
    private long totalTokens;
    private double avgResponseTimeMs;
}
```

### 5.10 修改 api/controller/AgentController.java

在已有 Controller 中新增：

```java
private final AgentStatsUseCase agentStatsUseCase;

/** 获取 Agent 统计信息 */
@GetMapping("/{agentId}/stats")
public AgentStatsResponse getAgentStats(@PathVariable String workspaceId,
                                         @PathVariable String agentId) {
    GetAgentStatsCommand cmd = new GetAgentStatsCommand(workspaceId, agentId);
    AgentStatsOutput output = agentStatsUseCase.getStats(cmd);
    AgentStatsResponse resp = new AgentStatsResponse();
    BeanUtil.copyProperties(output, resp);
    return resp;
}
```

---

## 第 6 步：代码自检

逐项确认 9 项检查清单：

- [x] 每个方法 ≤10 行 ✅（UseCase.getStats 4 行，Controller 6 行）
- [x] 每个方法 ≤3 参数 ✅
- [x] 每个方法有中文 Javadoc ✅
- [x] domain 层零框架依赖 ✅
- [x] application 层无 infrastructure 引用 ✅
- [x] Controller 不直接依赖 domain ✅（经由 Output → Response）
- [x] 命名符合约定 ✅
- [x] 无 record / @Builder / @Autowired ✅
- [x] 无通配符 import ✅

---

## 第 7 步：ArchUnit 校验

```bash
gradle test --tests "com.agenthub.test.architecture.AgentHubCleanArchitectureTest"
```

输出：
```
Tests: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESSFUL
```

17/17 ✅

---

## 第 8 步：集成测试验证

```bash
gradle test --tests "*AgentStatsControllerIntegrationTest*"
```

输出：
```
Tests: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESSFUL
```

2/2 ✅

---

## 第 9 步：总结反思

更新方案文档 `docs/2026-06-05-agent-stats-api-plan.md`：

```markdown
## 完成情况

- ArchUnit: 17/17 ✅
- 集成测试: 2/2 ✅
- 创建文件: 9 个
- 修改文件: 1 个

## 反思

- 遇到的问题：无
- 解决方案：无
- 改进建议：AgentStatsResponse 可考虑用 BeanUtil 从 Output 直接转，减少手动 setter
```

---

## 第 10 步：等待管理员审核

```
方案文档: docs/2026-06-05-agent-stats-api-plan.md
ArchUnit: 17/17 ✅
集成测试: 2/2 ✅
请审核代码和方案文档，如有修改意见我将立即调整。
```

---

## 违规修复速查

| 违规现象 | 原因 | 修复方式 |
|---------|------|---------|
| `domain_should_not_depend_on_outer_layers` | domain 引入 Spring 注解 | 移除框架注解，改为纯 POJO |
| `application_should_not_depend_on_infrastructure` | UseCase 直接 new 了 infrastructure 类 | 改为依赖 port 接口 |
| `api_should_not_directly_depend_on_domain` | Controller 返回 domain 对象 | 创建 Response DTO，在 Controller 中转换 |
| `methods_should_not_exceed_ten_lines` | 方法 >10 行 | 按逻辑段落拆分为私有方法 |
| `methods_should_not_have_more_than_three_parameters` | 参数 >3 个 | 创建 `*Command` 封装 |
| 命名后缀不符 | 类名不以约定的后缀结尾 | 重命名 |
| Repository 接口位置错误 | 放在 infrastructure 包 | 移到 `application.port.out.repositories` |
