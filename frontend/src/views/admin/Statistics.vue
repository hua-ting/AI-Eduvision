<template>
  <div class="statistics">
    <n-space vertical size="large">
      <!-- 资料统计卡片 -->
      <n-card title="资料统计" :bordered="false">
        <n-grid :x-gap="16" :y-gap="16" :cols="4">
          <n-gi>
            <n-statistic label="资料总数" :value="materialStats.total">
              <template #prefix>
                <n-icon size="24" color="#18a058">
                  <document-text-outline />
                </n-icon>
              </template>
            </n-statistic>
          </n-gi>
          <n-gi>
            <n-statistic label="在线资料" :value="materialStats.online">
              <template #prefix>
                <n-icon size="24" color="#2080f0">
                  <checkmark-circle-outline />
                </n-icon>
              </template>
            </n-statistic>
          </n-gi>
          <n-gi>
            <n-statistic label="待审核" :value="materialStats.pending">
              <template #prefix>
                <n-icon size="24" color="#f0a020">
                  <time-outline />
                </n-icon>
              </template>
            </n-statistic>
          </n-gi>
          <n-gi>
            <n-statistic label="下架资料" :value="materialStats.offline">
              <template #prefix>
                <n-icon size="24" color="#d03050">
                  <close-circle-outline />
                </n-icon>
              </template>
            </n-statistic>
          </n-gi>
        </n-grid>
      </n-card>

      <!-- 用户互动统计 -->
      <n-card title="用户互动" :bordered="false">
        <n-grid :x-gap="16" :y-gap="16" :cols="2">
          <n-gi>
            <n-statistic label="总浏览量" :value="materialStats.totalViews">
              <template #prefix>
                <n-icon size="24" color="#8a2be2">
                  <eye-outline />
                </n-icon>
              </template>
            </n-statistic>
          </n-gi>
          <n-gi>
            <n-statistic label="总收藏量" :value="materialStats.totalCollects">
              <template #prefix>
                <n-icon size="24" color="#ff6347">
                  <heart-outline />
                </n-icon>
              </template>
            </n-statistic>
          </n-gi>
        </n-grid>
      </n-card>

      <!-- 快速操作 -->
      <n-card title="快速操作" :bordered="false">
        <n-space>
          <n-button type="primary" @click="$router.push('/admin/material')">
            资料管理
          </n-button>
          <n-button type="info" @click="$router.push('/admin/user')">
            用户管理
          </n-button>
          <n-button @click="refreshData">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            刷新数据
          </n-button>
        </n-space>
      </n-card>
    </n-space>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import {
  DocumentTextOutline,
  CheckmarkCircleOutline,
  TimeOutline,
  CloseCircleOutline,
  EyeOutline,
  HeartOutline,
  RefreshOutline
} from '@vicons/ionicons5'
import { getMaterialStats } from '@/api/admin/material'

const message = useMessage()

const materialStats = ref({
  total: 0,
  online: 0,
  pending: 0,
  offline: 0,
  totalViews: 0,
  totalCollects: 0
})

const fetchStats = async () => {
  try {
    const { data } = await getMaterialStats()
    materialStats.value = data
  } catch (error) {
    message.error('获取统计数据失败')
  }
}

const refreshData = () => {
  fetchStats()
  message.success('数据已刷新')
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.statistics {
  padding: 20px;
}
</style>
