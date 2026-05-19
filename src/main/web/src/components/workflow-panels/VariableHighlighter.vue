<template>
  <div v-if="variables.length > 0" class="variable-highlighter">
    <div v-for="ref in extractedRefs" :key="ref" class="variable-tag" :class="getVarClass(ref)">
      <span class="var-icon">{{ isValidVar(ref) ? '✓' : '✗' }}</span>
      <span class="var-text">{{ ref }}</span>
      <span v-if="getVarInfo(ref)" class="var-info">{{ getVarInfo(ref) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { VarTreeItem } from '@/types/workflow-node'
import { 
  extractVariableRefs, 
  parseVariableRef,
  validateVariableRef 
} from '@/utils/variable-system'

interface Props {
  text: string
  variables: VarTreeItem[]
}

const props = defineProps<Props>()

const extractedRefs = computed(() => {
  return extractVariableRefs(props.text)
})

function isValidVar(ref: string): boolean {
  return validateVariableRef(ref, props.variables)
}

function getVarClass(ref: string): string {
  return isValidVar(ref) ? 'valid' : 'invalid'
}

function getVarInfo(ref: string): string {
  const refInfo = parseVariableRef(ref)
  if (!refInfo) return ''
  
  const varItem = props.variables.find(
    v => v.key === `${refInfo.nodeId}.${refInfo.outputKey}`
  )
  
  if (!varItem) return ''
  return `${varItem.label} (${varItem.type})`
}
</script>

<style scoped>
.variable-highlighter {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.variable-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.variable-tag.valid {
  background: #f0f9ff;
  color: #0e7490;
  border: 1px solid #bae6fd;
}

.variable-tag.invalid {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.var-icon {
  font-size: 10px;
}

.var-text {
  font-weight: 500;
}

.var-info {
  opacity: 0.7;
  margin-left: 4px;
}
</style>
