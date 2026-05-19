/**
 * 节点Schema定义
 * 定义所有可用节点的模板和默认配置
 */

import type { NodeSchema, InputParam, OutputParam } from '@/types/workflow-node'

// ==================== 默认配置 ====================

const DEFAULT_MODEL_CONFIG = {
  temperature: 0.7,
  max_tokens: 2000,
  top_p: 0.9,
  frequency_penalty: 0,
  presence_penalty: 0
}

const DEFAULT_LLM_OUTPUT: OutputParam[] = [
  { key: 'output', name: '模型输出', type: 'string', description: '大模型生成的文本内容' },
  { key: 'tokens', name: 'Token数', type: 'number', description: '使用的token数量' }
]

const DEFAULT_API_OUTPUT: OutputParam[] = [
  { key: 'response', name: '响应数据', type: 'object', description: 'API响应数据' },
  { key: 'status', name: '状态码', type: 'number', description: 'HTTP状态码' }
]

const DEFAULT_RETRIEVAL_OUTPUT: OutputParam[] = [
  { key: 'documents', name: '检索结果', type: 'array', description: '检索到的文档列表' },
  { key: 'scores', name: '相似度分数', type: 'array', description: '每个文档的相似度分数' }
]

// ==================== 节点Schema定义 ====================

/**
 * 开始节点Schema
 */
export const StartSchema: NodeSchema = {
  type: 'start',
  title: '开始',
  icon: '▶',
  desc: '工作流的开始节点，定义工作流的输入参数',
  category: '基础',
  defaultParams: {
    input_params: [],
    output_params: [],
    node_param: {
      input_params: []
    }
  },
  allowSingleTest: false
}

/**
 * 结束节点Schema
 */
export const EndSchema: NodeSchema = {
  type: 'end',
  title: '结束',
  icon: '⏹',
  desc: '工作流的结束节点，定义工作流的输出参数',
  category: '基础',
  defaultParams: {
    input_params: [],
    output_params: [],
    node_param: {
      output_params: []
    }
  },
  allowSingleTest: false
}

/**
 * LLM节点Schema
 */
export const LLMSchema: NodeSchema = {
  type: 'llm',
  title: '大模型',
  icon: '🤖',
  desc: '调用大语言模型，根据提示词生成内容',
  category: 'AI',
  defaultParams: {
    input_params: [
      { key: 'sys_prompt', name: '系统提示词', type: 'string', required: false, value: '' },
      { key: 'user_prompt', name: '用户提示词', type: 'string', required: true, value: '' }
    ],
    output_params: DEFAULT_LLM_OUTPUT,
    node_param: {
      model_id: '',
      model_config: DEFAULT_MODEL_CONFIG,
      sys_prompt: '',
      user_prompt: '',
      output_key: 'llm_output'
    }
  },
  allowSingleTest: true,
  checkValid: (data) => {
    const param = data.node_param as any
    if (!param.model_id) {
      return { valid: false, message: '请选择模型' }
    }
    if (!param.user_prompt) {
      return { valid: false, message: '请输入用户提示词' }
    }
    return { valid: true }
  },
  getRefVariables: (data) => {
    const param = data.node_param as any
    const vars: string[] = []
    const regex = /\$\{([^}]+)\}/g
    let match
    while ((match = regex.exec(param.sys_prompt + param.user_prompt)) !== null) {
      vars.push(match[0])
    }
    return vars
  }
}

/**
 * API节点Schema
 */
export const APISchema: NodeSchema = {
  type: 'api',
  title: 'API调用',
  icon: '🔗',
  desc: '调用外部API接口',
  category: '集成',
  defaultParams: {
    input_params: [
      { key: 'url', name: 'API地址', type: 'string', required: true, value: '' },
      { key: 'body', name: '请求体', type: 'object', required: false, value: {} }
    ],
    output_params: DEFAULT_API_OUTPUT,
    node_param: {
      method: 'POST',
      url: '',
      headers: { 'Content-Type': 'application/json' },
      body: '',
      timeout: 30000,
      output_key: 'api_response'
    }
  },
  allowSingleTest: true,
  checkValid: (data) => {
    const param = data.node_param as any
    if (!param.url) {
      return { valid: false, message: '请输入API地址' }
    }
    return { valid: true }
  }
}

/**
 * 脚本节点Schema
 */
export const ScriptSchema: NodeSchema = {
  type: 'script',
  title: '脚本',
  icon: '📝',
  desc: '执行自定义脚本代码',
  category: '逻辑',
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'result', name: '执行结果', type: 'any', description: '脚本执行返回的结果' }
    ],
    node_param: {
      language: 'javascript',
      code: '// 在此编写脚本代码\nreturn { result: "Hello" }',
      output_key: 'script_result'
    }
  },
  allowSingleTest: true,
  checkValid: (data) => {
    const param = data.node_param as any
    if (!param.code) {
      return { valid: false, message: '请编写脚本代码' }
    }
    return { valid: true }
  }
}

/**
 * 知识检索节点Schema
 */
export const RetrievalSchema: NodeSchema = {
  type: 'retrieval',
  title: '知识检索',
  icon: '🔍',
  desc: '从知识库中检索相关信息',
  category: 'AI',
  defaultParams: {
    input_params: [
      { key: 'query', name: '查询文本', type: 'string', required: true, value: '' }
    ],
    output_params: DEFAULT_RETRIEVAL_OUTPUT,
    node_param: {
      knowledge_base_id: '',
      query: '',
      top_k: 5,
      score_threshold: 0.7,
      output_key: 'retrieval_result'
    }
  },
  allowSingleTest: true,
  checkValid: (data) => {
    const param = data.node_param as any
    if (!param.knowledge_base_id) {
      return { valid: false, message: '请选择知识库' }
    }
    if (!param.query) {
      return { valid: false, message: '请输入查询文本' }
    }
    return { valid: true }
  }
}

/**
 * 条件节点Schema
 */
export const ConditionSchema: NodeSchema = {
  type: 'condition',
  title: '条件判断',
  icon: '◇',
  desc: '根据条件表达式进行分支判断',
  category: '逻辑',
  defaultParams: {
    input_params: [],
    output_params: [],
    node_param: {
      conditions: [
        { expression: '', label: '分支1' },
        { expression: '', label: '分支2' }
      ],
      default_branch: 'default'
    }
  },
  allowSingleTest: false,
  checkValid: (data) => {
    const param = data.node_param as any
    if (!param.conditions || param.conditions.length === 0) {
      return { valid: false, message: '请至少定义一个条件分支' }
    }
    return { valid: true }
  }
}

/**
 * 任务节点Schema
 */
export const TaskSchema: NodeSchema = {
  type: 'task',
  title: '任务',
  icon: '⚙',
  desc: '执行一个任务或操作',
  category: '基础',
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'result', name: '任务结果', type: 'any', description: '任务执行结果' }
    ],
    node_param: {
      action: '',
      parameters: {},
      output_key: 'task_result'
    }
  },
  allowSingleTest: true,
  checkValid: (data) => {
    const param = data.node_param as any
    if (!param.action) {
      return { valid: false, message: '请指定任务动作' }
    }
    return { valid: true }
  }
}

/**
 * 循环节点Schema
 */
export const LoopSchema: NodeSchema = {
  type: 'loop',
  title: '循环',
  icon: '↻',
  desc: '循环执行任务直到满足条件',
  category: '逻辑',
  defaultParams: {
    input_params: [],
    output_params: [],
    node_param: {
      max_iterations: 10,
      condition: '',
      iteration_var: 'item'
    }
  },
  allowSingleTest: false
}

/**
 * 并行节点Schema
 */
export const ParallelSchema: NodeSchema = {
  type: 'parallel',
  title: '并行',
  icon: '⫿',
  desc: '并行执行多个分支',
  category: '逻辑',
  defaultParams: {
    input_params: [],
    output_params: [],
    node_param: {
      branches: [],
      wait_all: true
    }
  },
  allowSingleTest: false
}

/**
 * 迭代器节点Schema
 */
export const IteratorSchema: NodeSchema = {
  type: 'iterator',
  title: '迭代器',
  icon: '🔄',
  desc: '遍历数组或集合中的每个元素',
  category: '逻辑',
  defaultParams: {
    input_params: [
      { key: 'input_array', name: '输入数组', type: 'array', required: true, value: [] }
    ],
    output_params: [],
    node_param: {
      input_array: '',
      item_var: 'item',
      index_var: 'index'
    }
  },
  allowSingleTest: false
}

/**
 * 变量处理节点Schema
 */
export const VariableSchema: NodeSchema = {
  type: 'variable',
  title: '变量处理',
  icon: '📦',
  desc: '设置、获取或删除变量',
  category: '数据',
  defaultParams: {
    input_params: [],
    output_params: [],
    node_param: {
      operations: []
    }
  },
  allowSingleTest: false
}

/**
 * 输入节点Schema
 */
export const InputSchema: NodeSchema = {
  type: 'input',
  title: '输入',
  icon: '📥',
  desc: '接收用户输入',
  category: '交互',
  defaultParams: {
    input_params: [],
    output_params: [
      { key: 'value', name: '输入值', type: 'any', description: '用户输入的值' }
    ],
    node_param: {
      input_type: 'text',
      label: '请输入',
      required: true
    }
  },
  allowSingleTest: false
}

/**
 * 输出节点Schema
 */
export const OutputSchema: NodeSchema = {
  type: 'output',
  title: '输出',
  icon: '📤',
  desc: '输出结果给用户',
  category: '交互',
  defaultParams: {
    input_params: [
      { key: 'value', name: '输出值', type: 'any', required: true, value: '' }
    ],
    output_params: [],
    node_param: {
      output_type: 'text',
      value: ''
    }
  },
  allowSingleTest: false
}

// ==================== Schema映射表 ====================

/**
 * 所有节点Schema的映射表
 */
export const NODE_SCHEMA_MAP: Record<string, NodeSchema> = {
  start: StartSchema,
  end: EndSchema,
  llm: LLMSchema,
  api: APISchema,
  script: ScriptSchema,
  retrieval: RetrievalSchema,
  condition: ConditionSchema,
  task: TaskSchema,
  loop: LoopSchema,
  parallel: ParallelSchema,
  iterator: IteratorSchema,
  variable: VariableSchema,
  input: InputSchema,
  output: OutputSchema
}

/**
 * 获取节点Schema
 */
export function getNodeSchema(type: string): NodeSchema | undefined {
  return NODE_SCHEMA_MAP[type]
}

/**
 * 获取所有节点Schema列表
 */
export function getAllNodeSchemas(): NodeSchema[] {
  return Object.values(NODE_SCHEMA_MAP)
}

/**
 * 按分类获取节点Schema
 */
export function getNodeSchemasByCategory(): Record<string, NodeSchema[]> {
  const result: Record<string, NodeSchema[]> = {}
  
  for (const schema of Object.values(NODE_SCHEMA_MAP)) {
    if (!result[schema.category]) {
      result[schema.category] = []
    }
    result[schema.category].push(schema)
  }
  
  return result
}
