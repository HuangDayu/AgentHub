// DAG节点类型
export type NodeType = 'start' | 'end' | 'task' | 'condition' | 'parallel' | 'loop'

// DAG节点定义
export interface DAGNode {
  id: string
  type: NodeType
  name: string
  description?: string
  config?: Record<string, any> // 节点配置
  position: {
    x: number
    y: number
  }
}

// DAG边定义
export interface DAGEdge {
  id: string
  source: string // 源节点ID
  target: string // 目标节点ID
  condition?: string // 条件表达式（用于条件分支）
  label?: string // 边的标签
}

// DAG图定义
export interface DAGGraph {
  nodes: DAGNode[]
  edges: DAGEdge[]
}

// 节点模板（用于创建新节点）
export interface NodeTemplate {
  type: NodeType
  name: string
  icon: string
  description: string
  defaultConfig?: Record<string, any>
}

// 预定义的节点模板
export const NODE_TEMPLATES: NodeTemplate[] = [
  {
    type: 'start',
    name: '开始',
    icon: '▶',
    description: '工作流的开始节点'
  },
  {
    type: 'end',
    name: '结束',
    icon: '⏹',
    description: '工作流的结束节点'
  },
  {
    type: 'task',
    name: '任务',
    icon: '⚙',
    description: '执行一个任务或操作',
    defaultConfig: {
      action: '',
      parameters: {}
    }
  },
  {
    type: 'condition',
    name: '条件',
    icon: '◇',
    description: '条件分支节点',
    defaultConfig: {
      expression: ''
    }
  },
  {
    type: 'parallel',
    name: '并行',
    icon: '⫿',
    description: '并行执行多个分支'
  },
  {
    type: 'loop',
    name: '循环',
    icon: '↻',
    description: '循环执行任务',
    defaultConfig: {
      maxIterations: 10,
      condition: ''
    }
  }
]
