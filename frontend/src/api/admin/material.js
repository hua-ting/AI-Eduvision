import request from '@/utils/request'

/**
 * 获取管理员资料列表
 */
export function getAdminMaterialList(params) {
  return request({
    url: '/admin/material/list',
    method: 'get',
    params
  })
}

/**
 * 获取资料详情
 */
export function getMaterialDetail(id) {
  return request({
    url: `/admin/material/${id}`,
    method: 'get'
  })
}

/**
 * 更新资料信息
 */
export function updateMaterial(id, data) {
  return request({
    url: `/admin/material/${id}`,
    method: 'put',
    data
  })
}

/**
 * 审核资料
 */
export function reviewMaterial(id, status) {
  return request({
    url: `/admin/material/${id}/review`,
    method: 'put',
    params: { status }
  })
}

/**
 * 更新资料状态
 */
export function updateMaterialStatus(id, status) {
  return request({
    url: `/admin/material/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 删除资料
 */
export function deleteMaterial(id) {
  return request({
    url: `/admin/material/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除资料
 */
export function batchDeleteMaterial(ids) {
  return request({
    url: '/admin/material/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 批量更新状态
 */
export function batchUpdateStatus(ids, status) {
  return request({
    url: '/admin/material/batch/status',
    method: 'put',
    params: { status },
    data: ids
  })
}

/**
 * 获取资料统计
 */
export function getMaterialStats() {
  return request({
    url: '/admin/material/stats',
    method: 'get'
  })
}

/**
 * 上传资料
 */
export function uploadMaterial(data) {
  return request({
    url: '/admin/material/upload',
    method: 'post',
    data
  })
}
