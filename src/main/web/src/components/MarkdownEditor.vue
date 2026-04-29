<template>
  <div class="markdown-editor">
    <div class="editor-toolbar">
      <button class="ghost" type="button" @click="togglePreview">
        {{ showPreview ? '编辑' : '预览' }}
      </button>
      <button class="ghost" type="button" @click="insertTemplate('**', '**')">B</button>
      <button class="ghost" type="button" @click="insertTemplate('*', '*')">I</button>
      <button class="ghost" type="button" @click="insertTemplate('`', '`')">Code</button>
      <button class="ghost" type="button" @click="insertTemplate('\n```\n', '\n```\n')">Block</button>
      <button class="ghost" type="button" @click="insertTemplate('- ', '')">List</button>
    </div>
    <div class="editor-content">
      <textarea
        v-show="!showPreview"
        v-model="localContent"
        class="editor-textarea"
        :placeholder="placeholder"
        @input="handleInput"
      ></textarea>
      <div v-show="showPreview" class="editor-preview" v-html="renderedContent"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { marked } from 'marked'

interface Props {
  modelValue: string
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入Markdown内容...',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const localContent = ref(props.modelValue)
const showPreview = ref(false)

const renderedContent = computed(() => {
  return marked(localContent.value) as string
})

watch(() => props.modelValue, (newValue) => {
  localContent.value = newValue
})

function handleInput() {
  emit('update:modelValue', localContent.value)
}

function togglePreview() {
  showPreview.value = !showPreview.value
}

function insertTemplate(prefix: string, suffix: string) {
  const textarea = document.querySelector('.editor-textarea') as HTMLTextAreaElement
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = localContent.value.substring(start, end)
  const beforeText = localContent.value.substring(0, start)
  const afterText = localContent.value.substring(end)

  localContent.value = beforeText + prefix + selectedText + suffix + afterText
  emit('update:modelValue', localContent.value)

  // Reset cursor position
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, end + prefix.length)
  }, 0)
}
</script>

<style scoped>
.markdown-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: rgba(22, 33, 50, 0.04);
  border-radius: 8px;
}

.editor-toolbar button {
  padding: 4px 12px;
  font-size: 14px;
  font-weight: 500;
}

.editor-content {
  min-height: 300px;
  border: 1px solid rgba(22, 33, 50, 0.14);
  border-radius: 8px;
  overflow: hidden;
}

.editor-textarea {
  width: 100%;
  height: 100%;
  min-height: 300px;
  padding: 16px;
  border: none;
  resize: vertical;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  background: white;
}

.editor-textarea:focus {
  outline: none;
}

.editor-preview {
  padding: 16px;
  min-height: 300px;
  background: white;
  overflow-y: auto;
}

.editor-preview :deep(h1) {
  font-size: 2em;
  margin: 0.67em 0;
  border-bottom: 1px solid rgba(22, 33, 50, 0.1);
  padding-bottom: 0.3em;
}

.editor-preview :deep(h2) {
  font-size: 1.5em;
  margin: 0.83em 0;
  border-bottom: 1px solid rgba(22, 33, 50, 0.1);
  padding-bottom: 0.3em;
}

.editor-preview :deep(h3) {
  font-size: 1.17em;
  margin: 1em 0;
}

.editor-preview :deep(p) {
  margin: 1em 0;
}

.editor-preview :deep(code) {
  background: rgba(22, 33, 50, 0.06);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.editor-preview :deep(pre) {
  background: rgba(22, 33, 50, 0.06);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1em 0;
}

.editor-preview :deep(pre code) {
  background: none;
  padding: 0;
}

.editor-preview :deep(ul),
.editor-preview :deep(ol) {
  margin: 1em 0;
  padding-left: 2em;
}

.editor-preview :deep(blockquote) {
  border-left: 4px solid #3a8ad6;
  padding-left: 16px;
  margin: 1em 0;
  color: #666;
}
</style>
