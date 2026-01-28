<template>
  <div class="collection-page">
    <!-- 顶部Banner -->
    <n-card class="banner-card" :bordered="false">
      <div class="banner-content">
        <div class="banner-icon">❤️</div>
        <h1 class="banner-title">我的收藏</h1>
        <p class="banner-desc">您收藏的优质学习资料和知识点</p>
      </div>
    </n-card>
    
    <!-- 切换标签 -->
    <n-card :bordered="false" style="margin-bottom: 20px;">
      <n-tabs v-model:value="activeTab" type="segment" @update:value="handleTabChange">
        <n-tab-pane name="knowledge" tab="知识点收藏">
        </n-tab-pane>
        <n-tab-pane name="material" tab="资料收藏">
        </n-tab-pane>
      </n-tabs>
    </n-card>
    
    <!-- 收藏列表 -->
    <div class="collection-list">
      <n-spin :show="loading">
        <!-- 知识点收藏 -->
        <div v-if="activeTab === 'knowledge'">
          <n-grid :cols="24" :x-gap="16" :y-gap="16">
            <n-grid-item 
              v-for="kp in knowledgePointList" 
              :key="kp.id" 
              :span="6"
            >
              <KnowledgePointCard 
                :knowledge-point="kp" 
                @view="handleKnowledgeView"
                @collect="handleKnowledgeCollect"
              />
            </n-grid-item>
          </n-grid>
          
          <n-empty 
            v-if="knowledgePointList.length === 0 && !loading" 
            description="还没有收藏任何知识点"
            size="large"
            style="margin-top: 80px;"
          >
            <template #icon>
              <n-icon :component="HeartDislikeOutline" :size="80" />
            </template>
            <template #extra>
              <n-button type="primary" @click="$router.push('/knowledge')">
                去知识点广场看看
              </n-button>
            </template>
          </n-empty>
        </div>
        
        <!-- 资料收藏 -->
        <div v-else>
          <n-grid :cols="24" :x-gap="16" :y-gap="16">
            <n-grid-item 
              v-for="material in materialList" 
              :key="material.id" 
              :span="6"
            >
              <MaterialCard 
                :material="material" 
                @view="handleView"
                @collect="handleCollect"
                @rate="handleRate"
              />
            </n-grid-item>
          </n-grid>
          
          <n-empty 
            v-if="materialList.length === 0 && !loading" 
            description="还没有收藏任何资料"
            size="large"
            style="margin-top: 80px;"
          >
            <template #icon>
              <n-icon :component="HeartDislikeOutline" :size="80" />
            </template>
            <template #extra>
              <n-button type="primary" @click="$router.push('/knowledge')">
                去知识点广场看看
              </n-button>
            </template>
          </n-empty>
        </div>
      </n-spin>
      
      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <n-pagination 
          v-model:page="pageNum"
          :page-size="pageSize"
          :item-count="total"
          show-size-picker
          :page-sizes="[12, 20, 40]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { HeartDislikeOutline } from '@vicons/ionicons5'
import MaterialCard from '@/components/MaterialCard.vue'
import KnowledgePointCard from '@/components/KnowledgePointCard.vue'
import { getMyCollections, toggleCollect, rateMaterial } from '@/api/material'
import { getUserCollections, toggleCollect as toggleKnowledgeCollect } from '@/api/knowledgePoint'

const router = useRouter()
const message = useMessage()

const activeTab = ref('knowledge')
const materialList = ref([])
const knowledgePointList = ref([])
const total = ref(0)
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)

// 切换标签
const handleTabChange = () => {
  pageNum.value = 1
  if (activeTab.value === 'knowledge') {
    fetchKnowledgeCollections()
  } else {
    fetchCollections()
  }
}

// 获取知识点收藏
const fetchKnowledgeCollections = async () => {
  try {
    loading.value = true
    const res = await getUserCollections(pageNum.value, pageSize.value)
    knowledgePointList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取知识点收藏失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取收藏列表
const fetchCollections = async () => {
  try {
    loading.value = true
    const res = await getMyCollections({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    materialList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    message.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 页面变化
const handlePageChange = (page) => {
  pageNum.value = page
  if (activeTab.value === 'knowledge') {
    fetchKnowledgeCollections()
  } else {
    fetchCollections()
  }
}

// 页面大小变化
const handlePageSizeChange = (size) => {
  pageSize.value = size
  pageNum.value = 1
  if (activeTab.value === 'knowledge') {
    fetchKnowledgeCollections()
  } else {
    fetchCollections()
  }
}

// 查看资料
const handleView = (materialId) => {
  router.push(`/app/material/${materialId}`)
}

// 取消收藏
const handleCollect = async (materialId) => {
  try {
    await toggleCollect(materialId)
    message.success('已取消收藏')
    // 从列表中移除
    fetchCollections()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 评分资料
const handleRate = async ({ materialId, rating }) => {
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
const handleKnowledgeView = (id) => {
  router.push(`/app/knowledge/${id}`)
}

// 取消知识点收藏
const handleKnowledgeCollect = async (id) => {
  try {
    await toggleKnowledgeCollect(id)
    message.success('已取消收藏')
    fetchKnowledgeCollections()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => {
  fetchKnowledgeCollections()
})
</script>

<style scoped>
.collection-page {
  padding: 24px;
  background: #F8FAFC;
  min-height: 100vh;
}

.banner-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.88), rgba(244, 114, 182, 0.88)),
              url('https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=1200&q=80') !important;
  background-size: cover;
  background-position: center;
  color: white;
  text-align: center;
  padding: 48px 32px;
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(236, 72, 153, 0.35);
  position: relative;
  overflow: hidden;
}

.banner-card::before {
  content: '';
  position: absolute;
  bottom: -30%;
  right: -5%;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(255,255,255,0.18) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.18; }
  50% { transform: scale(1.15); opacity: 0.25; }
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-icon {
  font-size: 56px;
  margin-bottom: 16px;
  animation: heartbeat 1.2s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  10% { transform: scale(1.15); }
  20% { transform: scale(1); }
  30% { transform: scale(1.15); }
  40% { transform: scale(1); }
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
  color: #FCE7F3;
  letter-spacing: 0.5px;
}

/* 覆盖 n-card 默认背景 */
:deep(.n-card:not(.banner-card)) {
  background: url('https://images.unsplash.com/photo-1533090161767-e6ffed986c88?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
  border-radius: 20px;
  border: 3px solid #FBCFE8 !important;
  box-shadow: 0 8px 30px rgba(236, 72, 153, 0.12);
  transition: all 0.3s ease;
}

:deep(.n-card:not(.banner-card):hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(236, 72, 153, 0.22);
}

:deep(.n-card.banner-card) {
  background: url('https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=1200&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
}

.collection-list {
  min-height: 500px;
  padding-top: 12px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 24px;
  background: #ffffff;
  border-radius: 20px;
  border: 3px solid #FBCFE8;
  box-shadow: 0 8px 30px rgba(236, 72, 153, 0.12);
}
</style>
