import request from '@/utils/request'

/**
 * 获取每日推荐
 */
export function getDailyRecommendations(count = 5) {
  return request({
    url: '/api/daily/recommendations',
    method: 'get',
    params: { count }
  })
}

/**
 * 生成推荐知识点
 */
export function generateRecommendedKP(category) {
  return request({
    url: '/api/daily/generate',
    method: 'post',
    params: { category }
  })
}/**
 * 清除用户缓存的推荐知识点
 */
export function clearDailyRecommendationsCache() {
  return request({
    url: '/api/daily/recommendations',
    method: 'delete'
  })
}

/**
 * 获取推荐主题列表
 */
export function getDailyRecommendTopics(count = 6) {
  return request({
    url: '/api/daily/topics',
    method: 'get',
    params: { count }
  })
}

/**
 * 清除用户主题缓存
 */
export function clearDailyRecommendTopicsCache() {
  return request({
    url: '/api/daily/topics/clear',
    method: 'post'
  })
}
