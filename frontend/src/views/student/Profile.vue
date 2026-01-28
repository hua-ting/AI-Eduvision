<template>
  <div class="profile-page">
    <!-- 基本信息 -->
    <n-card title="个人信息" :bordered="false" class="info-card">
      <n-form :model="formData" label-placement="left" label-width="100">
        <n-form-item label="头像">
          <n-space align="center" :size="20">
            <n-avatar :size="80" :src="formData.avatar" />
            <n-button @click="showAvatarModal = true">更换头像</n-button>
          </n-space>
        </n-form-item>
        
        <n-form-item label="用户名">
          <n-input v-model:value="formData.username" disabled />
        </n-form-item>
        
        <n-form-item label="昵称">
          <n-input v-model:value="formData.nickname" placeholder="请输入昵称" />
        </n-form-item>
        
        <n-form-item label="邮箱">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </n-form-item>
        
        <n-form-item label="兴趣标签">
          <n-select
            v-model:value="formData.interestTags"
            multiple
            :options="tagOptions"
            placeholder="选择您感兴趣的领域"
          />
        </n-form-item>
        
        <n-form-item>
          <n-space>
            <n-button type="primary" :loading="loading" @click="handleSave">
              保存修改
            </n-button>
            <n-button @click="handleReset">重置</n-button>
            <n-button type="warning" @click="showPasswordModal = true">
              修改密码
            </n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>

    <!-- 更换头像对话框 -->
    <n-modal v-model:show="showAvatarModal" preset="dialog" title="选择头像">
      <n-grid :x-gap="12" :y-gap="12" :cols="4">
        <n-gi v-for="avatar in avatarList" :key="avatar">
          <n-card
            :bordered="false"
            hoverable
            style="cursor: pointer;"
            @click="selectAvatar(avatar)"
          >
            <n-avatar :size="80" :src="avatar" />
          </n-card>
        </n-gi>
      </n-grid>
    </n-modal>
    <!-- 修改密码对话框 -->
    <n-modal v-model:show="showPasswordModal" preset="dialog" title="修改密码">
      <n-form :model="passwordForm" label-placement="left" label-width="80">
        <n-form-item label="原密码">
          <n-input
            v-model:value="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password-on="click"
          />
        </n-form-item>
        
        <n-form-item label="新密码">
          <n-input
            v-model:value="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password-on="click"
          />
        </n-form-item>
        
        <n-form-item label="确认密码">
          <n-input
            v-model:value="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password-on="click"
          />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showPasswordModal = false">取消</n-button>
          <n-button type="warning" @click="handleChangePassword">
            确认修改
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useMessage } from 'naive-ui'
import { updateUserInfo } from '@/api/user'

const userStore = useUserStore()
const message = useMessage()
const loading = ref(false)
const showAvatarModal = ref(false)
const showPasswordModal = ref(false)

const formData = reactive({
  username: '',
  nickname: '',
  email: '',
  avatar: '',
  interestTags: []
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const tagOptions = [
  { label: '算法', value: '算法' },
  { label: '数据结构', value: '数据结构' },
  { label: '数据库', value: '数据库' },
  { label: '操作系统', value: '操作系统' },
  { label: '计算机网络', value: '计算机网络' },
  { label: '人工智能', value: '人工智能' },
  { label: '机器学习', value: '机器学习' },
  { label: '深度学习', value: '深度学习' },
  { label: '前端开发', value: '前端开发' },
  { label: '后端开发', value: '后端开发' },
  { label: '移动开发', value: '移动开发' },
  { label: '大数据', value: '大数据' },
  { label: '云原生', value: '云原生' },
  { label: '软件工程', value: '软件工程' },
  { label: '高等数学', value: '高等数学' },
  { label: '线性代数', value: '线性代数' },
  { label: '概率论', value: '概率论' },
  { label: '大学英语', value: '大学英语' }
]

const avatarList = [
  // 可爱动物系列
  'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200&q=80', // 橘猫
  'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=200&q=80', // 柴犬
  'https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=200&q=80', // 北极熊
  'https://images.unsplash.com/photo-1437622368342-7a3d73a34c8f?w=200&q=80', // 小鸟
  'https://images.unsplash.com/photo-1574158622682-e40e69881006?w=200&q=80', // 猫咪特写
  'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=200&q=80', // 柯基
  // 自然风光系列
  'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=200&q=80', // 雪山
  'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=200&q=80', // 森林
  'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=200&q=80', // 海滩
  'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=200&q=80', // 星空
  // 美食系列
  'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=200&q=80', // 美食
  'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=200&q=80', // 甜品
  // 植物系列
  'https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=200&q=80', // 花朵
  'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=200&q=80', // 多肉植物
  // 艺术系列
  'https://images.unsplash.com/photo-1561214115-f2f134cc4912?w=200&q=80', // 水彩
  'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=200&q=80', // 彩色烟雾
]

// 选择头像
const selectAvatar = (avatar) => {
  formData.avatar = avatar
  showAvatarModal.value = false
}

// 保存修改
const handleSave = async () => {
  try {
    loading.value = true
    await updateUserInfo(formData)
    
    // 更新store
    userStore.setUserInfo({
      ...userStore.userInfo,
      ...formData
    })
    
    message.success('修改成功')
  } catch (error) {
    console.error('修改失败:', error)
    message.error('修改失败')
  } finally {
    loading.value = false
  }
}

// 重置
const handleReset = () => {
  loadUserInfo()
}

// 修改密码
const handleChangePassword = () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.warning('请填写完整')
    return
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次密码输入不一致')
    return
  }
  
  // 清空表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  showPasswordModal.value = false
  
  message.success('密码修改成功（功能待实现）')
}

// 加载用户信息
const loadUserInfo = () => {
  const userInfo = userStore.userInfo
  formData.username = userInfo.username
  formData.nickname = userInfo.nickname
  formData.email = userInfo.email || ''
  formData.avatar = userInfo.avatar
  
  // 解析兴趣标签
  if (userInfo.interestTags) {
    try {
      if (typeof userInfo.interestTags === 'string') {
        const tags = userInfo.interestTags.replace(/[\[\]"]/g, '').split(',')
        formData.interestTags = tags.map(t => t.trim()).filter(t => t)
      } else if (Array.isArray(userInfo.interestTags)) {
        formData.interestTags = userInfo.interestTags
      }
    } catch (e) {
      formData.interestTags = []
    }
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
  background: url('https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=1920&q=80');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  min-height: 100vh;
}

.info-card {
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(79, 70, 229, 0.12);
  animation: fadeIn 0.6s ease-out;
  margin-bottom: 24px;
  border: 3px solid #C7D2FE;
  transition: all 0.3s ease;
}

:deep(.info-card) {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px);
}

.info-card:hover {
  box-shadow: 0 12px 40px rgba(79, 70, 229, 0.18);
  transform: translateY(-2px);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
