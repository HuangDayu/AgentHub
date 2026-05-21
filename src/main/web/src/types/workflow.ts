/**
 * 工作流系统类型定义
 * 参照 spring-ai-alibaba spark-flow 的设计
 */

import type { Node, Edge } from '@vue-flow/core'

// ==================== 值类型 ====================
export type ValueType =
  | 'String' | 'Number' | 'Boolean' | 'Object'
  | 'File' | 'Array<String>' | 'Array<Number>'
  | 'Array<Boolean>' | 'Array<Object>' | 'Array<File>'

// ==================== 任务/节点状态 ====================
export type TaskStatus =
  | 'pending' | 'running' | 'success' | 'failed'
  | 'skipped' | 'stopped' | 'timeout' | 'cancelled'

// ==================== 节点输入参数 ====================
export interface NodeInputParam {
  id?: string
  key: string
  name?: string
  type?: ValueType
  value?: string
  value_from?: 'refer' | 'input' | 'clear'
  required?: boolean
  desc?: string
  description?: string
}

// ==================== 节点输出参数 ====================
export interface NodeOutputParam {
  id?: string
  key: string
  name?: string
  type?: ValueType
  desc?: string
  description?: string
  required?: boolean
  properties?: NodeOutputParam[]
}

// ==================== 条件分支 ====================
export interface ConditionItem {
  id?: string
  operator?: string
  left: string
  right: string
  left_type?: ValueType
  right_type?: ValueType
}

export interface BranchItem {
  id: string
  label: string
  logic?: 'and' | 'or'
  conditions?: ConditionItem[]
}

// ==================== 节点数据 ====================
export interface WorkflowNodeData<T = any> {
  /** 节点显示名称 */
  label: string
  /** 节点描述 */
  desc?: string
  /** 输入参数 */
  input_params: NodeInputParam[]
  /** 输出参数 */
  output_params: NodeOutputParam[]
  /** 业务参数 */
  node_param: T
}

// ==================== 工作流节点 ====================
export interface WorkflowNode extends Node<WorkflowNodeData> {
  type: string
}

// ==================== 工作流边 ====================
// 注意: @vue-flow/core 的 Edge 是联合类型 (DefaultEdge | SmoothStepEdgeType | BezierEdgeType)
// 因此必须使用 type (交叉类型) 而非 interface (extends)
export type WorkflowEdge = Edge & {
  label?: string
  condition?: string
}

// ==================== 工作流图 ====================
export interface WorkflowGraph {
  nodes: WorkflowNode[]
  edges: WorkflowEdge[]
}

// ==================== 节点Schema ====================
export interface NodeSchema {
  /** 节点类型标识 */
  type: string
  /** 图标 */
  icon: string
  /** 标题 */
  title: string
  /** 描述 */
  desc: string
  /** 默认参数 */
  defaultParams: Omit<WorkflowNodeData, 'label'>
  /** 是否系统节点(禁止删除/复制) */
  isSystem?: boolean
  /** 是否允许单点测试 */
  allowSingleTest?: boolean
  /** 上游节点类型限制 */
  allowSourceNodeTypes?: string[]
  /** 下游节点类型限制 */
  allowTargetNodeTypes?: string[]
  /** 禁止连接上游 */
  disableConnectSource?: boolean
  /** 禁止连接下游 */
  disableConnectTarget?: boolean
  /** 默认高度 */
  defaultHeight?: number
  /** 节点分组 */
  groupLabel?: string
  /** 背景色 */
  bgColor: string
  /** 是否允许自定义添加 */
  customAdd?: boolean
  /** 是否在菜单中隐藏 */
  hideInMenu?: boolean
  /** 禁止配置 */
  notAllowConfig?: boolean
  /** 是否是组节点 */
  isGroup?: boolean
  /** 禁止在组节点中出现 */
  disableInGroup?: boolean
  /** 节点分类 */
  category?: string
  /** 校验函数 */
  checkValid?: (data: WorkflowNodeData) => { valid: boolean; message?: string }
  /** 获取引用的变量 */
  getRefVariables?: (data: WorkflowNodeData) => string[]
}

// ==================== 检查项 ====================
export interface CheckListItem {
  node_id: string
  node_type: string
  node_name: string
  error_msgs: { label: string; error: string }[]
}

// ==================== 变量树 ====================
export interface VarItem {
  label: string
  value: string
  type: ValueType
  children?: VarItem[]
}

export interface VarTreeItem {
  label: string
  value: string
  type: ValueType
  children?: VarTreeItem[]
  key?: string
  nodeId?: string
  nodeType?: string
  description?: string
}

// ==================== 变量引用类型 ====================

export type VariableRef = string

export interface VariableRefInfo {
  nodeId: string
  outputKey: string
  fullRef: string
}

// ==================== 节点Schema输入输出参数 ====================

/** 输入参数（用于节点Schema定义） */
export interface InputParam {
  key: string
  name: string
  type: string
  required?: boolean
  value?: any
  description?: string
}

/** 输出参数（用于节点Schema定义） */
export interface OutputParam {
  key: string
  name: string
  type: string
  description?: string
}

// ==================== 执行结果 ====================
export interface WorkflowTaskResultItem {
  node_type: string
  node_name: string
  node_id: string
  node_content: string | NodeInputParam[]
  node_status: TaskStatus
  parent_node_id?: string
}

export interface WorkflowNodeResultItem {
  is_batch: boolean
  retry?: {
    happened: boolean
    retry_times: number
  }
  input?: string
  output?: string
  usages?: {
    prompt_tokens: number
    completion_tokens: number
    total_tokens: number
  }[]
  node_id: string
  node_name: string
  node_type: string
  node_status: TaskStatus
  parent_node_id?: string
  output_type?: 'json' | 'text'
  node_exec_time?: string
  error_info?: string
}

export interface WorkflowTaskProcess {
  task_id: string
  conversation_id?: string
  request_id: string
  task_status: TaskStatus
  task_results: WorkflowTaskResultItem[]
  error_code?: string
  error_info?: string
  node_results: WorkflowNodeResultItem[]
}

// ==================== API响应类型 ====================
export interface Workflow {
  id: string
  tenantId: string
  workspaceId: string
  workflowCode: string
  name: string
  description: string
  graphDefinition: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface WorkflowExecution {
  task_id: string
  workflowId: string
  status: TaskStatus
  input?: Record<string, any>
  output?: Record<string, any>
  node_results: WorkflowNodeResultItem[]
  duration?: number
  startTime?: string
  endTime?: string
  errorMessage?: string
}

export interface NodeResult {
  node_id: string
  status: TaskStatus
  output?: Record<string, any>
  input?: Record<string, any>
  error?: string
  duration?: number
}

export interface SSEEvent {
  type: 'node_start' | 'node_complete' | 'node_error' | 'workflow_complete'
  data: {
    node_id: string
    node_name: string
    status: TaskStatus
    outputs?: Record<string, any>
    error_message?: string
    start_time?: string
    end_time?: string
    duration_ms?: number
  }
}

// ==================== 调试相关 ====================
export interface DebugInputParam {
  key: string
  name: string
  type: ValueType
  required: boolean
  description: string
  value?: string
}
