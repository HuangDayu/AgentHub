/**
 * 变量引用系统
 * 处理工作流中的变量引用、解析和替换
 */

import type { 
  WorkflowNode, 
  WorkflowEdge, 
  VarTreeItem, 
  VariableRef, 
  VariableRefInfo,
  OutputParam 
} from '@/types/workflow-node'

// ==================== 变量引用解析 ====================

/**
 * 变量引用正则表达式
 * 匹配格式：${nodeId.outputKey}
 */
const VARIABLE_REF_REGEX = /\$\{([^}]+)\}/g

/**
 * 解析变量引用
 * @param ref 变量引用字符串，如：${Start.name}
 * @returns 解析结果
 */
export function parseVariableRef(ref: string): VariableRefInfo | null {
  const match = ref.match(/^\$\{(.+)\}$/)
  if (!match) return null
  
  const parts = match[1].split('.')
  if (parts.length < 2) return null
  
  return {
    nodeId: parts[0],
    outputKey: parts.slice(1).join('.'),
    fullRef: ref
  }
}

/**
 * 从文本中提取所有变量引用
 * @param text 包含变量引用的文本
 * @returns 变量引用列表
 */
export function extractVariableRefs(text: string): VariableRef[] {
  const refs: VariableRef[] = []
  let match
  
  while ((match = VARIABLE_REF_REGEX.exec(text)) !== null) {
    refs.push(match[0])
  }
  
  // 重置正则索引
  VARIABLE_REF_REGEX.lastIndex = 0
  
  return refs
}

/**
 * 检查文本是否包含变量引用
 * @param text 文本
 * @returns 是否包含变量引用
 */
export function hasVariableRef(text: string): boolean {
  return VARIABLE_REF_REGEX.test(text)
}

// ==================== 变量树构建 ====================

/**
 * 获取节点的上游节点列表
 * @param nodeId 当前节点ID
 * @param nodes 所有节点
 * @param edges 所有边
 * @returns 上游节点列表（按拓扑顺序）
 */
export function getUpstreamNodes(
  nodeId: string,
  nodes: WorkflowNode[],
  edges: WorkflowEdge[]
): WorkflowNode[] {
  const upstreamNodes: WorkflowNode[] = []
  const visited = new Set<string>()
  
  function traverse(id: string) {
    if (visited.has(id)) return
    visited.add(id)
    
    // 找到所有指向当前节点的边
    const incomingEdges = edges.filter(e => e.target === id)
    
    for (const edge of incomingEdges) {
      const sourceNode = nodes.find(n => n.id === edge.source)
      if (sourceNode && !upstreamNodes.includes(sourceNode)) {
        upstreamNodes.push(sourceNode)
        traverse(edge.source)
      }
    }
  }
  
  traverse(nodeId)
  
  return upstreamNodes
}

/**
 * 构建变量树
 * @param currentNodeId 当前节点ID
 * @param nodes 所有节点
 * @param edges 所有边
 * @returns 变量树
 */
export function buildVariableTree(
  currentNodeId: string,
  nodes: WorkflowNode[],
  edges: WorkflowEdge[]
): VarTreeItem[] {
  const upstreamNodes = getUpstreamNodes(currentNodeId, nodes, edges)
  const varTree: VarTreeItem[] = []
  
  for (const node of upstreamNodes) {
    const nodeVars = buildNodeVariables(node)
    varTree.push(...nodeVars)
  }
  
  return varTree
}

/**
 * 构建单个节点的变量列表
 * @param node 节点
 * @returns 变量列表
 */
export function buildNodeVariables(node: WorkflowNode): VarTreeItem[] {
  const vars: VarTreeItem[] = []
  
  // 添加节点的输出参数作为变量
  for (const output of node.data.output_params) {
    vars.push({
      key: `${node.id}.${output.key}`,
      label: `${node.data.label}.${output.name}`,
      type: output.type,
      nodeId: node.id,
      nodeType: node.type,
      description: output.description
    })
  }
  
  // 特殊处理：某些节点有额外的输出变量
  if (node.type === 'llm') {
    // LLM节点额外添加完整输出变量
    vars.push({
      key: `${node.id}.full_response`,
      label: `${node.data.label}.完整响应`,
      type: 'object',
      nodeId: node.id,
      nodeType: node.type
    })
  }
  
  return vars
}

// ==================== 变量值替换 ====================

/**
 * 替换文本中的变量引用为实际值
 * @param text 包含变量引用的文本
 * @param values 变量值映射
 * @returns 替换后的文本
 */
export function replaceVariableRefs(
  text: string,
  values: Record<string, any>
): string {
  return text.replace(VARIABLE_REF_REGEX, (match) => {
    const refInfo = parseVariableRef(match)
    if (!refInfo) return match
    
    const value = values[refInfo.nodeId]?.[refInfo.outputKey]
    if (value === undefined) return match
    
    // 如果值是对象或数组，转为JSON字符串
    if (typeof value === 'object') {
      return JSON.stringify(value)
    }
    
    return String(value)
  })
}

/**
 * 递归替换对象中的所有变量引用
 * @param obj 对象
 * @param values 变量值映射
 * @returns 替换后的对象
 */
export function replaceVariableRefsDeep(
  obj: any,
  values: Record<string, any>
): any {
  if (typeof obj === 'string') {
    return replaceVariableRefs(obj, values)
  }
  
  if (Array.isArray(obj)) {
    return obj.map(item => replaceVariableRefsDeep(item, values))
  }
  
  if (typeof obj === 'object' && obj !== null) {
    const result: Record<string, any> = {}
    for (const [key, value] of Object.entries(obj)) {
      result[key] = replaceVariableRefsDeep(value, values)
    }
    return result
  }
  
  return obj
}

// ==================== 变量验证 ====================

/**
 * 验证变量引用是否有效
 * @param ref 变量引用
 * @param availableVars 可用变量列表
 * @returns 是否有效
 */
export function validateVariableRef(
  ref: string,
  availableVars: VarTreeItem[]
): boolean {
  const refInfo = parseVariableRef(ref)
  if (!refInfo) return false
  
  return availableVars.some(v => v.key === `${refInfo.nodeId}.${refInfo.outputKey}`)
}

/**
 * 验证文本中的所有变量引用
 * @param text 文本
 * @param availableVars 可用变量列表
 * @returns 验证结果
 */
export function validateVariableRefs(
  text: string,
  availableVars: VarTreeItem[]
): { valid: boolean; invalidRefs: string[] } {
  const refs = extractVariableRefs(text)
  const invalidRefs: string[] = []
  
  for (const ref of refs) {
    if (!validateVariableRef(ref, availableVars)) {
      invalidRefs.push(ref)
    }
  }
  
  return {
    valid: invalidRefs.length === 0,
    invalidRefs
  }
}

// ==================== 变量提示 ====================

/**
 * 获取变量引用的提示信息
 * @param ref 变量引用
 * @param availableVars 可用变量列表
 * @returns 提示信息
 */
export function getVariableHint(
  ref: string,
  availableVars: VarTreeItem[]
): string {
  const refInfo = parseVariableRef(ref)
  if (!refInfo) return '无效的变量引用格式'
  
  const varItem = availableVars.find(
    v => v.key === `${refInfo.nodeId}.${refInfo.outputKey}`
  )
  
  if (!varItem) {
    return `未找到变量：${ref}`
  }
  
  return `${varItem.label} (${varItem.type})${varItem.description ? ` - ${varItem.description}` : ''}`
}

/**
 * 搜索匹配的变量
 * @param query 搜索关键词
 * @param availableVars 可用变量列表
 * @returns 匹配的变量列表
 */
export function searchVariables(
  query: string,
  availableVars: VarTreeItem[]
): VarTreeItem[] {
  const lowerQuery = query.toLowerCase()
  
  return availableVars.filter(v => 
    v.key.toLowerCase().includes(lowerQuery) ||
    v.label.toLowerCase().includes(lowerQuery)
  )
}

// ==================== 工具函数 ====================

/**
 * 创建变量引用字符串
 * @param nodeId 节点ID
 * @param outputKey 输出键
 * @returns 变量引用字符串
 */
export function createVariableRef(nodeId: string, outputKey: string): VariableRef {
  return `\${${nodeId}.${outputKey}}`
}

/**
 * 格式化变量树为可读文本
 * @param varTree 变量树
 * @returns 格式化文本
 */
export function formatVariableTree(varTree: VarTreeItem[]): string {
  return varTree.map(v => `${v.key}: ${v.label} (${v.type})`).join('\n')
}
