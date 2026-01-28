/**
 * 管理员知识点管理API
 */

import request from '@/utils/request'

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