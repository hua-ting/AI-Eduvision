<template>
  <div class="knowledge-review">
    <n-card :bordered="false" class="mb-4">
      <n-tabs type="line" v-model:value="tabValue" @update:value="handleTabChange">
        <n-tab-pane name="pending" tab="待审核">
          <n-space vertical :size="16">
            <n-grid cols="1 s:2 m:3 l:4" responsive="screen" :x-gap="12" :y-gap="12">
              <n-grid-item v-for="item in pendingReviews" :key="item.id">
                <n-card 
                  hoverable 
                  :bordered="false" 
                  class="review-card"
                  :content-style="{ padding: '16px' }"
                >
                  <template #header>
                    <n-space justify="space-between">
                      <n-ellipsis :line-clamp="1">{{ item.title }}</n-ellipsis>
                      <n-tag :type="getDifficultyType(item.difficulty)">
                        {{ item.difficulty }}
                      </n-tag>
                    </n-space>
                  </template>
                  
                  <div class="review-content">
                    <n-space vertical :size="8">
                      <n-space>
                        <n-tag type="info" size="small" :bordered="false">
                          {{ item.category }}
                        </n-tag>
                        <n-tag type="default" size="small" :bordered="false" v-if="item.subCategory">
                          {{ item.subCategory }}
                        </n-tag>
                      </n-space>
                      
                      <n-ellipsis :line-clamp="2" class="description">
                        {{ item.pendingContent || item.description }}
                      </n-ellipsis>
                      
                      <div class="stats">
                        <n-space :size="16">
                          <n-space :size="4" align="center">
                            <n-icon :component="EyeOutline" size="14" />
                            <span>{{ formatNumber(item.viewCount || 0) }}</span>
                          </n-space>
                          
                          <n-space :size="4" align="center">
                            <n-icon :component="HeartOutline" size="14" />
                            <span>{{ formatNumber(item.collectCount || 0) }}</span>
                          </n-space>
                          
                          <n-space :size="4" align="center">
                            <n-icon :component="StarOutline" size="14" />
                            <span>{{ item.avgRating || 0 }}</span>
                          </n-space>
                        </n-space>
                      </div>
                      
                      <div class="meta">
                        <n-space :size="12" :wrap="false">
                          <n-text depth="3" size="12">创建时间: {{ formatDate(item.createTime) }}</n-text>
                          <n-text depth="3" size="12">更新时间: {{ formatDate(item.updateTime) }}</n-text>
                        </n-space>
                      </div>
                    </n-space>
                  </div>
                  
                  <template #action>
                    <n-space justify="space-between">
                      <n-button 
                        type="primary" 
                        size="small" 
                        @click="handleEdit(item)"
                      >
                        编辑
                      </n-button>
                      <n-button 
                        type="success" 
                        size="small" 
                        @click="handleReview(item.id, 1)"
                        :loading="reviewLoading[item.id] === 1"
                      >
                        通过
                      </n-button>
                      <n-popconfirm 
                        @positive-click="handleReject(item.id)"
                        :disabled="reviewLoading[item.id] === 2"
                      >
                        <template #trigger>
                          <n-button 
                            type="error" 
                            size="small" 
                            :loading="reviewLoading[item.id] === 2"
                          >
                            拒绝
                          </n-button>
                        </template>
                        是否确定拒绝此修改申请？
                      </n-popconfirm>
                    </n-space>
                  </template>
                </n-card>
              </n-grid-item>
            </n-grid>
            
            <n-empty 
              v-if="pendingReviews.length === 0 && !loading" 
              description="暂无待审核内容"
              style="margin-top: 100px;"
            >
              <template #icon>
                <n-icon :component="DocumentTextOutline" :size="80" />
              </template>
            </n-empty>
          </n-space>
        </n-tab-pane>
        
        <n-tab-pane name="history" tab="审核历史">
          <n-data-table
            :columns="historyColumns"
            :data="reviewHistory"
            :loading="historyLoading"
            :pagination="historyPagination"
            :row-key="(row) => row.id"
          />
        </n-tab-pane>
      </n-tabs>
    </n-card>
    
    <!-- 审核意见模态框 -->
    <n-modal v-model:show="showReviewModal" :mask-closable="false">
      <n-card 
        style="width: 600px;" 
        title="审核意见" 
        :bordered="false" 
        size="huge"
        :segmented="{ content: 'soft', footer: 'soft' }"
      >
        <n-form :model="reviewForm" :label-width="80">
          <n-form-item label="审核结果">
            <n-radio-group v-model:value="reviewForm.status">
              <n-space>
                <n-radio :value="1">通过</n-radio>
                <n-radio :value="2">拒绝</n-radio>
              </n-space>
            </n-radio-group>
          </n-form-item>
          
          <n-form-item label="审核意见">
            <n-input 
              v-model:value="reviewForm.reason" 
              type="textarea" 
              placeholder="请输入审核意见（可选）"
              :autosize="{ minRows: 4, maxRows: 8 }"
            />
          </n-form-item>
        </n-form>
        
        <template #footer>
          <n-space justify="end">
            <n-button @click="showReviewModal = false">取消</n-button>
            <n-button 
              type="primary" 
              @click="submitReview"
              :loading="submitting"
            >
              确认审核
            </n-button>
          </n-space>
        </template>
      </n-card>
    </n-modal>
    
    <!-- 编辑模态框 -->
    <n-modal v-model:show="showEditModal" :mask-closable="false" style="width: 80vw; max-width: 1200px;">
      <n-card 
        title="编辑知识点内容" 
        :bordered="false" 
        size="huge"
        :segmented="{ content: 'soft', footer: 'soft' }"
      >
        <n-form :model="editForm" :label-width="80">
          <n-form-item label="标题">
            <n-input 
              v-model:value="editForm.title" 
              placeholder="请输入标题"
            />
          </n-form-item>
          
          <n-form-item label="描述">
            <n-input 
              v-model:value="editForm.description" 
              type="textarea" 
              placeholder="请输入描述"
              :autosize="{ minRows: 3, maxRows: 6 }"
            />
          </n-form-item>
          
          <n-form-item label="内容" :show-feedback="false">
            <markdown-editor
              v-model="editForm.content"
              :preview-mode-only="false"
              :max-height="'500px'"
            />
          </n-form-item>
        </n-form>
        
        <template #footer>
          <n-space justify="end">
            <n-button @click="showEditModal = false">取消</n-button>
            <n-button 
              type="primary" 
              @click="submitEdit"
              :loading="submitting"
            >
              保存并审核通过
            </n-button>
          </n-space>
        </template>
      </n-card>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { 
  EyeOutline, 
  HeartOutline, 
  StarOutline,
  DocumentTextOutline
} from '@vicons/ionicons5'
import { getPendingReviews, reviewKnowledgePoint, getReviewHistory } from '@/api/adminKnowledgeReview'
import { updateKnowledgePointContent } from '@/api/adminKnowledge'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const message = useMessage()

// 响应式数据
const tabValue = ref('pending')
const pendingReviews = ref([])
const reviewHistory = ref([])
const loading = ref(false)
const historyLoading = ref(false)
const reviewLoading = ref({})
const showReviewModal = ref(false)
const submitting = ref(false)
const currentReviewId = ref(null)

// 审核表单
const reviewForm = reactive({
  status: 1,
  reason: ''
})

// 历史记录分页
const historyPagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

// 监听标签页变化
watch(tabValue, async (newVal) => {
  if (newVal === 'history') {
    await fetchReviewHistory()
  } else if (newVal === 'pending') {
    await fetchPendingReviews()
  }
})

// 待审核记录表格列定义
const historyColumns = [
  {
    title: '知识点ID',
    key: 'id',
    width: 100
  },
  {
    title: '标题',
    key: 'title',
    render(row) {
      return h('div', {
        style: { maxWidth: '200px' }
      }, [
        h('div', { style: { fontWeight: 'bold' } }, row.title),
        h('div', { style: { fontSize: '12px', color: '#999', marginTop: '4px' } }, row.category)
      ])
    }
  },
  {
    title: '审核状态',
    key: 'auditStatus',
    render(row) {
      const statusMap = {
        1: { type: 'success', text: '通过' },
        2: { type: 'error', text: '拒绝' },
        0: { type: 'warning', text: '待审核' }
      }
      const status = statusMap[row.auditStatus] || { type: 'default', text: '未知' }
      return h('div', [
        h('n-tag', { type: status.type, size: 'small' }, status.text)
      ])
    }
  },
  {
    title: '审核意见',
    key: 'auditReason',
    render(row) {
      return h('div', { style: { maxWidth: '200px' } }, row.auditReason || '-')
    }
  },
  {
    title: '更新时间',
    key: 'updateTime',
    render(row) {
      return h('div', formatDate(row.updateTime))
    }
  }
]

// 获取待审核列表
const fetchPendingReviews = async () => {
  try {
    loading.value = true
    const response = await getPendingReviews({
      pageNum: 1,
      pageSize: 20
    })
    pendingReviews.value = response.data?.records || []
  } catch (error) {
    console.error('获取待审核列表失败:', error)
    message.error('获取待审核列表失败')
  } finally {
    loading.value = false
  }
}

// 获取审核历史
const fetchReviewHistory = async () => {
  try {
    historyLoading.value = true
    const response = await getReviewHistory({
      pageNum: historyPagination.value.page,
      pageSize: historyPagination.value.pageSize
    })
    reviewHistory.value = response.data?.records || []
    historyPagination.value.itemCount = response.data?.total || 0
  } catch (error) {
    console.error('获取审核历史失败:', error)
    message.error('获取审核历史失败')
  } finally {
    historyLoading.value = false
  }
}

// 处理审核（通过）
const handleReview = async (id, status) => {
  reviewLoading.value[id] = status
  
  try {
    const response = await reviewKnowledgePoint(id, {
      status,
      reason: status === 2 ? '管理员拒绝了此次修改申请' : ''
    })
    
    message.success(response.message || '审核成功')
    // 重新获取待审核列表
    await fetchPendingReviews()
  } catch (error) {
    console.error('审核失败:', error)
    message.error(error.message || '审核失败')
  } finally {
    reviewLoading.value[id] = null
  }
}

// 处理拒绝（带确认框）
const handleReject = async (id) => {
  reviewLoading.value[id] = 2
  
  try {
    const response = await reviewKnowledgePoint(id, {
      status: 2,
      reason: '管理员拒绝了此次修改申请'
    })
    
    message.success(response.message || '拒绝成功')
    // 重新获取待审核列表
    await fetchPendingReviews()
  } catch (error) {
    console.error('拒绝失败:', error)
    message.error(error.message || '拒绝失败')
  } finally {
    reviewLoading.value[id] = null
  }
}

// 显示审核模态框
const showReviewDialog = (id) => {
  currentReviewId.value = id
  reviewForm.status = 1
  reviewForm.reason = ''
  showReviewModal.value = true
}

// 处理编辑
const handleEdit = (item) => {
  // 创建一个临时编辑项用于编辑
  currentReviewId.value = item.id
  editForm.title = item.title
  editForm.description = item.description
  editForm.content = item.pendingContent || item.content
  showEditModal.value = true
}

// 显示编辑模态框
const showEditModal = ref(false)
const editForm = reactive({
  title: '',
  description: '',
  content: ''
})

// 提交编辑
const submitEdit = async () => {
  if (!currentReviewId.value) return
  
  submitting.value = true
  
  try {
    // 同时更新知识点内容和审核状态
    const updateResponse = await updateKnowledgePointContent(currentReviewId.value, {
      title: editForm.title,
      description: editForm.description,
      content: editForm.content
    })
    
    // 审核通过
    const reviewResponse = await reviewKnowledgePoint(currentReviewId.value, {
      status: 1, // 通过状态
      reason: '管理员已编辑并批准'
    })
    
    message.success('编辑并审核成功')
    showEditModal.value = false
    
    // 重新获取待审核列表
    await fetchPendingReviews()
  } catch (error) {
    console.error('编辑失败:', error)
    message.error(error.message || '编辑失败')
  } finally {
    submitting.value = false
  }
}

// 提交审核
const submitReview = async () => {
  if (!currentReviewId.value) return
  
  submitting.value = true
  
  try {
    const response = await reviewKnowledgePoint(currentReviewId.value, {
      status: reviewForm.status,
      reason: reviewForm.reason
    })
    
    message.success(response.message || '审核成功')
    showReviewModal.value = false
    
    // 重新获取待审核列表
    await fetchPendingReviews()
  } catch (error) {
    console.error('审核失败:', error)
    message.error(error.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

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
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num || 0
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 监听标签页切换
const handleTabChange = async (value) => {
  if (value === 'history') {
    await fetchReviewHistory()
  } else if (value === 'pending') {
    await fetchPendingReviews()
  }
}

onMounted(async () => {
  await fetchPendingReviews()
})
</script>

<style scoped>
.knowledge-review {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.mb-4 {
  margin-bottom: 16px;
}

.review-card {
  height: 100%;
  border-radius: 12px;
  border: 1px solid #e8eaec;
  transition: all 0.3s ease;
}

.review-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.review-content {
  min-height: 120px;
}

.description {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
}

.stats {
  margin-top: 8px;
}

.meta {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>