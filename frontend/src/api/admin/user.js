import request from '@/utils/request'

/**
 * 获取用户列表
 */
export function getAdminUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/admin/user/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
}

/**
 * 获取用户详情
 */
export function getUserDetail(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 */
export function updateUserInfo(id, data) {
  return request({
    url: `/admin/user/${id}`,
    method: 'put',
    data
  })
}
