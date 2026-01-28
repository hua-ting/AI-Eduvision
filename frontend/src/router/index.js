import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: 'EduVision - 智能学习推荐平台' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },

  {
    path: '/app',
    component: () => import('@/layout/index.vue'),
    redirect: '/app/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { title: '首页', icon: 'dashboard' }
      },
      {
        path: 'knowledge',
        name: 'KnowledgeSquare',
        component: () => import('@/views/student/KnowledgeSquare.vue'),
        meta: { title: '知识点广场', icon: 'bulb' }
      },
      {
        path: 'knowledge/:id',
        name: 'KnowledgePointDetail',
        component: () => import('@/views/student/KnowledgePointDetail.vue'),
        meta: { title: '知识点详情' }
      },
      {
        path: 'qa',
        name: 'QA',
        component: () => import('@/views/student/QA.vue'),
        meta: { title: 'AI问答', icon: 'chatbubble' }
      },
      {
        path: 'material',
        name: 'Materials',
        component: () => import('@/views/student/MaterialSquare.vue'),
        meta: { title: '资料广场', icon: 'book' }
      },
      {
        path: 'material/:id',
        name: 'MaterialDetail',
        component: () => import('@/views/student/MaterialDetail.vue'),
        meta: { title: '资料详情' }
      },
      {
        path: 'recommend',
        name: 'Recommend',
        component: () => import('@/views/student/Recommend.vue'),
        meta: { title: '智能推荐', icon: 'star' }
      },
      {
        path: 'daily',
        name: 'DailyRecommend',
        component: () => import('@/views/student/DailyRecommend.vue'),
        meta: { title: '每日推荐', icon: 'gift' }
      },
      {
        path: 'collection',
        name: 'Collection',
        component: () => import('@/views/student/Collection.vue'),
        meta: { title: '我的收藏', icon: 'heart' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '个人中心', icon: 'user' }
      },
      {
        path: 'user-profile',
        name: 'UserProfile',
        component: () => import('@/views/student/UserProfile.vue'),
        meta: { title: '我的画像', icon: 'person' }
      }
    ]
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('@/layout/index.vue'),
    redirect: '/admin/knowledge',
    meta: { title: '管理后台', requiresAdmin: true },
    children: [
      {
        path: 'knowledge',
        name: 'AdminKnowledge',
        component: () => import('@/views/admin/KnowledgePointManage.vue'),
        meta: { title: '知识点管理', icon: 'bulb', requiresAdmin: true }
      },
      {
        path: 'material',
        name: 'AdminMaterial',
        component: () => import('@/views/admin/MaterialManage.vue'),
        meta: { title: '资料管理', icon: 'book', requiresAdmin: true }
      },
      {
        path: 'crawler',
        name: 'MaterialCrawler',
        component: () => import('@/views/admin/MaterialCrawler.vue'),
        meta: { title: '资料采集', icon: 'cloud-download', requiresAdmin: true }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', icon: 'users', requiresAdmin: true }
      },
      {
        path: 'stats',
        name: 'AdminStats',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '数据统计', icon: 'chart', requiresAdmin: true }
      },
      {
        path: 'knowledge-review',
        name: 'AdminKnowledgeReview',
        component: () => import('@/views/admin/KnowledgeReview.vue'),
        meta: { title: '审核管理', icon: 'document-text', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
