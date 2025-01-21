import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import autoprefixer from 'autoprefixer'
import tailwind from 'tailwindcss'
import dotenv from 'dotenv';

// Load environment variables
dotenv.config();

const VITE_PORT = Number(process.env.VITE_PORT);
const VITE_API_PORT = Number(process.env.VITE_API_PORT);


// https://vite.dev/config/
export default defineConfig({
  base: './',
  css: {
    postcss: {
      plugins: [tailwind(), autoprefixer()],
    },
  },
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: VITE_PORT,
    proxy: {
      '/api': {
        target: `http://localhost:${VITE_API_PORT}`,
        changeOrigin: true
      }
    },
    hmr: {
      overlay: false
    }
  },
  build: {
    outDir: 'dist',
  },
  define: {
    'import.meta.env.VITE_API_PORT': JSON.stringify(process.env.VITE_API_PORT),
    'import.meta.env.VITE_NODE_ENV': JSON.stringify(process.env.NODE_ENV),
  },
})
