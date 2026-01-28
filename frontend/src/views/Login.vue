<template>
  <div class="login-container">
    <!-- 动态背景层 -->
    <div class="animated-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <!-- 左侧品牌视觉区 - 大屏展示，移动端隐藏 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <span class="logo-icon">AI</span>
        </div>
        <h1 class="brand-title">AI 学习推荐平台</h1>
        <p class="brand-desc">
          基于大模型的个性化学习助手，精准匹配你的学习路径
        </p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-icon">🎯</span>
            <span>智能知识点推荐</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📊</span>
            <span>学习轨迹可视化</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📈</span>
            <span>个性化成长规划</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-section">
      <div class="form-card">
        <div class="form-header">
          <h2 class="form-title">{{ activeTab === 'login' ? '欢迎回来' : '创建账号' }}</h2>
          <p class="form-subtitle">
            {{
              activeTab === 'login'
                ? '登录后体验个性化学习推荐'
                : '注册即可开启智能学习之旅'
            }}
          </p>
        </div>

        <n-tabs
          v-model:value="activeTab"
          size="large"
          class="form-tabs"
          type="line"
        >
          <n-tab-pane name="login" tab="登录" />
          <n-tab-pane name="register" tab="注册" />
        </n-tabs>

        <div class="form-body">
          <!-- 登录表单 -->
          <n-form
            v-if="activeTab === 'login'"
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            size="large"
            class="form-content"
          >
            <n-form-item path="username" class="form-item">
              <n-input
                v-model:value="loginForm.username"
                placeholder="请输入用户名"
                :input-props="{ autocomplete: 'username' }"
                class="form-input"
              >
                <template #prefix>
                  <n-icon class="input-icon"><PersonOutline /></n-icon>
                </template>
              </n-input>
            </n-form-item>

            <n-form-item path="password" class="form-item">
              <n-input
                v-model:value="loginForm.password"
                type="password"
                show-password-on="click"
                placeholder="请输入密码"
                :input-props="{ autocomplete: 'current-password' }"
                class="form-input"
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <n-icon class="input-icon"><LockClosedOutline /></n-icon>
                </template>
              </n-input>
            </n-form-item>

            <n-button
              type="primary"
              block
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="form-btn"
            >
              登录
            </n-button>
          </n-form>

          <!-- 注册表单 -->
          <n-form
            v-if="activeTab === 'register'"
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            size="large"
            class="form-content"
          >
            <n-form-item path="username" class="form-item">
              <n-input
                v-model:value="registerForm.username"
                placeholder="用户名(4-20个字符)"
                :input-props="{ autocomplete: 'off' }"
                class="form-input"
              >
                <template #prefix>
                  <n-icon class="input-icon"><PersonOutline /></n-icon>
                </template>
              </n-input>
            </n-form-item>

            <n-form-item path="password" class="form-item">
              <n-input
                v-model:value="registerForm.password"
                type="password"
                placeholder="密码(6-20个字符)"
                :input-props="{ autocomplete: 'new-password' }"
                class="form-input"
              >
                <template #prefix>
                  <n-icon class="input-icon"><LockClosedOutline /></n-icon>
                </template>
              </n-input>
            </n-form-item>

            <n-form-item path="nickname" class="form-item">
              <n-input
                v-model:value="registerForm.nickname"
                placeholder="昵称"
                class="form-input"
              >
                <template #prefix>
                  <n-icon class="input-icon"><HappyOutline /></n-icon>
                </template>
              </n-input>
            </n-form-item>

            <n-form-item path="email" class="form-item">
              <n-input
                v-model:value="registerForm.email"
                placeholder="邮箱(选填)"
                class="form-input"
              >
                <template #prefix>
                  <n-icon class="input-icon"><MailOutline /></n-icon>
                </template>
              </n-input>
            </n-form-item>

            <n-form-item
              path="interestTags"
              label="兴趣标签"
              label-placement="left"
              label-width="80px"
              class="form-item tag-item"
            >
              <n-select
                v-model:value="registerForm.interestTags"
                multiple
                placeholder="选择感兴趣的领域（推荐）"
                :options="tagOptions"
                class="form-select"
                max-tag-count="3"
                max-tag-placeholder="还有${count}个标签"
              />
            </n-form-item>

            <n-button
              type="primary"
              block
              size="large"
              :loading="loading"
              @click="handleRegister"
              class="form-btn"
            >
              注册
            </n-button>
          </n-form>
        </div>
      </div>

      <!-- 底部版权信息 -->
      <div class="form-footer">
        <p class="copyright">© 2025 AI 学习推荐平台 - 智能学习助手</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login, register } from '@/api/user'
import { PersonOutline, LockClosedOutline, HappyOutline, MailOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const activeTab = ref('login')
const loading = ref(false)

// 登录表单
const loginFormRef = ref(null)
const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册表单
const registerFormRef = ref(null)
const registerForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  interestTags: []
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度4-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

// 兴趣标签选项
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

// 登录
const handleLogin = async () => {
  try {
    await loginFormRef.value?.validate()
    loading.value = true

    const res = await login(loginForm)

    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.userInfo)

    message.success('登录成功')

    // 根据用户角色跳转不同页面
    if (res.data.userInfo.role === 1) {
      // 管理员跳转到管理后台
      router.push('/admin/material')
    } else {
      // 普通用户跳转到应用首页
      router.push('/app/dashboard')
    }
  } catch (error) {
    console.error('登录失败:', error)
    message.error(error.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}

// 注册
const handleRegister = async () => {
  try {
    await registerFormRef.value?.validate()
    loading.value = true

    await register(registerForm)

    message.success('注册成功,请登录')
    activeTab.value = 'login'

    // 清空表单
    registerForm.username = ''
    registerForm.password = ''
    registerForm.nickname = ''
    registerForm.email = ''
    registerForm.interestTags = []
  } catch (error) {
    console.error('注册失败:', error)
    message.error(error.message || '注册失败，请检查表单信息')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 全局容器 */
.login-container {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(180deg, #f0f9ff 0%, #e0f2fe 50%, #f0f9ff 100%);
  overflow: hidden;
  font-family: 'Jost', sans-serif;
  position: relative;
}

/* 动态背景层 */
.animated-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
  animation: float-blob 20s ease-in-out infinite;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  top: -10%;
  left: -10%;
  animation-delay: 0s;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #f97316, #fb923c);
  bottom: -15%;
  right: -5%;
  animation-delay: 5s;
}

.blob-3 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #38bdf8, #22d3ee);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 10s;
}

@keyframes float-blob {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* 左侧品牌区 - 大屏展示 */
.brand-section {
  flex: 1;
  background: rgba(14, 165, 233, 0.05);
  backdrop-filter: blur(30px);
  color: #0c4a6e;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
  z-index: 1;
  border-right: 1px solid rgba(14, 165, 233, 0.15);
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(rgba(14, 165, 233, 0.15) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 1;
}

.brand-content {
  max-width: 400px;
  z-index: 2;
  animation: fadeInLeft 0.8s ease-out;
}

.brand-logo {
  margin-bottom: 24px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  color: #fff;
  border-radius: 20px;
  font-size: 28px;
  font-weight: 800;
  box-shadow: 0 10px 30px rgba(14, 165, 233, 0.4);
}

.brand-title {
  font-family: 'Bodoni Moda', serif;
  font-size: 42px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 16px;
  color: #0c4a6e;
}

.brand-desc {
  font-size: 16px;
  line-height: 1.5;
  color: #64748b;
  margin-bottom: 32px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.feature-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

/* 右侧表单区 */
.form-section {
  flex: 0 0 520px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: transparent;
  position: relative;
  z-index: 1;
}

.form-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 30px 60px rgba(14, 165, 233, 0.15), inset 0 1px 1px rgba(255, 255, 255, 0.8);
  animation: fadeIn 0.8s ease-out;
}

.form-header {
  margin-bottom: 24px;
  text-align: center;
}

.form-title {
  font-family: 'Bodoni Moda', serif;
  font-size: 28px;
  font-weight: 700;
  color: #0c4a6e;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: #64748b;
}

/* 标签页样式 */
.form-tabs {
  margin-bottom: 28px;
}

:deep(.n-tabs-line) {
  --n-color: #6366f1;
}

:deep(.n-tabs-tab) {
  font-weight: 500;
  color: #64748b;
  padding: 8px 16px;
}

:deep(.n-tabs-tab--active) {
  color: #4338ca;
  font-weight: 600;
}

/* 表单内容 */
.form-body {
  width: 100%;
}

.form-content {
  width: 100%;
}

.form-item {
  margin-bottom: 20px;
}

.tag-item {
  margin-bottom: 24px;
}

/* 输入框样式 */
.form-input {
  width: 100%;
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.form-input .n-input__border) {
  border-color: #e2e8f0;
}

:deep(.form-input .n-input__border:hover) {
  border-color: #94a3b8;
}

:deep(.form-input .n-input__border:focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.1);
}

.input-icon {
  color: #94a3b8;
  font-size: 18px;
}

/* 选择器样式 */
.form-select {
  width: 100%;
  border-radius: 12px;
}

:deep(.form-select .n-select__border) {
  border-color: #e2e8f0;
}

/* 按钮样式 */
.form-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  background-color: #f97316; /* CTA Orange */
  border: none;
  color: #ffffff;
  font-weight: 700;
  font-size: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 8px 16px rgba(249, 115, 22, 0.2);
}

.form-btn:hover {
  background-color: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 12px 20px rgba(249, 115, 22, 0.3);
}

:deep(.form-btn .n-button__loading-icon) {
  color: #fff;
}

/* 底部信息 */
.form-footer {
  margin-top: 32px;
  text-align: center;
}

.copyright {
  font-size: 12px;
  color: #94a3b8;
}

/* 动画效果 */
@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .brand-section {
    display: none;
  }

  .form-section {
    flex: 1;
    padding: 20px 16px;
  }

  .form-card {
    padding: 24px 16px;
    box-shadow: none;
  }

  .form-title {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .form-section {
    padding: 16px 8px;
  }

  .form-card {
    padding: 20px 12px;
  }
}
</style>