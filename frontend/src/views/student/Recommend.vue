<template>
  <div class="recommend-page">
    <!-- 顶部Banner -->
    <n-card class="banner-card" :bordered="false">
      <div class="banner-content">
        <div class="banner-icon">✨</div>
        <h1 class="banner-title">智能推荐</h1>
        <p class="banner-desc">基于协同过滤和内容匹配的个性化推荐系统</p>
      </div>
    </n-card>
    
    <!-- Tab切换：知识点/资料 -->
    <n-card :bordered="false" style="margin-bottom: 20px;">
      <n-tabs v-model:value="contentTab" type="segment" @update:value="handleTabChange">
        <n-tab-pane name="knowledge" tab="💡 知识点">
          <!-- 推荐类型切换 -->
          <n-space :size="12" style="margin-top: 16px;">
            <n-tag
              v-for="type in knowledgeTypes"
              :key="type.value"
              :type="currentType === type.value ? 'warning' : 'default'"
              :bordered="false"
              size="large"
              style="cursor: pointer; padding: 8px 16px;"
              @click="handleTypeChange(type.value)"
            >
              <template #icon>
                <n-icon :component="type.icon" />
              </template>
              {{ type.label }}
            </n-tag>
          </n-space>
        </n-tab-pane>
        
        <n-tab-pane name="material" tab="📚 学习资料">
          <!-- 推荐类型切换 -->
          <n-space :size="12" style="margin-top: 16px;">
            <n-tag
              v-for="type in materialTypes"
              :key="type.value"
              :type="currentType === type.value ? 'warning' : 'default'"
              :bordered="false"
              size="large"
              style="cursor: pointer; padding: 8px 16px;"
              @click="handleTypeChange(type.value)"
            >
              <template #icon>
                <n-icon :component="type.icon" />
              </template>
              {{ type.label }}
            </n-tag>
          </n-space>
        </n-tab-pane>
      </n-tabs>
    </n-card>
    
    <!-- 推荐列表 -->
    <div class="recommend-list">
      <n-spin :show="loading">
        <!-- 知识点列表 -->
        <n-grid v-if="contentTab === 'knowledge'" :cols="24" :x-gap="16" :y-gap="16">
          <n-grid-item 
            v-for="kp in knowledgeList" 
            :key="kp.id" 
            :span="8"
          >
            <KnowledgePointCard 
              :knowledgePoint="kp" 
              @view="handleViewKnowledge"
              @collect="handleCollectKnowledge"
            />
          </n-grid-item>
        </n-grid>
        
        <!-- 资料列表 -->
        <n-grid v-if="contentTab === 'material'" :cols="24" :x-gap="16" :y-gap="16">
          <n-grid-item 
            v-for="material in materialList" 
            :key="material.id" 
            :span="6"
          >
            <MaterialCard 
              :material="material" 
              @view="handleViewMaterial"
              @collect="handleCollectMaterial"
              @rate="handleRateMaterial"
            />
          </n-grid-item>
        </n-grid>
        
        <!-- 空状态 -->
        <n-empty 
          v-if="(contentTab === 'knowledge' && knowledgeList.length === 0 && !loading) || (contentTab === 'material' && materialList.length === 0 && !loading)" 
          description="暂无推荐内容"
          size="large"
          style="margin-top: 80px;"
        >
          <template #icon>
            <n-icon :component="SparklesOutline" :size="80" />
          </template>
          <template #extra>
            <n-text depth="3">
              多浏览和收藏一些{{ contentTab === 'knowledge' ? '知识点' : '资料' }}，系统会学习您的偏好进行精准推荐
            </n-text>
          </template>
        </n-empty>
      </n-spin>
      
      <!-- 加载更多 -->
      <div class="load-more" v-if="displayList.length > 0 && displayList.length >= displayLimit">
        <n-button 
          type="primary" 
          size="large" 
          :loading="loading"
          @click="loadMore"
        >
          加载更多
        </n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { SparklesOutline, FlameOutline, PersonOutline } from '@vicons/ionicons5'
import MaterialCard from '@/components/MaterialCard.vue'
import KnowledgePointCard from '@/components/KnowledgePointCard.vue'
import { getPersonalizedRecommendations, getHotRecommendations } from '@/api/recommend'
import { toggleCollect, rateMaterial } from '@/api/material'
import { getDailyRecommendations } from '@/api/daily'
import { toggleCollect as toggleCollectKP } from '@/api/knowledgePoint'

const router = useRouter()
const message = useMessage()

const contentTab = ref('knowledge') // knowledge | material
const materialList = ref([])
const knowledgeList = ref([])
const loading = ref(false)
const currentType = ref('personalized')
const displayLimit = ref(12)

// 资料推荐类型
const materialTypes = [
  { label: '为你推荐', value: 'personalized', icon: PersonOutline },
  { label: '热门资料', value: 'hot', icon: FlameOutline }
]

// 知识点推荐类型
const knowledgeTypes = [
  { label: '为你推荐', value: 'personalized', icon: PersonOutline },
  { label: '热门知识点', value: 'hot', icon: FlameOutline }
]

// 当前显示列表
const displayList = computed(() => {
  return contentTab.value === 'material' ? materialList.value : knowledgeList.value
})

// Tab切换
const handleTabChange = () => {
  currentType.value = 'personalized'
  displayLimit.value = 12
  fetchRecommend()
}

// 获取推荐列表
const fetchRecommend = async () => {
  try {
    loading.value = true
    
    if (contentTab.value === 'knowledge') {
      // 知识点推荐
      const res = await getDailyRecommendations(displayLimit.value)
      knowledgeList.value = res.data || []
    } else {
      // 资料推荐
      let res
      if (currentType.value === 'personalized') {
        res = await getPersonalizedRecommendations(displayLimit.value)
      } else {
        res = await getHotRecommendations(displayLimit.value)
      }
      materialList.value = res.data || []
    }
  } catch (error) {
    console.error('获取推荐列表失败:', error)
    message.error('获取推荐列表失败')
    if (contentTab.value === 'knowledge') {
      knowledgeList.value = []
    } else {
      materialList.value = []
    }
  } finally {
    loading.value = false
  }
}

// 切换推荐类型
const handleTypeChange = (type) => {
  currentType.value = type
  displayLimit.value = 12
  fetchRecommend()
}

// 加载更多
const loadMore = () => {
  displayLimit.value += 12
  fetchRecommend()
}

// 查看资料
const handleViewMaterial = (materialId) => {
  router.push(`/app/material/${materialId}`)
}

// 收藏资料
const handleCollectMaterial = async (materialId) => {
  try {
    await toggleCollect(materialId)
    message.success('操作成功')
    const material = materialList.value.find(m => m.id === materialId)
    if (material) {
      material.isCollected = !material.isCollected
      material.collectCount += material.isCollected ? 1 : -1
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 评分资料
const handleRateMaterial = async ({ materialId, rating }) => {
  try {
    await rateMaterial(materialId, rating)
    message.success('评分成功')
    const material = materialList.value.find(m => m.id === materialId)
    if (material) {
      material.userRating = rating
    }
  } catch (error) {
    console.error('评分失败:', error)
  }
}

// 查看知识点
const handleViewKnowledge = (knowledgePointId) => {
  router.push(`/app/knowledge/${knowledgePointId}`)
}

// 收藏知识点
const handleCollectKnowledge = async (knowledgePointId) => {
  try {
    await toggleCollectKP(knowledgePointId)
    message.success('操作成功')
    const kp = knowledgeList.value.find(k => k.id === knowledgePointId)
    if (kp) {
      kp.isCollected = !kp.isCollected
      kp.collectCount += kp.isCollected ? 1 : -1
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 初始化
onMounted(() => {
  fetchRecommend()
})
</script>

<style scoped>
.recommend-page {
  padding: 24px;
  background: #F8FAFC;
  min-height: 100vh;
}

.banner-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.88), rgba(251, 146, 60, 0.88)),
              url('https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&q=80') !important;
  background-size: cover;
  background-position: center;
  color: white;
  text-align: center;
  padding: 48px 32px;
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(249, 115, 22, 0.35);
  position: relative;
  overflow: hidden;
}

.banner-card::before {
  content: '';
  position: absolute;
  top: -30%;
  left: -10%;
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 5s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(15px, -15px) scale(1.05); }
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-icon {
  font-size: 56px;
  margin-bottom: 16px;
  animation: sparkle 1.5s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { transform: rotate(0deg) scale(1); }
  25% { transform: rotate(-10deg) scale(1.1); }
  75% { transform: rotate(10deg) scale(1.1); }
}

.banner-title {
  font-family: 'Fredoka', cursive;
  font-weight: 700;
  font-size: 42px;
  color: #ffffff;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.15);
}

.banner-desc {
  margin: 0;
  font-size: 20px;
  font-family: 'Nunito', sans-serif;
  font-weight: 500;
  color: #FEF3C7;
  letter-spacing: 0.5px;
}

/* 覆盖 n-card 默认背景 */
:deep(.n-card:not(.banner-card)) {
  background: url('https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
  border-radius: 20px;
  border: 3px solid #FED7AA !important;
  box-shadow: 0 8px 30px rgba(249, 115, 22, 0.12);
  transition: all 0.3s ease;
}

:deep(.n-card:not(.banner-card):hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(249, 115, 22, 0.22);
}

:deep(.n-card.banner-card) {
  background: url('https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
}

.recommend-list {
  min-height: 500px;
  padding-top: 12px;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 24px;
}
</style>