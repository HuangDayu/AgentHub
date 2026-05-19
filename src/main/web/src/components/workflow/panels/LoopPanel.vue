<template>
  <div class="loop-panel">
    <div class="panel-header">
      <h3>循环节点配置</h3>
    </div>

    <div class="panel-body">
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="config.label" type="text" class="form-control" placeholder="输入节点名称" />
      </div>

      <div class="form-group">
        <label>循环类型</label>
        <select v-model="config.loopType" class="form-control">
          <option value="for">For循环（固定次数）</option>
          <option value="while">While循环（条件判断）</option>
          <option value="forEach">ForEach循环（遍历数组）</option>
        </select>
      </div>

      <div v-if="config.loopType === 'for'" class="form-group">
        <label>循环次数</label>
        <input v-model.number="config.iterations" type="number" class="form-control" min="1" placeholder="输入循环次数" />
      </div>

      <div v-if="config.loopType === 'while'" class="form-group">
        <label>循环条件</label>
        <textarea v-model="config.condition" class="form-control" rows="3" placeholder="输入循环条件表达式，例如：${counter} < 10"></textarea>
        <small class="form-text">支持变量引用，使用 ${变量名} 格式</small>
      </div>

      <div v-if="config.loopType === 'forEach'" class="form-group">
        <label>遍历数组</label>
        <input v-model="config.arrayVariable" type="text" class="form-control" placeholder="输入数组变量名，例如：${items}" />
      </div>

      <div class="form-group">
        <label>循环变量名</label>
        <input v-model="config.loopVariable" type="text" class="form-control" placeholder="循环中的当前项变量名，例如：item" />
      </div>

      <div class="form-group">
        <label>索引变量名</label>
        <input v-model="config.indexVariable" type="text" class="form-control" placeholder="循环索引变量名，例如：index" />
      </div>

      <div class="form-group">
        <label>最大迭代次数</label>
        <input v-model.number="config.maxIterations" type="number" class="form-control" min="1" placeholder="防止无限循环，默认100" />
        <small class="form-text">安全限制，防止无限循环</small>
      </div>

      <div class="form-group">
        <label>
          <input v-model="config.parallel" type="checkbox" />
          并行执行循环体
        </label>
        <small class="form-text">启用后，每次迭代将并行执行</small>
      </div>

      <div class="form-group">
        <label>失败处理策略</label>
        <select v-model="config.failureStrategy" class="form-control">
          <option value="stop">停止循环</option>
          <option value="continue">继续下一次迭代</option>
          <option value="ignore">忽略错误继续执行</option>
        </select>
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

interface LoopConfig {
  label: string
  loopType: 'for' | 'while' | 'forEach'
  iterations?: number
  condition?: string
  arrayVariable?: string
  loopVariable: string
  indexVariable: string
  maxIterations: number
  parallel: boolean
  failureStrategy: 'stop' | 'continue' | 'ignore'
}

const props = defineProps<{
  nodeData?: any
}>()

const emit = defineEmits<{
  save: [config: LoopConfig]
  cancel: []
}>()

const config = ref<LoopConfig>({
  label: props.nodeData?.label || '循环节点',
  loopType: props.nodeData?.data?.loopType || 'for',
  iterations: props.nodeData?.data?.iterations || 10,
  condition: props.nodeData?.data?.condition || '',
  arrayVariable: props.nodeData?.data?.arrayVariable || '',
  loopVariable: props.nodeData?.data?.loopVariable || 'item',
  indexVariable: props.nodeData?.data?.indexVariable || 'index',
  maxIterations: props.nodeData?.data?.maxIterations || 100,
  parallel: props.nodeData?.data?.parallel || false,
  failureStrategy: props.nodeData?.data?.failureStrategy || 'stop'
})

watch(() => props.nodeData, (newData) => {
  if (newData) {
    config.value = {
      label: newData.label || '循环节点',
      loopType: newData.data?.loopType || 'for',
      iterations: newData.data?.iterations || 10,
      condition: newData.data?.condition || '',
      arrayVariable: newData.data?.arrayVariable || '',
      loopVariable: newData.data?.loopVariable || 'item',
      indexVariable: newData.data?.indexVariable || 'index',
      maxIterations: newData.data?.maxIterations || 100,
      parallel: newData.data?.parallel || false,
      failureStrategy: newData.data?.failureStrategy || 'stop'
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
.loop-panel {
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
  border-color: #17a2b8;
  box-shadow: 0 0 0 2px rgba(23, 162, 184, 0.1);
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
  background: #17a2b8;
  color: white;
}

.btn-primary:hover {
  background: #138496;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}
</style>
