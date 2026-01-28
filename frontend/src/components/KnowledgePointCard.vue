<template>
  <n-card 
    hoverable 
    class="knowledge-card"
    @click="$emit('view', knowledgePoint.id)"
  >
    <!-- 知识点封面 -->
    <template #cover>
      <div class="cover-wrapper">
        <div class="cover-placeholder" :style="{ background: getIconColor(knowledgePoint.difficulty) }">
          <n-icon size="60" color="rgba(255,255,255,0.8)">
            <BulbOutline />
          </n-icon>
        </div>
        <!-- 难度标签 -->
        <div class="difficulty-badge" v-if="knowledgePoint.difficulty">
          <n-tag :type="getDifficultyType(knowledgePoint.difficulty)" size="small" :bordered="false" round>
            {{ knowledgePoint.difficulty }}
          </n-tag>
        </div>
      </div>
    </template>
    
    <!-- 知识点信息 -->
    <div class="knowledge-info">
      <div class="title-wrapper">
        <n-ellipsis :line-clamp="2" class="title">
          {{ knowledgePoint.title }}
        </n-ellipsis>
      </div>
      
      <div class="meta">
        <n-space :size="8">
          <n-tag :bordered="false" size="small" type="primary" round>
            {{ knowledgePoint.category }}
          </n-tag>
          <n-tag :bordered="false" size="small" type="info" round v-if="knowledgePoint.subCategory">
            {{ knowledgePoint.subCategory }}
          </n-tag>
        </n-space>
      </div>
      
      <!-- 描述 -->
      <n-ellipsis :line-clamp="2" class="description" v-if="knowledgePoint.description">
        {{ knowledgePoint.description }}
      </n-ellipsis>
      
      <!-- 标签 -->
      <div class="tags" v-if="knowledgePoint.tags && knowledgePoint.tags.length > 0">
        <n-space :size="6">
          <n-tag 
            v-for="(tag, index) in knowledgePoint.tags.slice(0, 3)" 
            :key="index"
            :bordered="false" 
            size="tiny"
            type="success"
            round
          >
            {{ tag }}
          </n-tag>
          <n-tag 
            v-if="knowledgePoint.tags.length > 3" 
            :bordered="false" 
            size="tiny"
            type="default"
            round
          >
            +{{ knowledgePoint.tags.length - 3 }}
          </n-tag>
        </n-space>
      </div>
      
      <!-- 统计信息 -->
      <div class="stats">
        <n-space :size="16" align="center">
          <div class="stat-item">
            <n-icon size="16" :component="EyeOutline" />
            <span>{{ formatNumber(knowledgePoint.viewCount || 0) }}</span>
          </div>
          
          <div class="stat-item">
            <n-icon size="16" :component="StarOutline" />
            <span>{{ knowledgePoint.avgRating || 0 }}</span>
          </div>
          
          <div class="stat-item">
            <n-icon size="16" :component="HeartOutline" />
            <span>{{ formatNumber(knowledgePoint.collectCount || 0) }}</span>
          </div>
        </n-space>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <template #action>
      <n-space justify="space-between" align="center">
        <n-button 
          text 
          :type="knowledgePoint.isCollected ? 'error' : 'default'"
          @click.stop="$emit('collect', knowledgePoint.id)"
          size="small"
        >
          <template #icon>
            <n-icon>
              <Heart v-if="knowledgePoint.isCollected" />
              <HeartOutline v-else />
            </n-icon>
          </template>
          {{ knowledgePoint.isCollected ? '已收藏' : '收藏' }}
        </n-button>
        
        <n-rate 
          :value="knowledgePoint.userRating || 0"
          :count="5"
          size="small"
          @update:value="(rating) => $emit('rate', { knowledgePointId: knowledgePoint.id, rating })"
          @click.stop
        />
      </n-space>
    </template>
  </n-card>
</template>

<script setup>
import { BulbOutline, EyeOutline, StarOutline, HeartOutline, Heart } from '@vicons/ionicons5'

const props = defineProps({
  knowledgePoint: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['view', 'collect', 'rate'])

const getDifficultyType = (difficulty) => {
  const typeMap = {
    '初级': 'success',
    '中级': 'warning',
    '高级': 'error'
  }
  return typeMap[difficulty] || 'default'
}

// 根据难度获取图标颜色
const getIconColor = (difficulty) => {
  const colorMap = {
    '初级': '#10b981',  // 翡翠绿
    '中级': '#f59e0b',  // 琥珀黄
    '高级': '#ef4444'   // 玫瑰红
  }
  return colorMap[difficulty] || '#94a3b8'
}

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}
</script>

<style scoped>
.knowledge-card {
  height: 100%;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 20px;
  overflow: hidden;
  border: 3px solid #C7D2FE;
  background: url('https://images.unsplash.com/photo-1509228468518-180dd4864904?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
}

.knowledge-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(79, 70, 229, 0.25);
  border-color: #4F46E5;
}

.cover-wrapper {
  position: relative;
  height: 140px;
}

.cover-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('https://images.unsplash.com/photo-1488190211105-8b0e65b80b4e?w=800&q=80');
  background-size: cover;
  background-position: center;
}

.difficulty-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.knowledge-info {
  padding: 4px 0;
}

.title-wrapper {
  margin-bottom: 12px;
}

.title {
  font-family: 'Fredoka', cursive;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
  color: #1E1B4B;
  text-shadow: 0 1px 3px rgba(255, 255, 255, 0.8);
}

.meta {
  margin-bottom: 10px;
}

.description {
  font-size: 13px;
  color: #334155;
  margin-bottom: 12px;
  line-height: 1.6;
  min-height: 36px;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
}

.tags {
  margin-bottom: 12px;
  min-height: 24px;
}

.stats {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}

.stat-item:hover {
  color: #666;
}
</style>
