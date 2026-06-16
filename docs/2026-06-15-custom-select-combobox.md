# CustomSelect Combobox 改造方案

> 日期：2026-06-15 · 状态：已完成

## 1. 需求概述

将所有原生 `<select>` 替换为 combobox 风格的 `CustomSelect`，支持输入文字过滤选项；Agent 创建/编辑表单添加 `type`/`runtimeCategory` 下拉框。

## 2. 架构设计

| 层 | 文件 | 操作 |
|----|------|------|
| Component | `src/components/CustomSelect.vue` | 重写为 combobox 风格 |
| View | `src/views/agenthub/AgentManagementView.vue` | 新增 type/runtimeCategory 下拉 |
| Component | `src/components/WorkspaceSelector.vue` | 去外层包装 + 修复默认选中 |
| 27+ Views | `src/views/agenthub/*.vue` + panels | 全部替换为 CustomSelect |

## 3. 核心方案

### CustomSelect 事件策略（关键决策）

| 事件 | 用途 | 说明 |
|------|------|------|
| `@click="selectOption(option)"` | 选项选中 | 使用 click 而非 mousedown，避免 `.preventDefault()` 带来的弹窗兼容问题 |
| `@focus="onFocus"` | 打开下拉 | 输入框获得焦点时打开，受 `selectedGuard` 保护 |
| `@click="onInputClick"` | 重复打开 | 已聚焦输入框再次点击时打开下拉（`@focus` 不重复触发） |
| `@input="onInput"` | 输入过滤 | 输入文本时打开下拉 |
| `v-click-outside="closeDropdown"` | 外部关闭 | 点击组件外部时关闭下拉 |

### 焦点管理

- **选中后不 `blur()`**：避免 `@focus`/`@blur` 连锁反应导致弹窗环境中下拉闪烁
- **`selectedGuard` 守卫**：选中后 200ms 内阻止 `onFocus` 和 `onInputClick` 重新打开下拉，解决 ModalDialog 弹窗中"闪烁"问题
- **光标保留**：选中后光标留在输入框末尾，用户可直接输入过滤搜索另一个选项

### 数据流

```
Request → Command → Domain Model → Output → Response
             ↓
CustomSelect v-model (modelValue)
             ↓
watch(modelValue, { immediate: true }) → inputValue 回显
watch(isOpen) → inputValue 清空/回显切换
```

## 4. 边界情况

- [x] 原生 `<select>` 全部替换为 CustomSelect，无残留 `searchable` prop
- [x] 选中后下拉关闭 + 显示选中标签（非灰色 placeholder）
- [x] 打开下拉默认高亮第一项：`highlightedIndex = 0`
- [x] 编辑时回显已有值：`watch(modelValue, { immediate: true })`
- [x] ModalDialog 弹窗中下拉选中 → 200ms `selectedGuard` 防闪烁
- [x] 选中后再次点击输入框 → `@click="onInputClick"` 重新打开下拉
- [x] 导航栏工作区下拉 → 去外层 `<div>` 包装 + `autoSelectFirstWorkspace` 条件增强
- [x] 构建验证：`npm run build` → 287 modules, 零错误

## 5. 检查清单

- [x] `selectOption` 方法 6 行
- [x] `onFocus`/`onInputClick` ≤3 参数
- [x] 无 `@blur`、无 `@mousedown`、无 `.prevent` 修饰符
- [x] 无通配符 import

## 完成情况

- 构建: npm run build ✅
- 原生 `<select>` 全部替换: 27+ 文件 ✅

## 反思

- 问题 1：`@mousedown.prevent` 在 ModalDialog 中无法选中 → 改用 `@click` + `selectedGuard`
- 问题 2：`@click` 在弹窗中选中后闪烁 → `onBlur` 过早关闭下拉导致 DOM 移除 → 移除 `@blur` + `selectedGuard`
- 问题 3：选中后无法再次打开下拉 → `@click="onInputClick"` 兜底
- 改进建议：后续如需减少 DOM 层级，可考虑将 `@click.stop` 从 ModalDialog.container 移除或改用 `@click.self`
