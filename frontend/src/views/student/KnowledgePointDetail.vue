<template>
  <div class="knowledge-detail">
    <n-spin :show="loading">
      <n-space vertical :size="20" v-if="knowledgePoint">
        <!-- 知识点头部 -->
        <n-card :bordered="false" class="header-card">
          <n-space vertical :size="16">
            <div class="title-section">
              <h1 class="knowledge-title">{{ knowledgePoint.title }}</h1>
              <n-space :size="12">
                <n-tag :bordered="false" type="success" size="large" round>
                  {{ knowledgePoint.category }}
                </n-tag>
                <n-tag
                    v-if="knowledgePoint.difficulty"
                    :bordered="false"
                    :type="getDifficultyType(knowledgePoint.difficulty)"
                    size="large"
                    round
                >
                  {{ knowledgePoint.difficulty }}
                </n-tag>
              </n-space>
            </div>

            <!-- 元信息 -->
            <n-space :size="24" class="meta-info">
              <n-space :size="8" align="center">
                <n-icon :component="TimeOutline" :size="18" />
                <span>{{ formatDate(knowledgePoint.createTime) }}</span>
              </n-space>

              <n-space :size="8" align="center">
                <n-icon :component="EyeOutline" :size="18" />
                <span>{{ formatNumber(knowledgePoint.viewCount) }} 浏览</span>
              </n-space>

              <n-space :size="8" align="center">
                <n-icon :component="HeartOutline" :size="18" />
                <span>{{ formatNumber(knowledgePoint.collectCount) }} 收藏</span>
              </n-space>

              <n-space :size="8" align="center">
                <n-icon :component="StarOutline" :size="18" />
                <span>{{ knowledgePoint.avgRating }} 分</span>
              </n-space>
            </n-space>

            <!-- 标签 -->
            <div class="tags" v-if="knowledgePoint.tags && knowledgePoint.tags.length > 0">
              <n-space :size="8">
                <n-tag
                    v-for="(tag, index) in knowledgePoint.tags"
                    :key="index"
                    :bordered="false"
                    type="info"
                    round
                >
                  #{{ tag }}
                </n-tag>
              </n-space>
            </div>
          </n-space>
        </n-card>

        <!-- 简要描述 -->
        <n-card :bordered="false" v-if="knowledgePoint.description">
          <template #header>
            <n-text strong :size="18">📝 简要描述</n-text>
          </template>
          <div class="description-content">
            {{ knowledgePoint.description }}
          </div>
        </n-card>

        <!-- 详细内容 -->
        <n-card :bordered="false">
          <template #header>
            <n-space justify="space-between" align="center">
              <n-text strong :size="18">💡 知识点详解</n-text>
              <n-space>
                <n-button
                    v-if="currentUserRole === 'admin'"
                    type="primary"
                    size="small"
                    @click="openEditModal"
                >
                  编辑
                </n-button>
                <n-button
                    v-else
                    type="info"
                    size="small"
                    @click="openEditModal"
                >
                  申请修改
                </n-button>
              </n-space>
            </n-space>
          </template>
          <!-- 原有Markdown展示 -->
          <div v-if="!isEditing" class="markdown-content">
            <MarkdownRenderer :content="knowledgePoint.content" />
          </div>
          <!-- 编辑模式：适配后的MarkdownEditor（核心修复：函数名/角色传参） -->
          <div v-else class="editor-wrapper" style="height: 800px; width: 100%;">
            <MarkdownEditor
                v-model="editingContent"
                :content-id="knowledgePoint.id + ''"
            :user-role="currentUserRole"
            :on-save="handleSaveSuccess"
            :on-submit-audit="handleSubmitAudit"
            @close="closeEditModal"
            @refresh-content="fetchKnowledgePointDetail"
            />
          </div>
        </n-card>

        <!-- 相关知识点 -->
        <n-card :bordered="false" v-if="knowledgePoint.relatedPoints && knowledgePoint.relatedPoints.length > 0">
          <template #header>
            <n-text strong :size="18">🔗 相关知识点</n-text>
          </template>
          <n-space :size="8">
            <n-tag
                v-for="(point, index) in knowledgePoint.relatedPoints"
                :key="index"
                :bordered="false"
                type="warning"
                size="medium"
                round
            >
              {{ point }}
            </n-tag>
          </n-space>
        </n-card>

        <!-- 操作区域 -->
        <n-card :bordered="false" class="action-card">
          <n-space justify="space-between" align="center">
            <n-space :size="12">
              <n-button
                  :type="knowledgePoint.isCollected ? 'error' : 'success'"
                  size="large"
                  round
                  @click="handleCollect"
              >
                <template #icon>
                  <n-icon>
                    <Heart v-if="knowledgePoint.isCollected" />
                    <HeartOutline v-else />
                  </n-icon>
                </template>
                {{ knowledgePoint.isCollected ? '取消收藏' : '收藏知识点' }}
              </n-button>
            </n-space>

            <n-space align="center" :size="16">
              <n-text>我的评分：</n-text>
              <n-rate
                  :value="knowledgePoint.userRating || 0"
                  :count="5"
                  size="large"
                  @update:value="handleRate"
              />
              <n-text depth="3">
                平均分：<n-text type="warning" strong>{{ knowledgePoint.avgRating }}</n-text> 分
              </n-text>
            </n-space>
          </n-space>
        </n-card>
      </n-space>

      <!-- 空状态 -->
      <n-empty
          v-else-if="!loading"
          description="知识点不存在"
          size="large"
          style="margin-top: 100px;"
      >
        <template #icon>
          <n-icon :component="BulbOutline" :size="80" />
        </template>
      </n-empty>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/store/user'
import {
  TimeOutline,
  EyeOutline,
  HeartOutline,
  StarOutline,
  Heart,
  BulbOutline
} from '@vicons/ionicons5'
// 接口导入
import { getKnowledgePointDetail, toggleCollect, rateKnowledgePoint, recordView } from '@/api/knowledgePoint'
// 组件导入
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

// 路由和全局实例
const route = useRoute()
const message = useMessage()

// 核心状态
const knowledgePoint = ref(null)
const loading = ref(false)
const startTime = ref(Date.now())

// 编辑相关状态（核心修复：角色默认值+类型映射）
const isEditing = ref(false)
const editingContent = ref('')
const currentUserRole = ref('normal') // 默认为普通用户，字符串类型
const currentUserId = ref(null)

// 难度标签类型映射
const getDifficultyType = (difficulty) => {
  const typeMap = {
    '初级': 'success',
    '中级': 'warning',
    '高级': 'error'
  }
  return typeMap[difficulty] || 'default'
}

// 数字格式化（万/千单位）
const formatNumber = (num) => {
  if (!num) return 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

// 日期格式化
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 获取知识点详情
const fetchKnowledgePointDetail = async () => {
  try {
    loading.value = true
    const res = await getKnowledgePointDetail(route.params.id)
    knowledgePoint.value = res.data
    // 立即记录浏览行为
    recordInitialView()
  } catch (error) {
    console.error('获取知识点详情失败:', error)
    message.error('获取知识点详情失败')
  } finally {
    loading.value = false
  }
}

// 初始浏览记录
const recordInitialView = async () => {
  try {
    await recordView(route.params.id, 0)
  } catch (error) {
    console.error('记录浏览失败:', error)
  }
}

// 收藏/取消收藏
const handleCollect = async () => {
  try {
    await toggleCollect(route.params.id)
    message.success('操作成功')
    fetchKnowledgePointDetail()
  } catch (error) {
    message.error('操作失败')
  }
}

// 评分处理
const handleRate = async (rating) => {
  try {
    await rateKnowledgePoint(route.params.id, rating)
    message.success('评分成功')
    fetchKnowledgePointDetail()
  } catch (error) {
    message.error('评分失败')
  }
}

// 记录浏览时长
const recordViewDuration = async () => {
  try {
    const duration = Math.floor((Date.now() - startTime.value) / 1000)
    await recordView(route.params.id, duration)
  } catch (error) {
    console.error('记录浏览时长失败:', error)
  }
}

// 核心修复：用户角色数字→字符串映射（0→normal，1→admin）
const mapUserRole = (roleNum) => {
  // 确保是数字类型，容错处理
  const num = Number(roleNum)
  return num === 1 ? 'admin' : 'normal'
}

// 打开编辑框（初始化编辑器内容和用户信息，核心修复：角色映射）
const openEditModal = () => {
  // 从store获取用户信息
  const userStore = useUserStore()
  if (userStore.userInfo) {
    currentUserRole.value = mapUserRole(userStore.userInfo.role) // 映射为字符串admin/normal
    currentUserId.value = userStore.userInfo.id
  } else {
    // 降级从localStorage获取
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (userInfo.role !== undefined) {
      currentUserRole.value = mapUserRole(userInfo.role) // 映射为字符串admin/normal
      currentUserId.value = userInfo.id
    }
  }
  // 初始化编辑内容为当前知识点内容
  editingContent.value = knowledgePoint.value.content
  isEditing.value = true
}

// 关闭编辑框（重置状态）
const closeEditModal = () => {
  isEditing.value = false
  editingContent.value = ''
}

// 编辑器保存成功回调（管理员模式，接收编辑器传的最新内容）
const handleSaveSuccess = async (editContent) => {
  try {
    // 【替换为你的真实后端接口】管理员保存知识点
    // 示例：import { updateKnowledgePoint } from '@/api/knowledgePoint'
    // await updateKnowledgePoint(knowledgePoint.value.id, { content: editContent, updateBy: currentUserId.value })
    message.success('内容保存成功')
    // 刷新详情页
    await fetchKnowledgePointDetail()
  } catch (error) {
    message.error('保存失败：' + (error.message || '服务器异常'))
    console.error('知识点保存失败：', error)
  }
}

// 编辑器提交审核成功回调（普通用户模式，接收编辑器传的最新内容）
const handleSubmitAudit = async (editContent) => {
  try {
    // 【替换为你的真实后端接口】普通用户提交知识点审核
    // 示例：import { submitKnowledgeAudit } from '@/api/knowledgePoint'
    // await submitKnowledgeAudit(knowledgePoint.value.id, { content: editContent, applyBy: currentUserId.value })
    message.success('审核申请已提交，等待管理员审核')
  } catch (error) {
    message.error('提交失败：' + (error.message || '服务器异常'))
    console.error('知识点提交审核失败：', error)
  }
}

// 页面挂载
onMounted(() => {
  fetchKnowledgePointDetail()
  startTime.value = Date.now()

  // 初始化用户信息（核心修复：角色映射）
  const userStore = useUserStore()
  if (userStore.userInfo) {
    currentUserRole.value = mapUserRole(userStore.userInfo.role)
    currentUserId.value = userStore.userInfo.id
  } else {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (userInfo.role !== undefined) {
      currentUserRole.value = mapUserRole(userInfo.role)
      currentUserId.value = userInfo.id
    }
  }
})

// 页面卸载（记录浏览时长）
onBeforeUnmount(() => {
  recordViewDuration()
})
</script>

<style scoped>
.header-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.title-section {
  margin-bottom: 16px;
}

.knowledge-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #333;
  line-height: 1.4;
}

.meta-info {
  color: #666;
  font-size: 14px;
}

.tags {
  margin-top: 16px;
}

/* Markdown展示区域样式优化 */
.markdown-content {
  line-height: 1.8;
  font-size: 14px;
  padding: 8px 0;
}

/* 编辑器容器：确保高度适配，编辑器继承此高度 */
.editor-wrapper {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  margin: 8px 0;
}

.action-card {
  position: sticky;
  bottom: 20px;
  border-radius: 20px;
  border: 3px solid #C7D2FE;
  background: rgba(255, 255, 255, 0.98) !important;
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(79, 70, 229, 0.2);
}

/* 描述区域样式优化 */
.description-content {
  line-height: 1.7;
  font-size: 14px;
  color: #333;
}
</style>