<template>
  <n-card 
    hoverable 
    class="material-card"
    @click="$emit('view', material.id)"
  >
    <!-- 资料封面 -->
    <template #cover>
      <div class="cover-wrapper">
        <div v-if="material.coverUrl" class="cover-image">
          <picture>
            <source :srcset="material.coverUrl.replace(/\\.(jpg|jpeg|png)$/, '.webp')" type="image/webp">
            <img :src="material.coverUrl" loading="lazy" alt="资料封面">
          </picture>
        </div>
        <div v-else class="cover-placeholder" :style="{ background: getIconColor(material.difficulty) }">
          <n-icon size="60" color="rgba(255,255,255,0.8)">
            <DocumentTextOutline />
          </n-icon>
        </div>
        <!-- 难度标签 -->
        <div class="difficulty-badge" v-if="material.difficulty">
          <n-tag :type="getDifficultyType(material.difficulty)" size="small" :bordered="false" round>
            {{ material.difficulty }}
          </n-tag>
        </div>
      </div>
    </template>
    
    <!-- 资料信息 -->
    <div class="material-info">
      <div class="title-wrapper">
        <n-ellipsis :line-clamp="2" class="title">
          {{ material.title }}
        </n-ellipsis>
      </div>
      
      <div class="meta">
        <n-space :size="8">
          <n-tag :bordered="false" size="small" type="primary" round>
            {{ material.category }}
          </n-tag>
          <n-tag :bordered="false" size="small" type="info" round v-if="material.author">
            {{ material.author }}
          </n-tag>
        </n-space>
      </div>
      
      <!-- 描述 -->
      <n-ellipsis :line-clamp="2" class="description" v-if="material.description">
        {{ material.description }}
      </n-ellipsis>
      
      <!-- 标签 -->
      <div class="tags" v-if="material.tags && material.tags.length > 0">
        <n-space :size="6">
          <n-tag 
            v-for="(tag, index) in material.tags.slice(0, 3)" 
            :key="index"
            :bordered="false" 
            size="tiny"
            type="success"
            round
          >
            {{ tag }}
          </n-tag>
          <n-tag 
            v-if="material.tags.length > 3" 
            :bordered="false" 
            size="tiny"
            type="default"
            round
          >
            +{{ material.tags.length - 3 }}
          </n-tag>
        </n-space>
      </div>
      
      <!-- 统计信息 -->
      <div class="stats">
        <n-space :size="16" align="center">
          <div class="stat-item">
            <n-icon size="16" :component="EyeOutline" />
            <span>{{ formatNumber(material.viewCount || 0) }}</span>
          </div>
          
          <div class="stat-item">
            <n-icon size="16" :component="StarOutline" />
            <span>{{ material.avgRating || 0 }}</span>
          </div>
          
          <div class="stat-item">
            <n-icon size="16" :component="HeartOutline" />
            <span>{{ formatNumber(material.collectCount || 0) }}</span>
          </div>
        </n-space>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <template #action>
      <n-space justify="space-between" align="center">
        <n-button 
          text 
          :type="material.isCollected ? 'error' : 'default'"
          @click.stop="$emit('collect', material.id)"
          size="small"
        >
          <template #icon>
            <n-icon>
              <Heart v-if="material.isCollected" />
              <HeartOutline v-else />
            </n-icon>
          </template>
          {{ material.isCollected ? '已收藏' : '收藏' }}
        </n-button>
        
        <n-rate 
          :value="material.userRating || 0"
          :count="5"
          size="small"
          @update:value="(rating) => $emit('rate', { materialId: material.id, rating })"
          @click.stop
        />
      </n-space>
    </template>
  </n-card>
</template>

<script setup>
import { DocumentTextOutline, EyeOutline, StarOutline, HeartOutline, Heart } from '@vicons/ionicons5'

// 定义props
const props = defineProps({
  material: {
    type: Object,
    required: true
  }
})

// 定义emits
const emit = defineEmits(['view', 'collect', 'rate'])

// 获取难度类型
const getDifficultyType = (difficulty) => {
  const typeMap = {
    '初级': 'success',
    '中级': 'warning',
    '高级': 'error'
  }
  return typeMap[difficulty] || 'default'
}

// 获取图标颜色
const getIconColor = (difficulty) => {
  const colorMap = {
    '初级': '#10b981',
    '中级': '#f59e0b',
    '高级': '#ef4444'
  }
  return colorMap[difficulty] || '#94a3b8'
}

// 格式化数字
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
.material-card {
  height: 100%;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(14, 165, 233, 0.2);
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(20px);
}

.material-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(14, 165, 233, 0.15);
  border-color: rgba(14, 165, 233, 0.4);
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
  background: linear-gradient(135deg, #38bdf8, #0ea5e9);
}

.difficulty-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.material-info {
  padding: 4px 0;
}

.title-wrapper {
  margin-bottom: 12px;
}

.title {
  font-family: 'Bodoni Moda', serif;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
  color: #0c4a6e;
}

.meta {
  margin-bottom: 10px;
}

.description {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.6;
  min-height: 36px;
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