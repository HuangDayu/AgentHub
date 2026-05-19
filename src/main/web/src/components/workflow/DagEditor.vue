<template>
  <div class="dag-editor">
    <!-- 节点工具栏 -->
    <div class="node-toolbar">
      <div class="toolbar-title">节点类型</div>
      <div class="node-types">
        <div
          v-for="nodeType in nodeTypes"
          :key="nodeType.type"
          class="node-type-item"
          draggable="true"
          @dragstart="onDragStart($event, nodeType)"
        >
          <div class="node-icon" :style="{ background: nodeType.color }">
            {{ nodeType.icon }}
          </div>
          <span>{{ nodeType.label }}</span>
        </div>
      </div>
    </div>

    <!-- VueFlow画布 -->
    <div class="flow-container">
      <VueFlow
        v-model:nodes="nodes"
        v-model:edges="edges"
        @nodes-change="onNodesChange"
        @edges-change="onEdgesChange"
        @connect="onConnect"
        :default-viewport="{ zoom: 1, x: 0, y: 0 }"
        :min-zoom="0.2"
        :max-zoom="4"
        fit-view-on-init
        class="vue-flow-canvas"
      >
        <!-- 背景 -->
        <Background pattern-color="#aaa" :gap="20" />

        <!-- 小地图 -->
        <MiniMap />

        <!-- 控制按钮 -->
        <Controls />

        <!-- 自定义节点 -->
        <template #node-start="nodeProps">
          <StartNode :data="nodeProps.data" />
        </template>
        <template #node-end="nodeProps">
          <EndNode :data="nodeProps.data" />
        </template>
        <template #node-llm="nodeProps">
          <LLMNode :data="nodeProps.data" />
        </template>
        <template #node-api="nodeProps">
          <ApiNode :data="nodeProps.data" />
        </template>
        <template #node-condition="nodeProps">
          <ConditionNode :data="nodeProps.data" />
        </template>
        <template #node-loop="nodeProps">
          <LoopNode :data="nodeProps.data" />
        </template>
        <template #node-parallel="nodeProps">
          <ParallelNode :data="nodeProps.data" />
        </template>
        <template #node-variable="nodeProps">
          <VariableNode :data="nodeProps.data" />
        </template>
        <template #node-code="nodeProps">
          <CodeNode :data="nodeProps.data" />
        </template>
        <template #node-tool="nodeProps">
          <ToolNode :data="nodeProps.data" />
        </template>
      </VueFlow>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import type { Node, Edge, Connection } from '@vue-flow/core'

// 导入自定义节点组件
import StartNode from './nodes/StartNode.vue'
import EndNode from './nodes/EndNode.vue'
import LLMNode from './nodes/LLMNode.vue'
import ApiNode from './nodes/ApiNode.vue'
import ConditionNode from './nodes/ConditionNode.vue'
import LoopNode from './nodes/LoopNode.vue'
import ParallelNode from './nodes/ParallelNode.vue'
import VariableNode from './nodes/VariableNode.vue'
import CodeNode from './nodes/CodeNode.vue'
import ToolNode from './nodes/ToolNode.vue'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const { onConnect, addNodes } = useVueFlow()

// 节点类型定义
const nodeTypes = [
  { type: 'start', label: '开始', icon: '▶', color: '#28a745' },
  { type: 'end', label: '结束', icon: '⏹', color: '#dc3545' },
  { type: 'llm', label: 'LLM', icon: '🤖', color: '#007bff' },
  { type: 'api', label: 'API', icon: '🌐', color: '#17a2b8' },
  { type: 'condition', label: '条件', icon: '❓', color: '#ffc107' },
  { type: 'loop', label: '循环', icon: '🔄', color: '#6610f2' },
  { type: 'parallel', label: '并行', icon: '⚡', color: '#fd7e14' },
  { type: 'variable', label: '变量', icon: '📝', color: '#20c997' },
  { type: 'code', label: '代码', icon: '💻', color: '#6f42c1' },
  { type: 'tool', label: '工具', icon: '🔧', color: '#e83e8c' }
]

// 节点和边数据
const nodes = ref<Node[]>([])
const edges = ref<Edge[]>([])

// 解析初始JSON
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    try {
      const graph = JSON.parse(newVal)
      nodes.value = graph.nodes || []
      edges.value = graph.edges || []
    } catch (e) {
      console.error('Failed to parse graph definition', e)
    }
  }
}, { immediate: true })

// 监听变化并更新JSON
watch([nodes, edges], () => {
  const graph = {
    nodes: nodes.value,
    edges: edges.value
  }
  emit('update:modelValue', JSON.stringify(graph, null, 2))
}, { deep: true })

// 拖拽开始
function onDragStart(event: DragEvent, nodeType: any) {
  if (event.dataTransfer) {
    event.dataTransfer.setData('application/vueflow', nodeType.type)
    event.dataTransfer.effectAllowed = 'move'
  }
}

// 节点变化处理
function onNodesChange(changes: any[]) {
  // 自动处理节点变化
}

// 边变化处理
function onEdgesChange(changes: any[]) {
  // 自动处理边变化
}

// 连接处理
onConnect((params: Connection) => {
  // 自动添加边
})
</script>

<style scoped>
.dag-editor {
  display: flex;
  height: 500px;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.node-toolbar {
  width: 150px;
  background: #f8f9fa;
  border-right: 1px solid #ddd;
  padding: 1rem;
  overflow-y: auto;
}

.toolbar-title {
  font-weight: bold;
  margin-bottom: 1rem;
  color: #333;
}

.node-types {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.node-type-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: grab;
  transition: all 0.2s;
}

.node-type-item:hover {
  border-color: #007bff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.node-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  color: white;
  font-size: 12px;
}

.flow-container {
  flex: 1;
  position: relative;
}

.vue-flow-canvas {
  width: 100%;
  height: 100%;
}
</style>
