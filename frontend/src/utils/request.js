import axios from 'axios'
import { useUserStore } from '@/store/user'
import { useMessage } from 'naive-ui'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 代理到后端
  timeout: 30000, // 增加超时时间到30秒
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从store获取token
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const { code, data, message } = response.data
    
    // 根据后端约定的状态码处理
    if (code === 200) {
      return response.data
    } else {
      // 非200状态，抛出错误让catch处理
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  (error) => {
    // 不在这里使用message提示，让调用方处理
    // 只在响应拦截器中处理401跳转登录
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

export default service