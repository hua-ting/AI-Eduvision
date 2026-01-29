<template>
  <div class="feishu-md-editor" :style="{ maxHeight: maxHeight }">
    <!-- 顶部操作栏：纯文字按钮 -->
    <div class="editor-header">
      <div class="header-left">
        <!-- 管理员：直接保存 -->
        <n-button
            v-if="userRole === 'admin'"
            type="primary"
            size="small"
            @click="handleSave"
            class="operate-btn"
        >
          直接保存
        </n-button>
        <!-- 普通用户：提交修改审核 -->
        <n-button
            v-else
            type="info"
            size="small"
            @click="handleSubmitAudit"
            class="operate-btn"
        >
          提交修改审核
        </n-button>
        <!-- 取消编辑 -->
        <n-button size="small" @click="handleCancel" class="operate-btn">
          取消
        </n-button>
      </div>
      <div class="header-right">
        <span class="draft-tip" v-if="lastSaved">草稿已保存：{{ lastSaved }}</span>
      </div>
    </div>

    <!-- 主体：目录+所见即所得编辑区 -->
    <div class="editor-main">
      <!-- 目录面板 -->
      <div class="toc-panel" v-if="showToc && tocItems.length > 0">
        <div class="toc-title">目录</div>
        <div class="toc-list">
          <div
              v-for="item in tocItems"
              :key="item.id"
              :class="['toc-item', `level-${item.level}`, { active: activeId === item.id }]"
              @click="scrollToItem(item.id)"
          >
            {{ item.text }}
          </div>
        </div>
      </div>

      <!-- 替代方案：使用textarea作为简单的Markdown编辑器 -->
        <div class="wysiwyg-edit-area">
          <textarea
              v-model="localValue"
              :placeholder="placeholder || '请输入内容（支持Markdown语法）'"
              @input="handleInput"
              @blur="saveDraft"
              class="simple-markdown-editor"
              rows="20"
          ></textarea>
          <div class="editor-tip">
            <n-text depth="3" size="small">
              💡 支持Markdown语法，例如：# 标题、**粗体**、```代码块```等
            </n-text>
          </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
// 仅引入Naive UI核心组件，编辑器已全局注册，无需再引入
import { NButton, useMessage } from 'naive-ui'

// 创建Naive UI消息实例
const message = useMessage()

// Props：保留所有自定义配置
const props = defineProps({
  modelValue: { type: String, default: '' }, // v-model绑定的值
  contentId: { type: String, required: true }, // 编辑内容唯一ID（确保字符串）
  userRole: { type: String, default: 'normal' }, // 角色：admin/normal（字符串）
  placeholder: { type: String, default: '' },
  maxHeight: { type: String, default: '800px' },
  autoSaveKey: { type: String, default: (p) => `md_draft_${p.contentId}` }, // 草稿缓存Key
  onSave: { type: Function, default: null }, // 保存回调函数
  onSubmitAudit: { type: Function, default: null } // 提交审核回调函数
})

// Emits：保留所有自定义事件
const emit = defineEmits(['close', 'refreshContent', 'update:modelValue'])

// 编辑内容双向绑定核心
const localValue = ref(props.modelValue || '')

// 监听localValue变化，同步到父组件（v-model核心）
watch(localValue, (newValue) => {
  if (newValue !== undefined && newValue !== null) {
    emit('update:modelValue', newValue)
  }
}, { deep: false }) // 字符串不需要deep监听

// 监听父组件modelValue变化，同步到编辑框
watch(() => props.modelValue, (newValue) => {
  if (newValue !== undefined && newValue !== null && newValue !== localValue.value) {
    localValue.value = newValue
  }
}, { immediate: true, deep: false }) // 字符串不需要deep监听

// 草稿相关状态
const lastSaved = ref(null)
// 目录相关状态
const showToc = ref(false)
const tocItems = ref([])
const activeId = ref('')
const observer = ref(null)

// 防抖计时器
let saveTimeout = null

// 草稿保存：自动保存到localStorage（添加防抖）
const saveDraft = () => {
  if (!props.autoSaveKey || !localValue.value.trim()) return
  
  // 清除之前的计时器
  if (saveTimeout) {
    clearTimeout(saveTimeout)
  }
  
  // 3秒防抖，用户停止输入后再保存
  saveTimeout = setTimeout(() => {
    localStorage.setItem(props.autoSaveKey, localValue.value)
    lastSaved.value = new Date().toLocaleTimeString('zh-CN')
    // 只更新时间显示，不弹出提示
  }, 3000)
}

// 草稿恢复：组件挂载时加载
const restoreDraft = () => {
  if (!props.autoSaveKey) return
  const draft = localStorage.getItem(props.autoSaveKey)
  if (draft && draft !== props.modelValue) {
    localValue.value = draft
    lastSaved.value = '已恢复本地草稿'
    message.info(lastSaved.value)
  }
}

// 滚动到指定目录项（保留函数结构，实际不使用）
const scrollToItem = (id) => {
  const el = document.getElementById(id)
  el && el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  activeId.value = id
}

// 输入事件：保存草稿（移除目录生成，因为使用textarea）
const handleInput = () => {
  saveDraft()
}

// 管理员：直接保存（调用父组件回调）
const handleSave = async () => {
  if (!localValue.value.trim()) {
    message.warning('编辑内容不能为空！')
    return
  }

  if (props.onSave) {
    try {
      await props.onSave(localValue.value)
      localStorage.removeItem(props.autoSaveKey)
      message.success('保存成功！内容已生效')
      emit('close')
      emit('refreshContent')
    } catch (error) {
      message.error(`保存失败：${error.message || '未知错误'}`)
      console.error('保存回调报错：', error)
    }
    return
  }

  message.error('请在父组件绑定onSave回调函数！')
}

// 普通用户：提交审核（调用父组件回调）
const handleSubmitAudit = async () => {
  if (!localValue.value.trim()) {
    message.warning('编辑内容不能为空！')
    return
  }

  if (props.onSubmitAudit) {
    try {
      await props.onSubmitAudit(localValue.value)
      localStorage.removeItem(props.autoSaveKey)
      message.success('审核申请已提交，等待管理员审核')
      emit('close')
    } catch (error) {
      message.error(`提交失败：${error.message || '未知错误'}`)
      console.error('提交审核回调报错：', error)
    }
    return
  }

  message.error('请在父组件绑定onSubmitAudit回调函数！')
}

// 取消编辑：通知父组件关闭
const handleCancel = () => {
  message.info('已取消编辑，草稿已保留')
  emit('close')
}

// 目录显隐切换（简化版，因为使用textarea）
const toggleToc = () => {
  showToc.value = !showToc.value
  // 由于使用textarea，不再生成目录
  if (showToc.value) {
    message.info('目录功能在简单编辑器模式下不可用')
    showToc.value = false
  }
}

// 组件挂载：恢复草稿
onMounted(() => {
  try {
    restoreDraft()
    
    // 自动聚焦到textarea
    nextTick(() => {
      const textarea = document.querySelector('.simple-markdown-editor')
      textarea && textarea.focus()
    })
    
  } catch (error) {
    console.warn('组件挂载警告:', error)
  }
})

// 组件卸载：销毁监听+最后保存草稿
onUnmounted(() => {
  // 清除防抖计时器
  if (saveTimeout) {
    clearTimeout(saveTimeout)
  }
  // 最后保存一次草稿
  if (props.autoSaveKey && localValue.value.trim()) {
    localStorage.setItem(props.autoSaveKey, localValue.value)
    lastSaved.value = new Date().toLocaleTimeString('zh-CN')
  }
})
</script>

<style scoped>
.feishu-md-editor {
  width: 100%;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
  position: relative;
  z-index: 1000;
  height: 100%;
}
.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e8e8e8;
}
.editor-header .header-left .operate-btn {
  margin-right: 8px;
}
.editor-header .header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.editor-header .header-right .draft-tip {
  font-size: 12px;
  color: #666;
}
.editor-main {
  display: flex;
  height: calc(100% - 48px);
  min-height: 700px;
}
.toc-panel {
  width: 240px;
  padding: 16px;
  background: #f9fafb;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
}
.toc-panel .toc-title {
  font-size: 14px;
  font-weight: 600;
  color: #165dff;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}
.toc-panel .toc-list .toc-item {
  padding: 6px 8px;
  font-size: 13px;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    background: #eef4ff;
    color: #165dff;
  }
  &.active {
    background: #eef4ff;
    color: #165dff;
    border-left: 2px solid #165dff;
  }
  &.level-1 { padding-left: 0; font-weight: 600; }
  &.level-2 { padding-left: 16px; }
  &.level-3 { padding-left: 32px; }
}
.wysiwyg-edit-area {
  flex: 1;
  padding: 16px 24px;
  overflow: hidden;
  height: 100%;
}
.feishu-editor-core {
  height: 100% !important;
  :deep(.v-md-editor) {
    border: none !important;
    height: 100% !important;
    :deep(.v-md-editor-toolbar) {
      background: #fff !important;
      border-bottom: 1px solid #e8e8e8 !important;
      padding: 4px 0 !important;
      position: sticky !important;
      top: 0 !important;
      z-index: 10 !important;
      :deep(.v-md-editor-toolbar__item) {
        margin: 0 4px !important;
        color: #333 !important;
        &:hover, &.active {
          background: #eef4ff !important;
          color: #165dff !important;
        }
      }
    }
    :deep(.v-md-editor__content) {
      padding: 16px 0 !important;
      line-height: 1.6 !important;
      font-size: 14px !important;
      color: #333 !important;
      min-height: calc(100% - 48px) !important;
      &:focus { outline: none !important; }
      h1, h2, h3 {
        color: #165dff !important;
        margin: 1.2em 0 0.6em !important;
        font-weight: 600 !important;
      }
      h1 { font-size: 20px !important; border-bottom: 2px solid #eef4ff !important; padding-bottom: 8px !important; }
      h2 { font-size: 18px !important; border-bottom: 1px solid #eef4ff !important; padding-bottom: 6px !important; }
      h3 { font-size: 16px !important; }
      pre {
        background: #f9fafb !important;
        border: 1px solid #e8e8e8 !important;
        border-radius: 8px !important;
        padding: 16px !important;
        margin: 1em 0 !important;
        overflow: auto !important;
      }
      code {
        background: #f5f7fa !important;
        padding: 2px 6px !important;
        border-radius: 4px !important;
        color: #e53935 !important;
      }
      blockquote {
        border-left: 4px solid #165dff !important;
        background: #f9fafb !important;
        padding: 8px 16px !important;
        margin: 1em 0 !important;
        color: #666 !important;
      }
      ul, ol {
        padding-left: 2em !important;
        margin: 0.8em 0 !important;
      }
      a {
        color: #165dff !important;
        text-decoration: none !important;
        &:hover { text-decoration: underline !important; }
      }
    }
    :deep(.v-md-editor-float-toolbar) {
      background: #fff !important;
      border: 1px solid #e8e8e8 !important;
      border-radius: 4px !important;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
    }
  }
}
/* 全屏模式层级优化 */
:deep(.v-md-editor-fullscreen) {
  z-index: 2000 !important;
}
/* 简单Markdown编辑器样式 */
.simple-markdown-editor {
  width: 100%;
  min-height: 500px;
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  background: #fff;
  resize: vertical;
  transition: all 0.3s;
  
  &:focus {
    outline: none;
    border-color: #165dff;
    box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
  }
  
  &::placeholder {
    color: #999;
  }
}

/* 编辑器提示信息 */
.editor-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 4px;
  border-left: 3px solid #165dff;
}

/* 移动端响应式适配 */
@media (max-width: 768px) {
  .editor-main .toc-panel {
    width: 200px;
    position: fixed;
    top: 48px;
    left: 0;
    height: calc(100vh - 48px);
    z-index: 100;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  }
  .wysiwyg-edit-area {
    padding: 8px 16px !important;
  }
  .editor-loading {
    height: 300px;
  }
}
</style>