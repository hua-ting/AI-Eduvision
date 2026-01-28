

<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <n-card class="welcome-card" :bordered="false">
      <n-space align="center" :size="20">
        <n-avatar :size="80" :src="userStore.userInfo.avatar" />
        <div>
          <h1 class="welcome-title">
            欢迎回来，{{ userStore.userInfo.nickname }}！
          </h1>
          <p class="welcome-subtitle">
            继续您的学习之旅
          </p>
        </div>
      </n-space>
    </n-card>

    <!-- 数据统计 -->
    <n-grid :x-gap="16" :y-gap="16" :cols="4" style="margin-top: 20px;">
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="浏览资料" :value="stats.viewCount">
            <template #prefix>
              <n-icon size="24" color="#0ea5e9">
                <eye-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="收藏资料" :value="stats.collectCount">
            <template #prefix>
              <n-icon size="24" color="#d03050">
                <heart-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="评分次数" :value="stats.ratingCount">
            <template #prefix>
              <n-icon size="24" color="#f0a020">
                <star-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="学习时长" :value="stats.learningTime" suffix="小时">
            <template #prefix>
              <n-icon size="24" color="#2080f0">
                <time-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 每日推荐 -->
    <n-card title="🌟 今日推荐" :bordered="false" style="margin-top: 20px;">
      <n-spin :show="recommendLoading">
        <n-grid :cols="24" :x-gap="16" :y-gap="16">
          <n-grid-item 
            v-for="kp in dailyRecommendations" 
            :key="kp.id" 
            :span="8"
          >
            <n-card 
              hoverable 
              class="recommend-card"
              @click="$router.push(`/app/knowledge/${kp.id}`)"
            >
              <h3 class="kp-title">{{ kp.title }}</h3>
              <n-space :size="8" style="margin-top: 8px;">
                <n-tag :bordered="false" type="success" size="small" round>
                  {{ kp.category }}
                </n-tag>
                <n-tag :bordered="false" type="warning" size="small" round>
                  {{ kp.difficulty }}
                </n-tag>
              </n-space>
              <p class="kp-desc">{{ kp.description }}</p>
            </n-card>
          </n-grid-item>
        </n-grid>
        <n-empty 
          v-if="dailyRecommendations.length === 0 && !recommendLoading" 
          description="暂无推荐"
        />
      </n-spin>
    </n-card>

    <!-- 最近浏览 -->
    <n-card title="最近浏览" :bordered="false" class="recent-card" style="margin-top: 20px;">
      <n-tabs v-model:value="viewTab" type="line">
        <n-tab-pane name="knowledge" tab="知识点">
          <n-spin :show="loading">
            <n-grid :cols="24" :x-gap="16" :y-gap="16">
              <n-grid-item
                v-for="item in recentKnowledgeViews"
                :key="item.id"
                :span="12"
              >
                <n-card
                  hoverable
                  class="recent-item-card"
                  @click="$router.push(`/app/knowledge/${item.knowledgePointId}`)"
                >
                  <div class="recent-item-content">
                    <div class="recent-item-icon">
                      <n-icon size="32" color="#4F46E5">
                        <bulb-outline />
                      </n-icon>
                    </div>
                    <div class="recent-item-info">
                      <h4 class="recent-item-title">{{ item.title }}</h4>
                      <n-space :size="8" style="margin-top: 8px;">
                        <n-tag :bordered="false" size="small" type="primary" round>{{ item.category }}</n-tag>
                        <n-text depth="3" style="font-size: 12px;">{{ formatDate(item.createTime) }}</n-text>
                      </n-space>
                    </div>
                  </div>
                </n-card>
              </n-grid-item>
            </n-grid>
            <n-empty v-if="recentKnowledgeViews.length === 0 && !loading" description="暂无浏览记录" />
          </n-spin>
        </n-tab-pane>
        <n-tab-pane name="material" tab="资料">
          <n-spin :show="loading">
            <n-grid :cols="24" :x-gap="16" :y-gap="16">
              <n-grid-item
                v-for="item in recentViews"
                :key="item.id"
                :span="12"
              >
                <n-card
                  hoverable
                  class="recent-item-card"
                  @click="$router.push(`/app/material/${item.materialId}`)"
                >
                  <div class="recent-item-content">
                    <div class="recent-item-icon">
                      <n-icon size="32" color="#10B981">
                        <book-outline />
                      </n-icon>
                    </div>
                    <div class="recent-item-info">
                      <h4 class="recent-item-title">{{ item.title }}</h4>
                      <n-space :size="8" style="margin-top: 8px;">
                        <n-tag :bordered="false" size="small" type="success" round>{{ item.category }}</n-tag>
                        <n-text depth="3" style="font-size: 12px;">{{ formatDate(item.createTime) }}</n-text>
                      </n-space>
                    </div>
                  </div>
                </n-card>
              </n-grid-item>
            </n-grid>
            <n-empty v-if="recentViews.length === 0 && !loading" description="暂无浏览记录" />
          </n-spin>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { 
  EyeOutline, 
  HeartOutline, 
  StarOutline, 
  TimeOutline,
  BookOutline,
  BulbOutline,
  Heart,
  PersonOutline
} from '@vicons/ionicons5'
import { formatDate } from '@/utils/format'
import { getUserStats, getRecentViews } from '@/api/user'
import { getRecentKnowledgeViews } from '@/api/knowledgePoint'
import { getDailyRecommendations } from '@/api/daily'

const userStore = useUserStore()
const loading = ref(false)
const viewTab = ref('knowledge')
const recommendLoading = ref(false)

const stats = ref({
  viewCount: 0,
  collectCount: 0,
  ratingCount: 0,
  learningTime: 0
})

const recentViews = ref([])
const recentKnowledgeViews = ref([])
const dailyRecommendations = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    const { data } = await getUserStats()
    stats.value = data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取最近浏览
const fetchRecentViews = async () => {
  try {
    loading.value = true
    const { data } = await getRecentViews(100) // 获取更多记录以便去重
    // 按照 materialId 去重，只保留每个资料的最新浏览记录
    const uniqueViews = []
    const seenIds = new Set()
    for (const item of data) {
      if (!seenIds.has(item.materialId)) {
        seenIds.add(item.materialId)
        uniqueViews.push(item)
      }
    }
    // 只保留最近的10条记录
    recentViews.value = uniqueViews.slice(0, 10)
  } catch (error) {
    console.error('获取浏览记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取知识点浏览记录
const fetchRecentKnowledgeViews = async () => {
  try {
    loading.value = true
    const { data } = await getRecentKnowledgeViews(100) // 获取更多记录以便去重
    // 按照 knowledgePointId 去重，只保留每个知识点的最新浏览记录
    const uniqueKnowledgeViews = []
    const seenIds = new Set()
    for (const item of data) {
      if (!seenIds.has(item.knowledgePointId)) {
        seenIds.add(item.knowledgePointId)
        uniqueKnowledgeViews.push(item)
      }
    }
    // 只保留最近的10条记录
    recentKnowledgeViews.value = uniqueKnowledgeViews.slice(0, 10)
  } catch (error) {
    console.error('获取知识点浏览记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取每日推荐
const fetchDailyRecommendations = async () => {
  try {
    recommendLoading.value = true
    const { data } = await getDailyRecommendations(6)
    dailyRecommendations.value = data
  } catch (error) {
    console.error('获取每日推荐失败:', error)
  } finally {
    recommendLoading.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentViews()
  fetchRecentKnowledgeViews()
  fetchDailyRecommendations()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: transparent;
  min-height: 100vh;
}

.welcome-card {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(20px);
  color: #0c4a6e;
  animation: fadeIn 0.6s ease-out;
  border: 1px solid rgba(14, 165, 233, 0.15);
}

.welcome-title {
  font-family: 'Bodoni Moda', serif;
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  color: #0c4a6e;
}

.welcome-subtitle {
  margin-top: 10px;
  color: #64748b;
  font-size: 16px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(20px);
  transition: all 0.3s ease;
  border: 1px solid rgba(14, 165, 233, 0.15);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(14, 165, 233, 0.1);
  border-color: rgba(14, 165, 233, 0.3);
}

/* 覆盖 n-card 默认背景 */
:deep(.n-card:not(.welcome-card):not(.stat-card):not(.recommend-card)) {
  background: url('https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
  border-radius: 20px;
  border: 2px solid #C7D2FE !important;
}

:deep(.n-card-header__main) {
  font-family: 'Fredoka', cursive !important;
  font-weight: 700 !important;
  color: #1E1B4B !important;
  text-shadow: 0 1px 3px rgba(255, 255, 255, 0.8);
}

.recommend-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  overflow: hidden;
  border: 2px solid #C7D2FE;
}

:deep(.recommend-card) {
  background: url('https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
}

.recommend-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 15px 30px rgba(79, 70, 229, 0.25);
  border-color: #4F46E5;
}

.kp-title {
  font-family: 'Fredoka', cursive;
  font-size: 18px;
  font-weight: 700;
  color: #1E1B4B;
  margin: 0;
  text-shadow: 0 1px 3px rgba(255, 255, 255, 0.8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kp-desc {
  margin-top: 12px;
  font-size: 14px;
  color: #334155;
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.5);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recent-card {
  border-radius: 20px;
  animation: fadeIn 0.6s ease-out;
  border: 3px solid #C7D2FE;
  transition: all 0.3s ease;
}

:deep(.recent-card) {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px);
}

.recent-card:hover {
  box-shadow: 0 12px 40px rgba(79, 70, 229, 0.18);
  transform: translateY(-2px);
}

.recent-item-card {
  height: 100%;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 16px;
  overflow: hidden;
  border: 2px solid #E0E7FF;
}

:deep(.recent-item-card) {
  background: url('https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
}

.recent-item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(79, 70, 229, 0.2);
  border-color: #4F46E5;
}

.recent-item-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.recent-item-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.15);
}

.recent-item-info {
  flex: 1;
  min-width: 0;
}

.recent-item-title {
  font-family: 'Fredoka', cursive;
  font-size: 16px;
  font-weight: 700;
  color: #1E1B4B;
  margin: 0;
  text-shadow: 0 1px 3px rgba(255, 255, 255, 0.8);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
