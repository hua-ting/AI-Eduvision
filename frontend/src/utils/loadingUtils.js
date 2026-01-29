// 加载状态管理工具

/**
 * 加载状态管理器
 */
export class LoadingManager {
  constructor() {
    this.loadingCount = 0
    this.loadingInstances = new Map()
  }

  /**
   * 显示加载状态
   * @param {string} key - 加载状态的唯一标识
   * @param {Object} options - 加载选项
   * @param {string} options.message - 加载提示信息
   * @param {boolean} options.fullscreen - 是否全屏显示
   * @returns {string} 加载实例的key
   */
  show(key, options = {}) {
    const { message = '加载中...', fullscreen = false } = options
    
    // 如果已经存在相同key的加载实例，直接返回
    if (this.loadingInstances.has(key)) {
      return key
    }

    this.loadingCount++
    
    // 创建加载实例（这里使用naive-ui的loadingBar，实际项目中可以根据使用的UI库调整）
    const loadingBar = window.$loadingBar
    if (loadingBar) {
      loadingBar.start()
    }

    this.loadingInstances.set(key, {
      message,
      fullscreen,
      startTime: Date.now()
    })

    return key
  }

  /**
   * 隐藏加载状态
   * @param {string} key - 加载状态的唯一标识
   */
  hide(key) {
    if (this.loadingInstances.has(key)) {
      this.loadingCount--
      this.loadingInstances.delete(key)

      // 如果没有加载实例了，关闭loadingBar
      if (this.loadingCount === 0) {
        const loadingBar = window.$loadingBar
        if (loadingBar) {
          loadingBar.finish()
        }
      }
    }
  }

  /**
   * 隐藏所有加载状态
   */
  hideAll() {
    this.loadingCount = 0
    this.loadingInstances.clear()
    
    const loadingBar = window.$loadingBar
    if (loadingBar) {
      loadingBar.finish()
    }
  }

  /**
   * 检查是否有加载状态
   * @returns {boolean}
   */
  isLoading() {
    return this.loadingCount > 0
  }
}

// 导出单例实例
export const loadingManager = new LoadingManager()

/**
 * 包装异步函数，自动处理加载状态
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 加载选项
 * @returns {Function} 包装后的函数
 */
export const withLoading = (asyncFn, options = {}) => {
  return async (...args) => {
    const key = `loading_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    loadingManager.show(key, options)
    
    try {
      return await asyncFn(...args)
    } finally {
      loadingManager.hide(key)
    }
  }
}
