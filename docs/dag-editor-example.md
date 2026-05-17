# DAG编辑器使用示例

## 基本用法

DAG编辑器已经集成到工作流管理页面中。在创建或编辑工作流时，可以使用可视化编辑器或JSON编辑器。

## 功能特性

### 1. 节点类型

支持以下节点类型：
- **开始节点** (Start): 工作流的起始点
- **结束节点** (End): 工作流的结束点
- **任务节点** (Task): 执行具体任务
- **条件节点** (Condition): 条件分支
- **并行节点** (Parallel): 并行执行多个分支
- **循环节点** (Loop): 循环执行任务

### 2. 可视化编辑

#### 添加节点
1. 从左侧节点面板拖拽节点模板到画布
2. 节点会自动放置在鼠标释放位置

#### 连接节点
1. 点击节点的输出连接点（右侧圆点）
2. 再点击目标节点的输入连接点（左侧圆点）
3. 自动创建连接边

#### 编辑节点
1. 点击节点选中
2. 在右侧属性面板编辑节点属性：
   - 节点名称
   - 节点类型
   - 描述
   - 配置（JSON格式）

#### 删除节点/边
1. 选中节点或边
2. 在属性面板点击"删除"按钮

### 3. JSON编辑

点击"切换到JSON编辑"按钮，可以直接编辑图定义的JSON格式。

JSON格式示例：
```json
{
  "nodes": [
    {
      "id": "node-1",
      "type": "start",
      "name": "开始",
      "description": "工作流开始",
      "position": { "x": 100, "y": 100 }
    },
    {
      "id": "node-2",
      "type": "task",
      "name": "执行任务",
      "description": "执行某个任务",
      "config": {
        "action": "send_email",
        "parameters": {
          "to": "user@example.com",
          "subject": "通知"
        }
      },
      "position": { "x": 300, "y": 100 }
    },
    {
      "id": "node-3",
      "type": "end",
      "name": "结束",
      "description": "工作流结束",
      "position": { "x": 500, "y": 100 }
    }
  ],
  "edges": [
    {
      "id": "edge-1",
      "source": "node-1",
      "target": "node-2"
    },
    {
      "id": "edge-2",
      "source": "node-2",
      "target": "node-3"
    }
  ]
}
```

### 4. 导出功能

点击"导出"按钮，可以将当前图定义导出为JSON文件。

## 工作流示例

### 示例1: 简单线性流程

```
开始 -> 任务1 -> 任务2 -> 结束
```

### 示例2: 条件分支流程

```
开始 -> 条件判断 -> [任务A | 任务B] -> 结束
```

### 示例3: 并行执行流程

```
开始 -> 并行节点 -> [任务A, 任务B, 任务C] -> 合并 -> 结束
```

### 示例4: 循环流程

```
开始 -> 循环节点 -> 任务 -> 结束
```

## 技术实现

### 数据结构

```typescript
// DAG节点
interface DAGNode {
  id: string
  type: NodeType
  name: string
  description?: string
  config?: Record<string, any>
  position: { x: number; y: number }
}

// DAG边
interface DAGEdge {
  id: string
  source: string
  target: string
  condition?: string
  label?: string
}

// DAG图
interface DAGGraph {
  nodes: DAGNode[]
  edges: DAGEdge[]
}
```

### 组件结构

```
DAGEditor.vue
├── 工具栏 (Toolbar)
│   ├── 标题
│   ├── 切换视图按钮
│   └── 导出按钮
├── 编辑区域
│   ├── 节点面板 (左侧)
│   ├── 画布区域 (中间)
│   │   ├── SVG边层
│   │   └── 节点层
│   └── 属性面板 (右侧)
└── JSON编辑器 (可选)
```

## 注意事项

1. **节点ID唯一性**: 每个节点的ID必须唯一，系统会自动生成
2. **边的方向**: 边只能从输出连接点连接到输入连接点
3. **自连接**: 不允许节点连接到自己
4. **重复连接**: 不允许创建重复的边
5. **JSON格式**: JSON编辑时请确保格式正确，否则会显示错误信息

## 未来扩展

可以考虑添加以下功能：
- 节点搜索和过滤
- 图结构验证（DAG检测）
- 撤销/重做功能
- 节点分组
- 子图支持
- 导入/导出多种格式
- 节点模板自定义
- 条件表达式编辑器
