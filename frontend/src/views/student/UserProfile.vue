<template>
  <div class="user-profile-page">
    <n-space vertical :size="20">
      <!-- 学习统计卡片 -->
      <n-card title="学习统计" :bordered="false">
        <n-grid cols="4" x-gap="20" responsive="screen">
          <n-gi>
            <n-statistic label="问答次数" :value="stats.totalQaCount || 0">
              <template #suffix>次</template>
            </n-statistic>
          </n-gi>
          <n-gi>
            <n-statistic label="知识点浏览" :value="stats.totalKnowledgeViews || 0">
              <template #suffix>次</template>
            </n-statistic>
          </n-gi>
          <n-gi>
            <n-statistic label="知识点收藏" :value="stats.totalKnowledgeCollects || 0">
              <template #suffix>个</template>
            </n-statistic>
          </n-gi>
          <n-gi>
            <n-statistic label="学习时长" :value="stats.learningDuration || 0">
              <template #suffix>分钟</template>
            </n-statistic>
          </n-gi>
        </n-grid>
        
        <n-divider />
        
        <n-space>
          <n-tag type="info" size="large">
            学习水平: {{ stats.learningLevel || '初级' }}
          </n-tag>
          <n-tag type="success" size="large" v-if="stats.lastActiveTime">
            最后活跃: {{ formatDate(stats.lastActiveTime) }}
          </n-tag>
        </n-space>
      </n-card>

      <!-- 偏好分析卡片 -->
      <n-card title="偏好分析" :bordered="false">
        <n-space vertical :size="16">
          <!-- 兴趣标签 -->
          <div>
            <n-text strong>兴趣标签</n-text>
            <n-space style="margin-top: 8px">
              <n-tag
                v-for="tag in preferences.interestTags"
                :key="tag"
                type="primary"
                :bordered="false"
              >
                {{ tag }}
              </n-tag>
              <n-tag v-if="!preferences.interestTags || preferences.interestTags.length === 0" type="default">
                暂无数据
              </n-tag>
            </n-space>
          </div>

          <!-- 知识点偏好 -->
          <div v-if="preferences.knowledgePreferences && Object.keys(preferences.knowledgePreferences).length > 0">
            <n-text strong>知识点偏好</n-text>
            <n-space vertical style="margin-top: 8px">
              <div v-for="(score, category) in preferences.knowledgePreferences" :key="category">
                <n-space align="center">
                  <n-text>{{ category }}</n-text>
                  <n-progress
                    type="line"
                    :percentage="Math.round(score * 100)"
                    :show-indicator="true"
                    style="width: 300px"
                  />
                </n-space>
              </div>
            </n-space>
          </div>

          <!-- 问答主题分布 -->
          <div v-if="preferences.qaTopics && Object.keys(preferences.qaTopics).length > 0">
            <n-text strong>问答主题分布</n-text>
            <n-space style="margin-top: 8px">
              <n-tag
                v-for="(count, topic) in preferences.qaTopics"
                :key="topic"
                type="success"
              >
                {{ topic }}: {{ count }}次
              </n-tag>
            </n-space>
          </div>

          <!-- 最喜欢的分类 -->
          <div v-if="preferences.favoriteCategory">
            <n-text strong>最喜欢的分类</n-text>
            <n-space style="margin-top: 8px">
              <n-tag type="warning" size="large">
                {{ preferences.favoriteCategory }}
              </n-tag>
            </n-space>
          </div>
        </n-space>
      </n-card>

      <!-- 操作按钮 -->
      <n-card :bordered="false">
        <n-space>
          <n-button type="primary" @click="handleRefresh" :loading="refreshing">
            <template #icon>
              <n-icon><RefreshOutline /></n-icon>
            </template>
            刷新画像
          </n-button>
        </n-space>
      </n-card>
    </n-space>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { RefreshOutline } from '@vicons/ionicons5'
import { getLearningStats, getUserPreferences, refreshProfile } from '@/api/userProfile'
import { formatDate } from '@/utils/format'

const message = useMessage()

const stats = reactive({
  totalQaCount: 0,
  totalKnowledgeViews: 0,
  totalKnowledgeCollects: 0,
  learningDuration: 0,
  learningLevel: '初级',
  lastActiveTime: null
})

const preferences = reactive({
  interestTags: [],
  qaTopics: {},
  knowledgePreferences: {},
  favoriteCategory: ''
})

const refreshing = ref(false)

const fetchStats = async () => {
  try {
    const res = await getLearningStats()
    Object.assign(stats, res.data)
  } catch (error) {
    message.error('获取学习统计失败')
  }
}

const fetchPreferences = async () => {
  try {
    const res = await getUserPreferences()
    Object.assign(preferences, res.data)
  } catch (error) {
    message.error('获取偏好分析失败')
  }
}

const handleRefresh = async () => {
  try {
    refreshing.value = true
    await refreshProfile()
    message.success('画像更新中，请稍后刷新查看')
    // 延迟刷新数据
    setTimeout(async () => {
      await fetchStats()
      await fetchPreferences()
    }, 2000)
  } catch (error) {
    message.error('刷新失败')
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  fetchStats()
  fetchPreferences()
})
</script>

<style scoped>
.user-profile-page {
  padding: 20px;
}
</style>
