<template>
  <div class="knowledge-manage">
    <!-- 统计卡片 -->
    <n-grid :x-gap="16" :y-gap="16" :cols="4" style="margin-bottom: 20px">
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="知识点总数" :value="stats.total">
            <template #prefix>
              <n-icon size="24" color="#18a058">
                <bulb-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="在线知识点" :value="stats.online">
            <template #prefix>
              <n-icon size="24" color="#2080f0">
                <checkmark-circle-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="待审核" :value="stats.pending">
            <template #prefix>
              <n-icon size="24" color="#f0a020">
                <time-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="下架知识点" :value="stats.offline">
            <template #prefix>
              <n-icon size="24" color="#d03050">
                <close-circle-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 搜索和操作栏 -->
    <n-card :bordered="false" style="margin-bottom: 20px">
      <n-space vertical>
        <n-space>
          <n-input
            v-model:value="queryForm.keyword"
            placeholder="搜索知识点标题或内容"
            style="width: 300px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon><search-outline /></n-icon>
            </template>
          </n-input>
          
          <n-select
            v-model:value="queryForm.category"
            placeholder="分类"
            style="width: 120px"
            clearable
            :options="categoryOptions"
          />
          
          <n-select
            v-model:value="queryForm.difficulty"
            placeholder="难度"
            style="width: 120px"
            clearable
            :options="difficultyOptions"
          />
          
          <n-select
            v-model:value="queryForm.status"
            placeholder="状态"
            style="width: 120px"
            clearable
            :options="statusOptions"
          />
          
          <n-button type="primary" @click="handleSearch">
            <template #icon>
              <n-icon><search-outline /></n-icon>
            </template>
            搜索
          </n-button>
          
          <n-button @click="handleReset">
            <template #icon>
              <n-icon><refresh-outline /></n-icon>
            </template>
            重置
          </n-button>
        </n-space>

        <!-- 批量操作 -->
        <n-space>
          <n-button
            type="success"
            :disabled="selectedIds.length === 0"
            @click="handleBatchStatus(1)"
          >
            批量上架
          </n-button>
          <n-button
            type="warning"
            :disabled="selectedIds.length === 0"
            @click="handleBatchStatus(0)"
          >
            批量下架
          </n-button>
          <n-button
            type="error"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </n-button>
        </n-space>
      </n-space>
    </n-card>

    <!-- 知识点列表 -->
    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="knowledgePointList"
        :pagination="pagination"
        :loading="loading"
        :row-key="row => row.id"
        :scroll-x="1800"
        @update:checked-row-keys="handleCheck"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </n-card>

    <!-- 审核对话框 -->
    <n-modal v-model:show="showAuditModal" preset="dialog" title="审核知识点" style="width: 500px">
      <n-form>
        <n-form-item label="审核结果">
          <n-radio-group v-model:value="auditForm.status">
            <n-space>
              <n-radio :value="1">通过</n-radio>
              <n-radio :value="0">拒绝</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="拒绝原因" v-if="auditForm.status === 0">
          <n-input
            v-model:value="auditForm.reason"
            type="textarea"
            placeholder="请输入拒绝原因"
            :rows="3"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showAuditModal = false">取消</n-button>
          <n-button type="primary" :loading="auditLoading" @click="handleSaveAudit">
            确定
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 编辑对话框 -->
    <n-modal v-model:show="showEditModal" preset="dialog" title="编辑知识点" style="width: 800px">
      <n-form ref="editFormRef" :model="editForm" label-placement="left" label-width="80">
        <n-form-item label="标题" required>
          <n-input v-model:value="editForm.title" placeholder="请输入知识点标题" />
        </n-form-item>
        <n-form-item label="分类" required>
          <n-select v-model:value="editForm.category" :options="categoryOptions" placeholder="请选择分类" />
        </n-form-item>
        <n-form-item label="难度" required>
          <n-select v-model:value="editForm.difficulty" :options="difficultyOptions" placeholder="请选择难度" />
        </n-form-item>
        <n-form-item label="内容" required :show-feedback="false">
          <markdown-editor
            v-model="editForm.content"
            :preview-mode-only="false"
            :max-height="'500px'"
            :content-id="editForm.id.toString()"
            :user-role="'admin'"
            :on-save="handleAdminSave"
          />
        </n-form-item>
        <n-form-item label="关键词">
          <n-input v-model:value="editForm.keywords" placeholder="多个关键词用逗号分隔" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showEditModal = false">取消</n-button>
          <n-button type="primary" :loading="editLoading" @click="handleSaveEdit">
            保存
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, nextTick } from 'vue'
import { useMessage, useDialog, NButton, NSpace, NTag, NEllipsis, NDataTable, NSwitch } from 'naive-ui'
import {
  BulbOutline,
  CheckmarkCircleOutline,
  TimeOutline,
  CloseCircleOutline,
  SearchOutline,
  RefreshOutline,
  CreateOutline
} from '@vicons/ionicons5'
import { formatDate } from '@/utils/format'
import {
  getAdminKnowledgePointList,
  getKnowledgePointStats,
  auditKnowledgePoint,
  updateKnowledgePointStatus,
  deleteKnowledgePoint,
  batchDeleteKnowledgePoint,
  batchUpdateStatus,
  getKnowledgePointDetail,
  updateKnowledgePoint
} from '@/api/admin/knowledgePoint'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const message = useMessage()
const dialog = useDialog()

const queryForm = reactive({
  keyword: '',
  category: null,
  difficulty: null,
  status: null,
  pageNum: 1,
  pageSize: 20
})

const knowledgePointList = ref([])
const loading = ref(false)
const selectedIds = ref([])
const stats = ref({
  total: 0,
  online: 0,
  offline: 0,
  pending: 0
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

const showAuditModal = ref(false)
const auditForm = reactive({
  id: null,
  status: 1,
  reason: ''
})
const auditLoading = ref(false)

const showEditModal = ref(false)
const editForm = reactive({
  id: null,
  title: '',
  category: '',
  difficulty: '',
  content: '',
  keywords: ''
})
const editLoading = ref(false)

const categoryOptions = [
  { label: '算法', value: '算法' },
  { label: '数据库', value: '数据库' },
  { label: '人工智能', value: '人工智能' },
  { label: '前端开发', value: '前端开发' },
  { label: '后端开发', value: '后端开发' },
  { label: '数学', value: '数学' },
  { label: '语言', value: '语言' }
]

const difficultyOptions = [
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' }
]

const statusOptions = [
  { label: '下架', value: 0 },
  { label: '上架', value: 1 },
  { label: '待审核', value: 2 }
]

const columns = [
  { type: 'selection' },
  { 
    title: 'ID', 
    key: 'id', 
    width: 80,
    fixed: 'left'
  },
  {
    title: '标题',
    key: 'title',
    width: 250,
    ellipsis: { tooltip: true },
    fixed: 'left'
  },
  {
    title: '分类',
    key: 'category',
    width: 120,
    render: (row) => row.category ? h(NTag, { type: 'success', bordered: false }, { default: () => row.category }) : '-'
  },
  {
    title: '难度',
    key: 'difficulty',
    width: 100,
    render: (row) => {
      if (!row.difficulty) return '-'
      const typeMap = { '初级': 'success', '中级': 'warning', '高级': 'error' }
      return h(NTag, { type: typeMap[row.difficulty] || 'default', bordered: false }, { default: () => row.difficulty })
    }
  },
  {
    title: '浏览量',
    key: 'viewCount',
    width: 100
  },
  {
    title: '收藏量',
    key: 'collectCount',
    width: 100
  },
  {
    title: '评分',
    key: 'avgRating',
    width: 100,
    render: (row) => (row.avgRating || 0).toFixed(1)
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        0: { label: '下架', type: 'error' },
        1: { label: '上架', type: 'success' },
        2: { label: '待审核', type: 'warning' }
      }
      const status = statusMap[row.status]
      return status ? h(NTag, { type: status.type, bordered: false }, { default: () => status.label }) : '-'
    }
  },
  {
    title: '上下架',
    key: 'statusSwitch',
    width: 80,
    render: (row) => {
      // 待审核状态不显示开关
      if (row.status === 2) return '-'
      
      return h(NSwitch, {
        value: row.status === 1,
        onUpdateValue: async (value) => {
          const newStatus = value ? 1 : 0
          
          try {
            await updateKnowledgePointStatus(row.id, newStatus)
            message.success(value ? '上架成功' : '下架成功')
            // 重新加载列表以更新状态
            await fetchList()
            fetchStats()
          } catch (err) {
            // 显示后端返回的错误信息
            const errorMsg = err.response?.data?.message || err.message || '操作失败'
            message.error(errorMsg)
            // 失败后重新加载恢复状态
            await fetchList()
          }
        }
      })
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180,
    render: (row) => formatDate(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: (row) => {
      const buttons = []
      
      // 待审核显示审核按钮
      if (row.status === 2) {
        buttons.push(
          h(NButton, {
            size: 'small',
            type: 'primary',
            onClick: () => handleAudit(row)
          }, { default: () => '审核' })
        )
      }
      
      // 编辑按钮
      buttons.push(
        h(NButton, {
          size: 'small',
          type: 'info',
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' })
      )
      
      // 删除按钮
      buttons.push(
        h(NButton, {
          size: 'small',
          type: 'error',
          onClick: () => handleDelete(row.id)
        }, { default: () => '删除' })
      )
      
      return h(NSpace, { size: 8 }, { default: () => buttons })
    }
  }
]

const fetchList = async () => {
  try {
    loading.value = true
    const res = await getAdminKnowledgePointList(queryForm)
    
    // 兼容不同响应格式
    if (res.data) {
      if (res.data.records) {
        // MyBatis-Plus分页格式
        knowledgePointList.value = res.data.records || []
        pagination.itemCount = res.data.total || 0
        
        // 调试：查看实际数据
        console.log('知识点数据:', knowledgePointList.value)
        if (knowledgePointList.value.length > 0) {
          console.log('第一条数据:', knowledgePointList.value[0])
          console.log('status值:', knowledgePointList.value[0].status, 'typeof:', typeof knowledgePointList.value[0].status)
        }
      } else if (Array.isArray(res.data)) {
        // 直接数组格式
        knowledgePointList.value = res.data
        pagination.itemCount = res.data.length
      } else {
        knowledgePointList.value = []
        pagination.itemCount = 0
      }
    }
  } catch (error) {
    message.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const res = await getKnowledgePointStats()
    stats.value = res.data
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  pagination.page = 1
  fetchList()
}

const handleReset = () => {
  Object.assign(queryForm, {
    keyword: '',
    category: null,
    difficulty: null,
    status: null,
    pageNum: 1,
    pageSize: 20
  })
  pagination.page = 1
  fetchList()
}

const handleCheck = (keys) => {
  selectedIds.value = keys
}

const handlePageChange = (page) => {
  queryForm.pageNum = page
  pagination.page = page
  fetchList()
}

const handlePageSizeChange = (pageSize) => {
  queryForm.pageSize = pageSize
  queryForm.pageNum = 1
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchList()
}

const handleAudit = (row) => {
  auditForm.id = row.id
  auditForm.status = 1
  auditForm.reason = ''
  showAuditModal.value = true
}

const handleSaveAudit = async () => {
  try {
    auditLoading.value = true
    await auditKnowledgePoint(auditForm.id, auditForm.status, auditForm.reason)
    message.success('审核成功')
    showAuditModal.value = false
    fetchList()
    fetchStats()
  } catch (error) {
    message.error('审核失败')
  } finally {
    auditLoading.value = false
  }
}

const handleEdit = async (row) => {
  try {
    const res = await getKnowledgePointDetail(row.id)
    const detail = res.data
    Object.assign(editForm, {
      id: detail.id,
      title: detail.title,
      category: detail.category,
      difficulty: detail.difficulty,
      content: detail.content,
      keywords: detail.keywords || ''
    })
    
    // 等待编辑框完全渲染后再显示模态框，确保内容能正确加载
    await nextTick()
    showEditModal.value = true
  } catch (error) {
    message.error('获取知识点详情失败')
  }
}

// 管理员保存内容（通过Markdown编辑器）
const handleAdminSave = async (content) => {
  // 更新编辑表单中的内容
  editForm.content = content
  
  if (!editForm.title || !editForm.category || !editForm.difficulty || !editForm.content) {
    message.warning('请填写完整信息')
    return Promise.reject(new Error('请填写完整信息'))
  }
  
  try {
    editLoading.value = true
    await updateKnowledgePoint(editForm.id, editForm)
    return Promise.resolve() // 成功时返回resolved promise
  } catch (error) {
    message.error('修改失败')
    return Promise.reject(error) // 失败时返回rejected promise
  } finally {
    editLoading.value = false
  }
}

const handleSaveEdit = async () => {
  if (!editForm.title || !editForm.category || !editForm.difficulty || !editForm.content) {
    message.warning('请填写完整信息')
    return
  }
  
  try {
    editLoading.value = true
    await updateKnowledgePoint(editForm.id, editForm)
    message.success('修改成功')
    showEditModal.value = false
    fetchList()
  } catch (error) {
    message.error('修改失败')
  } finally {
    editLoading.value = false
  }
}

const handleChangeStatus = async (id, status) => {
  try {
    await updateKnowledgePointStatus(id, status)
    message.success('操作成功')
    fetchList()
    fetchStats()
  } catch (error) {
    message.error('操作失败')
  }
}

const handleDelete = (id) => {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这个知识点吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteKnowledgePoint(id)
        message.success('删除成功')
        fetchList()
        fetchStats()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

const handleBatchStatus = (status) => {
  dialog.warning({
    title: '确认操作',
    content: `确定要${status === 1 ? '上架' : '下架'}选中的知识点吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await batchUpdateStatus(selectedIds.value, status)
        message.success('操作成功')
        selectedIds.value = []
        fetchList()
        fetchStats()
      } catch (error) {
        message.error('操作失败')
      }
    }
  })
}

const handleBatchDelete = () => {
  dialog.error({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedIds.value.length} 个知识点吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await batchDeleteKnowledgePoint(selectedIds.value)
        message.success('删除成功')
        selectedIds.value = []
        fetchList()
        fetchStats()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  fetchList()
  fetchStats()
})
</script>

<style scoped>
.knowledge-manage {
  padding: 20px;
}

.stat-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>
