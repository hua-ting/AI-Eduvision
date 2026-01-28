/**
 * 内容管理API
 */

import request from '@/utils/request'

/**
 * 保存内容（管理员权限）
 */
export function saveContent(data) {
  return request({
    url: '/api/content/save',
    method: 'post',
    data
  })
}

/**
 * 提交内容审核申请（普通用户权限）
 */
export function submitForReview(data) {
  return request({
    url: '/api/content/submit-review',
    method: 'post',
    data
  })
}

/**
 * 获取内容详情
 */
export function getContentById(id) {
  return request({
    url: `/api/content/${id}`,
    method: 'get'
  })
}