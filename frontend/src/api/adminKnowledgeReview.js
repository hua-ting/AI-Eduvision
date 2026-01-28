/**
 * 管理员知识点审核管理API
 */

import request from '@/utils/request'

/**
 * 获取待审核的知识点列表
 */
export function getPendingReviews(params) {
  return request({
    url: '/api/admin/knowledge-review/pending',
    method: 'get',
    params
  })
}

/**
 * 审核知识点修改申请
 */
export function reviewKnowledgePoint(id, data) {
  return request({
    url: `/api/admin/knowledge-review/review/${id}`,
    method: 'post',
    data
  })
}

/**
 * 更新知识点内容
 */
export function updateKnowledgePointContent(id, data) {
  return request({
    url: `/api/admin/knowledge/${id}/content`,
    method: 'put',
    data
  })
}

/**
 * 获取审核历史
 */
export function getReviewHistory(params) {
  return request({
    url: '/api/admin/knowledge-review/history',
    method: 'get',
    params
  })
}