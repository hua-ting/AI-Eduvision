<template>
  <div class="material-detail">
    <n-spin :show="loading">
      <n-space vertical :size="20" v-if="material">
        <!-- 资料头部 -->
        <n-card :bordered="false" class="header-card">
          <n-space vertical :size="16">
            <div class="title-section">
              <h1 class="material-title">{{ material.title }}</h1>
              <n-space :size="12">
                <n-tag :bordered="false" type="primary" size="large" round>
                  {{ material.category }}
                </n-tag>
                <n-tag 
                  v-if="material.difficulty"
                  :bordered="false" 
                  :type="getDifficultyType(material.difficulty)" 
                  size="large"
                  round
                >
                  {{ material.difficulty }}
                </n-tag>
              </n-space>
            </div>
            
            <!-- 元信息 -->
            <n-space :size="24" class="meta-info">
              <n-space :size="8" align="center">
                <n-icon :component="PersonOutline" :size="18" />
                <span>{{ material.author || '未知' }}</span>
              </n-space>
              
              <n-space :size="8" align="center">
                <n-icon :component="TimeOutline" :size="18" />
                <span>{{ formatDate(material.createTime) }}</span>
              </n-space>
              
              <n-space :size="8" align="center">
                <n-icon :component="EyeOutline" :size="18" />
                <span>{{ formatNumber(material.viewCount) }} 浏览</span>
              </n-space>
              
              <n-space :size="8" align="center">
                <n-icon :component="HeartOutline" :size="18" />
                <span>{{ formatNumber(material.collectCount) }} 收藏</span>
              </n-space>
              
              <n-space :size="8" align="center">
                <n-icon :component="StarOutline" :size="18" />
                <span>{{ material.avgRating }} 分</span>
              </n-space>
            </n-space>
            
            <!-- 标签 -->
            <div class="tags" v-if="material.tags && material.tags.length > 0">
              <n-space :size="8">
                <n-tag 
                  v-for="(tag, index) in material.tags" 
                  :key="index"
                  :bordered="false" 
                  type="info"
                  round
                >
                  #{{ tag }}
                </n-tag>
              </n-space>
            </div>
          </n-space>
        </n-card>
        
        <!-- AI智能摘要 -->
        <n-card :bordered="false" v-if="material.summary" class="ai-summary-card">
          <template #header>
            <n-space align="center">
              <n-icon :component="SparklesOutline" :size="24" color="#f59e0b" />
              <n-gradient-text type="warning" :size="20">
                AI智能摘要
              </n-gradient-text>
            </n-space>
          </template>
          
          <n-space vertical :size="16">
            <div class="summary-text">
              {{ material.summary }}
            </div>
            
            <!-- 关键要点 -->
            <div v-if="material.keyPoints && material.keyPoints.length > 0">
              <n-text strong>📌 关键要点：</n-text>
              <n-ul class="key-points-list">
                <n-li v-for="(point, index) in material.keyPoints" :key="index">
                  {{ point }}
                </n-li>
              </n-ul>
            </div>
            
            <!-- 关键词 -->
            <div v-if="material.keywords && material.keywords.length > 0">
              <n-text strong>🎯 关键词：</n-text>
              <n-space :size="8" style="margin-top: 8px;">
                <n-tag 
                  v-for="(keyword, index) in material.keywords" 
                  :key="index"
                  :bordered="false"
                  type="success"
                  round
                >
                  {{ keyword }}
                </n-tag>
              </n-space>
            </div>
          </n-space>
        </n-card>
        
        <!-- 资料描述 -->
        <n-card :bordered="false">
          <template #header>
            <n-text strong :size="18">📝 资料描述</n-text>
          </template>
          <MarkdownRenderer :content="material.description || '暂无描述'" />
        </n-card>
        
        <!-- 操作区域 -->
        <n-card :bordered="false" class="action-card">
          <n-space justify="space-between" align="center">
            <n-space :size="12">
              <n-button 
                :type="material.isCollected ? 'error' : 'primary'"
                size="large"
                round
                @click="handleCollect"
              >
                <template #icon>
                  <n-icon>
                    <Heart v-if="material.isCollected" />
                    <HeartOutline v-else />
                  </n-icon>
                </template>
                {{ material.isCollected ? '取消收藏' : '收藏资料' }}
              </n-button>
              
              <n-button size="large" round>
                <template #icon>
                  <n-icon :component="DownloadOutline" />
                </template>
                下载资料
              </n-button>
            </n-space>
            
            <n-space align="center" :size="16">
              <n-text>我的评分：</n-text>
              <n-rate 
                :value="material.userRating || 0"
                :count="5"
                size="large"
                @update:value="handleRate"
              />
              <n-text depth="3">
                平均分：<n-text type="warning" strong>{{ material.avgRating }}</n-text> 分
              </n-text>
            </n-space>
          </n-space>
        </n-card>
      </n-space>
      
      <!-- 空状态 -->
      <n-empty 
        v-else-if="!loading"
        description="资料不存在"
        size="large"
        style="margin-top: 100px;"
      >
        <template #icon>
          <n-icon :component="DocumentTextOutline" :size="80" />
        </template>
      </n-empty>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { 
  PersonOutline, 
  TimeOutline, 
  EyeOutline, 
  HeartOutline, 
  StarOutline,
  SparklesOutline,
  DownloadOutline,
  Heart,
  DocumentTextOutline
} from '@vicons/ionicons5'
import { getMaterialDetail, toggleCollect, rateMaterial, recordView } from '@/api/material'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const route = useRoute()
const message = useMessage()

const material = ref(null)
const loading = ref(false)
const startTime = ref(null) // 记录页面进入时间

// 获取难度类型
const getDifficultyType = (difficulty) => {
  const typeMap = {
    '初级': 'success',
    '中级': 'warning',
    '高级': 'error'
  }
  return typeMap[difficulty] || 'default'
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 获取资料详情
const fetchMaterialDetail = async () => {
  try {
    loading.value = true
    const res = await getMaterialDetail(route.params.id)
    material.value = res.data
    
    // 记录进入页面时间
    startTime.value = Date.now()
  } catch (error) {
    console.error('获取资料详情失败:', error)
    message.error('获取资料详情失败')
  } finally {
    loading.value = false
  }
}

// 收藏/取消收藏
const handleCollect = async () => {
  try {
    await toggleCollect(route.params.id)
    message.success('操作成功')
    fetchMaterialDetail()
  } catch (error) {
    message.error('操作失败')
  }
}

// 评分
const handleRate = async (rating) => {
  try {
    await rateMaterial(route.params.id, rating)
    message.success('评分成功')
    fetchMaterialDetail()
  } catch (error) {
    message.error('评分失败')
  }
}

// 记录浏览行为
const recordViewBehavior = async () => {
  if (!startTime.value) return
  
  const duration = Math.floor((Date.now() - startTime.value) / 1000)
  try {
    await recordView(route.params.id, duration)
  } catch (error) {
    console.error('记录浏览行为失败:', error)
  }
}

onMounted(() => {
  fetchMaterialDetail()
})
</script>

<style scoped>
.header-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.title-section {
  margin-bottom: 16px;
}

.material-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #333;
  line-height: 1.4;
}

.meta-info {
  color: #666;
  font-size: 14px;
}

.tags {
  margin-top: 16px;
}

.ai-summary-card {
  box-shadow: 0 2px 12px rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.summary-text {
  line-height: 1.8;
  font-size: 15px;
  color: #444;
}

.key-points-list {
  margin-top: 12px;
  line-height: 1.8;
}

/* 移除旧的代码样式类，使用全局样式 */
</style>