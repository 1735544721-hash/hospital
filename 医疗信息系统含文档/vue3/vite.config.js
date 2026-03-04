import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3001,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8888', // 确保这里指向正确的后端服务地址，例如如果后端在8080端口，改为http://localhost:8080
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 移除自动注入，防止循环导入
      }
    }
  },
  // 定义环境变量
  define: {
    'import.meta.env.VITE_APP_BASE_API': JSON.stringify('/api')
  }
}) 