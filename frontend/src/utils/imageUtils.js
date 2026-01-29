// 图片加载优化工具

/**
 * 预加载图片
 * @param {string[]} urls - 图片URL数组
 * @returns {Promise<void>}
 */
export const preloadImages = async (urls) => {
  const promises = urls.map(url => {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = resolve
      img.onerror = reject
      img.src = url
    })
  })
  await Promise.all(promises)
}

/**
 * 获取优化的图片URL
 * @param {string} originalUrl - 原始图片URL
 * @returns {string} 优化后的图片URL
 */
export const getOptimizedImageUrl = (originalUrl) => {
  // 这里可以根据需要实现图片优化逻辑
  // 比如返回WebP格式的图片URL
  return originalUrl
}

/**
 * 延迟加载图片
 * @param {HTMLElement} imgElement - 图片元素
 * @param {string} src - 图片源地址
 */
export const lazyLoadImage = (imgElement, src) => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        imgElement.src = src
        observer.unobserve(imgElement)
      }
    })
  })
  observer.observe(imgElement)
  return observer
}
