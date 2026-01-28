<template>
  <div class="user-manage">
    <!-- 搜索和操作栏 -->
    <n-card :bordered="false" style="margin-bottom: 20px">
      <n-space>
        <n-input
          v-model:value="queryForm.keyword"
          placeholder="搜索用户名、昵称、邮箱"
          style="width: 300px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon><search-outline /></n-icon>
          </template>
        </n-input>
        
        <n-select
          v-model:value="queryForm.role"
          placeholder="角色"
          style="width: 120px"
          clearable
          :options="roleOptions"
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
    </n-card>

    <!-- 用户列表 -->
    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="userList"
        :pagination="pagination"
        :loading="loading"
        :scroll-x="1200"
      />
    </n-card>

    <!-- 编辑用户对话框 -->
    <n-modal v-model:show="showEditModal" preset="dialog" title="编辑用户" style="width: 600px">
      <n-form :model="editForm" label-placement="left" label-width="80">
        <n-form-item label="用户名">
          <n-input v-model:value="editForm.username" disabled />
        </n-form-item>
        <n-form-item label="昵称">
          <n-input v-model:value="editForm.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="邮箱">
          <n-input v-model:value="editForm.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="角色">
          <n-select v-model:value="editForm.role" :options="roleOptions" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="editForm.status" :options="statusOptions" />
        </n-form-item>
        <n-form-item label="兴趣标签">
          <n-select
            v-model:value="editForm.interestTags"
            multiple
            tag
            placeholder="请输入标签，回车确认"
          />
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
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NButton, NTag, NSpace, NPopconfirm, NModal, NForm, NFormItem, NInput, NSelect } from 'naive-ui'
import { SearchOutline, RefreshOutline } from '@vicons/ionicons5'
import { getAdminUserList, updateUserStatus, deleteUser, getUserDetail, updateUserInfo } from '@/api/admin/user'
import { formatDate } from '@/utils/format'

const message = useMessage()
const loading = ref(false)
const userList = ref([])
const showEditModal = ref(false)
const editForm = ref({
  id: null,
  username: '',
  nickname: '',
  email: '',
  role: 0,
  status: 1,
  interestTags: []
})
const editLoading = ref(false)

const queryForm = reactive({
  keyword: '',
  role: null,
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

const roleOptions = [
  { label: '学生', value: 0 },
  { label: '管理员', value: 1 }
]

const statusOptions = [
  { label: '禁用', value: 0 },
  { label: '正常', value: 1 }
]

const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '用户名',
    key: 'username',
    width: 150
  },
  {
    title: '昵称',
    key: 'nickname',
    width: 150
  },
  {
    title: '邮箱',
    key: 'email',
    width: 200
  },
  {
    title: '角色',
    key: 'role',
    width: 100,
    render: (row) => {
      const roleMap = {
        0: { text: '学生', type: 'info' },
        1: { text: '管理员', type: 'success' }
      }
      const role = roleMap[row.role] || { text: '未知', type: 'default' }
      return h(NTag, { type: role.type }, { default: () => role.text })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        0: { text: '禁用', type: 'error' },
        1: { text: '正常', type: 'success' }
      }
      const status = statusMap[row.status] || { text: '未知', type: 'default' }
      return h(NTag, { type: status.type }, { default: () => status.text })
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
    width: 220,
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
          row.status === 1 && h(
            NButton,
            {
              size: 'small',
              type: 'warning',
              onClick: () => handleStatusChange(row.id, 0)
            },
            { default: () => '禁用' }
          ),
          row.status === 0 && h(
            NButton,
            {
              size: 'small',
              type: 'success',
              onClick: () => handleStatusChange(row.id, 1)
            },
            { default: () => '启用' }
          ),
          row.role === 0 && h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.id)
            },
            {
              default: () => '确定删除该用户吗？',
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

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await getAdminUserList(queryForm)
    userList.value = data.records
    pagination.itemCount = data.total
    pagination.page = data.current
    pagination.pageSize = data.size
  } catch (error) {
    message.error('获取用户列表失败')
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
  queryForm.role = null
  queryForm.pageNum = 1
  pagination.page = 1
  fetchData()
}

// 状态变更
const handleStatusChange = async (id, status) => {
  try {
    await updateUserStatus(id, status)
    message.success(status === 1 ? '启用成功' : '禁用成功')
    fetchData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await deleteUser(id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error('删除失败')
  }
}

// 编辑用户
const handleEdit = async (id) => {
  try {
    const { data } = await getUserDetail(id)
    editForm.value = {
      id: data.id,
      username: data.username,
      nickname: data.nickname || '',
      email: data.email || '',
      role: data.role,
      status: data.status,
      interestTags: data.interestTags || []
    }
    showEditModal.value = true
  } catch (error) {
    message.error('获取用户信息失败')
  }
}

// 保存编辑
const handleSaveEdit = async () => {
  editLoading.value = true
  try {
    await updateUserInfo(editForm.value.id, editForm.value)
    message.success('更新成功')
    showEditModal.value = false
    fetchData()
  } catch (error) {
    message.error('更新失败')
  } finally {
    editLoading.value = false
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
  fetchData()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}
</style>
