<template>
  <div class="markdown-renderer">
    <div class="renderer-header" v-if="showHeader">
      <div class="header-actions">
        <n-button size="small" quaternary @click="copyAllCode" v-if="showCopyAllBtn">
          <template #icon>
            <n-icon><copy-outline /></n-icon>
          </template>
          复制全部代码
        </n-button>
        <n-button size="small" quaternary @click="toggleToc" v-if="tocItems.length > 0">
          <template #icon>
            <n-icon><list-outline /></n-icon>
          </template>
          {{ showToc ? '隐藏目录' : '显示目录' }}
        </n-button>
        <n-button size="small" quaternary @click="scrollToTop">
          <template #icon>
            <n-icon><arrow-up /></n-icon>
          </template>
          返回顶部
        </n-button>
      </div>
    </div>
    
    <div class="renderer-container">
      <div v-if="showToc && tocItems.length > 0" class="toc-panel">
        <div class="toc-title">目录</div>
        <div class="toc-items">
          <div
            v-for="item in tocItems"
            :key="item.id"
            :class="['toc-item', `toc-level-${item.level}`, { active: activeHeadingId === item.id }]"
            :style="{ paddingLeft: (item.level - 1) * 16 + 'px' }"
            @click="scrollToHeading(item.id)"
          >
            {{ item.text }}
          </div>
        </div>
      </div>
      
      <div ref="contentRef" class="markdown-content" v-html="renderedContent"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import {
  CopyOutline,
  ListOutline,
  ArrowUp
} from '@vicons/ionicons5'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  showCopyAllBtn: {
    type: Boolean,
    default: true
  },
  maxHeight: {
    type: String,
    default: 'none'
  }
})

// Refs
const contentRef = ref(null)

// State
const tocItems = ref([])
const activeHeadingId = ref('')
const showToc = ref(false)
const observer = ref(null)

// 配置marked
marked.setOptions({
  highlight: function(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
    return hljs.highlight(code, { language }).value;
  },
  langPrefix: 'hljs language-',
  pedantic: false,
  gfm: true,
  breaks: false,
  sanitize: false,
  smartLists: true,
  smartypants: false,
  xhtml: false
});

// 计算属性
const renderedContent = computed(() => {
  if (!props.content) return ''
  
  let html = marked(props.content)
  
  // 为标题添加ID以便目录导航
  html = html.replace(/<h([1-6])>(.*?)<\/h\1>/g, (match, level, text) => {
    const id = text.replace(/[^\w\u4e00-\u9fff]/g, '-').toLowerCase()
    return `<h${level} id="${id}" class="heading-with-anchor">${text}<a href="#${id}" class="header-anchor">#</a></h${level}>`
  })
  
  return html
})

// 生成目录
const generateToc = async () => {
  await nextTick()
  if (!contentRef.value) return
  
  const headings = contentRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
  tocItems.value = Array.from(headings).map(heading => ({
    id: heading.id,
    text: heading.textContent.replace('#', '').replace(/\s+/g, ' ').trim(),
    level: parseInt(heading.tagName.charAt(1))
  }))
}

// 复制全部代码
const copyAllCode = async () => {
  if (!contentRef.value) return
  
  const codeBlocks = contentRef.value.querySelectorAll('pre code')
  if (codeBlocks.length === 0) {
    alert('没有找到代码块')
    return
  }
  
  let allCode = ''
  
  codeBlocks.forEach((block, index) => {
    allCode += `代码块 ${index + 1}:\n`
    allCode += '```' + (block.className.match(/language-(\w+)/)?.[1] || '') + '\n'
    allCode += block.textContent + '\n```\n\n'
  })
  
  allCode = allCode.trim()
  
  try {
    await navigator.clipboard.writeText(allCode)
    alert('所有代码已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = allCode
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    alert('所有代码已复制到剪贴板')
  }
}

// 切换目录显示
const toggleToc = () => {
  showToc.value = !showToc.value
}

// 滚动到顶部
const scrollToTop = () => {
  if (contentRef.value) {
    contentRef.value.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 滚动到标题
const scrollToHeading = (id) => {
  const element = document.getElementById(id)
  if (element && contentRef.value) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
    // 更新活动标题
    activeHeadingId.value = id
  }
}

// 监听滚动以高亮目录项
const handleScroll = () => {
  if (!contentRef.value || tocItems.value.length === 0) return
  
  const scrollPosition = contentRef.value.scrollTop + 100 // 偏移量避免刚好在边界
  const headings = contentRef.value.querySelectorAll('h1[id], h2[id], h3[id], h4[id], h5[id], h6[id]')
  
  let currentHeading = null
  for (let i = 0; i < headings.length; i++) {
    const heading = headings[i]
    const rect = heading.getBoundingClientRect()
    const contentRect = contentRef.value.getBoundingClientRect()
    
    if (rect.top - contentRect.top <= 0 && rect.bottom - contentRect.top > 0) {
      currentHeading = heading.getAttribute('id')
      break
    }
  }
  
  if (currentHeading) {
    activeHeadingId.value = currentHeading
  }
}

// 监听内容变化
watch(() => props.content, async () => {
  await nextTick()
  generateToc()
}, { immediate: true })

onMounted(async () => {
  await generateToc()
  
  if (contentRef.value) {
    contentRef.value.addEventListener('scroll', handleScroll)
    observer.value = new MutationObserver(() => {
      generateToc()
    })
    observer.value.observe(contentRef.value, {
      childList: true,
      subtree: true
    })
  }
})

onUnmounted(() => {
  if (contentRef.value) {
    contentRef.value.removeEventListener('scroll', handleScroll)
  }
  if (observer.value) {
    observer.value.disconnect()
  }
})
</script>

<style scoped>
.markdown-renderer {
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.renderer-header {
  padding: 8px 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: flex-end;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.renderer-container {
  display: flex;
  min-height: 300px;
  max-height: v-bind(maxHeight);
}

.toc-panel {
  width: 220px;
  background: #fafafa;
  border-right: 1px solid #e0e0e0;
  padding: 12px;
  overflow-y: auto;
  flex-shrink: 0;
}

.toc-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 4px;
}

.toc-items {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.toc-item {
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #555;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.toc-item:hover {
  background: #e9ecef;
}

.toc-item.active {
  background: #0ea5e9;
  color: white;
}

.toc-level-1 { padding-left: 0; font-weight: bold; font-size: 14px; }
.toc-level-2 { padding-left: 16px; }
.toc-level-3 { padding-left: 32px; }
.toc-level-4 { padding-left: 48px; }
.toc-level-5 { padding-left: 64px; }
.toc-level-6 { padding-left: 80px; }

.markdown-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  line-height: 1.6;
  color: #333;
}

/* 标题样式 */
.markdown-content :deep(h1) {
  font-size: 1.8em;
  margin: 0.67em 0 0.5em;
  border-bottom: 2px solid #eee;
  padding-bottom: 0.3em;
  color: #2c3e50;
  font-weight: 600;
}

.markdown-content :deep(h2) {
  font-size: 1.5em;
  margin: 0.83em 0 0.5em;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.3em;
  color: #34495e;
  font-weight: 600;
}

.markdown-content :deep(h3) {
  font-size: 1.25em;
  margin: 1em 0 0.5em;
  color: #34495e;
  font-weight: 600;
}

.markdown-content :deep(h4) {
  font-size: 1.1em;
  margin: 1.33em 0 0.5em;
  color: #555;
  font-weight: 600;
}

.markdown-content :deep(h5) {
  font-size: 1em;
  margin: 1.67em 0 0.5em;
  color: #666;
}

.markdown-content :deep(h6) {
  font-size: 0.85em;
  margin: 2.33em 0 0.5em;
  color: #777;
}

/* 段落样式 */
.markdown-content :deep(p) {
  margin: 1em 0;
  line-height: 1.8;
}

/* 代码块样式 */
.markdown-content :deep(pre) {
  background: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow: auto;
  font-size: 0.85em;
  line-height: 1.45;
  border: 1px solid #e1e4e8;
  margin: 1em 0;
}

.markdown-content :deep(code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.85em;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  border-radius: 0;
  overflow: visible;
}

/* 引用样式 */
.markdown-content :deep(blockquote) {
  margin: 0 0 1em;
  padding: 0.5em 1em 0.5em 1.2em;
  border-left: 4px solid #ddd;
  background: #f9f9f9;
  color: #666;
  font-style: italic;
}

/* 列表样式 */
.markdown-content :deep(ul), .markdown-content :deep(ol) {
  padding-left: 2em;
  margin: 1em 0;
}

.markdown-content :deep(li) {
  margin: 0.25em 0;
}

.markdown-content :deep(ul) {
  list-style-type: disc;
}

.markdown-content :deep(ol) {
  list-style-type: decimal;
}

/* 表格样式 */
.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
  background: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.markdown-content :deep(th), .markdown-content :deep(td) {
  border: 1px solid #dfe2e5;
  padding: 6px 13px;
  text-align: left;
}

.markdown-content :deep(th) {
  background-color: #f6f8fa;
  font-weight: 600;
  color: #24292e;
}

/* 图片样式 */
.markdown-content :deep(img) {
  max-width: 100%;
  box-sizing: content-box;
  margin: 1em 0;
  border-radius: 4px;
  border: 1px solid #e1e4e8;
}

/* 加粗、斜体、删除线样式 */
.markdown-content :deep(strong) {
  font-weight: 600;
  color: #24292e;
}

.markdown-content :deep(em) {
  font-style: italic;
  color: #586069;
}

.markdown-content :deep(del) {
  color: #6a737d;
  text-decoration: line-through;
}

/* 链接样式 */
.markdown-content :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

/* 锚点样式 */
.header-anchor {
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  text-decoration: none;
  color: inherit;
}

.heading-with-anchor:hover .header-anchor {
  opacity: 1;
}

/* 代码行号 */
.markdown-content :deep(.hljs-ln-numbers) {
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  -khtml-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  text-align: center;
  color: #999;
  vertical-align: top;
  padding-right: 8px;
  margin-right: 8px;
  border-right: 1px solid #e1e4e8;
}

.markdown-content :deep(.hljs-ln-code) {
  padding-left: 8px;
}
</style>