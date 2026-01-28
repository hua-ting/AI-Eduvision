import request from '@/utils/request'

/**
 * 获取用户画像
 */
export function getUserProfile() {
  return request({
    url: '/api/user-profile',
    method: 'get'
  })
}

/**
 * 获取学习统计
 */
export function getLearningStats() {
  return request({
    url: '/api/user-profile/stats',
    method: 'get'
  })
}

/**
 * 获取用户偏好
 */
export function getUserPreferences() {
  return request({
    url: '/api/user-profile/preferences',
    method: 'get'
  })
}

/**
 * 手动刷新画像
 */
export function refreshProfile() {
  return request({
    url: '/api/user-profile/refresh',
    method: 'get'
  })
}
