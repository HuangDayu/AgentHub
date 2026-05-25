import { defineStore } from 'pinia';
import { ref } from 'vue';
import { spanApi } from '@/api/span';
import type { Span } from '@/types/span';

/**
 * Span 状态管理
 */
export const useSpanStore = defineStore('span', () => {
  const spans = ref<Span[]>([]);
  const loading = ref(false);
  const currentSpan = ref<Span | null>(null);

  /**
   * 加载所有 Span
   */
  const loadSpans = async () => {
    loading.value = true;
    try {
      spans.value = await spanApi.list();
    } finally {
      loading.value = false;
    }
  };

  /**
   * 按 Trace ID 加载 Span
   */
  const loadSpansByTrace = async (traceId: string) => {
    loading.value = true;
    try {
      spans.value = await spanApi.listByTrace(traceId);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 按 Run ID 加载 Span
   */
  const loadSpansByRun = async (runId: string) => {
    loading.value = true;
    try {
      spans.value = await spanApi.listByRun(runId);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 获取单个 Span
   */
  const loadSpan = async (spanId: string) => {
    loading.value = true;
    try {
      currentSpan.value = await spanApi.get(spanId);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 删除 Span
   */
  const deleteSpan = async (spanId: string) => {
    await spanApi.delete(spanId);
    spans.value = spans.value.filter(s => s.id !== spanId);
  };

  return {
    spans,
    loading,
    currentSpan,
    loadSpans,
    loadSpansByTrace,
    loadSpansByRun,
    loadSpan,
    deleteSpan,
  };
});
