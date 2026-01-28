# Markdown组件使用指南

## 1. MarkdownEditor 组件

### 1.1 基本用法

```vue
<template>
  <MarkdownEditor 
    v-model="content" 
    placeholder="请输入Markdown内容..."
    :auto-save-key="'unique-key-for-this-editor'"
    :max-height="'500px'"
  />
</template>

<script setup>
import { ref } from 'vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const content = ref('# Hello World\n\nThis is a **markdown** editor.')
</script>
```

### 1.2 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| v-model | String | '' | 绑定的Markdown内容 |
| placeholder | String | '请输入Markdown内容...' | 输入框占位符 |
| previewModeOnly | Boolean | false | 是否只显示预览模式 |
| autoSaveKey | String | null | 自动保存的唯一键值，用于区分不同编辑器 |
| maxHeight | String | '500px' | 编辑器最大高度 |

### 1.3 功能特性

- **工具栏**: 提供常用的Markdown格式化按钮
- **实时预览**: 支持编辑和预览模式切换
- **自动保存**: 自动保存草稿到localStorage
- **目录导航**: 自动生成文档目录
- **代码复制**: 支持复制单个或全部代码块

## 2. MarkdownRenderer 组件

### 2.1 基本用法

```vue
<template>
  <MarkdownRenderer :content="markdownContent" />
</template>

<script setup>
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const markdownContent = `
# 标题
这是一个**富文本**内容。

## 代码示例
\`\`\`javascript
console.log('Hello World');
\`\`\`
`
</script>
```

### 2.2 属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| content | String | '' | 要渲染的Markdown内容 |
| showHeader | Boolean | true | 是否显示操作头部 |
| showCopyAllBtn | Boolean | true | 是否显示复制全部代码按钮 |
| maxHeight | String | 'none' | 最大高度限制 |

### 2.3 功能特性

- **语法高亮**: 支持多种编程语言的代码高亮
- **目录导航**: 自动生成文档目录并支持点击跳转
- **代码复制**: 支持复制全部代码块
- **锚点链接**: 自动为标题生成锚点链接

## 3. 在现有页面中替换旧组件

### 3.1 知识点详情页 (KnowledgePointDetail.vue)

```vue
<!-- 替换原来的 -->
<div class="markdown-content" v-html="renderMarkdown(knowledgePoint.content)"></div>

<!-- 为 -->
<MarkdownRenderer :content="knowledgePoint.content" />
```

### 3.2 资料详情页 (MaterialDetail.vue)

```vue
<!-- 替换原来的 -->
<div class="markdown-content" v-html="renderMarkdown(material.description || '暂无描述')"></div>

<!-- 为 -->
<MarkdownRenderer :content="material.description || '暂无描述'" />
```

### 3.3 知识点贡献页 (KnowledgeSquare.vue)

```vue
<!-- 替换原来的 -->
<n-input
  v-model:value="contributeForm.content"
  type="textarea"
  placeholder="请输入知识点详细内容"
  :rows="5"
/>

<!-- 为 -->
<MarkdownEditor
  v-model="contributeForm.content"
  :auto-save-key="'knowledge-contribute'"
  placeholder="请输入知识点详细内容，支持Markdown格式"
  :max-height="'400px'"
/>
```

## 4. 注意事项

### 4.1 性能优化

- 对于长文档，建议使用 `maxHeight` 属性限制容器高度
- 在列表页中避免同时渲染大量Markdown内容，考虑使用虚拟滚动
- 对于频繁更新的内容，可使用 `v-memo` 进行优化

### 4.2 数据安全

- 所有Markdown内容都会经过净化处理，防止XSS攻击
- 不要在Markdown中直接嵌入脚本标签
- 服务端存储时也应进行相应的安全检查

### 4.3 样式定制

全局样式定义在 `src/styles/markdown-styles.css` 中，可以根据需要进行调整：

```css
/* 自定义代码块样式 */
.markdown-content pre {
  background: #2d3748; /* 深色主题 */
  border-radius: 8px;
  padding: 16px;
  overflow: auto;
}

/* 自定义标题样式 */
.markdown-content h1 {
  color: #2d3748;
  border-bottom: 2px solid #cbd5e0;
}
```

## 5. 扩展功能

### 5.1 自定义工具栏

如果需要自定义工具栏，可以通过插槽实现：

```vue
<MarkdownEditor v-model="content">
  <template #toolbar>
    <div class="custom-toolbar">
      <!-- 自定义工具按钮 -->
    </div>
  </template>
</MarkdownEditor>
```

### 5.2 事件监听

组件提供了多个事件供监听：

```vue
<MarkdownEditor 
  v-model="content"
  @change="onContentChange"
  @save-draft="onSaveDraft"
  @preview-toggle="onPreviewToggle"
/>
```

## 6. 最佳实践

1. **唯一自动保存键**: 为每个编辑器提供唯一的 `autoSaveKey`，避免草稿冲突
2. **内容长度限制**: 对用户输入的Markdown内容设置合理长度限制
3. **权限验证**: 在编辑和保存前验证用户权限
4. **错误处理**: 妥善处理渲染错误，提供备用显示方案
5. **移动端适配**: 在小屏幕上调整编辑器布局和字体大小