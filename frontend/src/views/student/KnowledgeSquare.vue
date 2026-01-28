<template>
  <div class="knowledge-square">
    <n-spin :show="loading">
      <!-- 搜索和筛选区域 -->
      <n-card :bordered="false" class="filter-card">
        <n-form :model="queryParams" inline size="medium">
          <n-form-item label="分类">
            <n-select 
              v-model:value="queryParams.category" 
              :options="categoryOptions" 
              placeholder="请选择分类"
              style="width: 150px;"
              clearable
            />
          </n-form-item>
          
          <n-form-item label="难度">
            <n-select 
              v-model:value="queryParams.difficulty" 
              :options="difficultyOptions" 
              placeholder="请选择难度"
              style="width: 120px;"
              clearable
            />
          </n-form-item>
          
          <n-form-item label="搜索">
            <n-input 
              v-model:value="queryParams.keyword" 
              placeholder="搜索标题或描述" 
              style="width: 200px;"
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          
          <n-form-item>
            <n-button type="primary" @click="handleSearch">
              搜索
            </n-button>
          </n-form-item>
        </n-form>
      </n-card>

      <!-- 知识点列表 -->
      <div class="knowledge-list">
        <n-empty 
          v-if="knowledgePoints.length === 0 && !loading" 
          description="暂无知识点"
          style="margin-top: 100px;"
        >
          <template #icon>
            <n-icon :component="BulbOutline" :size="80" />
          </template>
        </n-empty>
        
        <n-grid 
          v-else 
          :cols="gridCols"
          responsive="screen" 
          :x-gap="16" 
          :y-gap="16"
        >
          <n-grid-item v-for="item in knowledgePoints" :key="item.id">
            <KnowledgePointCard 
              :knowledgePoint="item" 
              @view="goToDetail"
              @collect="handleCollect"
              @rate="handleRate"
            />
          </n-grid-item>
        </n-grid>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <n-pagination
          v-model:page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :item-count="Number(total)"
          :page-sizes="[12, 24, 36, 48]"
          show-size-picker
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { 
  EyeOutline, 
  HeartOutline, 
  StarOutline, 
  TimeOutline,
  BulbOutline
} from '@vicons/ionicons5'
import { getKnowledgePointList, toggleCollect as toggleCollectKP } from '@/api/knowledgePoint'
import KnowledgePointCard from '@/components/KnowledgePointCard.vue'

const router = useRouter()
const message = useMessage()

// 状态
const loading = ref(false)
const knowledgePoints = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  category: null,
  difficulty: null,
  keyword: null
})

// 网格列数设置
const gridCols = ref('1 s:2 m:3 l:4 xl:5 2xl:6')

// 分类选项
const categoryOptions = ref([
  { label: '算法', value: '算法' },
  { label: '数据库', value: '数据库' },
  { label: '人工智能', value: '人工智能' },
  { label: '前端开发', value: '前端开发' },
  { label: '后端开发', value: '后端开发' },
  { label: '高等数学', value: '高等数学' },
  { label: '线性代数', value: '线性代数' },
  { label: '概率论', value: '概率论' },
  { label: '大学英语', value: '大学英语' },
  { label: '计算机网络', value: '计算机网络' }
])

// 难度选项
const difficultyOptions = ref([
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' }
])

// 获取知识点列表
const fetchKnowledgePoints = async () => {
  try {
    loading.value = true
    const res = await getKnowledgePointList(queryParams)
    knowledgePoints.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取知识点列表失败:', error)
    message.error('获取知识点列表失败')
  } finally {
    loading.value = false
  }
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/app/knowledge/${id}`)
}

// 处理收藏
const handleCollect = async (knowledgePointId) => {
  try {
    await toggleCollectKP(knowledgePointId)
    message.success('操作成功')
    const kp = knowledgePoints.value.find(k => k.id === knowledgePointId)
    if (kp) {
      kp.isCollected = !kp.isCollected
      kp.collectCount += kp.isCollected ? 1 : -1
    }
  } catch (error) {
    console.error('操作失败:', error)
    message.error('操作失败')
  }
}

// 处理评分
const handleRate = async ({ knowledgePointId, rating }) => {
  // 这里可以添加评分逻辑，暂时不实现具体评分API调用
  console.log('评分:', knowledgePointId, rating)
  const kp = knowledgePoints.value.find(k => k.id === knowledgePointId)
  if (kp) {
    kp.userRating = rating
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchKnowledgePoints()
}

// 页码改变
const handlePageChange = (page) => {
  queryParams.pageNum = page
  fetchKnowledgePoints()
}

// 页面大小改变
const handlePageSizeChange = (pageSize) => {
  queryParams.pageSize = pageSize
  queryParams.pageNum = 1
  fetchKnowledgePoints()
}

onMounted(() => {
  fetchKnowledgePoints()
})
</script>

<style scoped>
.knowledge-square {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 64px);
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.knowledge-list {
  margin-bottom: 20px;
}

.knowledge-card {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.knowledge-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.cover-image {
  position: relative;
  height: 120px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.category-tag {
  font-size: 14px;
  font-weight: 600;
}

.difficulty-tag {
  margin-top: 4px;
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.description {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-info {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.stats {
  font-size: 12px;
  color: #999;
}

.stat-value {
  color: #333;
  font-weight: 500;
}

.time {
  font-size: 12px;
  color: #999;
}

.pagination {
  text-align: center;
  margin-top: 30px;
}
</style>