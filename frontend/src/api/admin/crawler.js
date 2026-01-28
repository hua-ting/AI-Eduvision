import request from '@/utils/request'

/**
 * 搜索资料
 */
export function searchMaterials(data) {
  return request({
    url: '/admin/crawler/search',
    method: 'post',
    data
  })
}

/**
 * 批量导入资料
 */
export function importMaterials(data) {
  return request({
    url: '/admin/crawler/import',
    method: 'post',
    data
  })
}

/**
 * 获取可用资料源
 */
export function getAvailableSources() {
  return request({
    url: '/admin/crawler/sources',
    method: 'get'
  })
}
