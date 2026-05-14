<template>
  <div class="markdown-content" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps<{
  content: string
}>()

// 配置marked选项
marked.setOptions({
  breaks: true, // 支持换行符转换为<br>
  gfm: true, // 支持GitHub风格的Markdown
})

const renderedContent = computed(() => {
  if (!props.content) return ''
  try {
    return marked.parse(props.content) as string
  } catch {
    return props.content
  }
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.4;
  word-break: break-word;
}

.markdown-content :deep(h1) {
  font-size: 1.5em;
  margin: 0.3em 0;
  font-weight: 600;
}

.markdown-content :deep(h2) {
  font-size: 1.3em;
  margin: 0.3em 0;
  font-weight: 600;
}

.markdown-content :deep(h3) {
  font-size: 1.1em;
  margin: 0.3em 0;
  font-weight: 600;
}

.markdown-content :deep(p) {
  margin: 0.3em 0;
}

.markdown-content :deep(code) {
  background: rgba(22, 33, 50, 0.1);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
}

.markdown-content :deep(pre) {
  background: rgba(22, 33, 50, 0.06);
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 0.3em 0;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0.3em 0;
  padding-left: 1.5em;
}

.markdown-content :deep(blockquote) {
  border-left: 3px solid #3a8ad6;
  padding-left: 12px;
  margin: 0.3em 0;
  color: #666;
}

.markdown-content :deep(a) {
  color: #3a8ad6;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 0.3em 0;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid rgba(22, 33, 50, 0.1);
  padding: 6px;
  text-align: left;
}

.markdown-content :deep(th) {
  background: rgba(22, 33, 50, 0.05);
}
</style>
