# 前端美化与优化方案

> 记录于 2026-05-26，用于后续开发迭代优化参考

本文档记录了 AgentHub 前端的美化改造方案，涵盖主题系统（4 套主题）、视觉效果控制、组件美化及页面优化等内容。

---

## 一、主题系统

### 1.1 主题架构

4 套主题定义在 `src/styles/themes.css`，通过 `[data-theme="..."]` 属性选择器切换：

| 主题 ID | 名称 | 风格 | 色温 |
|---------|------|------|------|
| `aether` | Aether | 清新明亮（默认） | 冷白/蓝 |
| `nocturne` | Nocturne | 暗色深邃 | 暗蓝/灰 |
| `verdant` | Verdant | 森林自然 | 绿/暖白 |
| `cipher` | Cipher | 赛博科技 | 暗紫/绿 |

所有主题统一了标题字体 `--font-heading: 'Instrument Sans'`，避免切换主题时字体变化。

### 1.2 主题切换

- **主题选择 UI**：位于 `EffectToggle` 面板（由右边悬浮主题按钮触发），使用 `CustomSelect` 下拉框，每个选项附带描述说明
- **视图布局**："主题选择" 标签在左，下拉框在右
- **状态管理**：`useThemeStore` 管理当前主题，持久化到 localStorage

### 1.3 关键 CSS 变量体系

每个主题定义以下变量群组：
- `--color-primary` / `--color-primary-dark` — 主色
- `--bg-*` — 背景色（card, page, header, input 等）
- `--color-text` / `--color-heading` / `--color-text-muted` — 文字色
- `--color-border` / `--color-border-strong` — 边框色
- `--shadow-*` — 阴影
- `--color-primary-subtle` — 主色半透明态，用于 hover 背景

---

## 二、视觉效果控制系统

### 2.1 架构

`EffectToggle` 组件（`src/components/EffectToggle.vue`）提供三个独立开关：

| 效果 | CSS 控制类名 | 说明 |
|------|-------------|------|
| 毛玻璃 | `glass-disabled` | 卡片/面板的半透明模糊背景 |
| 悬浮 | `float-disabled` | 鼠标悬停时元素轻微上浮 |
| 动画 | `animation-disabled` | 页面过渡和交互动画 |

所有效果开关持久化到 localStorage（key: `effect-settings`）。

### 2.2 CSS 类名体系

定义于 `src/common/base.css`：

| 类名 | 效果 | 适用元素 |
|------|------|---------|
| `glass-effect` | 轻度毛玻璃（70%不透明度 + 20px blur） | 面板、卡片 |
| `glass-effect-medium` | 中等毛玻璃（50%不透明度 + 20px blur） | 需要更强透明度的元素 |
| `glass-effect-dark` | 深色毛玻璃（30%不透明度 + 30px blur） | 需要很强透明度的元素 |
| `float-effect` | 悬浮效果（hover 时 translateY(-4px) + 阴影增强） | 卡片、按钮等交互元素 |
| `glass-float` | 毛玻璃 + 悬浮组合 | 主要面板和页面容器 |
| `effects-disabled` | 完全禁用所有效果 | `<html>` |

### 2.3 禁用机制

`themes.css` 定义禁用规则：

```css
.glass-disabled [class*="glass"],
.glass-disabled [style*="backdrop-filter"] {
  backdrop-filter: none !important;
  -webkit-backdrop-filter: none !important;
}
.float-disabled [class*="float"] {
  transform: none !important;
}
```

通过 `classList.add/remove` 在 `<html>` 上切换 `float-disabled` / `glass-disabled` / `animation-disabled` 来控制全局效果开关。

---

## 三、页面容器效果

### 3.1 页面根容器

所有页面根 `<section>` 添加 `glass-float` 类，持毛玻璃 + 悬浮效果：

| 文件 | 添加/状态 |
|------|----------|
| `AgentConfigView.vue` | 已有 |
| `AgentStudioView.vue` | 已添加 |
| `AgentTeamManagementView.vue` | 已有 |
| `KnowledgeWorkbenchView.vue` | 已有 |
| `McpToolView.vue` | 已有 |
| `MemoryManagementView.vue` | 已有 |
| `ModelConfigView.vue` | 已有 |
| `PromptTemplateView.vue` | 已有 |
| `RetrievalView.vue` | 已有 |
| `ScheduledTaskView.vue` | 已有 |
| `SettingsView.vue` | 已有 |
| `SkillManagementView.vue` | 已有 |
| `StrategyManagementView.vue` | 已有 |
| `SystemToolsView.vue` | 已添加 |
| `VectorStoreConfigView.vue` | 已有 |
| `WorkflowManagementView.vue` | 已有 |
| `WorkspaceOverviewView.vue` | 已添加 |

### 3.2 内部容器效果

内部面板/卡片/表格容器逐个添加 `float-effect` 类：

**表格卡片（`article.table-card`）**：
- AgentStudioView, McpToolView, KnowledgeWorkbenchView, PromptTemplateView, ModelConfigView, VectorStoreConfigView

**面板（`article.panel`）**：
- AgentConfigView, ScheduledTaskView (header + content), SystemToolsView, RetrievalView (form + results), WorkspaceOverviewView

**直接表格包装器**：
- AgentTeamManagementView → `div.team-list`, SkillManagementView → `div.skill-list`,
  WorkflowManagementView → `div.workflow-list`, MemoryManagementView → `div.memory-list`

---

## 四、全局表单元素悬浮效果

`src/common/base.css` 添加全局 hover 效果：

```css
input:hover,
textarea:hover,
select:hover {
  border-color: var(--color-primary, #3a7bd5);
  box-shadow: 0 0 0 2px var(--color-primary-subtle, rgba(58, 123, 213, 0.08));
}
```

覆盖页面：所有使用原生 input/textarea/select 的页面（非聊天页面上次已有独立处理）。

---

## 五、浮动按钮统一

### 5.1 颜色统一

所有浮动按钮统一使用主色渐变，与设置按钮一致：

```css
background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
```

涉及组件：
- `FloatingAddButton.vue`
- `FloatingSyncButton.vue`
- `RuntimeChatView.vue` → `.toggle-runtime-fab`, `.new-session-fab`

### 5.2 页面过渡动画修复

修复因 `Transition` 的 `pageIn`/`pageOut` 动画使用 `transform` 导致 `position: fixed` 浮动按钮跳动的问题。移除 `TenantLayout.vue` 中 `pageIn` 和 `pageOut` 的 `transform` 属性，仅保留 `opacity` 动画。

---

## 六、可复用组件

### 6.1 CustomSelect

`src/components/CustomSelect.vue`

- 支持 `v-model`、`placeholder`、`disabled`、`error`、`hint`
- 支持搜索过滤（`searchable` 属性）
- 选项支持 `label` + `description` 双行展示
- 点击外部关闭、过渡动画

### 6.2 CustomButton

`src/components/CustomButton.vue`

- Types: `primary` / `secondary` / `ghost` / `danger` / `success` / `warning` / `info`
- Sizes: `small` / `medium` / `large`

### 6.3 ModalDialog

`src/components/ModalDialog.vue`

- 使用 `Teleport` 渲染到 body
- 支持标题、确认/取消按钮、关闭按钮
- `v-model:visible` 控制显隐
- 禁用确认、加载状态等

---

## 七、工具函数

`src/utils/pageEnhancer.ts` 提供：

- `useEffects()` — 获取/切换/重置效果配置
- `useModalForm()` — 表单弹窗操作
- `enhancePageContainer()` — 为页面容器自动添加效果类
- `addGlassEffect()` / `addFloatEffect()` / `addGlassFloatEffect()` — 命令式添加效果类

---

## 八、主题切换入口迁移

- 从 `TenantLayout.vue` 的 header 中移除了 `<ThemeSwitcher/>` 组件
- 从 `SettingsView.vue` 中移除了"主题选择"独立区域
- 统一整合到 `EffectToggle.vue` 面板（由 `FloatingEffectButton` 触发）

---

## 九、注意事项与优化建议

### 9.1 CSS 优先级

- `base.css` 在 `themes.css` 之前导入（`main.ts` 第 5、6 行）
- `float-disabled` 禁用规则在 `themes.css` 中使用 `!important`，正确覆盖 `base.css` 中的 hover 效果
- 新增容器时，给内部卡片/面板添加 `float-effect` 类即可获得悬浮效果

### 9.2 新增页面开发模板

```vue
<template>
  <section class="grid glass-float">
    <div class="page-header">
      <h2>页面标题</h2>
      <p class="muted">页面描述</p>
    </div>
    <article class="table-card float-effect">
      <table>...</table>
    </article>
  </section>
</template>
```

### 9.3 常见问题

1. **浮动按钮跳动**：页面过渡动画中不要使用 `transform`，会影响 `position: fixed` 元素
2. **主题切换字体变化**：所有主题的 `--font-heading` 需保持一致
3. **深色主题文字**：确保所有文字颜色使用 CSS 变量而非固定色值
4. **效果开关**：新添加的效果类需在 `themes.css` 中添加对应的禁用规则
