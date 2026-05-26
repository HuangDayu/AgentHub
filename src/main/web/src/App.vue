<template>
  <RouterView />
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useThemeStore } from '@/stores/theme'

// Initialize theme on app mount — applies saved theme from localStorage
useThemeStore()
</script>
