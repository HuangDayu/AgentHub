import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  base: '/',
  build: {
    outDir: resolve(__dirname, '../resources/static'),
    emptyOutDir: true,
    rollupOptions: {
      onwarn(warning, warn) {
        // 忽略__PURE__注释警告
        if (warning.code === 'INVALID_ANNOTATION' &&
            warning.message.includes('__PURE__')) {
          return
        }
        warn(warning)
      }
    }
  },
  resolve: {
    alias: { '@': resolve(__dirname, 'src') },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
