<template>
  <div class="retrieval-panel">
    <div class="form-group">
      <label>知识库ID</label>
      <input v-model="localConfig.knowledgeBaseId" placeholder="选择或输入知识库ID" @input="emitUpdate" />
    </div>
    <div class="form-group">
      <label>检索查询</label>
      <textarea v-model="localConfig.query" rows="3" placeholder="输入检索查询文本，支持 {{变量名}} 引用" @input="emitUpdate"></textarea>
    </div>
    <div class="form-group">
      <label>检索数量 (Top-K)</label>
      <input type="number" v-model.number="localConfig.topK" min="1" max="100" @input="emitUpdate" />
    </div>
    <div class="form-group">
      <label>分数阈值</label>
      <input type="number" v-model.number="localConfig.scoreThreshold" min="0" max="1" step="0.05" @input="emitUpdate" />
    </div>
    <div class="form-group">
      <label>检索类型</label>
      <select v-model="localConfig.retrievalType" @change="emitUpdate">
        <option value="similarity">相似度检索 (Similarity)</option>
        <option value="hybrid">混合检索 (Hybrid)</option>
      </select>
    </div>
    <div class="form-group">
      <label>处理模式</label>
      <select v-model="localConfig.processMode" @change="emitUpdate">
        <option value="list">列表 (List)</option>
        <option value="concat">拼接 (Concat)</option>
        <option value="structured">结构化 (Structured)</option>
      </select>
    </div>
    <div class="form-group">
      <label class="checkbox-label">
        <input type="checkbox" v-model="localConfig.includeMetadata" @change="emitUpdate" />
        包含元数据
      </label>
    </div>
    <div class="form-group">
      <label class="checkbox-label">
        <input type="checkbox" v-model="localConfig.includeScores" @change="emitUpdate" />
        包含分数
      </label>
    </div>
    <div class="form-group">
      <label>分隔符</label>
      <input v-model="localConfig.separator" placeholder="\\n\\n" @input="emitUpdate" />
      <span class="field-desc">拼接模式下文档之间的分隔符</span>
    </div>
    <div class="form-group">
      <label>输出变量名</label>
      <input v-model="localConfig.outputVariable" placeholder="retrievedDocs" @input="emitUpdate" />
      <span class="field-desc">检索结果在上下文中的变量名</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'

const props = defineProps<{ node: any }>()
const emit = defineEmits<{ update: [updates: any] }>()

const localConfig = reactive({ ...(props.node.data?.node_param || {}) })

watch(() => props.node, (val) => {
  Object.assign(localConfig, val.data?.node_param || {})
}, { deep: true })

function emitUpdate() {
  emit('update', { node_param: { ...localConfig } })
}
</script>

<style scoped>
.retrieval-panel { padding: 0; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #666; margin-bottom: 6px; }
.form-group select,
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border-strong);
  border-radius: 4px;
  font-size: 13px;
  box-sizing: border-box;
}
.form-group textarea { resize: vertical; font-family: inherit; }
.checkbox-label { display: flex; align-items: center; gap: 8px; cursor: pointer; font-weight: 400 !important; }
.checkbox-label input[type="checkbox"] { width: auto; }
.field-desc { display: block; font-size: 11px; color: #999; margin-top: 4px; }
</style>
