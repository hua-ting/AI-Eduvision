import request from '@/utils/request'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 获取用户统计数据
 */
export function getUserStats() {
  return request({
    url: '/user/stats',
    method: 'get'
  })
}

/**
 * 获取最近浏览记录
 */
export function getRecentViews(limit = 10) {
  return request({
    url: '/user/recent-views',
    method: 'get',
    params: { limit }
  })
}
