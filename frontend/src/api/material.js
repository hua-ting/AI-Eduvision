import request from '@/utils/request'


/**
 * 获取资料列表
 */
export function getMaterialList(params) {
  return request({
    url: '/material/list',
    method: 'get',
    params
  })
}

/**
 * 获取资料详情
 */
export function getMaterialDetail(id) {
  return request({
    url: `/material/${id}`,
    method: 'get'
  })
}

/**
 * 记录浏览
 */
export function recordView(materialId, duration) {
  return request({
    url: `/material/${materialId}/view`,
    method: 'post',
    params: { duration }
  })
}

/**
 * 收藏/取消收藏
 */
export function toggleCollect(materialId) {
  return request({
    url: `/material/${materialId}/collect`,
    method: 'post'
  })
}

/**
 * 评分
 */
export function rateMaterial(materialId, rating) {
  return request({
    url: `/material/${materialId}/rate`,
    method: 'post',
    params: { rating }
  })
}

/**
 * 获取我的收藏
 */
export function getMyCollections(params) {
  return request({
    url: '/material/my-collections',
    method: 'get',
    params
  })
}

/**
 * 学生上传资料
 */
export function uploadMaterial(data) {
  return request({
    url: '/material/upload',
    method: 'post',
    data
  })
}

/**
 * 批量获取资料详情
 */
export function getBatchMaterialDetails(ids) {
  return request({
    url: '/material/batch/detail',
    method: 'get',
    params: { ids: ids.join(',') }
  })
}

/**
 * 批量验证内容
 */
export function batchValidateContent(data) {
  return request({
    url: '/material/validate/batch',
    method: 'post',
    data
  })
}