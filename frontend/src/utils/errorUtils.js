// 错误处理工具

/**
 * 错误类型
 */
export const ErrorType = {
  NETWORK: 'NETWORK_ERROR',
  AUTHENTICATION: 'AUTH_ERROR',
  VALIDATION: 'VALIDATION_ERROR',
  SERVER: 'SERVER_ERROR',
  UNKNOWN: 'UNKNOWN_ERROR'
}

/**
 * 错误处理器
 */
export class ErrorHandler {
  constructor() {
    this.errorHandlers = new Map()
  }

  /**
   * 注册错误处理器
   * @param {string} type - 错误类型
   * @param {Function} handler - 错误处理函数
   */
  registerHandler(type, handler) {
    this.errorHandlers.set(type, handler)
  }

  /**
   * 处理错误
   * @param {Error} error - 错误对象
   * @param {Object} options - 处理选项
   * @param {boolean} options.showMessage - 是否显示错误消息
   * @param {string} options.defaultMessage - 默认错误消息
   * @returns {Object} 错误信息
   */
  handle(error, options = {}) {
    const { showMessage = true, defaultMessage = '操作失败' } = options
    
    let errorInfo = {
      type: ErrorType.UNKNOWN,
      message: defaultMessage,
      originalError: error
    }

    // 分析错误类型
    if (error.response) {
      // 服务器返回错误
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          errorInfo.type = ErrorType.AUTHENTICATION
          errorInfo.message = '登录已过期，请重新登录'
          break
        case 400:
          errorInfo.type = ErrorType.VALIDATION
          errorInfo.message = data.message || '参数错误'
          break
        case 403:
          errorInfo.message = '权限不足'
          break
        case 404:
          errorInfo.message = '资源不存在'
          break
        case 500:
          errorInfo.type = ErrorType.SERVER
          errorInfo.message = '服务器内部错误'
          break
        default:
          errorInfo.message = data.message || `请求失败 (${status})`
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      errorInfo.type = ErrorType.NETWORK
      errorInfo.message = '网络连接失败，请检查网络设置'
    } else {
      // 请求配置错误
      errorInfo.message = error.message || defaultMessage
    }

    // 调用对应类型的处理器
    if (this.errorHandlers.has(errorInfo.type)) {
      this.errorHandlers.get(errorInfo.type)(errorInfo)
    }

    // 显示错误消息
    if (showMessage) {
      this.showErrorMessage(errorInfo.message)
    }

    return errorInfo
  }

  /**
   * 显示错误消息
   * @param {string} message - 错误消息
   */
  showErrorMessage(message) {
    // 使用naive-ui的message组件
    const messageInstance = window.$message
    if (messageInstance) {
      messageInstance.error(message)
    } else {
      console.error(message)
    }
  }
}

// 导出单例实例
export const errorHandler = new ErrorHandler()

/**
 * 包装异步函数，自动处理错误
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 错误处理选项
 * @returns {Promise<any>} 异步函数的返回值
 */
export const withErrorHandling = async (asyncFn, options = {}) => {
  try {
    return await asyncFn()
  } catch (error) {
    errorHandler.handle(error, options)
    return null
  }
}

/**
 * 初始化错误处理器
 */
export const initErrorHandlers = () => {
  // 注册认证错误处理器
  errorHandler.registerHandler(ErrorType.AUTHENTICATION, (errorInfo) => {
    // 跳转到登录页
    setTimeout(() => {
      window.location.href = '/login'
    }, 1000)
  })

  // 注册网络错误处理器
  errorHandler.registerHandler(ErrorType.NETWORK, (errorInfo) => {
    // 可以在这里添加网络错误的特殊处理
    console.warn('网络错误:', errorInfo)
  })
}
