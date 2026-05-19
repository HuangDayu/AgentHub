/**
 * 工作流类型定义
 * 包含工作流、节点、边、执行结果等核心类型
 */

// ==================== 基础类型 ====================

/**
 * 节点类型枚举
 */
export type NodeType = 
  | 'start'        // 开始节点
  | 'end'          // 结束节点
  | 'llm'          // 大模型节点
  | 'task'         // 任务节点
  | 'condition'    // 条件判断节点
  | 'parallel'     // 并行节点
  | 'loop'         // 循环节点
  | 'tool'         // 工具节点
  | 'retrieval'    // 检索节点
  | 'script'       // 脚本节点
  | 'variable'     // 变量赋值节点

/**
 * 节点状态枚举
 */
export type NodeStatus = 
  | 'idle'         // 空闲
  | 'pending'      // 待执行
  | 'running'      // 执行中
  | 'success'      // 成功
  | 'failed'       // 失败
  | 'timeout'      // 超时
  | 'skipped'      // 跳过

/**
 * 工作流状态枚举
 */
export type WorkflowStatus = 
  | 'draft'        // 草稿
  | 'published'    // 已发布
  | 'deprecated'   // 已废弃
  | 'archived'     // 已归档

// ==================== 工作流定义 ====================

/**
 * 工作流接口
 */
export interface Workflow {
  id: string                      // 工作流ID
  tenantId: string                // 租户ID
  workspaceId: string             // 工作空间ID
  workflowCode: string            // 工作流代码
  name: string                    // 工作流名称
  description: string             // 工作流描述
  graphDefinition: string         // 图定义（JSON字符串）
  status: WorkflowStatus          // 工作流状态
  version?: number                // 版本号
  createdAt: string               // 创建时间
  updatedAt: string               // 更新时间
}

/**
 * 工作流节点接口
 */
export interface WorkflowNode {
  id: string                      // 节点ID
  type: NodeType                  // 节点类型
  name: string                    // 节点名称
  description?: string            // 节点描述
  config: NodeConfig              // 节点配置
  position: NodePosition          // 节点位置
  status?: NodeStatus             // 节点状态
}

/**
 * 节点位置接口
 */
export interface NodePosition {
  x: number                       // X坐标
  y: number                       // Y坐标
}

/**
 * 节点配置接口
 */
export interface NodeConfig {
  inputParams?: InputParam[]      // 输入参数
  outputParams?: OutputParam[]    // 输出参数
  nodeParams?: Record<string, any> // 节点参数
  retryPolicy?: RetryPolicy       // 重试策略
  timeout?: number                // 超时时间（毫秒）
}

/**
 * 输入参数接口
 */
export interface InputParam {
  name: string                    // 参数名称
  type: string                    // 参数类型
  required: boolean               // 是否必填
  defaultValue?: any              // 默认值
  description?: string            // 参数描述
  value?: any                     // 参数值（可以是变量引用）
}

/**
 * 输出参数接口
 */
export interface OutputParam {
  name: string                    // 参数名称
  type: string                    // 参数类型
  description?: string            // 参数描述
}

/**
 * 重试策略接口
 */
export interface RetryPolicy {
  maxRetries: number              // 最大重试次数
  retryDelay: number              // 重试延迟（毫秒）
  retryOn: string[]               // 重试条件
}

/**
 * 工作流边接口
 */
export interface WorkflowEdge {
  id: string                      // 边ID
  source: string                  // 源节点ID
  target: string                  // 目标节点ID
  sourceHandle?: string           // 源节点句柄
  targetHandle?: string           // 目标节点句柄
  condition?: EdgeCondition       // 边条件
  label?: string                  // 边标签
}

/**
 * 边条件接口
 */
export interface EdgeCondition {
  expression: string              // 条件表达式
  description?: string            // 条件描述
}

/**
 * 工作流图定义接口
 */
export interface WorkflowGraph {
  nodes: WorkflowNode[]           // 节点列表
  edges: WorkflowEdge[]           // 边列表
  variables?: WorkflowVariable[]  // 工作流变量
}

/**
 * 工作流变量接口
 */
export interface WorkflowVariable {
  name: string                    // 变量名称
  type: string                    // 变量类型
  defaultValue?: any              // 默认值
  description?: string            // 变量描述
}

// ==================== 执行相关 ====================

/**
 * 节点执行结果接口
 */
export interface NodeResult {
  node_id: string                 // 节点ID
  status: NodeStatus              // 执行状态
  input: Record<string, any>      // 输入数据
  output?: Record<string, any>    // 输出数据
  error?: string                  // 错误信息
  start_time: string              // 开始时间
  end_time?: string               // 结束时间
  duration?: number               // 执行时长（毫秒）
}

/**
 * 工作流执行结果接口
 */
export interface WorkflowExecution {
  task_id: string                 // 任务ID
  workflow_id: string             // 工作流ID
  status: NodeStatus              // 整体状态
  node_results: NodeResult[]      // 节点执行结果
  start_time: string              // 开始时间
  end_time?: string               // 结束时间
  duration?: number               // 总执行时长
  error?: string                  // 错误信息
}

/**
 * SSE事件类型
 */
export type SSEEventType = 
  | 'node_start'                  // 节点开始
  | 'node_complete'               // 节点完成
  | 'node_error'                  // 节点错误
  | 'workflow_complete'           // 工作流完成
  | 'workflow_error'              // 工作流错误

/**
 * SSE事件接口
 */
export interface SSEEvent {
  type: SSEEventType              // 事件类型
  data: any                       // 事件数据
  timestamp: string               // 时间戳
}

// ==================== 节点模板 ====================

/**
 * 节点模板接口
 */
export interface NodeTemplate {
  type: NodeType                  // 节点类型
  name: string                    // 节点名称
  icon: string                    // 节点图标
  description: string             // 节点描述
  category: string                // 节点分类
  defaultConfig?: Partial<NodeConfig> // 默认配置
}

/**
 * 预定义的节点模板
 */
export const NODE_TEMPLATES: NodeTemplate[] = [
  {
    type: 'start',
    name: '开始',
    icon: '▶',
    description: '工作流的开始节点',
    category: 'control'
  },
  {
    type: 'end',
    name: '结束',
    icon: '⏹',
    description: '工作流的结束节点',
    category: 'control'
  },
  {
    type: 'llm',
    name: 'LLM',
    icon: '🤖',
    description: '大语言模型节点',
    category: 'ai',
    defaultConfig: {
      nodeParams: {
        model: '',
        temperature: 0.7,
        maxTokens: 2000
      }
    }
  },
  {
    type: 'condition',
    name: '条件',
    icon: '◇',
    description: '条件分支节点',
    category: 'control',
    defaultConfig: {
      nodeParams: {
        conditions: []
      }
    }
  },
  {
    type: 'parallel',
    name: '并行',
    icon: '⫿',
    description: '并行执行多个分支',
    category: 'control'
  },
  {
    type: 'loop',
    name: '循环',
    icon: '↻',
    description: '循环执行任务',
    category: 'control',
    defaultConfig: {
      nodeParams: {
        maxIterations: 10,
        condition: ''
      }
    }
  },
  {
    type: 'tool',
    name: '工具',
    icon: '🔧',
    description: '调用外部工具',
    category: 'action',
    defaultConfig: {
      nodeParams: {
        toolName: '',
        parameters: {}
      }
    }
  },
  {
    type: 'retrieval',
    name: '检索',
    icon: '🔍',
    description: '知识库检索节点',
    category: 'action',
    defaultConfig: {
      nodeParams: {
        knowledgeBaseId: '',
        topK: 5
      }
    }
  },
  {
    type: 'script',
    name: '脚本',
    icon: '📜',
    description: '执行自定义脚本',
    category: 'action',
    defaultConfig: {
      nodeParams: {
        language: 'javascript',
        code: ''
      }
    }
  },
  {
    type: 'variable',
    name: '变量',
    icon: '📝',
    description: '变量赋值节点',
    category: 'action',
    defaultConfig: {
      nodeParams: {
        assignments: []
      }
    }
  }
]
