<template>
  <div class="daily-recommend">
    <n-page-header>
      <template #title>
        <n-gradient-text type="info" :size="28">
          🎯 AI知识点创作
        </n-gradient-text>
      </template>
      <template #subtitle>
        基于您的学习画像，AI为您生成个性化知识点主题
      </template>
      <template #extra>
        <n-button secondary type="warning" @click="clearRecommendationsCache" :loading="topicsLoading">
          <template #icon>
            <n-icon><refresh-outline /></n-icon>
          </template>
          重新生成推荐
        </n-button>
      </template>
    </n-page-header>

    <!-- 用户画像概览 -->
    <n-card title="📊 您的学习画像" :bordered="false" style="margin-top: 20px;">
      <n-descriptions :column="4" bordered>
        <n-descriptions-item label="学习水平">
          <n-tag :type="getLevelType(userProfile.learningLevel)" round>
            {{ userProfile.learningLevel || '未知' }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="学习时长">
          {{ formatDuration(userProfile.learningDuration) }}
        </n-descriptions-item>
        <n-descriptions-item label="偏好分类">
          <n-tag type="success" round>{{ userProfile.favoriteCategory || '待分析' }}</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="活跃度">
          <n-progress 
            type="line" 
            :percentage="calculateActivity()" 
            :indicator-placement="'inside'"
            processing
          />
        </n-descriptions-item>
      </n-descriptions>
    </n-card>

    <!-- 步骤1: 选择主题 -->
    <n-card v-if="step === 1" title="💡 选择您感兴趣的主题" :bordered="false" style="margin-top: 20px;">
      <template #header-extra>
        <n-button secondary type="primary" @click="generateTopics" :loading="topicsLoading">
          <template #icon>
            <n-icon><refresh-outline /></n-icon>
          </template>
          重新生成主题
        </n-button>
      </template>
      
      <n-spin :show="topicsLoading">
        <n-grid :cols="24" :x-gap="16" :y-gap="16">
          <n-grid-item 
            v-for="(topic, index) in topics" 
            :key="index" 
            :span="12"
          >
            <n-card 
              hoverable 
              :class="['topic-card', { 'selected': selectedTopic === topic }]"
              @click="selectTopic(topic)"
            >
              <div class="topic-header">
                <n-icon size="32" :color="topic.color">
                  <bulb-outline />
                </n-icon>
                <h3 class="topic-title">{{ topic.title }}</h3>
              </div>
              <p class="topic-desc">{{ topic.description }}</p>
              <n-space :size="8" style="margin-top: 12px;">
                <n-tag :bordered="false" type="info" size="small" round>
                  {{ topic.category }}
                </n-tag>
                <n-tag :bordered="false" type="warning" size="small" round>
                  {{ topic.difficulty }}
                </n-tag>
              </n-space>
              <div v-if="selectedTopic === topic" class="selected-badge">
                <n-icon size="24" color="#fff">
                  <checkmark-circle />
                </n-icon>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>

        <n-empty 
          v-if="topics.length === 0 && !topicsLoading" 
          description="暂无主题，点击上方按钮生成"
        />
      </n-spin>

      <n-space justify="center" style="margin-top: 24px;">
        <n-button 
          type="primary" 
          size="large" 
          :disabled="!selectedTopic"
          @click="goToGenerate"
        >
          下一步：生成知识点
        </n-button>
      </n-space>
    </n-card>

    <!-- 步骤2: 生成知识点 -->
    <n-card v-if="step === 2" title="✨ AI生成知识点" :bordered="false" style="margin-top: 20px;">
      <template #header-extra>
        <n-button text @click="backToTopics">
          ← 返回主题选择
        </n-button>
      </template>

      <n-alert type="info" style="margin-bottom: 20px;">
        <template #icon>
          <n-icon><bulb-outline /></n-icon>
        </template>
        已选择主题：<strong>{{ selectedTopic?.title }}</strong>
      </n-alert>

      <n-spin :show="generating">
        <div v-if="!generatedKP" class="generate-placeholder">
          <n-empty description="点击下方按钮开始生成知识点">
            <template #icon>
              <n-icon size="80" color="#ccc">
                <sparkles-outline />
              </n-icon>
            </template>
          </n-empty>
          <n-space justify="center" style="margin-top: 24px;">
            <n-button 
              type="primary" 
              size="large" 
              @click="generateKnowledgePoint"
              :loading="generating"
            >
              <template #icon>
                <n-icon><sparkles-outline /></n-icon>
              </template>
              AI生成知识点
            </n-button>
          </n-space>
        </div>

        <div v-else class="kp-editor">
          <n-form ref="formRef" :model="generatedKP" label-placement="top" require-mark-placement="left">
            <n-form-item label="知识点标题" path="title" required>
              <n-input 
                v-model:value="generatedKP.title" 
                placeholder="请输入知识点标题"
                :maxlength="100"
                show-count
              />
            </n-form-item>

            <n-grid :cols="2" :x-gap="16">
              <n-grid-item>
                <n-form-item label="分类" path="category" required>
                  <n-select 
                    v-model:value="generatedKP.category" 
                    :options="categoryOptions"
                  />
                </n-form-item>
              </n-grid-item>
              <n-grid-item>
                <n-form-item label="难度" path="difficulty" required>
                  <n-select 
                    v-model:value="generatedKP.difficulty" 
                    :options="difficultyOptions"
                  />
                </n-form-item>
              </n-grid-item>
            </n-grid>

            <n-form-item label="简要描述" path="description">
              <n-input 
                v-model:value="generatedKP.description" 
                type="textarea"
                placeholder="请输入简要描述"
                :autosize="{ minRows: 2, maxRows: 4 }"
                :maxlength="200"
                show-count
              />
            </n-form-item>

            <n-form-item label="详细内容" path="content" required style="height: 400px;">
              <MarkdownEditor
                v-model="generatedKP.content"
                :auto-save-key="'daily-knowledge-' + Date.now()"
                placeholder="请输入详细内容，支持Markdown格式"
                :max-height="'350px'"
              />
            </n-form-item>

            <n-form-item label="标签">
              <n-dynamic-tags v-model:value="generatedKP.tags" />
            </n-form-item>
          </n-form>

          <n-space justify="end" style="margin-top: 24px;">
            <n-button @click="regenerateKP" :loading="generating">
              <template #icon>
                <n-icon><refresh-outline /></n-icon>
              </template>
              重新生成
            </n-button>
            <n-button type="primary" @click="submitForReview" :loading="submitting">
              <template #icon>
                <n-icon><cloud-upload-outline /></n-icon>
              </template>
              提交审核
            </n-button>
          </n-space>
        </div>
      </n-spin>
    </n-card>

    <!-- 使用说明 -->
    <n-card title="❓ 使用说明" :bordered="false" style="margin-top: 20px;">
      <n-steps :current="step" :status="step === 2 && generatedKP ? 'finish' : 'process'">
        <n-step title="选择主题" description="AI根据您的画像生成个性化主题" />
        <n-step title="生成知识点" description="使用AI自动生成知识点内容" />
        <n-step title="编辑提交" description="编辑内容后提交审核" />
        <n-step title="审核通过" description="审核通过后加入知识库" />
      </n-steps>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import {
  RefreshOutline,
  BulbOutline,
  CheckmarkCircle,
  SparklesOutline,
  CloudUploadOutline
} from '@vicons/ionicons5'
import { getUserProfile } from '@/api/userProfile'
import { askQuestion } from '@/api/qa'
import { contributeKnowledgePoint, batchValidateContent } from '@/api/knowledgePoint'
import { clearDailyRecommendationsCache, getDailyRecommendTopics, clearDailyRecommendTopicsCache } from '@/api/daily'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const router = useRouter()
const message = useMessage()
const step = ref(1) // 1: 选择主题, 2: 生成知识点

const userProfile = ref({
  learningLevel: '',
  learningDuration: 0,
  favoriteCategory: '',
  totalKnowledgeViews: 0,
  totalKnowledgeCollects: 0,
  totalQaCount: 0
})

const topicsLoading = ref(false)
const topics = ref([])
const selectedTopic = ref(null)

const generating = ref(false)
const submitting = ref(false)
const generatedKP = ref(null)

const categoryOptions = [
  { label: '算法', value: '算法' },
  { label: '数据库', value: '数据库' },
  { label: '人工智能', value: '人工智能' },
  { label: '前端开发', value: '前端开发' },
  { label: '后端开发', value: '后端开发' },
  { label: '计算机网络', value: '计算机网络' },
  { label: '操作系统', value: '操作系统' },
  { label: '其他', value: '其他' }
]

const difficultyOptions = [
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' }
]

const topicColors = ['#667eea', '#f59e0b', '#10b981', '#ec4899', '#8b5cf6', '#06b6d4']

// 获取用户画像
const fetchUserProfile = async () => {
  try {
    const { data } = await getUserProfile()
    userProfile.value = data
  } catch (error) {
    console.error('获取用户画像失败:', error)
  }
}

// 生成主题
const generateTopics = async () => {
  topicsLoading.value = true
  try {
    // 调用后端API获取主题，使用缓存机制
    const { data } = await getDailyRecommendTopics(6)
    
    // 使用预设的颜色
    topics.value = data.map((topic, index) => ({
      ...topic,
      color: topicColors[index % topicColors.length]
    }))
    
    message.success('主题获取成功')
  } catch (error) {
    console.error('获取主题失败:', error)
    message.error('获取主题失败，请重试')
    
    // 如果后端API失败，使用默认主题
    topics.value = getDefaultTopics()
  } finally {
    topicsLoading.value = false
  }
}

// 构建主题生成提示词
const buildTopicsPrompt = () => {
  const level = userProfile.value.learningLevel || '中级'
  const category = userProfile.value.favoriteCategory || '计算机'
  
  return `根据以下信息生成 6 个学习主题：

- 学习水平：${level}
- 偏好方向：${category}

要求：
1. 主题难度适合 ${level}
2. 以 ${category} 为主，可适当扩展相关领域
3. 每个主题要具体实用

只返回 JSON 数组（不要其他文字）：
[
  {
    "title": "主题名称",
    "description": "为什么学这个（50字内）",
    "category": "分类（算法/数据库/人工智能/前端开发/后端开发/计算机网络/操作系统/其他）",
    "difficulty": "初级/中级/高级"
  }
]`
}

// 解析主题
const parseTopics = (answer) => {
  try {
    // 提取JSON部分
    let jsonStr = answer
    const start = answer.indexOf('[')
    const end = answer.lastIndexOf(']') + 1
    if (start >= 0 && end > start) {
      jsonStr = answer.substring(start, end)
    }
    
    // 清理JSON字符串，移除可能的额外字符
    jsonStr = jsonStr.trim()
    
    // 如果仍然无法解析，尝试更严格的清理
    if (jsonStr.charAt(0) !== '[' || jsonStr.charAt(jsonStr.length - 1) !== ']') {
      throw new Error('Invalid JSON format')
    }
    
    return JSON.parse(jsonStr)
  } catch (error) {
    console.error('解析主题失败:', error)
    console.log('原始响应:', answer)
    return []
  }
}

// 默认主题
const getDefaultTopics = () => {
  let category = userProfile.value.favoriteCategory

  // 如果画像中还没有favoriteCategory，尝试从兴趣标签推断
  if (!category && userProfile.value.interestTags) {
    try {
      const rawTags = userProfile.value.interestTags
      const tags = typeof rawTags === 'string' ? JSON.parse(rawTags) : rawTags
      if (Array.isArray(tags) && tags.length > 0) {
        category = tags[0]
      }
    } catch (e) {
      // 忽略解析错误，使用后备默认值
    }
  }

  if (!category) {
    category = '算法'
  }

  const level = userProfile.value.learningLevel || '中级'
  
  return [
    {
      title: `${category}核心概念入门`,
      description: `掌握${category}的基础概念和核心思想，为深入学习打下坚实基础`,
      category: category,
      difficulty: level,
      color: topicColors[0]
    },
    {
      title: `${category}实战应用技巧`,
      description: `学习${category}在实际项目中的应用方法和最佳实践`,
      category: category,
      difficulty: level,
      color: topicColors[1]
    },
    {
      title: `${category}性能优化策略`,
      description: `深入理解性能优化原理，提升系统效率和用户体验`,
      category: category,
      difficulty: level,
      color: topicColors[2]
    },
    {
      title: `${category}常见问题解析`,
      description: `解决学习和开发中的常见难题，避免踩坑`,
      category: category,
      difficulty: level,
      color: topicColors[3]
    },
    {
      title: `${category}进阶技术探索`,
      description: `探索高级特性和前沿技术，拓展技术视野`,
      category: category,
      difficulty: level,
      color: topicColors[4]
    },
    {
      title: `${category}项目实战案例`,
      description: `通过真实项目案例学习完整的开发流程和架构设计`,
      category: category,
      difficulty: level,
      color: topicColors[5]
    }
  ]
}

// 选择主题
const selectTopic = (topic) => {
  selectedTopic.value = topic
}

// 前往生成
const goToGenerate = () => {
  step.value = 2
}

// 返回主题选择
const backToTopics = () => {
  step.value = 1
  generatedKP.value = null
}

// 生成知识点
const generateKnowledgePoint = async () => {
  generating.value = true
  try {
    // 使用选中的主题调用AI生成知识点，这是用户真实需求，应该出现在问答历史
    const question = `请讲解：${selectedTopic.value.title}

背景：${selectedTopic.value.description}

要求用 Markdown 格式写清楚，包含核心概念、应用场景和注意事项即可，有代码示例更好。字数 300-800 字。`
    
    // 标记为每日推荐来源，这样不会记录到普通问答历史中
    const { data } = await askQuestion(question, { source: 'DAILY_TOPIC', timeout: 30000 })
    
    // 构建知识点对象
    generatedKP.value = {
      title: selectedTopic.value.title,
      category: selectedTopic.value.category,
      difficulty: selectedTopic.value.difficulty,
      description: selectedTopic.value.description,
      content: data.answer, // AI返回的已经是Markdown格式
      tags: extractTags(selectedTopic.value.title)
    }
    
    message.success('知识点生成成功，请编辑后提交审核')
  } catch (error) {
    console.error('生成知识点失败:', error)
    message.error('生成知识点失败，请重试')
  } finally {
    generating.value = false
  }
}

// 重新生成
const regenerateKP = async () => {
  generatedKP.value = null
  await generateKnowledgePoint()
}

// 提取标签
const extractTags = (title) => {
  const tags = [selectedTopic.value.category]
  if (title.includes('入门')) tags.push('入门')
  if (title.includes('实战')) tags.push('实战')
  if (title.includes('优化')) tags.push('性能优化')
  if (title.includes('进阶')) tags.push('进阶')
  return tags
}

// 提交审核
const submitForReview = async () => {
  if (!generatedKP.value.title || !generatedKP.value.content) {
    message.warning('请填写完整的知识点信息')
    return
  }
  
  submitting.value = true
  try {
    // 批量验证内容
    const validationResponse = await batchValidateContent([{
      title: generatedKP.value.title,
      category: generatedKP.value.category,
      subCategory: '',
      difficulty: generatedKP.value.difficulty,
      description: generatedKP.value.description,
      content: generatedKP.value.content,
      tags: generatedKP.value.tags || []
    }]);
    const validationErrors = validationResponse.data && validationResponse.data[-1];
    
    if (validationErrors && validationErrors.length > 0) {
      message.warning('内容格式有问题：' + validationErrors.join('，'));
      return;
    }
    
    await contributeKnowledgePoint({
      title: generatedKP.value.title,
      category: generatedKP.value.category,
      subCategory: '',
      difficulty: generatedKP.value.difficulty,
      description: generatedKP.value.description,
      content: generatedKP.value.content,
      tags: generatedKP.value.tags || [], // 直接发送数组，不要JSON.stringify
      status: 0 // 待审核
    })
    
    message.success('知识点已提交审核，审核通过后将加入知识库')
    
    // 重置状态
    setTimeout(() => {
      step.value = 1
      generatedKP.value = null
      selectedTopic.value = null
    }, 1500)
  } catch (error) {
    console.error('提交失败:', error)
    message.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 获取学习水平类型
const getLevelType = (level) => {
  const typeMap = {
    '初级': 'success',
    '中级': 'warning',
    '高级': 'error'
  }
  return typeMap[level] || 'default'
}

// 格式化学习时长
const formatDuration = (minutes) => {
  if (!minutes) return '0小时'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
  }
  return `${mins}分钟`
}

// 计算活跃度
const calculateActivity = () => {
  const total = (userProfile.value.totalKnowledgeViews || 0) + 
                (userProfile.value.totalKnowledgeCollects || 0) * 2 + 
                (userProfile.value.totalQaCount || 0) * 3
  return Math.min(100, total)
}

// 清除推荐缓存
const clearRecommendationsCache = async () => {
  try {
    topicsLoading.value = true
    await clearDailyRecommendationsCache()
    await clearDailyRecommendTopicsCache() // 同时清除主题缓存
    message.success('推荐缓存已清除，正在重新生成推荐...')
    // 重新获取推荐
    await generateTopics()
  } catch (error) {
    console.error('清除缓存失败:', error)
    message.error('清除缓存失败，请重试')
  } finally {
    topicsLoading.value = false
  }
}

onMounted(async () => {
  await fetchUserProfile()
  await generateTopics()
})
</script>

<style scoped>
.daily-recommend {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.topic-card {
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  height: 100%;
  border: 2px solid transparent;
}

.topic-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.topic-card.selected {
  border-color: #18a058;
  box-shadow: 0 8px 24px rgba(24, 160, 88, 0.25);
}

.topic-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.topic-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
}

.topic-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin: 0;
}

.selected-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #18a058;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: scaleIn 0.3s ease;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.generate-placeholder {
  padding: 40px 0;
}

.kp-editor {
  background: white;
  padding: 24px;
  border-radius: 8px;
}
</style>
