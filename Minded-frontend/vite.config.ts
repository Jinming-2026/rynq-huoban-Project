import { defineConfig } from 'vite'
import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite';
import { VantResolver } from 'unplugin-vue-components/resolvers';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), Components({
    resolvers: [VantResolver()],
  }),],

  resolve: {
    alias: {
      // 路径别名：@ 指向 src 目录
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
