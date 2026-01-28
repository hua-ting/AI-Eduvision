<template>
  <div class="qa-container">
    <n-card title="AI问答助手" :bordered="false">
      <template #header-extra>
        <n-tag type="success" size="small">
          <template #icon>
            <n-icon :component="CheckmarkCircle" />
          </template>
          智能问答
        </n-tag>
      </template>

      <!-- 问答输入区 -->
      <n-space vertical :size="16">
        <n-input
          v-model:value="question"
          type="textarea"
          placeholder="请输入您的问题，AI将为您解答..."
          :rows="4"
          :maxlength="500"
          show-count
          @keydown.ctrl.enter="handleAsk"
        />
        <n-space>
          <n-button
            type="primary"
            :loading="asking"
            :disabled="!question.trim()"
            @click="handleAsk"
          >
            <template #icon>
              <n-icon :component="Send" />
            </template>
            提问 (Ctrl+Enter)
          </n-button>
          <n-button @click="question = ''">
            <template #icon>
              <n-icon :component="Close" />
            </template>
            清空
          </n-button>
        </n-space>
      </n-space>

      <!-- 当前回答展示 -->
      <n-card v-if="currentAnswer" title="AI回答" class="answer-card" :bordered="false">
        <template #header-extra>
          <n-space>
            <n-tag size="small">{{ currentAnswer.modelName }}</n-tag>
            <n-tag size="small" type="info">{{ currentAnswer.duration }}ms</n-tag>
          </n-space>
        </template>
        
        <div class="answer-content">
          {{ currentAnswer.answer }}
        </div>

        <template #footer>
          <n-space justify="end">
            <n-button
              type="primary"
              :loading="generating"
              @click="handleGenerate"
            >
              <template #icon>
                <n-icon :component="BulbOutline" />
              </template>
              一键生成知识点
            </n-button>
          </n-space>
        </template>
      </n-card>
    </n-card>

    <!-- 问答历史 -->
    <n-card title="问答历史" class="history-card" :bordered="false">
      <n-list bordered>
        <n-list-item v-for="item in historyList" :key="item.id">
          <template #prefix>
            <n-icon :component="ChatbubbleEllipses" size="20" color="#0ea5e9" />
          </template>
          
          <n-thing>
            <template #header>
              <n-ellipsis style="max-width: 600px">
                {{ item.question }}
              </n-ellipsis>
            </template>
            <template #description>
              <n-space size="small">
                <n-tag size="tiny" type="info">{{ item.modelName }}</n-tag>
                <n-text depth="3" style="font-size: 12px">
                  {{ formatDate(item.createTime) }}
                </n-text>
                <n-tag v-if="item.generatedKpId" size="tiny" type="success">
                  已生成知识点
                </n-tag>
              </n-space>
            </template>
            <n-ellipsis :line-clamp="2" style="margin-top: 8px">
              {{ item.answer }}
            </n-ellipsis>
          </n-thing>

          <template #suffix>
            <n-space>
              <n-button
                size="small"
                @click="viewAnswer(item)"
              >
                查看
              </n-button>
              <n-button
                v-if="!item.generatedKpId"
                size="small"
                type="primary"
                @click="generateFromHistory(item)"
              >
                生成知识点
              </n-button>
              <n-button
                v-else
                size="small"
                @click="$router.push(`/app/knowledge/${item.generatedKpId}`)"
              >
                查看知识点
              </n-button>
            </n-space>
          </template>
        </n-list-item>

        <template #footer>
          <n-pagination
            v-model:page="pageNum"
            :page-count="totalPages"
            @update:page="fetchHistory"
          />
        </template>
      </n-list>
    </n-card>

    <!-- 知识点编辑弹窗 -->
    <n-modal
      v-model:show="showKPModal"
      preset="card"
      title="编辑知识点"
      style="width: 800px"
      :bordered="false"
      :segmented="{ content: 'soft', footer: 'soft' }"
    >
      <n-form ref="kpFormRef" :model="kpForm" label-placement="left" label-width="80px">
        <n-form-item label="标题">
          <n-input v-model:value="kpForm.title" placeholder="知识点标题" />
        </n-form-item>
        <n-form-item label="分类">
          <n-select
            v-model:value="kpForm.category"
            :options="categoryOptions"
            placeholder="选择分类"
          />
        </n-form-item>
        <n-form-item label="子分类">
          <n-input v-model:value="kpForm.subCategory" placeholder="可选" />
        </n-form-item>
        <n-form-item label="难度">
          <n-select
            v-model:value="kpForm.difficulty"
            :options="difficultyOptions"
          />
        </n-form-item>
        <n-form-item label="标签">
          <n-dynamic-tags v-model:value="kpForm.tags" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input
            v-model:value="kpForm.description"
            type="textarea"
            :rows="3"
            placeholder="简要描述"
          />
        </n-form-item>
        <n-form-item label="内容" style="height: 400px;">
          <MarkdownEditor
            v-model="kpForm.content"
            :auto-save-key="'qa-knowledge-' + Date.now()"
            placeholder="详细内容，支持Markdown格式"
            :max-height="'350px'"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showKPModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmitKP">
            提交审核
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import {
  Send,
  Close,
  BulbOutline,
  ChatbubbleEllipses,
  CheckmarkCircle
} from '@vicons/ionicons5'
import { askQuestion, generateKnowledgePoint, getQAHistory } from '@/api/qa'
import { contributeKnowledgePoint, batchValidateContent } from '@/api/knowledgePoint'
import MarkdownEditor from '@/components/MarkdownEditor.vue'

const message = useMessage()

// 问答相关
const question = ref('')
const asking = ref(false)
const currentAnswer = ref(null)
const generating = ref(false)

// 历史记录
const historyList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)

// 知识点编辑
const showKPModal = ref(false)
const submitting = ref(false)
const kpForm = ref({
  title: '',
  category: '',
  subCategory: '',
  difficulty: '中级',
  tags: [],
  description: '',
  content: ''
})

const categoryOptions = [
  { label: '算法', value: '算法' },
  { label: '数据库', value: '数据库' },
  { label: '人工智能', value: '人工智能' },
  { label: '前端开发', value: '前端开发' },
  { label: '后端开发', value: '后端开发' },
  { label: '其他', value: '其他' }
]

const difficultyOptions = [
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' }
]

// 提问
const handleAsk = async () => {
  if (!question.value.trim()) {
    message.warning('请输入问题')
    return
  }

  asking.value = true
  try {
    const res = await askQuestion(question.value)
    currentAnswer.value = res.data
    message.success('AI回答成功')
    question.value = ''
    await fetchHistory() // 刷新历史
  } catch (error) {
    message.error(error.message || '提问失败')
  } finally {
    asking.value = false
  }
}

// 生成知识点
const handleGenerate = async () => {
  if (!currentAnswer.value) return

  generating.value = true
  try {
    const res = await generateKnowledgePoint(currentAnswer.value.id)
    message.success('知识点已生成，等待审核')
    currentAnswer.value.generatedKpId = res.data
    await fetchHistory()
  } catch (error) {
    message.error(error.message || '生成失败')
  } finally {
    generating.value = false
  }
}

// 从历史生成
const generateFromHistory = async (item) => {
  try {
    const res = await generateKnowledgePoint(item.id)
    message.success('知识点已生成')
    item.generatedKpId = res.data
    await fetchHistory()
  } catch (error) {
    message.error(error.message || '生成失败')
  }
}

// 查看回答
const viewAnswer = (item) => {
  currentAnswer.value = {
    id: item.id,
    question: item.question,
    answer: item.answer,
    modelName: item.modelName,
    duration: item.duration,
    generatedKpId: item.generatedKpId
  }
}

// 获取历史
const fetchHistory = async () => {
  try {
    const res = await getQAHistory({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    historyList.value = res.data.records
    totalPages.value = Math.ceil(res.data.total / pageSize.value)
  } catch (error) {
    message.error('获取历史失败')
  }
}

// 提交知识点
const handleSubmitKP = async () => {
  submitting.value = true
  try {
    // 批量验证内容
    const validationResponse = await batchValidateContent([kpForm.value]);
    const validationErrors = validationResponse.data && validationResponse.data[kpForm.value.id || -1];
    
    if (validationErrors && validationErrors.length > 0) {
      message.warning('内容格式有问题：' + validationErrors.join('，'));
      return;
    }
    
    await contributeKnowledgePoint(kpForm.value)
    message.success('提交成功，等待审核')
    showKPModal.value = false
    await fetchHistory()
  } catch (error) {
    message.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.qa-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background: #F8FAFC;
  min-height: 100vh;
}

:deep(.n-card) {
  background: url('https://images.unsplash.com/photo-1488190211105-8b0e65b80b4e?w=800&q=80') !important;
  background-size: cover !important;
  background-position: center !important;
  border-radius: 20px;
  border: 3px solid #C7D2FE !important;
  box-shadow: 0 8px 30px rgba(79, 70, 229, 0.12);
  transition: all 0.3s ease;
}

:deep(.n-card:hover) {
  box-shadow: 0 12px 40px rgba(79, 70, 229, 0.18);
}

.answer-card {
  margin-top: 20px;
}

.answer-content {
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  line-height: 1.8;
  white-space: pre-wrap;
  color: #1E1B4B;
  font-family: 'Nunito', sans-serif;
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.08);
}

.history-card {
  margin-top: 20px;
}

:deep(.n-list-item) {
  padding: 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.n-list-item:hover) {
  background: rgba(79, 70, 229, 0.05);
}
</style>
