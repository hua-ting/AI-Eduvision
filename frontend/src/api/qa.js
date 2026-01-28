import request from '@/utils/request'

/**
 * 提问
 */
export function askQuestion(question, options = {}) {
  const { source, ...config } = options
  return request({
    url: '/api/qa/ask',
    method: 'post',
    data: { question, source },
    ...config // 允许传入额外的配置，如超时时间
  })
}

/**
 * 从问答生成知识点
 */
export function generateKnowledgePoint(qaId) {
  return request({
    url: `/api/qa/generate/${qaId}`,
    method: 'post'
  })
}

/**
 * 获取问答历史
 */
export function getQAHistory(params) {
  return request({
    url: '/api/qa/history',
    method: 'get',
    params
  })
}