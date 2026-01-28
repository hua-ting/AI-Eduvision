import request from '@/utils/request'

/**
 * 获取个性化推荐
 */
export function getPersonalizedRecommendations(limit = 10) {
  return request({
    url: '/recommend/personalized',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取热门推荐
 */
export function getHotRecommendations(limit = 10) {
  return request({
    url: '/recommend/hot',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取相似资料
 */
export function getSimilarMaterials(materialId, limit = 6) {
  return request({
    url: `/recommend/similar/${materialId}`,
    method: 'get',
    params: { limit }
  })
}
