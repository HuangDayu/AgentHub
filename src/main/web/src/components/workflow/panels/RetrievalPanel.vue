<template>
  <div class="retrieval-panel">
    <div class="panel-header">
      <h3>知识检索节点配置</h3>
    </div>

    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>

      <div class="form-group">
        <label>知识库选择</label>
        <select v-model="config.knowledgeBaseId" class="form-control">
          <option value="">请选择知识库</option>
          <option v-for="kb in knowledgeBases" :key="kb.id" :value="kb.id">
            {{ kb.name }} ({{ kb.documentCount }} 文档)
          </option>
        </select>
      </div>

      <div class="form-group">
        <label>查询内容</label>
        <textarea v-model="config.query" class="form-control" rows="3" placeholder="输入查询内容，支持变量引用 ${变量名}"></textarea>
        <small class="form-text">支持使用变量引用，例如：${userQuestion}</small>
      </div>

      <div class="form-group">
        <label>检索方式</label>
        <select v-model="config.retrievalType" class="form-control">
          <option value="similarity">相似度检索</option>
          <option value="mmr">最大边际相关性（MMR）</option>
          <option value="hybrid">混合检索（向量+关键词）</option>
        </select>
      </div>

      <div class="form-group">
        <label>返回文档数量（TopK）</label>
        <input v-model.number="config.topK" type="number" class="form-control" min="1" max="20" placeholder="返回最相关的K个文档" />
      </div>

      <div class="form-group">
        <label>相似度阈值</label>
        <input v-model.number="config.scoreThreshold" type="number" class="form-control" min="0" max="1" step="0.1" placeholder="最低相似度分数（0-1）" />
        <small class="form-text">低于此阈值的文档将被过滤</small>
      </div>

      <div v-if="config.retrievalType === 'mmr'" class="form-group">
        <label>MMR多样性参数</label>
        <input v-model.number="config.mmrLambda" type="number" class="form-control" min="0" max="1" step="0.1" placeholder="平衡相关性和多样性（0-1）" />
        <small class="form-text">值越大越注重相关性，越小越注重多样性</small>
      </div>

      <div class="form-group">
        <label>
          <input v-model="config.includeMetadata" type="checkbox" />
          包含文档元数据
        </label>
        <small class="form-text">返回结果中包含文档的元数据信息</small>
      </div>

      <div class="form-group">
        <label>
          <input v-model="config.includeScores" type="checkbox" />
          包含相似度分数
        </label>
        <small class="form-text">返回结果中包含每个文档的相似度分数</small>
      </div>

      <div class="form-group">
        <label>输出变量名</label>
        <input v-model="config.outputVariable" type="text" class="form-control" placeholder="检索结果保存的变量名" />
        <small class="form-text">检索到的文档将保存到此变量中</small>
      </div>

      <div class="form-group">
        <label>文档处理方式</label>
        <select v-model="config.processMode" class="form-control">
          <option value="list">列表形式（返回文档数组）</option>
          <option value="concat">拼接形式（拼接所有文档内容）</option>
          <option value="structured">结构化形式（包含内容和元数据）</option>
        </select>
      </div>

      <div v-if="config.processMode === 'concat'" class="form-group">
        <label>文档分隔符</label>
        <input v-model="config.separator" type="text" class="form-control" placeholder="文档之间的分隔符" />
      </div>

      <div class="form-group">
        <label>过滤条件</label>
        <textarea v-model="config.filters" class="form-control" rows="2" placeholder='{"category": "tech", "year": {"$gte": 2020}}'></textarea>
        <small class="form-text">可选：JSON格式的过滤条件</small>
      </div>
    </div>

    <div class="panel-footer">
      <button @click="handleTest" class="btn btn-info">测试检索</button>
      <button @click="handleCancel" class="btn btn-secondary">取消</button>
      <button @click="handleSave" class="btn btn-primary">保存</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

interface KnowledgeBase {
  id: string
  name: string
  documentCount: number
}

interface RetrievalConfig {
  label: string
  knowledgeBaseId: string
  query: string
  retrievalType: 'similarity' | 'mmr' | 'hybrid'
  topK: number
  scoreThreshold: number
  mmrLambda: number
  includeMetadata: boolean
  includeScores: boolean
  outputVariable: string
  processMode: 'list' | 'concat' | 'structured'
  separator: string
  filters: string
}

const props = defineProps<{
  nodeData?: any
}>()

const emit = defineEmits<{
  save: [config: RetrievalConfig]
  cancel: []
  test: [config: RetrievalConfig]
}>()

const knowledgeBases = ref<KnowledgeBase[]>([])

const config = ref<RetrievalConfig>({
  label: props.nodeData?.label || '知识检索',
  knowledgeBaseId: props.nodeData?.data?.knowledgeBaseId || '',
  query: props.nodeData?.data?.query || '',
  retrievalType: props.nodeData?.data?.retrievalType || 'similarity',
  topK: props.nodeData?.data?.topK || 5,
  scoreThreshold: props.nodeData?.data?.scoreThreshold || 0.5,
  mmrLambda: props.nodeData?.data?.mmrLambda || 0.5,
  includeMetadata: props.nodeData?.data?.includeMetadata || false,
  includeScores: props.nodeData?.data?.includeScores || true,
  outputVariable: props.nodeData?.data?.outputVariable || 'retrievedDocs',
  processMode: props.nodeData?.data?.processMode || 'list',
  separator: props.nodeData?.data?.separator || '\n\n',
  filters: props.nodeData?.data?.filters || '{}'
})

// 加载知识库列表
onMounted(async () => {
  try {
    // TODO: 从API加载知识库列表
    knowledgeBases.value = [
      { id: 'kb1', name: '技术文档库', documentCount: 150 },
      { id: 'kb2', name: '产品手册库', documentCount: 80 },
      { id: 'kb3', name: 'FAQ知识库', documentCount: 200 }
    ]
  } catch (error) {
    console.error('Failed to load knowledge bases:', error)
  }
})

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '知识检索',
      knowledgeBaseId: newData.data?.knowledgeBaseId || '',
      query: newData.data?.query || '',
      retrievalType: newData.data?.retrievalType || 'similarity',
      topK: newData.data?.topK || 5,
      scoreThreshold: newData.data?.scoreThreshold || 0.5,
      mmrLambda: newData.data?.mmrLambda || 0.5,
      includeMetadata: newData.data?.includeMetadata || false,
      includeScores: newData.data?.includeScores || true,
      outputVariable: newData.data?.outputVariable || 'retrievedDocs',
      processMode: newData.data?.processMode || 'list',
      separator: newData.data?.separator || '\n\n',
      filters: newData.data?.filters || '{}'
    }
  }
}, { deep: true })

const handleSave = () => {
  emit('save', config.value)
}

const handleCancel = () => {
  emit('cancel')
}

const handleTest = () => {
  emit('test', config.value)
}
</script>

<style scoped>
.retrieval-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #ddd;
  background: #f8f9fa;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
}

.panel-body {
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-control:focus {
  outline: none;
  border-color: #4169e1;
  box-shadow: 0 0 0 2px rgba(65, 105, 225, 0.1);
}

textarea.form-control {
  resize: vertical;
  font-family: 'Monaco', 'Menlo', monospace;
}

.form-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #6c757d;
}

.panel-footer {
  padding: 16px;
  border-top: 1px solid #ddd;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-primary {
  background: #4169e1;
  color: white;
}

.btn-primary:hover {
  background: #3659d5;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn-info {
  background: #17a2b8;
  color: white;
}

.btn-info:hover {
  background: #138496;
}
</style>
