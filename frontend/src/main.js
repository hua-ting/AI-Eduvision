import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
// ========== 1. 导入Pinia核心方法（官方标准写法）==========
import { createPinia } from 'pinia'
// 创建Pinia实例（必须步骤，激活Pinia）
const pinia = createPinia()

// ========== 2. 编辑器配置（适配1.7.12，极简无错，保留不变）==========
import VMdEditor from '@kangc/v-md-editor'
import VMdPreview from '@kangc/v-md-editor/lib/preview'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import '@kangc/v-md-editor/lib/style/preview.css'

// ========== 3. Naive UI原有配置（保留不变）==========
import { createDiscreteApi } from 'naive-ui'
const { message, notification, dialog, loadingBar } = createDiscreteApi(['message', 'notification', 'dialog', 'loadingBar'])

// ========== 4. 创建Vue应用并按顺序挂载依赖（关键：pinia必须在router前/后挂载，确保最先激活）==========
const app = createApp(App)
// 先挂载Pinia（核心！确保组件使用useStore前Pinia已激活）
app.use(pinia)
// 再挂载路由
app.use(router)

// 全局挂载Naive UI实例
app.provide('message', message)
app.provide('notification', notification)
app.provide('dialog', dialog)
app.provide('loadingBar', loadingBar)

// 全局注册编辑器/预览组件
app.component('VMdEditor', VMdEditor)
app.component('VMdPreview', VMdPreview)

// 挂载应用
app.mount('#app')