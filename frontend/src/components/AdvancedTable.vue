<template>
  <div class="advanced-table-container">
    <n-data-table
      :columns="columns"
      :data="data"
      :loading="loading"
      :pagination="paginationConfig"
      :scroll-x="scrollX"
      :max-height="maxHeight"
      :virtual-scroll="virtualScroll"
      :striped="striped"
      :bordered="bordered"
      :row-key="rowKey"
      size="medium"
      flex-height
      @update:page="handlePageChange"
      @update:page-size="handlePageSizeChange"
      @update:checked-row-keys="handleCheckedRowKeys"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  columns: {
    type: Array,
    required: true
  },
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  // 分页配置
  pagination: {
    type: [Object, Boolean],
    default: false
  },
  // 行的 key
  rowKey: {
    type: Function,
    default: (row) => row.id
  },
  // 横向滚动宽度
  scrollX: {
    type: [Number, String],
    default: undefined
  },
  // 最大高度（启用虚拟滚动）
  maxHeight: {
    type: Number,
    default: 600
  },
  // 虚拟滚动（大数据量优化）
  virtualScroll: {
    type: Boolean,
    default: true
  },
  // 斑马纹
  striped: {
    type: Boolean,
    default: true
  },
  // 边框
  bordered: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:page', 'update:pageSize', 'update:checked-row-keys'])

// 分页配置
const paginationConfig = computed(() => {
  if (props.pagination === false) {
    return false
  }
  
  return {
    page: props.pagination?.page || 1,
    pageSize: props.pagination?.pageSize || 20,
    itemCount: props.pagination?.total || props.data.length,
    showSizePicker: true,
    pageSizes: [10, 20, 50, 100],
    prefix: ({ itemCount }) => `共 ${itemCount} 条`,
    ...props.pagination
  }
})

// 页码变化
const handlePageChange = (page) => {
  emit('update:page', page)
}

// 页大小变化
const handlePageSizeChange = (pageSize) => {
  emit('update:pageSize', pageSize)
}

// 行选择变化
const handleCheckedRowKeys = (keys) => {
  emit('update:checked-row-keys', keys)
}
</script>

<style scoped>
.advanced-table-container {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.n-data-table) {
  --n-th-color: #fafafa;
  --n-th-font-weight: 600;
}

:deep(.n-data-table-thead) {
  position: sticky;
  top: 0;
  z-index: 2;
}

:deep(.n-data-table-tr:hover) {
  background-color: #fafafa;
}
</style>
