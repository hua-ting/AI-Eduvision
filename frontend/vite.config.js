import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  build: {
    chunkSizeWarningLimit: 800,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    cssCodeSplit: true,
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('naive-ui')) {
              return 'chunk-naive'
            }
            if (id.includes('vue')) {
              return 'chunk-vue'
            }
            if (id.includes('echarts')) {
              return 'chunk-echarts'
            }
            if (id.includes('axios')) {
              return 'chunk-axios'
            }
            return 'vendor'
          }
          // 按功能模块分块
          if (id.includes('views/student')) {
            return 'chunk-student'
          }
          if (id.includes('views/admin')) {
            return 'chunk-admin'
          }
          if (id.includes('components')) {
            return 'chunk-components'
          }
        },
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    }
  },
  plugins: [
    vue(),
    AutoImport({
      imports: [
        'vue',
        'vue-router',
        'pinia'
      ],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      dirs: ['src/components'],
      extensions: ['vue'],
      resolvers: [NaiveUiResolver({ importStyle: 'css' })],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
