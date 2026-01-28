<template>
  <div class="material-manage">
    <!-- 统计卡片 -->
    <n-grid :x-gap="16" :y-gap="16" :cols="4" style="margin-bottom: 20px">
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="资料总数" :value="stats.total">
            <template #prefix>
              <n-icon size="24" color="#18a058">
                <document-text-outline />
              </n-icon>
            </template>
          </n-statistic>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card :bordered="false" class="stat-card">
          <n-statistic label="在线资料" :value="stats.online">
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
          <n-statistic label="下架资料" :value="stats.offline">
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
            placeholder="搜索资料标题或描述"
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
          <n-button type="primary" @click="showUploadModal = true">
            <template #icon>
              <n-icon><add-outline /></n-icon>
            </template>
            上传资料
          </n-button>
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

    <!-- 资料列表 -->
    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="materialList"
        :pagination="pagination"
        :loading="loading"
        :row-key="row => row.id"
        :scroll-x="1800"
        @update:checked-row-keys="handleCheck"
      />
    </n-card>

    <!-- 编辑对话框 -->
    <n-modal v-model:show="showEditModal" preset="dialog" title="编辑资料" style="width: 600px">
      <n-form :model="editForm" label-placement="left" label-width="80">
        <n-form-item label="标题">
          <n-input v-model:value="editForm.title" placeholder="请输入资料标题" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input
            v-model:value="editForm.description"
            type="textarea"
            placeholder="请输入资料描述"
            :rows="3"
          />
        </n-form-item>
        <n-form-item label="分类">
          <n-select
            v-model:value="editForm.category"
            :options="categoryOptions"
            placeholder="请选择分类"
          />
        </n-form-item>
        <n-form-item label="难度">
          <n-select
            v-model:value="editForm.difficulty"
            :options="difficultyOptions"
            placeholder="请选择难度"
          />
        </n-form-item>
        <n-form-item label="文件URL">
          <n-input v-model:value="editForm.fileUrl" placeholder="请输入文件URL" />
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
import { ref, reactive, onMounted, h } from 'vue'
import { 
  useMessage, 
  NButton, 
  NTag, 
  NSpace, 
  NPopconfirm, 
  NModal, 
  NForm, 
  NFormItem, 
  NInput, 
  NSelect 
} from 'naive-ui'
import {
  DocumentTextOutline,
  CheckmarkCircleOutline,
  TimeOutline,
  CloseCircleOutline,
  SearchOutline,
  RefreshOutline,
  AddOutline
} from '@vicons/ionicons5'
import {
  getAdminMaterialList,
  getMaterialDetail,
  updateMaterial,
  updateMaterialStatus,
  deleteMaterial,
  batchDeleteMaterial,
  batchUpdateStatus,
  getMaterialStats,
  uploadMaterial
} from '@/api/admin/material'
import { formatDate } from '@/utils/format'

const message = useMessage()
const loading = ref(false)
const materialList = ref([])
const selectedIds = ref([])
const showEditModal = ref(false)
const editForm = ref({})
const editLoading = ref(false)
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

const stats = ref({
  total: 0,
  online: 0,
  pending: 0,
  offline: 0
})

const queryForm = reactive({
  keyword: '',
  category: null,
  difficulty: null,
  status: null,
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page) => {
    pagination.page = page
    queryForm.pageNum = page
    fetchData()
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    queryForm.pageSize = pageSize
    queryForm.pageNum = 1
    fetchData()
  }
})

const categoryOptions = [
  { label: '教材', value: '教材' },
  { label: '课件', value: '课件' },
  { label: '论文', value: '论文' },
  { label: '其他', value: '其他' }
]

const difficultyOptions = [
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' }
]

const statusOptions = [
  { label: '下架', value: 0 },
  { label: '在线', value: 1 },
  { label: '待审核', value: 2 }
]

const columns = [
  {
    type: 'selection'
  },
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '标题',
    key: 'title',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '分类',
    key: 'category',
    width: 100
  },
  {
    title: '难度',
    key: 'difficulty',
    width: 100,
    render: (row) => {
      const typeMap = {
        '初级': 'success',
        '中级': 'warning',
        '高级': 'error'
      }
      return h(NTag, { type: typeMap[row.difficulty] || 'default' }, { default: () => row.difficulty })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        0: { text: '下架', type: 'error' },
        1: { text: '在线', type: 'success' },
        2: { text: '待审核', type: 'warning' }
      }
      const status = statusMap[row.status] || { text: '未知', type: 'default' }
      return h(NTag, { type: status.type }, { default: () => status.text })
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
    render: (row) => row.avgRating?.toFixed(1) || '0.0'
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
    width: 180,
    fixed: 'right',
    render: (row) => {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: 'info',
              onClick: () => handleEdit(row.id)
            },
            { default: () => '编辑' }
          ),
          row.status === 2 && h(
            NButton,
            {
              size: 'small',
              type: 'success',
              onClick: () => handleReview(row.id, 1)
            },
            { default: () => '通过' }
          ),
          row.status === 2 && h(
            NButton,
            {
              size: 'small',
              type: 'error',
              onClick: () => handleReview(row.id, 0)
            },
            { default: () => '拒绝' }
          ),
          row.status === 1 && h(
            NButton,
            {
              size: 'small',
              type: 'warning',
              onClick: () => handleStatusChange(row.id, 0)
            },
            { default: () => '下架' }
          ),
          row.status === 0 && h(
            NButton,
            {
              size: 'small',
              type: 'success',
              onClick: () => handleStatusChange(row.id, 1)
            },
            { default: () => '上架' }
          ),
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.id)
            },
            {
              default: () => '确定删除该资料吗？',
              trigger: () => h(
                NButton,
                {
                  size: 'small',
                  type: 'error'
                },
                { default: () => '删除' }
              )
            }
          )
        ]
      })
    }
  }
]

// 获取统计数据
const fetchStats = async () => {
  try {
    const { data } = await getMaterialStats()
    stats.value = data
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getAdminMaterialList(queryForm)
    materialList.value = data.records
    pagination.itemCount = data.total
    pagination.page = data.current
    pagination.pageSize = data.size
  } catch (error) {
    console.error('获取资料列表失败:', error)
    message.error('获取资料列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.pageNum = 1
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.category = null
  queryForm.difficulty = null
  queryForm.status = null
  queryForm.pageNum = 1
  pagination.page = 1
  fetchData()
}

// 审核
const handleReview = async (id, status) => {
  try {
    await updateMaterialStatus(id, status)
    message.success(status === 1 ? '审核通过' : '已拒绝')
    fetchData()
    fetchStats()
  } catch (error) {
    message.error('操作失败')
  }
}

// 状态变更
const handleStatusChange = async (id, status) => {
  try {
    await updateMaterialStatus(id, status)
    message.success(status === 1 ? '上架成功' : '下架成功')
    fetchData()
    fetchStats()
  } catch (error) {
    message.error('操作失败')
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await deleteMaterial(id)
    message.success('删除成功')
    fetchData()
    fetchStats()
  } catch (error) {
    message.error('删除失败')
  }
}

// 批量更新状态
const handleBatchStatus = async (status) => {
  try {
    await batchUpdateStatus(selectedIds.value, status)
    message.success(status === 1 ? '批量上架成功' : '批量下架成功')
    selectedIds.value = []
    fetchData()
    fetchStats()
  } catch (error) {
    message.error('操作失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await batchDeleteMaterial(selectedIds.value)
    message.success('批量删除成功')
    selectedIds.value = []
    fetchData()
    fetchStats()
  } catch (error) {
    message.error('批量删除失败')
  }
}

// 选择行
const handleCheck = (keys) => {
  selectedIds.value = keys
}

// 编辑
const handleEdit = async (id) => {
  try {
    const { data } = await getMaterialDetail(id)
    editForm.value = { ...data }
    showEditModal.value = true
  } catch (error) {
    message.error('获取资料信息失败')
  }
}

// 保存编辑
const handleSaveEdit = async () => {
  editLoading.value = true
  try {
    await updateMaterial(editForm.value.id, editForm.value)
    message.success('更新成功')
    showEditModal.value = false
    fetchData()
  } catch (error) {
    message.error('更新失败')
  } finally {
    editLoading.value = false
  }
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
    message.success('上传成功')
    showUploadModal.value = false
    
    // 重置表单
    uploadForm.title = ''
    uploadForm.description = ''
    uploadForm.category = null
    uploadForm.difficulty = null
    uploadForm.tags = []
    uploadForm.fileUrl = ''
    
    fetchData()
    fetchStats()
  } catch (error) {
    const errorMsg = error.response?.data?.message || '上传失败'
    message.error(errorMsg)
  } finally {
    uploadLoading.value = false
  }
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  queryForm.pageNum = page
  fetchData()
}

// 页大小变化
const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  queryForm.pageSize = pageSize
  queryForm.pageNum = 1
  fetchData()
}

onMounted(() => {
  fetchStats()
  fetchData()
})
</script>

<style scoped>
.material-manage {
  padding: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card :deep(.n-statistic__label) {
  color: rgba(255, 255, 255, 0.9);
}

.stat-card :deep(.n-statistic__value) {
  color: white;
}
</style>
