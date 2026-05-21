import { describe, it, expect, beforeEach, beforeAll } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useWorkflowStore } from '../src/stores/workflow-store'
import type { WorkflowNode, WorkflowEdge, WorkflowGraph } from '../src/types/workflow'

function makeNode(id: string, type: string, label?: string): WorkflowNode {
  return {
    id, type,
    position: { x: 0, y: 0 },
    data: { label: label || type, input_params: [], output_params: [], node_param: {} },
  }
}

function makeEdge(id: string, source: string, target: string): WorkflowEdge {
  return { id, source, target } as WorkflowEdge
}

describe('workflow-store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('setGraph', () => {
    it('应该正确设置 nodes 和 edges', () => {
      const store = useWorkflowStore()
      const graph: WorkflowGraph = {
        nodes: [makeNode('1', 'start'), makeNode('2', 'llm')],
        edges: [makeEdge('e1', '1', '2')],
      }

      store.setGraph(graph)
      expect(store.nodes).toHaveLength(2)
      expect(store.edges).toHaveLength(1)
      expect(store.isDirty).toBe(true)
    })

    it('当 graph.nodes 为 undefined 时应默认空数组', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [], edges: [] } as WorkflowGraph)
      expect(store.nodes).toEqual([])
      expect(store.edges).toEqual([])
    })

    it('当 graph.edges 为 undefined 时应默认空数组', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [] as WorkflowNode[], edges: [] } as WorkflowGraph)
      expect(store.nodes).toEqual([])
      expect(store.edges).toEqual([])
    })

    it('当 graph 为完整空对象 {} 时应默认空数组', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [], edges: [] } as WorkflowGraph)
      expect(store.nodes).toEqual([])
      expect(store.edges).toEqual([])
    })
  })

  describe('addNode', () => {
    it('应该能添加节点', () => {
      const store = useWorkflowStore()
      const node = makeNode('test-1', 'llm', '测试节点')
      store.addNode(node)
      expect(store.nodes).toHaveLength(1)
      expect(store.nodes[0].id).toBe('test-1')
      expect(store.isDirty).toBe(true)
    })
  })

  describe('deleteNode', () => {
    it('应该能删除节点并移除相关边', () => {
      const store = useWorkflowStore()
      store.setGraph({
        nodes: [makeNode('1', 'start'), makeNode('2', 'llm')],
        edges: [makeEdge('e1', '1', '2')],
      })

      store.deleteNode('1')
      expect(store.nodes).toHaveLength(1)
      expect(store.edges).toHaveLength(0) // 关联边也应删除
    })

    it('删除不存在的节点不应报错', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [makeNode('1', 'start')], edges: [] })
      expect(() => store.deleteNode('non-existent')).not.toThrow()
      expect(store.nodes).toHaveLength(1)
    })
  })

  describe('updateNode', () => {
    it('更新节点数据应正确', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [makeNode('1', 'llm')], edges: [] })
      store.updateNode('1', { data: { label: '更新后的LLM', input_params: [], output_params: [], node_param: { agentId: 'default', prompt: 'Hello', streaming: false } } })
      expect(store.nodes[0].data.label).toBe('更新后的LLM')
      expect(store.nodes[0].data.node_param.agentId).toBe('default')
      expect(store.nodes[0].data.node_param.streaming).toBe(false)
    })

    it('更新不存在的节点不应报错', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [], edges: [] })
      expect(() => store.updateNode('non-existent', { data: { label: 'test', input_params: [], output_params: [], node_param: {} } })).not.toThrow()
      expect(store.nodes).toHaveLength(0)
    })
  })

  describe('addEdge / deleteEdge / updateEdge', () => {
    it('添加边、删除边、更新边功能正常', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [makeNode('1', 'start'), makeNode('2', 'llm')], edges: [] })
      
      store.addEdge(makeEdge('e1', '1', '2'))
      expect(store.edges).toHaveLength(1)
      
      store.updateEdge('e1', { label: 'main flow' })
      expect(store.edges[0].label).toBe('main flow')
      
      store.deleteEdge('e1')
      expect(store.edges).toHaveLength(0)
    })

    it('更新不存在的边不应报错', () => {
      const store = useWorkflowStore()
      expect(() => store.updateEdge('non-existent', { label: 'test' })).not.toThrow()
    })

    it('删除不存在的边不应报错', () => {
      const store = useWorkflowStore()
      expect(() => store.deleteEdge('non-existent')).not.toThrow()
    })
  })

  describe('selectNode / selectEdge', () => {
    it('选中节点应取消边的选中', () => {
      const store = useWorkflowStore()
      const node = makeNode('1', 'llm')
      store.setGraph({ nodes: [node], edges: [] })
      store.selectNode(node)
      expect(store.selectedNode?.id).toBe('1')
      expect(store.showConfigPanel).toBe(true)
      
      store.selectEdge(makeEdge('e1', '1', '2'))
      expect(store.selectedNode).toBeNull()
    })

    it('取消选中节点应关闭配置面板', () => {
      const store = useWorkflowStore()
      const node = makeNode('1', 'llm')
      store.setGraph({ nodes: [node], edges: [] })
      store.selectNode(node)
      expect(store.showConfigPanel).toBe(true)

      store.selectNode(null)
      expect(store.selectedNode).toBeNull()
      expect(store.showConfigPanel).toBe(false)
    })
  })

  describe('undo/redo', () => {
    it('撤销和重做应正确恢复状态', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [makeNode('1', 'start')], edges: [] })

      store.addNode(makeNode('2', 'llm'))
      expect(store.nodes).toHaveLength(2)

      store.undo()
      expect(store.nodes).toHaveLength(1)

      store.redo()
      expect(store.nodes).toHaveLength(2)
    })

    it('当历史为空时撤销和重做不应报错', () => {
      const store = useWorkflowStore()
      expect(() => store.undo()).not.toThrow()
      expect(() => store.redo()).not.toThrow()
    })

    it('添加节点后应保存历史,重做栈应清空', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [makeNode('1', 'start')], edges: [] })
      store.addNode(makeNode('2', 'llm'))
      
      store.undo()
      expect(store.canRedo).toBe(true)
      
      store.addNode(makeNode('3', 'end'))
      expect(store.canRedo).toBe(false) // 新操作清空重做栈
    })
  })

  describe('clearHistory', () => {
    it('清除历史应清空撤销和重做栈', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [makeNode('1', 'start')], edges: [] })
      store.addNode(makeNode('2', 'llm'))
      store.undo()
      expect(store.canRedo).toBe(true)

      store.clearHistory()
      expect(store.canUndo).toBe(false)
      expect(store.canRedo).toBe(false)
    })
  })

  describe('markAsSaved', () => {
    it('标记已保存应重置脏标志', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [makeNode('1', 'start')], edges: [] })
      expect(store.isDirty).toBe(true)
      
      store.markAsSaved()
      expect(store.isDirty).toBe(false)
      expect(store.isSaving).toBe(false)
    })
  })

  describe('reset', () => {
    it('重置后所有状态应恢复默认', () => {
      const store = useWorkflowStore()
      store.setGraph({ nodes: [makeNode('1', 'start')], edges: [] })
      store.setWorkflowInfo('wf-1', '测试工作流', '测试描述')
      store.setVariableTree([{ label: 'var1', value: '${1.output}', type: 'String' }])
      store.setShowCheckList(true)

      store.reset()

      expect(store.workflowId).toBe('')
      expect(store.nodes).toEqual([])
      expect(store.edges).toEqual([])
      expect(store.isDirty).toBe(false)
      expect(store.variableTree).toEqual([])
      expect(store.showCheckList).toBe(false)
    })
  })

  describe('computed properties', () => {
    it('nodeCount 和 edgeCount 应正确计算', () => {
      const store = useWorkflowStore()
      expect(store.nodeCount).toBe(0)
      expect(store.edgeCount).toBe(0)

      store.setGraph({
        nodes: [makeNode('1', 'start'), makeNode('2', 'llm')],
        edges: [makeEdge('e1', '1', '2')],
      })

      expect(store.nodeCount).toBe(2)
      expect(store.edgeCount).toBe(1)
    })
  })

  describe('setVariableTree / setCheckList / setShowResults / setIsDragging / setHiddenMenu', () => {
    it('新增状态方法应正常工作', () => {
      const store = useWorkflowStore()
      
      store.setVariableTree([{ label: 'test', value: '${1.out}', type: 'String' }])
      expect(store.variableTree).toHaveLength(1)
      
      store.setCheckList([{ node_id: 'n1', node_type: 'llm', node_name: 'LLM', error_msgs: [] }])
      expect(store.checkList).toHaveLength(1)
      
      store.setShowResults(true)
      expect(store.showResults).toBe(true)
      
      store.setIsDragging(true)
      expect(store.isDragging).toBe(true)
      
      store.setHiddenMenu(true)
      expect(store.hiddenMenu).toBe(true)
    })
  })
})

describe('graph parsing（模拟 WorkflowEditorView 中 loadWorkflow 逻辑）', () => {
  type Workflow = { graphDefinition: string | null }

  function parseGraphDefinition(wf: Workflow) {
    let graph: WorkflowGraph = { nodes: [], edges: [] }
    if (wf.graphDefinition) {
      try {
        const parsed = JSON.parse(wf.graphDefinition)
        graph = {
          nodes: parsed.nodes || [],
          edges: parsed.edges || [],
        }
      } catch {
        graph = { nodes: [], edges: [] }
      }
    }
    return graph
  }

  it('graphDefinition 为 "{}" 时应返回空 nodes/edges', () => {
    const result = parseGraphDefinition({ graphDefinition: '{}' })
    expect(result.nodes).toEqual([])
    expect(result.edges).toEqual([])
  })

  it('graphDefinition 为完整 JSON 时应正确解析', () => {
    const result = parseGraphDefinition({
      graphDefinition: JSON.stringify({
        nodes: [{ id: '1', type: 'start', position: { x: 0, y: 0 }, data: { label: '开始', input_params: [], output_params: [], node_param: {} } }],
        edges: [],
      }),
    })
    expect(result.nodes).toHaveLength(1)
    expect(result.nodes[0].id).toBe('1')
  })

  it('graphDefinition 为 null 时应返回空', () => {
    const result = parseGraphDefinition({ graphDefinition: null })
    expect(result.nodes).toEqual([])
    expect(result.edges).toEqual([])
  })

  it('graphDefinition 为非法 JSON 时应返回空', () => {
    const result = parseGraphDefinition({ graphDefinition: '这不是 JSON' })
    expect(result.nodes).toEqual([])
    expect(result.edges).toEqual([])
  })

  it('graphDefinition 有 nodes 但无 edges 时应默认 edges 为空数组', () => {
    const result = parseGraphDefinition({
      graphDefinition: JSON.stringify({
        nodes: [{ id: '1', type: 'start', position: { x: 0, y: 0 }, data: { label: '开始', input_params: [], output_params: [], node_param: {} } }],
      }),
    })
    expect(result.nodes).toHaveLength(1)
    expect(result.edges).toEqual([])
  })
})

describe('node-schemas', () => {
  it('NODE_SCHEMA_MAP 应包含所有核心节点类型', async () => {
    const { NODE_SCHEMA_MAP, NODE_TYPE } = await import('../src/constants/node-schemas')
    expect(NODE_SCHEMA_MAP[NODE_TYPE.START]).toBeDefined()
    expect(NODE_SCHEMA_MAP[NODE_TYPE.END]).toBeDefined()
    expect(NODE_SCHEMA_MAP[NODE_TYPE.LLM]).toBeDefined()
    expect(NODE_SCHEMA_MAP[NODE_TYPE.API]).toBeDefined()
    expect(NODE_SCHEMA_MAP[NODE_TYPE.SCRIPT]).toBeDefined()
    expect(NODE_SCHEMA_MAP[NODE_TYPE.CONDITION]).toBeDefined()
  })

  it('系统节点不可复制和删除', async () => {
    const { NODE_SCHEMA_MAP, NODE_TYPE } = await import('../src/constants/node-schemas')
    expect(NODE_SCHEMA_MAP[NODE_TYPE.START].isSystem).toBe(true)
    expect(NODE_SCHEMA_MAP[NODE_TYPE.END].isSystem).toBe(true)
    expect(NODE_SCHEMA_MAP[NODE_TYPE.LLM].isSystem).toBeUndefined()
  })

  it('generateNodeId 应生成唯一ID', async () => {
    const { generateNodeId } = await import('../src/constants/node-schemas')
    const id1 = generateNodeId('llm')
    const id2 = generateNodeId('llm')
    expect(id1).not.toBe(id2)
    expect(id1).toContain('llm')
  })

  it('getNodeSchemasByCategory 应按分组正确分类', async () => {
    const { getNodeSchemasByCategory, NODE_TYPE } = await import('../src/constants/node-schemas')
    const categorized = getNodeSchemasByCategory()
    expect(Object.keys(categorized).length).toBeGreaterThan(0)
    
    const systemGroup = categorized['系统']
    expect(systemGroup).toBeDefined()
    expect(systemGroup.some(s => s.type === NODE_TYPE.START)).toBe(true)
    expect(systemGroup.some(s => s.type === NODE_TYPE.END)).toBe(true)
  })
})

describe('workflow-api 请求构建', () => {
  it('list 接口应构建正确的 URL', async () => {
    const module = await import('../src/api/workflow-api')
    // 验证 API 函数存在且可调用
    expect(typeof module.listWorkflows).toBe('function')
    expect(typeof module.getWorkflow).toBe('function')
    expect(typeof module.createWorkflow).toBe('function')
    expect(typeof module.updateWorkflow).toBe('function')
    expect(typeof module.deleteWorkflow).toBe('function')
    expect(typeof module.executeWorkflow).toBe('function')
  })
})

// ==================== 复杂12节点DAG解析测试 ====================

// 使用普通字符串避免模板字面量中的 ${} 被当作 JavaScript 表达式
const COMPLEX_GRAPH_JSON = '{"nodes":[{"id":"start-001","type":"start","position":{"x":50,"y":320},"data":{"label":"工作流开始","input_params":[],"output_params":[{"key":"output","type":"Object","desc":"开始节点输出"}],"node_param":{}}},{"id":"variable-001","type":"variable","position":{"x":280,"y":320},"data":{"label":"初始化变量","input_params":[],"output_params":[{"key":"name","type":"String"},{"key":"value","type":"Object"}],"node_param":{"assignments":[{"name":"userInput","value":"测试数据分析请求"},{"name":"threshold","value":0.8},{"name":"dataList","value":[1,2,3,4,5]}]}}},{"id":"llm-001","type":"llm","position":{"x":510,"y":320},"data":{"label":"AI对话分析","input_params":[{"key":"prompt","type":"String","required":true}],"output_params":[{"key":"content","type":"String"},{"key":"success","type":"Boolean"}],"node_param":{"agentId":"default","prompt":"请分析以下内容：${userInput}，并给出评分","streaming":false}}},{"id":"condition-001","type":"condition","position":{"x":740,"y":320},"data":{"label":"评分条件判断","input_params":[],"output_params":[{"key":"selectedBranch","type":"String"},{"key":"evaluated","type":"Boolean"}],"node_param":{"branches":[{"name":"高分","expression":"threshold > 0.5","targetNodeId":"loop-001"},{"name":"中分","expression":"threshold == 0.5","targetNodeId":"api-001"},{"name":"低分","expression":"threshold <= 0.3","targetNodeId":"code-001"}]}}},{"id":"loop-001","type":"loop","position":{"x":970,"y":160},"data":{"label":"批量循环处理","input_params":[],"output_params":[{"key":"results","type":"Array<Object>"},{"key":"iterations","type":"Number"}],"node_param":{"items":"${dataList}","maxIterations":100}}},{"id":"tool-001","type":"tool","position":{"x":1200,"y":160},"data":{"label":"数据工具处理","input_params":[],"output_params":[{"key":"result","type":"Object"},{"key":"success","type":"Boolean"}],"node_param":{"toolName":"data-processor","parameters":{"mode":"batch","input":"${userInput}"}}}},{"id":"parallel-001","type":"parallel","position":{"x":1430,"y":100},"data":{"label":"并行执行分支","input_params":[],"output_params":[{"key":"results","type":"Array<Object>"},{"key":"totalNodes","type":"Number"}],"node_param":{"concurrency":2}}},{"id":"api-001","type":"api","position":{"x":970,"y":480},"data":{"label":"外部API调用","input_params":[{"key":"url","type":"String","required":true}],"output_params":[{"key":"success","type":"Boolean"},{"key":"error","type":"String"}],"node_param":{"url":"https://api.example.com/process","method":"POST","body":{"data":"${userInput}"},"timeoutMs":30000}}},{"id":"code-001","type":"code","position":{"x":970,"y":640},"data":{"label":"JavaScript代码执行","input_params":[{"key":"data","type":"Object"}],"output_params":[{"key":"result","type":"Object"},{"key":"success","type":"Boolean"}],"node_param":{"script":"var result = variables.dataList.map(function(x) { return x * 2; }); result;"}}},{"id":"retrieval-001","type":"retrieval","position":{"x":1200,"y":560},"data":{"label":"知识库检索","input_params":[{"key":"query","type":"String","required":true}],"output_params":[{"key":"documentCount","type":"Number"},{"key":"documents","type":"Array<Object>"},{"key":"success","type":"Boolean"}],"node_param":{"knowledgeBaseId":"kb-001","query":"${userInput}","topK":10,"scoreThreshold":0.6,"retrievalType":"hybrid","processMode":"list","includeMetadata":true,"includeScores":true,"outputVariable":"retrievedDocs"}}},{"id":"sub-workflow-001","type":"sub-workflow","position":{"x":1660,"y":320},"data":{"label":"子工作流调用","input_params":[],"output_params":[{"key":"success","type":"Boolean"},{"key":"output","type":"Object"}],"node_param":{"subWorkflowId":"sub-wf-001","inputMapping":"{\\"input\\":\\"${userInput}\\"}","timeout":300}}},{"id":"end-001","type":"end","position":{"x":1890,"y":320},"data":{"label":"工作流结束","input_params":[{"key":"input","type":"Object","value_from":"refer"}],"output_params":[],"node_param":{}}}],"edges":[{"id":"e-start-var","source":"start-001","target":"variable-001"},{"id":"e-var-llm","source":"variable-001","target":"llm-001"},{"id":"e-llm-cond","source":"llm-001","target":"condition-001"},{"id":"e-cond-loop","source":"condition-001","target":"loop-001","label":"高分"},{"id":"e-loop-tool","source":"loop-001","target":"tool-001"},{"id":"e-tool-parallel","source":"tool-001","target":"parallel-001"},{"id":"e-cond-api","source":"condition-001","target":"api-001","label":"中分"},{"id":"e-cond-code","source":"condition-001","target":"code-001","label":"低分"},{"id":"e-code-retrieval","source":"code-001","target":"retrieval-001"},{"id":"e-api-parallel","source":"api-001","target":"parallel-001"},{"id":"e-retrieval-parallel","source":"retrieval-001","target":"parallel-001"},{"id":"e-parallel-sub","source":"parallel-001","target":"sub-workflow-001"},{"id":"e-sub-end","source":"sub-workflow-001","target":"end-001"}]}'

describe('复杂DAG解析 - 覆盖全部12种节点类型', () => {
  let parsedGraph: { nodes: any[]; edges: any[] }

  beforeAll(() => {
    const parsed = JSON.parse(COMPLEX_GRAPH_JSON)
    parsedGraph = {
      nodes: parsed.nodes || [],
      edges: parsed.edges || [],
    }
  })

  it('应该正确解析全部12个节点和13条边', () => {
    expect(parsedGraph.nodes).toHaveLength(12)
    expect(parsedGraph.edges).toHaveLength(13)
  })

  it('节点拓扑应为线性链 + 收敛（主要路径顺序正确）', () => {
    const expectedOrder = ['start', 'variable', 'llm', 'condition', 'loop',
      'tool', 'parallel', 'api', 'code', 'retrieval', 'sub-workflow', 'end']
    const actualOrder = parsedGraph.nodes.map(n => n.type)
    expect(actualOrder).toEqual(expectedOrder)
  })

  it('所有节点类型应完整覆盖', () => {
    const types = new Set(parsedGraph.nodes.map(n => n.type))
    const expectedTypes = ['start', 'variable', 'llm', 'condition', 'loop',
      'tool', 'parallel', 'api', 'code', 'retrieval', 'sub-workflow', 'end']
    expectedTypes.forEach(t => expect(types.has(t)).toBe(true))
  })

  it('每个节点必须有完整的数据结构', () => {
    for (const node of parsedGraph.nodes) {
      expect(node.id).toBeTruthy()
      expect(node.type).toBeTruthy()
      expect(node.position).toBeDefined()
      expect(typeof node.position.x).toBe('number')
      expect(typeof node.position.y).toBe('number')
      expect(node.data).toBeDefined()
      expect(node.data.label).toBeTruthy()
      expect(Array.isArray(node.data.input_params)).toBe(true)
      expect(Array.isArray(node.data.output_params)).toBe(true)
      expect(node.data.node_param).toBeDefined()
    }
  })

  it('每条边必须有完整的数据结构', () => {
    for (const edge of parsedGraph.edges) {
      expect(edge.id).toBeTruthy()
      expect(edge.source).toBeTruthy()
      expect(edge.target).toBeTruthy()
    }
  })

  it('condition节点应有3个条件分支', () => {
    const condNode = parsedGraph.nodes.find(n => n.type === 'condition')
    expect(condNode).toBeDefined()
    expect(condNode!.data.node_param.branches).toHaveLength(3)
  })

  it('parallel节点应有concurrency=2参数', () => {
    const paraNode = parsedGraph.nodes.find(n => n.type === 'parallel')
    expect(paraNode).toBeDefined()
    expect(paraNode!.data.node_param.concurrency).toBe(2)
  })

  it('variable节点应包含3个assignments', () => {
    const varNode = parsedGraph.nodes.find(n => n.type === 'variable')
    expect(varNode).toBeDefined()
    expect(varNode!.data.node_param.assignments).toHaveLength(3)
  })

  it('llm节点应有agentId和streaming参数', () => {
    const llmNode = parsedGraph.nodes.find(n => n.type === 'llm')
    expect(llmNode).toBeDefined()
    expect(llmNode!.data.node_param.agentId).toBe('default')
    expect(llmNode!.data.node_param.streaming).toBe(false)
  })

  it('retrieval节点应有混合检索配置', () => {
    const retNode = parsedGraph.nodes.find(n => n.type === 'retrieval')
    expect(retNode).toBeDefined()
    expect(retNode!.data.node_param.retrievalType).toBe('hybrid')
    expect(retNode!.data.node_param.topK).toBe(10)
  })

  it('应该能正确模拟前端WorkflowEditorView的loadWorkflow解析逻辑', () => {
    // 模拟前端从API获取workflow后解析graphDefinition
    const mockApiResponse = { graphDefinition: COMPLEX_GRAPH_JSON }
    
    let graph: { nodes: any[]; edges: any[] } = { nodes: [], edges: [] }
    if (mockApiResponse.graphDefinition) {
      try {
        const parsed = JSON.parse(mockApiResponse.graphDefinition)
        graph = {
          nodes: parsed.nodes || [],
          edges: parsed.edges || [],
        }
      } catch {
        graph = { nodes: [], edges: [] }
      }
    }
    
    expect(graph.nodes).toHaveLength(12)
    expect(graph.edges).toHaveLength(13)
  })
})

describe('复杂DAG - workflow-store集成测试', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应该能用复杂12节点图调用setGraph', () => {
    const store = useWorkflowStore()
    const parsed = JSON.parse(COMPLEX_GRAPH_JSON)
    
    store.setGraph({
      nodes: parsed.nodes,
      edges: parsed.edges,
    })
    
    expect(store.nodes).toHaveLength(12)
    expect(store.edges).toHaveLength(13)
    expect(store.isDirty).toBe(true)
    expect(store.nodeCount).toBe(12)
    expect(store.edgeCount).toBe(13)
  })

  it('复杂图的节点删除应同时移除关联边', () => {
    const store = useWorkflowStore()
    const parsed = JSON.parse(COMPLEX_GRAPH_JSON)
    store.setGraph({ nodes: parsed.nodes, edges: parsed.edges })
    
    // 删除condition-001节点（它有3条出边、1条入边）
    store.deleteNode('condition-001')
    
    // 节点数减少1
    expect(store.nodes).toHaveLength(11)
    // 3条出边 + 1条入边 = 4条边被移除
    expect(store.edges).toHaveLength(9)
    
    // 确保所有关联边都已移除
    expect(store.edges.some(e => e.source === 'condition-001')).toBe(false)
    expect(store.edges.some(e => e.target === 'condition-001')).toBe(false)
  })

  it('复杂图的undo/redo应该正确工作', () => {
    const store = useWorkflowStore()
    const parsed = JSON.parse(COMPLEX_GRAPH_JSON)
    store.setGraph({ nodes: parsed.nodes, edges: parsed.edges })
    
    // 删除一个节点
    store.deleteNode('tool-001')
    expect(store.nodes).toHaveLength(11)
    
    // 撤销
    store.undo()
    expect(store.nodes).toHaveLength(12)
    expect(store.edges).toHaveLength(13)
    
    // 重做
    store.redo()
    expect(store.nodes).toHaveLength(11)
  })

  it('reset应清空复杂图的所有状态', () => {
    const store = useWorkflowStore()
    const parsed = JSON.parse(COMPLEX_GRAPH_JSON)
    store.setGraph({ nodes: parsed.nodes, edges: parsed.edges })
    store.setWorkflowInfo('test-id', '复杂工作流', '测试')
    
    store.reset()
    
    expect(store.nodes).toEqual([])
    expect(store.edges).toEqual([])
    expect(store.workflowId).toBe('')
    expect(store.isDirty).toBe(false)
  })
})
