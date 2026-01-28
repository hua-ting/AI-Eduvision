<template>
  <div class="material-square">
    <!-- 顶部Banner -->
    <n-card class="banner-card" :bordered="false">
      <div class="banner-content">
        <div class="banner-icon">📚</div>
        <h1 class="banner-title">学习资料广场</h1>
        <p class="banner-desc">发现优质学习资源，开启智慧学习之旅</p>
      </div>
    </n-card>
    
    <!-- 搜索和筛选区域 -->
    <n-card class="search-card" :bordered="false">
      <n-space vertical :size="16">
        <!-- 搜索框 -->
        <n-input-group>
          <n-input 
            v-model:value="searchParams.keyword" 
            placeholder="搜索资料标题或描述"
            size="large"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
          </n-input>
          <n-button type="primary" size="large" @click="handleSearch">
            <template #icon>
              <n-icon :component="SearchOutline" />
            </template>
            搜索
          </n-button>
        </n-input-group>
        
        <!-- 筛选条件 -->
        <n-space :size="12">
          <n-button type="primary" @click="showUploadModal = true">
            <template #icon>
              <n-icon :component="AddOutline" />
            </template>
            上传资料
          </n-button>
          
          <n-select 
            v-model:value="searchParams.category" 
            :options="categoryOptions" 
            placeholder="选择分类"
            clearable
            style="width: 150px;"
          />
          
          <n-select 
            v-model:value="searchParams.difficulty" 
            :options="difficultyOptions" 
            placeholder="选择难度"
            clearable
            style="width: 150px;"
          />
          
          <n-select 
            v-model:value="searchParams.orderBy" 
            :options="sortOptions" 
            placeholder="排序方式"
            style="width: 150px;"
          />
          
          <n-button secondary @click="resetSearch">
            <template #icon>
              <n-icon :component="RefreshOutline" />
            </template>
            重置
          </n-button>
        </n-space>
        
        <!-- 热门标签 -->
        <div class="tag-filter" v-if="popularTags.length > 0">
          <n-space align="center" :size="8">
            <n-text depth="3">热门标签:</n-text>
            <n-space :size="8">
              <n-tag 
                v-for="tag in popularTags" 
                :key="tag"
                :type="searchParams.tags && searchParams.tags.includes(tag) ? 'primary' : 'default'"
                :bordered="false"
                checkable
                size="medium"
                round
                @click="toggleTag(tag)"
              >
                {{ tag }}
              </n-tag>
            </n-space>
          </n-space>
        </div>
      </n-space>
    </n-card>
    
    <!-- 统计信息 -->
    <n-card :bordered="false" class="stats-card">
      <n-space :size="30">
        <n-statistic label="资料总数" :value="total">
          <template #prefix>
            <n-icon :component="DocumentTextOutline" color="#0ea5e9" />
          </template>
        </n-statistic>
        <n-statistic label="本次搜索" :value="materialList.length">
          <template #prefix>
            <n-icon :component="CheckmarkCircleOutline" color="#2080f0" />
          </template>
        </n-statistic>
      </n-space>
    </n-card>
    
    <!-- 资料列表 -->
    <div class="material-list">
      <n-spin :show="loading">
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
        
        <!-- 空状态 -->
        <n-empty 
          v-if="materialList.length === 0 && !loading" 
          description="暂无资料数据"
          size="large"
          style="margin-top: 80px;"
        >
          <template #icon>
            <n-icon :component="FolderOpenOutline" :size="80" />
          </template>
          <template #extra>
            <n-button @click="resetSearch">重新搜索</n-button>
          </template>
        </n-empty>
      </n-spin>
      
      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <n-pagination 
          v-model:page="searchParams.pageNum"
          :page-size="searchParams.pageSize"
          :item-count="total"
          show-size-picker
          :page-sizes="[12, 20, 40, 60]"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </div>

    <!-- 上传资料对话框 -->
    <n-modal v-model:show="showUploadModal" preset="dialog" title="上传资料" style="width: 600px">
      <n-form :model="uploadForm" label-placement="left" label-width="80">
        <n-form-item label="标题" required>
          <n-input v-model:value="uploadForm.title" placeholder="请输入资料标题" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input
            v-model:value="uploadForm.description"
            type="textarea"
            placeholder="请输入资料描述"
            :rows="3"
          />
        </n-form-item>
        <n-form-item label="分类" required>
          <n-select
            v-model:value="uploadForm.category"
            :options="categoryOptions"
            placeholder="请选择分类"
          />
        </n-form-item>
        <n-form-item label="难度" required>
          <n-select
            v-model:value="uploadForm.difficulty"
            :options="difficultyOptions"
            placeholder="请选择难度"
          />
        </n-form-item>
        <n-form-item label="标签">
          <n-select
            v-model:value="uploadForm.tags"
            multiple
            tag
            placeholder="请输入标签，回车确认"
          />
        </n-form-item>
        <n-form-item label="文件URL" required>
          <n-input v-model:value="uploadForm.fileUrl" placeholder="请输入文件URL" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showUploadModal = false">取消</n-button>
          <n-button type="primary" :loading="uploadLoading" @click="handleUpload">
            上传
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { 
  SearchOutline, 
  RefreshOutline,
  DocumentTextOutline,
  CheckmarkCircleOutline,
  FolderOpenOutline,
  AddOutline
} from '@vicons/ionicons5'
import MaterialCard from '@/components/MaterialCard.vue'
import { getMaterialList, uploadMaterial } from '@/api/material'

const router = useRouter()
const message = useMessage()

// 搜索参数
const searchParams = reactive({
  keyword: '',
  category: null,
  difficulty: null,
  tags: [],
  orderBy: 'createTime',
  orderType: 'desc',
  pageNum: 1,
  pageSize: 20
})

// 资料列表数据
const materialList = ref([])
const total = ref(0)
const loading = ref(false)
const showUploadModal = ref(false)
const uploadForm = reactive({
  title: '',
  description: '',
  category: null,
  difficulty: null,
  tags: [],
  fileUrl: ''
})
const uploadLoading = ref(false)

// 分类选项
const categoryOptions = [
  { label: '教材', value: '教材' },
  { label: '课件', value: '课件' },
  { label: '论文', value: '论文' },
  { label: '其他', value: '其他' }
]

// 难度选项
const difficultyOptions = [
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' }
]

// 排序选项
const sortOptions = [
  { label: '最新', value: 'createTime' },
  { label: '最热', value: 'viewCount' },
  { label: '评分', value: 'avgRating' }
]

// 热门标签（模拟数据）
const popularTags = ref([
  '算法', '数据库', '人工智能', '前端开发', 
  '后端开发', '高等数学', '线性代数', '概率论'
])

// 切换标签
const toggleTag = (tag) => {
  if (!searchParams.tags) {
    searchParams.tags = []
  }
  
  const index = searchParams.tags.indexOf(tag)
  if (index > -1) {
    searchParams.tags.splice(index, 1)
  } else {
    searchParams.tags.push(tag)
  }
  
  handleSearch()
}

// 搜索资料
const handleSearch = async () => {
  try {
    loading.value = true
    const params = { ...searchParams }
    
    // 处理标签参数
    if (params.tags && params.tags.length > 0) {
      params.tags = params.tags.join(',')
    } else {
      delete params.tags
    }
    
    const res = await getMaterialList(params)
    materialList.value = res.data.records
    total.value = Number(res.data.total)
  } catch (error) {
    console.error('获取资料列表失败:', error)
    message.error('获取资料列表失败')
  } finally {
    loading.value = false
  }
}

// 页面变化
const handlePageChange = (page) => {
  searchParams.pageNum = page
  handleSearch()
}

// 重置搜索
const resetSearch = () => {
  searchParams.keyword = ''
  searchParams.category = null
  searchParams.difficulty = null
  searchParams.tags = []
  searchParams.pageNum = 1
  handleSearch()
}

// 页面大小变化
const handlePageSizeChange = (pageSize) => {
  searchParams.pageSize = pageSize
  searchParams.pageNum = 1
  handleSearch()
}

// 查看资料
const handleView = (materialId) => {
  router.push(`/app/material/${materialId}`)
}

// 收藏资料
const handleCollect = (materialId) => {
  console.log('收藏资料:', materialId)
  // TODO: 调用收藏接口
}

// 评分资料
const handleRate = ({ materialId, rating }) => {
  console.log('评分资料:', materialId, rating)
  // TODO: 调用评分接口
}

// 上传资料
const handleUpload = async () => {
  if (!uploadForm.title || !uploadForm.category || !uploadForm.difficulty || !uploadForm.fileUrl) {
    message.warning('请填写完整信息')
    return
  }

  uploadLoading.value = true
  try {
    await uploadMaterial(uploadForm)
    message.success('上传成功，等待管理员审核')
    showUploadModal.value = false
    
    // 重置表单
    uploadForm.title = ''
    uploadForm.description = ''
    uploadForm.category = null
    uploadForm.difficulty = null
    uploadForm.tags = []
    uploadForm.fileUrl = ''
  } catch (error) {
    const errorMsg = error.response?.data?.message || '上传失败'
    message.error(errorMsg)
  } finally {
    uploadLoading.value = false
  }
}

// 初始化
onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.material-square {
  padding: 24px;
  background: #F8FAFC;
  min-height: 100vh;
}

.banner-card {
  margin-bottom: 24px;
  color: white;
  text-align: center;
  padding: 48px 32px;
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(79, 70, 229, 0.35);
  position: relative;
  overflow: hidden;
  animation: fadeInDown 0.6s ease-out;
}

:deep(.banner-card) {
  background: url('https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=1200&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
}

.banner-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-20px, -20px) scale(1.1); }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-icon {
  font-size: 56px;
  margin-bottom: 16px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
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
  color: #EEF2FF;
  letter-spacing: 0.5px;
}

.search-card {
  margin-bottom: 24px;
  border-radius: 20px;
  border: 3px solid #C7D2FE !important;
  box-shadow: 0 8px 30px rgba(79, 70, 229, 0.12);
  transition: all 0.3s ease;
  animation: fadeIn 0.6s ease-out 0.1s both;
}

:deep(.search-card) {
  background: url('https://images.unsplash.com/photo-1557683316-973673baf926?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
}

.search-card:hover {
  transform: translateY(-4px) scale(1.01);
  border-color: #4F46E5 !important;
  box-shadow: 0 12px 40px rgba(79, 70, 229, 0.25);
}

:deep(.search-card:hover) {
  background: url('https://images.unsplash.com/photo-1557683316-973673baf926?w=800&q=80') !important;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.stats-card {
  margin-bottom: 24px;
  border-radius: 20px;
  border: 3px solid #C7D2FE !important;
  box-shadow: 0 8px 30px rgba(79, 70, 229, 0.12);
  animation: fadeIn 0.6s ease-out 0.2s both;
  transition: all 0.3s ease;
}

:deep(.stats-card) {
  background: url('https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(79, 70, 229, 0.2);
}

.tag-filter {
  padding: 20px;
  background: linear-gradient(135deg, #EEF2FF, #E0E7FF);
  border-radius: 16px;
  border: 2px solid #C7D2FE;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 24px;
  background: #ffffff;
  border-radius: 20px;
  border: 3px solid #C7D2FE;
  box-shadow: 0 8px 30px rgba(79, 70, 229, 0.12);
}
</style>