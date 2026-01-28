<template>
  <div class="crawler-page">
    <n-card title="资料采集" :bordered="false">
      <!-- 搜索表单 -->
      <n-space vertical :size="20">
        <n-form inline :label-width="80">
          <n-form-item label="关键词">
            <n-input 
              v-model:value="searchForm.keyword" 
              placeholder="请输入搜索关键词"
              style="width: 300px;"
              @keyup.enter="handleSearch"
            />
          </n-form-item>
          
          <n-form-item label="资料源">
            <n-select 
              v-model:value="searchForm.sourceType" 
              :options="sourceOptions"
              placeholder="选择资料源"
              style="width: 150px;"
            />
          </n-form-item>
          
          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSearch" :loading="searching">
                <template #icon>
                  <n-icon :component="SearchOutline" />
                </template>
                搜索
              </n-button>
              <n-button @click="handleReset">重置</n-button>
            </n-space>
          </n-form-item>
        </n-form>

        <!-- 搜索结果 -->
        <n-card v-if="searchResults.length > 0" :bordered="false" style="background: #f5f7fa;">
          <template #header>
            <n-space justify="space-between">
              <span>搜索结果（{{searchResults.length}}条）</span>
              <n-button 
                type="success" 
                :disabled="selectedRows.length === 0"
                @click="handleImport"
                :loading="importing"
              >
                <template #icon>
                  <n-icon :component="CloudDownloadOutline" />
                </template>
                导入选中（{{selectedRows.length}}）
              </n-button>
            </n-space>
          </template>

          <n-data-table
            :columns="columns"
            :data="searchResults"
            :row-key="row => row.url"
            @update:checked-row-keys="handleCheck"
            :pagination="false"
            max-height="500"
          />
        </n-card>

        <n-empty v-else-if="!searching" description="请输入关键词搜索资料" />
      </n-space>
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { useMessage, NTag, NSpace, NButton } from 'naive-ui'
import { SearchOutline, CloudDownloadOutline } from '@vicons/ionicons5'
import { searchMaterials, importMaterials, getAvailableSources } from '@/api/admin/crawler'

const message = useMessage()
const searching = ref(false)
const importing = ref(false)

const searchForm = ref({
  keyword: '',
  sourceType: 'CSDN',
  page: 1,
  pageSize: 20
})

const sourceOptions = ref([])
const searchResults = ref([])
const selectedRows = ref([])

// 表格列定义
const columns = [
  { type: 'selection' },
  {
    title: '标题',
    key: 'title',
    width: 300,
    ellipsis: { tooltip: true }
  },
  {
    title: '描述',
    key: 'description',
    width: 250,
    ellipsis: { tooltip: true }
  },
  {
    title: '分类',
    key: 'category',
    width: 100,
    render: (row) => {
      return h(NTag, { type: 'primary', size: 'small' }, { default: () => row.category })
    }
  },
  {
    title: '难度',
    key: 'difficulty',
    width: 80,
    render: (row) => {
      const typeMap = { '初级': 'success', '中级': 'warning', '高级': 'error' }
      return h(NTag, { type: typeMap[row.difficulty], size: 'small' }, { default: () => row.difficulty })
    }
  },
  {
    title: '来源',
    key: 'source',
    width: 100
  },
  {
    title: '作者',
    key: 'author',
    width: 100
  }
]

// 获取资料源列表
const fetchSources = async () => {
  try {
    const { data } = await getAvailableSources()
    sourceOptions.value = data.map(item => ({ label: item, value: item }))
  } catch (error) {
    message.error('获取资料源失败')
  }
}

// 搜索
const handleSearch = async () => {
  if (!searchForm.value.keyword) {
    message.warning('请输入搜索关键词')
    return
  }

  try {
    searching.value = true
    const { data } = await searchMaterials(searchForm.value)
    searchResults.value = data
    message.success(`找到 ${data.length} 条资料`)
  } catch (error) {
    message.error('搜索失败')
  } finally {
    searching.value = false
  }
}

// 重置
const handleReset = () => {
  searchForm.value.keyword = ''
  searchForm.value.sourceType = 'CSDN'
  searchResults.value = []
  selectedRows.value = []
}

// 选择行
const handleCheck = (keys) => {
  selectedRows.value = keys
}

// 导入
const handleImport = async () => {
  if (selectedRows.value.length === 0) {
    message.warning('请选择要导入的资料')
    return
  }

  try {
    importing.value = true
    const materials = searchResults.value.filter(item => selectedRows.value.includes(item.url))
    await importMaterials(materials)
    message.success(`成功导入 ${materials.length} 条资料`)
    
    // 清空选择
    selectedRows.value = []
  } catch (error) {
    message.error('导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  fetchSources()
})
</script>

<style scoped>
.crawler-page {
  padding: 24px;
}
</style>
