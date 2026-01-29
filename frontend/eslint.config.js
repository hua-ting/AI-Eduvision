import { defineConfig } from 'eslint'
import vue from 'eslint-plugin-vue'
import standard from '@vue/eslint-config-standard'

export default defineConfig({
  root: true,
  env: {
    node: true,
    browser: true
  },
  plugins: {
    vue
  },
  extends: [
    'plugin:vue/vue3-essential',
    standard
  ],
  rules: {
    // 基本规则
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    
    // Vue特定规则
    'vue/multi-word-component-names': 'off',
    'vue/no-unused-vars': 'error',
    'vue/require-default-prop': 'warn',
    
    // 代码风格规则
    'indent': ['error', 2],
    'linebreak-style': ['error', 'unix'],
    'quotes': ['error', 'single'],
    'semi': ['error', 'never'],
    'comma-dangle': ['error', 'only-multiline'],
    'space-before-function-paren': ['error', 'never'],
    
    // 变量规则
    'no-unused-vars': 'error',
    'no-undef': 'error',
    
    // 其他规则
    'prefer-const': 'error',
    'no-var': 'error'
  },
  ignores: [
    'node_modules/',
    'dist/',
    'dist-ssr/',
    '*.local',
    '.vscode/',
    '.idea/',
    'build/',
    'out/',
    'coverage/',
    '*.tmp',
    '*.temp',
    '.env',
    '.env.local',
    '.env.*.local',
    'src/auto-imports.d.ts',
    'src/components.d.ts'
  ]
})