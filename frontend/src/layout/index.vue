<template>
  <n-layout has-sider style="min-height: 100vh; background: transparent;">
    <!-- 侧边栏 -->
    <n-layout-sider
      v-model:collapsed="collapsed"
      show-trigger
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :native-scrollbar="false"
      class="glass-sider"
      style="position: fixed; left: 0; top: 0; bottom: 0; z-index: 1005;"
    >
      <div class="logo" @click="openHomepageInNewTab" style="cursor: pointer;">
        <div class="logo-section" v-show="!collapsed">
          <div class="logo-icon-mini">AI</div>
          <span class="logo-text-mini">EduVision</span>
        </div>
        <div class="logo-icon-mini" v-show="collapsed">AI</div>
      </div>
      
      <n-menu
        v-model:value="activeKey"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        @update:value="handleMenuClick"
        style="border-right: none;"
      />
    </n-layout-sider>

    <!-- 主内容区 -->
    <n-layout class="main-layout-container" :style="{ marginLeft: collapsed ? '64px' : '240px', transition: 'margin-left 0.3s' }">
      <!-- 动态背景 -->
      <div class="app-bg">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
      </div>

      <!-- 头部 -->
      <n-layout-header 
        class="glass-header"
        style="
          position: fixed; 
          z-index: 1001; 
          right: 0; 
          height: 64px; 
          padding: 0 24px; 
          display: flex; 
          align-items: center; 
          justify-content: space-between; 
          width: 100%;
        " 
        :style="{ left: collapsed ? '64px' : '240px', width: collapsed ? 'calc(100% - 64px)' : 'calc(100% - 240px)', transition: 'left 0.3s, width 0.3s' }"
      >
        <div class="header-left">
          <h2 class="page-title">{{ currentTitle }}</h2>
        </div>
        
        <div class="header-right" style="display: flex; align-items: center; gap: 16px; z-index: 1002;">
          <n-dropdown :options="userOptions" @select="handleUserAction">
            <n-button text style="padding: 8px;">
              <n-space align="center">
                <n-avatar
                  round
                  size="small"
                  :src="userStore.userInfo?.avatar || ''"
                />
                <span style="color: #333; font-size: 14px; font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;">{{ userStore.userInfo?.nickname || '用户' }}</span>
              </n-space>
            </n-button>
          </n-dropdown>
        </div>
      </n-layout-header>

      <!-- 内容 -->
      <n-layout-content
        style="margin-top: 64px; min-height: calc(100vh - 64px); overflow-y: auto;"
        content-style="padding: 24px;"
        :native-scrollbar="false"
      >
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, h, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { NIcon, useMessage } from 'naive-ui'
import {
  HomeOutline,
  BulbOutline,
  BookOutline,
  StarOutline,
  HeartOutline,
  PersonOutline,
  LogOutOutline,
  PeopleOutline,
  StatsChartOutline,
  CloudDownloadOutline,
  ChatbubbleEllipses,
  GiftOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

// Logo URL - 可以替换为你的图片路径
const logoUrl = ref('/logo.jpg') // 示例: '/images/logo.png'

const activeKey = ref('dashboard')
const collapsed = ref(false)

// 渲染图标
const renderIcon = (icon) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 菜单选项 - 根据角色动态生成
const menuOptions = computed(() => {
  const isAdmin = userStore.userInfo.role === 1
  
  if (isAdmin) {
    // 管理员菜单
    return [
      {
        label: '知识点管理',
        key: 'admin/knowledge',
        icon: renderIcon(BulbOutline)
      },
      {
        label: '资料管理',
        key: 'admin/material',
        icon: renderIcon(BookOutline)
      },
      {
        label: '资料采集',
        key: 'admin/crawler',
        icon: renderIcon(CloudDownloadOutline)
      },
      {
        label: '用户管理',
        key: 'admin/user',
        icon: renderIcon(PeopleOutline)
      },
      {
        label: '数据统计',
        key: 'admin/stats',
        icon: renderIcon(StatsChartOutline)
      },
      {
        label: '审核管理',
        key: 'admin/knowledge-review',
        icon: renderIcon(BulbOutline)
      }
    ]
  } else {
    // 学生菜单
    return [
      {
        label: '首页',
        key: 'dashboard',
        icon: renderIcon(HomeOutline)
      },
      {
        label: '知识点广场',
        key: 'knowledge',
        icon: renderIcon(BulbOutline)
      },
      {
        label: 'AI问答',
        key: 'qa',
        icon: renderIcon(ChatbubbleEllipses)
      },
      {
        label: 'AI知识点创作',
        key: 'daily',
        icon: renderIcon(GiftOutline)
      },
      {
        label: '资料广场',
        key: 'material',
        icon: renderIcon(BookOutline)
      },
      {
        label: '智能推荐',
        key: 'recommend',
        icon: renderIcon(StarOutline)
      },
      {
        label: '我的收藏',
        key: 'collection',
        icon: renderIcon(HeartOutline)
      },
      {
        label: '个人中心',
        key: 'profile',
        icon: renderIcon(PersonOutline)
      }
    ]
  }
})

// 用户下拉菜单
const userOptions = [
  {
    label: '个人中心',
    key: 'profile',
    icon: renderIcon(PersonOutline)
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon(LogOutOutline)
  }
]

// 当前标题
const currentTitle = computed(() => {
  const item = menuOptions.value.find(item => item.key === activeKey.value)
  return item?.label || '学习推荐系统'
})

// 菜单点击
const handleMenuClick = async (key) => {
  try {
    const isAdmin = userStore.userInfo.role === 1
    if (isAdmin) {
      await router.push(`/${key}`)
    } else {
      await router.push(`/app/${key}`)
    }
  } catch (error) {
    console.error('路由跳转失败:', error)
    message.error('页面跳转失败，请重试')
  }
}

// 用户操作
const handleUserAction = async (key) => {
  try {
    if (key === 'logout') {
      userStore.logout()
      message.success('已退出登录')
      await router.push('/login')
    } else if (key === 'profile') {
      await router.push('/app/profile')
      activeKey.value = 'profile'
    }
  } catch (error) {
    console.error('用户操作失败:', error)
    message.error('操作失败，请重试')
  }
}

// 打开首页到新标签页
const openHomepageInNewTab = () => {
  window.open('/', '_blank')
}

// 监听路由变化更新激活菜单
router.afterEach((to) => {
  const path = to.path
  if (path.startsWith('/admin/')) {
    // 对于管理员路径，提取第二部分（去掉开头的空字符串和'admin'）
    const parts = path.split('/')
    if (parts.length >= 3) {
      activeKey.value = `admin/${parts[2]}`
    }
  } else if (path.startsWith('/app/')) {
    // 匹配 /app/dashboard 后的部分
    const parts = path.split('/')
    if (parts.length >= 3) {
      activeKey.value = parts[2]
    }
  }
})

// 在组件挂载后立即同步当前路由状态
const syncCurrentRoute = () => {
  const currentPath = router.currentRoute.value.path
  const path = currentPath
  if (path.startsWith('/admin/')) {
    const parts = path.split('/')
    if (parts.length >= 3) {
      activeKey.value = `admin/${parts[2]}`
    }
  } else if (path.startsWith('/app/')) {
    const parts = path.split('/')
    if (parts.length >= 3) {
      activeKey.value = parts[2]
    }
  }
}

// 在组件挂载后同步路由状态
onMounted(() => {
  syncCurrentRoute()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bodoni+Moda:wght@400;500;600;700&family=Jost:wght@300;400;500;600;700&display=swap');

.main-layout-container {
  background: linear-gradient(180deg, #f0f9ff 0%, #e0f2fe 50%, #f0f9ff 100%);
  font-family: 'Jost', sans-serif;
  position: relative;
  overflow: hidden;
}

/* 动态背景层 */
.app-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.2;
  animation: float-blob 20s ease-in-out infinite;
}

.blob-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  top: -20%;
  right: -10%;
}

.blob-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #f97316, #fb923c);
  bottom: -10%;
  left: -5%;
  animation-delay: -5s;
}

@keyframes float-blob {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(50px, -80px) scale(1.1); }
  66% { transform: translate(-30px, 40px) scale(0.9); }
}

/* 侧边栏玻璃效果 */
.glass-sider {
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(14, 165, 233, 0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: transparent;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon-mini {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  font-size: 14px;
}

.logo-text-mini {
  font-family: 'Bodoni Moda', serif;
  font-size: 18px;
  font-weight: 700;
  color: #0c4a6e;
}

/* 头部玻璃效果 */
.glass-header {
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(14, 165, 233, 0.1) !important;
}

.page-title {
  margin: 0;
  font-family: 'Bodoni Moda', serif;
  font-size: 20px;
  font-weight: 700;
  color: #0c4a6e;
}

/* 菜单样式调整 */
:deep(.n-menu-item-content) {
  border-radius: 12px;
  margin: 4px 8px;
}

:deep(.n-menu-item-content--selected) {
  background-color: rgba(14, 165, 233, 0.1) !important;
}

:deep(.n-menu-item-content--selected .n-menu-item-content-header) {
  color: #0ea5e9 !important;
  font-weight: 600;
}

:deep(.n-menu-item-content:hover) {
  background-color: rgba(14, 165, 233, 0.05);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
