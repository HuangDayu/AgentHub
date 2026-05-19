<template>
  <div class="parallel-panel">
    <div class="panel-header">
      <h3>并行节点配置</h3>
    </div>

    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>

      <div class="form-group">
        <label>并行模式</label>
        <select v-model="config.mode" class="form-control">
          <option value="all">全部执行（等待所有分支完成）</option>
          <option value="any">任一完成（任一分支完成即继续）</option>
          <option value="race">竞速模式（第一个完成的分支结果）</option>
        </select>
      </div>

      <div class="form-group">
        <label>最大并发数</label>
        <input v-model.number="config.maxConcurrency" type="number" class="form-control" min="1" max="10" placeholder="限制同时执行的分支数" />
        <small class="form-text">防止资源耗尽，建议不超过10</small>
      </div>

      <div class="form-group">
        <label>超时时间（秒）</label>
        <input v-model.number="config.timeout" type="number" class="form-control" min="1" placeholder="所有分支的超时时间" />
      </div>

      <div class="form-group">
        <label>失败处理策略</label>
        <select v-model="config.failureStrategy" class="form-control">
          <option value="failFast">快速失败（任一失败立即停止）</option>
          <option value="continue">继续执行（等待所有分支完成）</option>
          <option value="ignore">忽略失败（只收集成功的分支结果）</option>
        </select>
      </div>

      <div class="form-group">
        <label>结果合并策略</label>
        <select v-model="config.mergeStrategy" class="form-control">
          <option value="object">对象合并（合并所有分支结果为对象）</option>
          <option value="array">数组合并（收集所有分支结果为数组）</option>
          <option value="first">首个结果（使用第一个完成的分支结果）</option>
        </select>
      </div>

      <div class="form-group">
        <label>分支权重配置</label>
        <textarea v-model="config.branchWeights" class="form-control" rows="3" placeholder='{"branch1": 0.5, "branch2": 0.3, "branch3": 0.2}'></textarea>
        <small class="form-text">可选：为不同分支设置权重，用于结果合并</small>
      </div>

      <div class="form-group">
        <label>
          <input v-model="config.collectErrors" type="checkbox" />
          收集所有错误信息
        </label>
        <small class="form-text">启用后，即使部分分支失败也会收集其错误信息</small>
      </div>
    </div>

    <div class="panel-footer">
      <button @click="handleCancel" class="btn btn-secondary">取消</button>
      <button @click="handleSave" class="btn btn-primary">保存</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface ParallelConfig {
  label: string
  mode: 'all' | 'any' | 'race'
  maxConcurrency: number
  timeout: number
  failureStrategy: 'failFast' | 'continue' | 'ignore'
  mergeStrategy: 'object' | 'array' | 'first'
  branchWeights: string
  collectErrors: boolean
}

const props = defineProps<{
  nodeData?: any
}>()

const emit = defineEmits<{
  save: [config: ParallelConfig]
  cancel: []
}>()

const config = ref<ParallelConfig>({
  label: props.nodeData?.label || '并行节点',
  mode: props.nodeData?.data?.mode || 'all',
  maxConcurrency: props.nodeData?.data?.maxConcurrency || 5,
  timeout: props.nodeData?.data?.timeout || 300,
  failureStrategy: props.nodeData?.data?.failureStrategy || 'failFast',
  mergeStrategy: props.nodeData?.data?.mergeStrategy || 'object',
  branchWeights: props.nodeData?.data?.branchWeights || '',
  collectErrors: props.nodeData?.data?.collectErrors || false
})

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '并行节点',
      mode: newData.data?.mode || 'all',
      maxConcurrency: newData.data?.maxConcurrency || 5,
      timeout: newData.data?.timeout || 300,
      failureStrategy: newData.data?.failureStrategy || 'failFast',
      mergeStrategy: newData.data?.mergeStrategy || 'object',
      branchWeights: newData.data?.branchWeights || '',
      collectErrors: newData.data?.collectErrors || false
    }
  }
}, { deep: true })

const handleSave = () => {
  emit('save', config.value)
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.parallel-panel {
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
  border-color: #6f42c1;
  box-shadow: 0 0 0 2px rgba(111, 66, 193, 0.1);
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
  background: #6f42c1;
  color: white;
}

.btn-primary:hover {
  background: #5a32a3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}
</style>
