/**
 * 变量系统工具
 * 构建节点可用的变量树，用于变量引用
 */

import type { 
  WorkflowNode, 
  WorkflowEdge, 
  VarTreeItem, 
  VariableRef, 
  VariableRefInfo,
  OutputParam 
} from '@/types/workflow'

/**
 * 根据当前节点ID构建变量树
 * 查找所有上游节点的输出参数作为可用变量
 */
export function buildVariableTree(
  currentNodeId: string,
  nodes: WorkflowNode[],
  edges: WorkflowEdge[]
): VarTreeItem[] {
  // 找到当前节点的所有上游节点
  const upstreamNodes = findUpstreamNodes(currentNodeId, nodes, edges)

  const tree: VarTreeItem[] = []

  for (const node of upstreamNodes) {
    const outputParams = node.data?.output_params || []
    if (outputParams.length === 0 && !node.data?.node_param) continue

    const children: VarTreeItem[] = []

    for (const param of outputParams) {
      children.push({
        label: param.key,
        value: `\${${node.id}.${param.key}}`,
        type: param.type || 'String',
        desc: param.desc,
      })
    }

    tree.push({
      label: node.data?.label || node.id,
      value: node.id,
      type: 'Object',
      children: children.length > 0 ? children : undefined,
    })
  }

  // 添加全局变量
  tree.unshift({
    label: '全局变量',
    value: 'global',
    type: 'Object',
    children: [
      { label: '会话ID', value: '${global.session_id}', type: 'String' },
      { label: '用户ID', value: '${global.user_id}', type: 'String' },
      { label: '时间戳', value: '${global.timestamp}', type: 'Number' },
    ],
  })

  // 添加当前节点的输入参数（如果是开始节点）
  const currentNode = nodes.find(n => n.id === currentNodeId)
  if (currentNode?.type === 'start') {
    const inputParams = currentNode.data?.input_params || []
    if (inputParams.length > 0) {
      tree.unshift({
        label: '输入参数',
        value: 'input',
        type: 'Object',
        children: inputParams.map(p => ({
          label: p.key,
          value: `\${input.${p.key}}`,
          type: p.type || 'String',
        })),
      })
    }
  }

  return tree
}

/**
 * 查找节点的所有上游节点（包括间接上游）
 */
function findUpstreamNodes(
  nodeId: string,
  nodes: WorkflowNode[],
  edges: WorkflowEdge[]
): WorkflowNode[] {
  const visited = new Set<string>()
  const result: WorkflowNode[] = []

  function traverse(currentId: string) {
    const incomingEdges = edges.filter(e => e.target === currentId)
    for (const edge of incomingEdges) {
      if (!visited.has(edge.source)) {
        visited.add(edge.source)
        const sourceNode = nodes.find(n => n.id === edge.source)
        if (sourceNode) {
          result.push(sourceNode)
          traverse(edge.source)
        }
      }
    }
  }

  traverse(nodeId)
  return result
}

/**
 * 从文本中提取变量引用 ${nodeId.paramKey}
 */
export function extractVariables(text: string): string[] {
  const regex = /\$\{(.*?)\}/g
  const matches = text.match(regex)
  if (!matches) return []

  return matches.map(m => m.substring(2, m.length - 1))
}

/**
 * 解析变量引用，返回 { nodeId, paramKey }
 */
export function parseVariableRef(ref: string): { nodeId: string; paramKey: string } | null {
  const parts = ref.split('.')
  if (parts.length !== 2) return null
  return { nodeId: parts[0], paramKey: parts[1] }
}

/**
 * 生成不重复的名称
 */
export function generateUniqueName(name: string, existingNames: string[]): string {
  const set = new Set(existingNames)
  let index = 1
  let uniqueName = `${name}${index}`

  while (set.has(uniqueName)) {
    index++
    uniqueName = `${name}${index}`
  }

  return uniqueName
}
