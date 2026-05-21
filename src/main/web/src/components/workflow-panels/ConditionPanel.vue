<template>
  <div class="condition-panel">
    <p class="hint">条件判断节点通过SpEL表达式分支决定执行路径</p>
    <div class="form-group">
      <label>分支规则</label>
      <div v-for="(branch, idx) in branches" :key="idx" class="branch-item">
        <div class="branch-header">
          <span class="branch-label">{{ branch.name || `分支 ${idx + 1}` }}</span>
          <button class="btn-sm btn-danger" @click="removeBranch(idx)">×</button>
        </div>
        <div class="branch-fields">
          <div class="field-row">
            <label class="sub-label">分支名称</label>
            <input v-model="branch.name" placeholder="分支名称" @input="emitUpdate" />
          </div>
          <div class="field-row">
            <label class="sub-label">SpEL 表达式</label>
            <input v-model="branch.expression" placeholder='例如: amount > 1000' @input="emitUpdate" />
          </div>
          <div class="field-row">
            <label class="sub-label">目标节点ID</label>
            <input v-model="branch.targetNodeId" placeholder="目标节点ID" @input="emitUpdate" />
          </div>
        </div>
      </div>
    </div>
    <button class="btn btn-primary btn-block" @click="addBranch">+ 添加分支</button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ node: any }>()
const emit = defineEmits<{ update: [updates: any] }>()

const branches = ref<any[]>(props.node.data?.node_param?.branches || [])

watch(() => props.node, (val) => {
  branches.value = val.data?.node_param?.branches || []
}, { deep: true })

function addBranch() {
  branches.value.push({
    name: `分支 ${branches.value.length + 1}`,
    expression: '',
    targetNodeId: ''
  })
  emitUpdate()
}

function removeBranch(idx: number) {
  branches.value.splice(idx, 1)
  emitUpdate()
}

function emitUpdate() {
  emit('update', { node_param: { branches: JSON.parse(JSON.stringify(branches.value)) } })
}
</script>

<style scoped>
.condition-panel { padding: 0; }
.hint { font-size: 12px; color: #999; margin-bottom: 12px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #666; margin-bottom: 6px; }
.branch-item {
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  background: #fafafa;
}
.branch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.branch-label { font-weight: 600; font-size: 13px; color: #333; }
.branch-fields { margin-bottom: 8px; }
.field-row { margin-bottom: 8px; }
.sub-label { font-size: 11px; color: #888; margin-bottom: 3px; display: block; }
.field-row input { width: 100%; padding: 6px 8px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 12px; box-sizing: border-box; }
.btn-sm { padding: 2px 8px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
.btn-danger { background: transparent; color: #ff4d4f; }
.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; }
.btn-primary { background: #1677ff; color: white; }
.btn-block { width: 100%; }
</style>
