/**
 * 工作流节点类型定义
 * 参考spring-ai-alibaba项目的节点系统设计
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
  | 'api'          // API调用节点
  | 'script'       // 脚本节点
  | 'retrieval'    // 知识检索节点
  | 'input'        // 输入节点
  | 'output'       // 输出节点
  | 'iterator'     // 迭代器节点
  | 'variable'     // 变量处理节点

/**
 * 参数类型
 */
export type ParamType = 'string' | 'number' | 'boolean' | 'object' | 'array' | 'any'

/**
 * 变量引用格式：${nodeId.outputKey}
 */
export type VariableRef = string

// ==================== 参数定义 ====================

/**
 * 输入参数项
 */
export interface InputParam {
  key: string              // 参数键名
  name: string             // 参数显示名称
  type: ParamType          // 参数类型
  required: boolean        // 是否必填
  default?: any            // 默认值
  value?: any              // 实际值（可以是变量引用）
  description?: string     // 参数描述
}

/**
 * 输出参数项
 */
export interface OutputParam {
  key: string              // 输出键名
  name: string             // 输出显示名称
  type: ParamType          // 输出类型
  description?: string     // 输出描述
}

// ==================== 节点参数配置 ====================

/**
 * 开始节点参数
 */
export interface StartNodeParam {
  input_params: InputParam[]  // 工作流输入参数定义
}

/**
 * 结束节点参数
 */
export interface EndNodeParam {
  output_params: OutputParam[]  // 工作流输出参数定义
}

/**
 * LLM节点参数
 */
export interface LLMNodeParam {
  model_id: string              // 模型ID
  model_config: {
    temperature: number         // 温度参数
    max_tokens: number          // 最大token数
    top_p: number              // Top-p采样
    frequency_penalty: number   // 频率惩罚
    presence_penalty: number    // 存在惩罚
  }
  sys_prompt: string            // 系统提示词（支持变量引用）
  user_prompt: string           // 用户提示词（支持变量引用）
  output_key: string            // 输出变量名
}

/**
 * API节点参数
 */
export interface ApiNodeParam {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  url: string                   // API地址（支持变量引用）
  headers: Record<string, string>  // 请求头
  body?: string                 // 请求体（支持变量引用）
  timeout: number               // 超时时间（毫秒）
  output_key: string            // 输出变量名
}

/**
 * 脚本节点参数
 */
export interface ScriptNodeParam {
  language: 'javascript' | 'python'
  code: string                  // 脚本代码
  output_key: string            // 输出变量名
}

/**
 * 知识检索节点参数
 */
export interface RetrievalNodeParam {
  knowledge_base_id: string     // 知识库ID
  query: string                 // 查询文本（支持变量引用）
  top_k: number                 // 返回结果数量
  score_threshold: number       // 分数阈值
  output_key: string            // 输出变量名
}

/**
 * 条件节点参数
 */
export interface ConditionNodeParam {
  conditions: Array<{
    expression: string          // 条件表达式
    label: string               // 分支标签
  }>
  default_branch: string        // 默认分支
}

/**
 * 任务节点参数
 */
export interface TaskNodeParam {
  action: string                // 任务动作
  parameters: Record<string, any>  // 任务参数
  output_key: string            // 输出变量名
}

/**
 * 循环节点参数
 */
export interface LoopNodeParam {
  max_iterations: number        // 最大迭代次数
  condition: string             // 循环条件
  iteration_var: string         // 迭代变量名
}

/**
 * 并行节点参数
 */
export interface ParallelNodeParam {
  branches: string[]            // 分支节点ID列表
  wait_all: boolean             // 是否等待所有分支完成
}

/**
 * 迭代器节点参数
 */
export interface IteratorNodeParam {
  input_array: string           // 输数组（支持变量引用）
  item_var: string              // 当前项变量名
  index_var: string             // 索引变量名
}

/**
 * 变量处理节点参数
 */
export interface VariableNodeParam {
  operations: Array<{
    type: 'set' | 'get' | 'delete'
    key: string
    value?: any
  }>
}

/**
 * 输入节点参数
 */
export interface InputNodeParam {
  input_type: 'text' | 'file' | 'select'
  label: string
  default?: any
  required: boolean
}

/**
 * 输出节点参数
 */
export interface OutputNodeParam {
  output_type: 'text' | 'file' | 'json'
  value: string                 // 输出值（支持变量引用）
}

/**
 * 节点参数联合类型
 */
export type NodeParam = 
  | StartNodeParam
  | EndNodeParam
  | LLMNodeParam
  | ApiNodeParam
  | ScriptNodeParam
  | RetrievalNodeParam
  | ConditionNodeParam
  | TaskNodeParam
  | LoopNodeParam
  | ParallelNodeParam
  | IteratorNodeParam
  | VariableNodeParam
  | InputNodeParam
  | OutputNodeParam

// ==================== 节点数据定义 ====================

/**
 * 工作流节点数据
 */
export interface WorkflowNodeData {
  label: string                 // 节点显示名称
  desc?: string                 // 节点描述
  input_params: InputParam[]    // 输入参数
  output_params: OutputParam[]  // 输出参数
  node_param: NodeParam         // 节点特定参数
}

/**
 * 工作流节点
 */
export interface WorkflowNode {
  id: string                    // 节点唯一ID
  type: NodeType                // 节点类型
  position: {
    x: number
    y: number
  }
  data: WorkflowNodeData        // 节点数据
  width?: number                // 节点宽度
  height?: number               // 节点高度
  parentId?: string             // 父节点ID（用于分组）
}

/**
 * 工作流边
 */
export interface WorkflowEdge {
  id: string                    // 边唯一ID
  source: string                // 源节点ID
  target: string                // 目标节点ID
  sourceHandle?: string         // 源连接点ID
  targetHandle?: string         // 目标连接点ID
  label?: string                // 边标签
  condition?: string            // 条件表达式
}

/**
 * 工作流图数据
 */
export interface WorkflowGraph {
  nodes: WorkflowNode[]
  edges: WorkflowEdge[]
}

// ==================== 节点Schema定义 ====================

/**
 * 节点Schema（节点模板定义）
 */
export interface NodeSchema {
  type: NodeType                // 节点类型
  title: string                 // 节点标题
  icon: string                  // 节点图标
  desc: string                  // 节点描述
  category: string              // 节点分类
  defaultParams: {
    input_params: InputParam[]
    output_params: OutputParam[]
    node_param: NodeParam
  }
  allowSingleTest: boolean      // 是否允许单独测试
  checkValid?: (data: WorkflowNodeData) => { valid: boolean; message?: string }
  getRefVariables?: (data: WorkflowNodeData) => VariableRef[]
}

// ==================== 变量系统 ====================

/**
 * 变量树项
 */
export interface VarTreeItem {
  key: string                   // 变量键（如：Start.name）
  label: string                 // 变量显示名称
  type: ParamType               // 变量类型
  nodeId: string                // 所属节点ID
  nodeType: NodeType            // 所属节点类型
  children?: VarTreeItem[]      // 子变量
}

/**
 * 变量引用解析结果
 */
export interface VariableRefInfo {
  nodeId: string                // 节点ID
  outputKey: string             // 输出键
  fullRef: string               // 完整引用（${nodeId.outputKey}）
}

// ==================== 执行相关 ====================

/**
 * 任务状态
 */
export type TaskStatus = 
  | 'pending'      // 待执行
  | 'running'      // 执行中
  | 'success'      // 成功
  | 'failed'       // 失败
  | 'timeout'      // 超时
  | 'skipped'      // 跳过

/**
 * 节点执行结果
 */
export interface NodeResult {
  node_id: string               // 节点ID
  status: TaskStatus            // 执行状态
  input: Record<string, any>    // 输入数据
  output?: Record<string, any>  // 输出数据
  error?: string                // 错误信息
  start_time: string            // 开始时间
  end_time?: string             // 结束时间
  duration?: number             // 执行时长（毫秒）
}

/**
 * 工作流执行结果
 */
export interface WorkflowExecution {
  task_id: string               // 任务ID
  workflow_id: string           // 工作流ID
  status: TaskStatus            // 整体状态
  node_results: NodeResult[]    // 节点执行结果
  start_time: string            // 开始时间
  end_time?: string             // 结束时间
  duration?: number             // 总执行时长
  error?: string                // 错误信息
}
