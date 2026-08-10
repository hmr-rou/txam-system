import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/login': 'http://localhost:8080',
      '/logout': 'http://localhost:8080',
      '/admin': 'http://localhost:8080',
      '/student': 'http://localhost:8080',
    }
  }
})
