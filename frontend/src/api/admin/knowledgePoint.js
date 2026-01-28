import request from '@/utils/request'

/**
 * 获取知识点列表（管理员）
 */
export function getAdminKnowledgePointList(params) {
  return request({
    url: '/admin/knowledge/list',
    method: 'get',
    params
  })
}

/**
 * 获取知识点详情（管理员）
 */
export function getKnowledgePointDetail(id) {
  return request({
    url: `/admin/knowledge/${id}`,
    method: 'get'
  })
}

/**
 * 审核知识点
 */
export function auditKnowledgePoint(id, status, reason) {
  return request({
    url: `/admin/knowledge/${id}/audit`,
    method: 'put',
    params: { status, reason }
  })
}

/**
 * 上下架知识点
 */
export function updateKnowledgePointStatus(id, status) {
  return request({
    url: `/admin/knowledge/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 删除知识点
 */
export function deleteKnowledgePoint(id) {
  return request({
    url: `/admin/knowledge/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除知识点
 */
export function batchDeleteKnowledgePoint(ids) {
  return request({
    url: '/admin/knowledge/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 批量更新状态
 */
export function batchUpdateStatus(ids, status) {
  return request({
    url: '/admin/knowledge/batch/status',
    method: 'put',
    data: ids,
    params: { status }
  })
}

/**
 * 更新知识点信息
 */
export function updateKnowledgePoint(id, data) {
  return request({
    url: `/admin/knowledge/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取统计信息
 */
export function getKnowledgePointStats() {
  return request({
    url: '/admin/knowledge/stats',
    method: 'get'
  })
}
