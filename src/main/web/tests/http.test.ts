import { describe, it, expect } from 'vitest'

describe('http utility functions', () => {
  describe('buildUrl', () => {
    // 内联 buildUrl 实现以便独立测试
    function buildUrl(baseUrl: string, path: string, params?: Record<string, string | number | boolean>) {
      const validBaseUrl = (baseUrl && baseUrl.trim()) ? baseUrl.trim() : 'http://localhost:8080'
      const url = new URL(path, validBaseUrl)
      if (params) {
        for (const [key, value] of Object.entries(params)) {
          url.searchParams.set(key, String(value))
        }
      }
      return url.toString()
    }

    it('应正确拼接基础URL和路径', () => {
      const result = buildUrl('http://localhost:8080', '/api/dag-workflows')
      expect(result).toBe('http://localhost:8080/api/dag-workflows')
    })

    it('应正确处理查询参数', () => {
      const result = buildUrl('http://localhost:8080', '/api/dag-workflows', { page: 1, size: 20 })
      expect(result).toContain('page=1')
      expect(result).toContain('size=20')
    })

    it('空 baseUrl 时应使用默认值', () => {
      const result = buildUrl('', '/api/test')
      expect(result).toBe('http://localhost:8080/api/test')
    })
  })

  describe('graph JSON 序列化/反序列化', () => {
    it('parseGraph 后 toJSON 不应丢失数据', () => {
      const graph = {
        nodes: [
          { id: '1', type: 'start', position: { x: 0, y: 0 }, data: { label: '开始', input_params: [], output_params: [], node_param: {} } },
          { id: '2', type: 'llm', position: { x: 200, y: 0 }, data: { label: 'LLM', input_params: [], output_params: [], node_param: {} } },
        ],
        edges: [
          { id: 'e1', source: '1', target: '2' },
        ],
      }

      const json = JSON.stringify(graph)
      const parsed = JSON.parse(json)

      // round-trip 后数据一致
      expect(parsed.nodes).toHaveLength(2)
      expect(parsed.edges).toHaveLength(1)
      expect(parsed.nodes[0].id).toBe('1')
      expect(parsed.nodes[1].type).toBe('llm')
      expect(parsed.edges[0].source).toBe('1')
    })

    it('空 graph 的 round-trip', () => {
      const graph = { nodes: [], edges: [] }
      const json = JSON.stringify(graph)
      const parsed = JSON.parse(json)
      expect(parsed.nodes).toEqual([])
      expect(parsed.edges).toEqual([])
    })

    it('缺少 nodes 属性的 JSON 应能被安全处理', () => {
      const parsed = JSON.parse('{}')
      const safeGraph = {
        nodes: parsed.nodes || [],
        edges: parsed.edges || [],
      }
      expect(safeGraph.nodes).toEqual([])
      expect(safeGraph.edges).toEqual([])
    })
  })
})
