# 代码审查修复方案

> 日期：2026-05-30
> 审查发现 26 个问题（4 CRITICAL + 8 HIGH + 10 MEDIUM + 4 LOW）

---

## CRITICAL 修复

### 1. PlanExecutor 无限循环
- 添加最大迭代次数限制（1000 步）
- 添加循环依赖检测

### 2. PlanTools.addStep 创建重复计划
- ExecutionPlanUseCase 新增 addStepToPlan(planId, stepInput)

### 3. RuntimeTools.exec 流死锁
- 使用 redirectErrorStream(true) 合并输出流

### 4. RuntimeTools.codeExecution 命令注入
- 写入临时文件后执行，而非 -c 参数

## HIGH 修复

### 5. RuntimeTools.readStream 资源泄漏
- 使用 try-with-resources

### 6. A2ATools.awaitTaskResult 线程阻塞
- 添加最大超时限制 60 秒

### 7. InMemory 实现竞态条件
- 使用 computeIfPresent 原子更新

### 8. AgentToolsFactory 静态可变状态
- 移除 static 修饰符

### 9. PlanExecutor 步骤失败仍标记 COMPLETED
- 检查执行结果，失败时标记 FAILED

### 10. ModelSwitchTools 无反馈
- 未找到配置时返回错误

## MEDIUM 修复

### 11. ToolFilterUseCase 名称匹配
- 改为精确匹配 equals

### 12. SystemPromptBuilderUseCase 空指针
- 添加空值检查

### 13. ExecutionPlanUseCase 步骤顺序
- 使用实际索引作为 order

### 14. RuntimeTools 超时后进程清理
- 调用 process.destroyForcibly()

### 15. A2ATools 返回 null
- 返回错误 DTO

---

## 实施顺序

```
1-4: CRITICAL 修复
5-10: HIGH 修复
11-15: MEDIUM 修复
编译验证 + ArchUnit
```
