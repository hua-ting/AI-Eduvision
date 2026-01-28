import request from '@/utils/request'


/**
 * 获取知识点列表
 */
export function getKnowledgePointList(params) {
  return request({
    url: '/knowledge/list',
    method: 'get',
    params
  })
}

/**
 * 获取知识点详情
 */
export function getKnowledgePointDetail(id) {
  return request({
    url: `/knowledge/${id}`,
    method: 'get'
  })
}

/**
 * 记录浏览
 */
export function recordView(knowledgePointId, duration) {
  return request({
    url: `/knowledge/${knowledgePointId}/view`,
    method: 'post',
    params: { duration }
  })
}

/**
 * 收藏/取消收藏
 */
export function toggleCollect(knowledgePointId) {
  return request({
    url: `/knowledge/${knowledgePointId}/collect`,
    method: 'post'
  })
}

/**
 * 评分
 */
export function rateKnowledgePoint(knowledgePointId, rating) {
  return request({
    url: `/knowledge/${knowledgePointId}/rate`,
    method: 'post',
    params: { rating }
  })
}

/**
 * 获取我的收藏
 */
export function getUserCollections(pageNum, pageSize) {
  return request({
    url: '/knowledge/my-collections',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 获取最近浏览
 */
export function getRecentKnowledgeViews(limit) {
  return request({
    url: '/user/recent-knowledge-views',
    method: 'get',
    params: { limit }
  })
}

/**
 * 贡献知识点
 */
export function contributeKnowledgePoint(data) {
  return request({
    url: '/knowledge/contribute',
    method: 'post',
    data
  })
}

/**
 * 批量获取知识点详情
 */
export function getBatchKnowledgePointDetails(ids) {
  return request({
    url: '/knowledge/batch/detail',
    method: 'get',
    params: { ids: ids.join(',') }
  })
}

/**
 * 批量更新知识点
 */
export function batchUpdateKnowledgePoints(data) {
  return request({
    url: '/knowledge/batch/update',
    method: 'post',
    data
  })
}

/**
 * 批量验证内容
 */
export function batchValidateContent(data) {
  return request({
    url: '/knowledge/validate/batch',
    method: 'post',
    data
  })
}
