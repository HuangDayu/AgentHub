/**
 * 工作流节点Schema定义
 * 定义所有可用节点类型及其默认参数
 */

import type { NodeSchema, InputParam, OutputParam } from '@/types/workflow'

/**
 * 按照 @vue-flow/core 的节点类型常量
 */
export const NODE_TYPE = {
  START: 'start',
  END: 'end',
  LLM: 'llm',
  API: 'api',
  SCRIPT: 'script',
  RETRIEVAL: 'retrieval',
  CONDITION: 'condition',
  PARALLEL: 'parallel',
  PARALLEL_START: 'parallel-start',
  PARALLEL_END: 'parallel-end',
  LOOP: 'loop',
  LOOP_START: 'loop-start',
  LOOP_END: 'loop-end',
  VARIABLE: 'variable',
  CODE: 'code',
  TOOL: 'tool',
  SUB_WORKFLOW: 'sub-workflow',
  AGENT: 'agent',
  TASK: 'task',
  NOTIFICATION: 'notification',
  INPUT: 'input',
  OUTPUT: 'output',
}

// ==================== 节点Schema定义 ====================

const StartSchema: NodeSchema = {
  type: NODE_TYPE.START,
  icon: '▶',
  title: '开始',
  desc: '工作流的开始节点，定义输入参数',
  bgColor: '#52c41a',
  isSystem: true,
  groupLabel: '系统',
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'output', type: 'Object', desc: '开始节点输出' }
    ],
    node_param: {},
  },
}

const EndSchema: NodeSchema = {
  type: NODE_TYPE.END,
  icon: '⏹',
  title: '结束',
  desc: '工作流的结束节点，定义输出结果',
  bgColor: '#ff4d4f',
  isSystem: true,
  groupLabel: '系统',
  defaultParams: {
    input_params: [
      { key: 'input', type: 'Object', value_from: 'refer' }
    ],
    output_params: [],
    node_param: {},
  },
}

const LLMSchema: NodeSchema = {
  type: NODE_TYPE.LLM,
  icon: '🧠',
  title: 'LLM',
  desc: '调用大语言模型节点',
  bgColor: '#1677ff',
  allowSingleTest: true,
  groupLabel: 'AI',
  defaultParams: {
    input_params: [
      { key: 'prompt', type: 'String', name: '提示词', required: true, description: '发送给LLM的提示词模板，支持变量引用' },
    ],
    output_params: [
      { key: 'content', type: 'String', name: 'LLM回复', description: 'LLM返回的文本内容' },
      { key: 'success', type: 'Boolean', name: '是否成功', description: '调用是否成功' },
    ],
    node_param: {
      agentId: '',
      prompt: '',
      streaming: false,
    },
  },
}

const APISchema: NodeSchema = {
  type: NODE_TYPE.API,
  icon: '🔗',
  title: 'API调用',
  desc: '调用外部HTTP API',
  bgColor: '#fa8c16',
  allowSingleTest: true,
  groupLabel: '集成',
  defaultParams: {
    input_params: [
      { key: 'url', type: 'String', name: '请求URL', required: true, description: 'HTTP请求URL，支持变量引用' },
    ],
    output_params: [
      { key: 'success', type: 'Boolean', name: '是否成功', description: 'API调用是否成功' },
      { key: 'error', type: 'String', name: '错误信息', description: '调用失败时的错误信息' },
    ],
    node_param: {
      url: '',
      method: 'GET',
      body: {},
      timeoutMs: 30000,
    },
  },
}

const ScriptSchema: NodeSchema = {
  type: NODE_TYPE.SCRIPT,
  icon: '📜',
  title: '脚本',
  desc: '执行自定义脚本代码',
  bgColor: '#722ed1',
  allowSingleTest: true,
  groupLabel: '处理',
  defaultParams: {
    input_params: [
      { key: 'data', type: 'Object', name: '输入数据', description: '传递给脚本的上下文变量' },
    ],
    output_params: [
      { key: 'result', type: 'Object', name: '执行结果', description: '脚本执行返回的结果' },
      { key: 'success', type: 'Boolean', name: '是否成功' },
    ],
    node_param: {
      script: '',
    },
  },
}

const RetrievalSchema: NodeSchema = {
  type: NODE_TYPE.RETRIEVAL,
  icon: '📚',
  title: '知识检索',
  desc: '从知识库检索相关信息',
  bgColor: '#13c2c2',
  allowSingleTest: true,
  groupLabel: 'AI',
  defaultParams: {
    input_params: [
      { key: 'query', type: 'String', name: '检索查询', required: true, description: '检索的查询文本，支持变量引用' },
    ],
    output_params: [
      { key: 'documentCount', type: 'Number', name: '检索文档数', description: '检索到的文档数量' },
      { key: 'documents', type: 'Array<Object>', name: '文档列表', description: '检索到的文档列表' },
      { key: 'content', type: 'String', name: '拼接内容', description: '拼接模式下的文档内容' },
      { key: 'success', type: 'Boolean', name: '是否成功' },
    ],
    node_param: {
      knowledgeBaseId: '',
      query: '',
      topK: 5,
      scoreThreshold: 0.5,
      retrievalType: 'similarity',
      processMode: 'list',
      includeMetadata: false,
      includeScores: true,
      separator: '\n\n',
      outputVariable: 'retrievedDocs',
    },
  },
}

const ConditionSchema: NodeSchema = {
  type: NODE_TYPE.CONDITION,
  icon: '◇',
  title: '条件判断',
  desc: '根据条件分支执行不同路径',
  bgColor: '#fadb14',
  groupLabel: '流程控制',
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'selectedBranch', type: 'String', name: '选中分支', description: '匹配的条件分支名称' },
      { key: 'branchCount', type: 'Number', name: '分支数量' },
      { key: 'evaluated', type: 'Boolean', name: '已评估' },
    ],
    node_param: {
      branches: [] as any[],
    },
  },
}

const ParallelSchema: NodeSchema = {
  type: NODE_TYPE.PARALLEL,
  icon: '⫿',
  title: '并行分支',
  desc: '并行执行多个分支',
  bgColor: '#eb2f96',
  groupLabel: '流程控制',
  isGroup: true,
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'results', type: 'Array<Object>', name: '执行结果', description: '各分支的执行结果列表' },
      { key: 'totalNodes', type: 'Number', name: '节点总数' },
    ],
    node_param: {
      concurrency: 4,
    },
  },
}

const LoopSchema: NodeSchema = {
  type: NODE_TYPE.LOOP,
  icon: '↻',
  title: '循环',
  desc: '循环执行子节点',
  bgColor: '#2f54eb',
  groupLabel: '流程控制',
  isGroup: true,
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'results', type: 'Array<Object>', name: '循环结果', description: '每次迭代的执行结果' },
      { key: 'iterations', type: 'Number', name: '迭代次数' },
    ],
    node_param: {
      items: '',
      maxIterations: 100,
    },
  },
}

const VariableSchema: NodeSchema = {
  type: NODE_TYPE.VARIABLE,
  icon: '📋',
  title: '变量赋值',
  desc: '设置工作流变量',
  bgColor: '#08979c',
  groupLabel: '处理',
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'name', type: 'String', name: '变量名' },
      { key: 'value', type: 'Object', name: '变量值' },
    ],
    node_param: {
      assignments: [] as any[],
    },
  },
}

const CodeSchema: NodeSchema = {
  type: NODE_TYPE.CODE,
  icon: '💻',
  title: '代码执行',
  desc: '执行JavaScript代码片段',
  bgColor: '#531dab',
  allowSingleTest: true,
  groupLabel: '处理',
  defaultParams: {
    input_params: [
      { key: 'data', type: 'Object', name: '输入数据', description: '传递给脚本的上下文变量' },
    ],
    output_params: [
      { key: 'result', type: 'Object', name: '执行结果', description: '代码执行返回的结果' },
      { key: 'success', type: 'Boolean', name: '是否成功' },
    ],
    node_param: {
      script: '',
    },
  },
}

const ToolSchema: NodeSchema = {
  type: NODE_TYPE.TOOL,
  icon: '🔧',
  title: '工具调用',
  desc: '调用系统工具或MCP工具',
  bgColor: '#237804',
  allowSingleTest: true,
  groupLabel: '集成',
  customAdd: true,
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'result', type: 'Object', name: '工具输出', description: '工具执行返回的结果' },
      { key: 'toolName', type: 'String', name: '工具名称' },
      { key: 'success', type: 'Boolean', name: '是否成功' },
    ],
    node_param: {
      toolName: '',
      parameters: {},
    },
  },
}

const AgentSchema: NodeSchema = {
  type: NODE_TYPE.AGENT,
  icon: '🤖',
  title: 'Agent',
  desc: '调用子Agent执行任务，LLM节点实际使用agentId调用AgentPort',
  bgColor: '#0958d9',
  allowSingleTest: true,
  groupLabel: 'AI',
  defaultParams: {
    input_params: [
      { key: 'instruction', type: 'String', name: '指令', required: true, description: '发给Agent的指令' },
    ],
    output_params: [
      { key: 'response', type: 'String', name: '回复', description: 'Agent回复内容' },
    ],
    node_param: {
      agentId: '',
      prompt: '',
    },
  },
}

const NotificationSchema: NodeSchema = {
  type: NODE_TYPE.NOTIFICATION,
  icon: '🔔',
  title: '通知',
  desc: '发送通知消息',
  bgColor: '#cf1322',
  groupLabel: '系统',
  defaultParams: {
    input_params: [
      { key: 'message', type: 'String', value_from: 'input', desc: '通知内容' },
    ],
    output_params: [
      { key: 'sent', type: 'Boolean', desc: '是否发送成功' },
    ],
    node_param: {
      channel: 'email',
      recipients: [],
      title: '',
    },
  },
}

const InputSchema: NodeSchema = {
  type: NODE_TYPE.INPUT,
  icon: '📥',
  title: '用户输入',
  desc: '等待用户输入',
  bgColor: '#7cb305',
  groupLabel: '系统',
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'user_input', type: 'Object', desc: '用户输入值' },
    ],
    node_param: {
      prompt: '',
      input_type: 'text',
      required: true,
    },
  },
}

const OutputSchema: NodeSchema = {
  type: NODE_TYPE.OUTPUT,
  icon: '📤',
  title: '输出',
  desc: '输出信息给用户',
  bgColor: '#389e0d',
  groupLabel: '系统',
  defaultParams: {
    input_params: [
      { key: 'output_content', type: 'Object', value_from: 'refer' },
    ],
    output_params: [
      { key: 'response', type: 'Object', desc: '输出内容' },
    ],
    node_param: {
      output_type: 'text',
    },
  },
}

const SubWorkflowSchema: NodeSchema = {
  type: NODE_TYPE.SUB_WORKFLOW,
  icon: '🔀',
  title: '子工作流',
  desc: '调用另一个工作流作为子流程',
  bgColor: '#096dd9',
  allowSingleTest: true,
  groupLabel: '流程控制',
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'success', type: 'Boolean', name: '是否成功' },
      { key: 'executionId', type: 'String', name: '执行ID' },
      { key: 'output', type: 'Object', name: '子工作流输出' },
    ],
    node_param: {
      subWorkflowId: '',
      inputMapping: '',
      outputMapping: '',
      timeout: 300,
    },
  },
}

// ==================== Schema Map ====================

/**
 * 所有节点Schema Map
 * key为节点type
 */
export const NODE_SCHEMA_MAP: Record<string, NodeSchema> = {
  [NODE_TYPE.START]: StartSchema,
  [NODE_TYPE.END]: EndSchema,
  [NODE_TYPE.LLM]: LLMSchema,
  [NODE_TYPE.API]: APISchema,
  [NODE_TYPE.SCRIPT]: ScriptSchema,
  [NODE_TYPE.RETRIEVAL]: RetrievalSchema,
  [NODE_TYPE.CONDITION]: ConditionSchema,
  [NODE_TYPE.PARALLEL]: ParallelSchema,
  [NODE_TYPE.LOOP]: LoopSchema,
  [NODE_TYPE.VARIABLE]: VariableSchema,
  [NODE_TYPE.CODE]: CodeSchema,
  [NODE_TYPE.TOOL]: ToolSchema,
  [NODE_TYPE.AGENT]: AgentSchema,
  [NODE_TYPE.NOTIFICATION]: NotificationSchema,
  [NODE_TYPE.INPUT]: InputSchema,
  [NODE_TYPE.OUTPUT]: OutputSchema,
  [NODE_TYPE.SUB_WORKFLOW]: SubWorkflowSchema,
}

/**
 * 按分组获取节点Schema
 */
export function getNodeSchemasByCategory(): Record<string, NodeSchema[]> {
  const result: Record<string, NodeSchema[]> = {}
  for (const schema of Object.values(NODE_SCHEMA_MAP)) { if (schema.hideInMenu) continue; addSchemaToCategory(result, schema) }
  return result
}

function addSchemaToCategory(result: Record<string, NodeSchema[]>, schema: NodeSchema) {
  const group = schema.groupLabel || '其他'
  if (!result[group]) result[group] = []
  result[group].push(schema)
}

/**
 * 根据类型获取节点Schema
 */
export function getNodeSchema(type: string): NodeSchema | undefined {
  return NODE_SCHEMA_MAP[type]
}

/**
 * 生成唯一节点ID
 */
export function generateNodeId(type: string): string {
  return `${type}_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`
}

/**
 * 生成唯一边ID
 */
export function generateEdgeId(): string {
  return `edge_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`
}
