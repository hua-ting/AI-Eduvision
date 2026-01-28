<template>
  <div class="home-container" @scroll="handleScroll">
    <!-- 导航栏 -->
    <nav class="navbar" :class="{ 'is-scrolled': isScrolled }">
      <div class="navbar-content">
        <div class="logo-section">
          <div class="logo-icon">AI</div>
          <span class="logo-text">EduVision</span>
        </div>
        <button class="cta-button" @click="goToLogin">
          <span>开启学习之旅</span>
          <svg class="arrow-icon" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>
      <!-- 滚动进度条 -->
      <div class="scroll-progress" :style="{ width: scrollProgress * 100 + '%' }"></div>
    </nav>

    <!-- Hero 主屏 -->
    <section class="hero-section" :style="{ transform: `translateY(${parallaxOffset * 0.5}px)` }">
      <div class="hero-background"></div>
      <div class="hero-content">
        <div class="hero-badge" data-aos="fade-down">
          <span class="badge-dot"></span>
          <span>基于 Transformer 大模型</span>
        </div>
        <h1 class="hero-title" data-aos="fade-up" data-aos-delay="100">
          重新定义
          <span class="highlight-text">智能学习</span>
        </h1>
        <p class="hero-subtitle" data-aos="fade-up" data-aos-delay="200">
          AI 驱动的个性化推荐系统，让知识获取更高效、更精准
        </p>
        <div class="hero-actions" data-aos="fade-up" data-aos-delay="300">
          <button class="primary-button" @click="goToLogin">
            立即体验
            <svg class="button-arrow" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
          <button class="secondary-button" @click="scrollToFeatures">
            了解更多
          </button>
        </div>
        <!-- 统计数据 -->
        <div class="hero-stats" data-aos="fade-up" data-aos-delay="400">
          <div class="stat-item">
            <div class="stat-number">{{ animatedUsers }}+</div>
            <div class="stat-label">活跃用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ animatedKnowledge }}+</div>
            <div class="stat-label">知识点</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ animatedMaterials }}+</div>
            <div class="stat-label">学习资料</div>
          </div>
        </div>
      </div>
      <!-- 浮动元素 -->
      <div class="floating-elements">
        <div class="float-card card-1"></div>
        <div class="float-card card-2"></div>
        <div class="float-card card-3"></div>
      </div>
    </section>

    <!-- Sticky Scroll 演示区 -->
    <section class="transformation-section" ref="transformSection">
      <div class="section-header" data-aos="fade-up">
        <span class="section-tag">核心技术</span>
        <h2 class="section-title">从资料到知识点的智能解构</h2>
        <p class="section-desc">基于 Transformer 模型的深度语义理解</p>
      </div>
      <div class="transformation-visual" :class="{ 'is-active': transformProgress > 0.2 }">
        <div class="material-preview" :style="{ opacity: 1 - transformProgress, scale: 1 - transformProgress * 0.3 }">
          <div class="preview-header">
            <div class="preview-icon">📄</div>
            <span>原始学习资料</span>
          </div>
          <div class="preview-content">
            <div class="content-line" v-for="i in 5" :key="i"></div>
          </div>
        </div>
        <div class="arrow-flow" :class="{ 'is-animated': transformProgress > 0.4 }">
          <svg class="flow-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M5 12h14M12 5l7 7-7 7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span class="flow-label">AI 解析</span>
        </div>
        <div class="knowledge-result" :style="{ opacity: transformProgress, scale: 0.7 + transformProgress * 0.3 }">
          <div class="result-grid">
            <div class="result-card" v-for="i in 6" :key="i" :style="{ transitionDelay: `${i * 0.1}s` }">
              <div class="card-icon">💡</div>
              <div class="card-title">知识点 {{ i }}</div>
              <div class="card-tag">{{ ['初级', '中级', '高级'][i % 3] }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Bento Grid 功能展示 -->
    <section class="features-section">
      <div class="section-header" data-aos="fade-up">
        <span class="section-tag">产品功能</span>
        <h2 class="section-title">全方位智能学习体验</h2>
      </div>
      <div class="bento-grid">
        <div class="bento-item large" data-aos="fade-up" data-aos-delay="100" @click="goToLogin">
          <div class="bento-background bg-1"></div>
          <div class="bento-content">
            <div class="bento-icon">🤖</div>
            <h3 class="bento-title">AI 智能问答</h3>
            <p class="bento-desc">基于大模型的知识检索与推理，实时解答你的学习疑问</p>
            <div class="bento-tag">热门</div>
          </div>
        </div>
        <div class="bento-item medium" data-aos="fade-up" data-aos-delay="200" @click="goToLogin">
          <div class="bento-background bg-2"></div>
          <div class="bento-content">
            <div class="bento-icon">📊</div>
            <h3 class="bento-title">学习看板</h3>
            <p class="bento-desc">可视化进度跟踪</p>
          </div>
        </div>
        <div class="bento-item medium" data-aos="fade-up" data-aos-delay="300" @click="goToLogin">
          <div class="bento-background bg-3"></div>
          <div class="bento-content">
            <div class="bento-icon">🎯</div>
            <h3 class="bento-title">个性推荐</h3>
            <p class="bento-desc">千人千面学习路径</p>
          </div>
        </div>
        <div class="bento-item small" data-aos="fade-up" data-aos-delay="400" @click="goToLogin">
          <div class="bento-background bg-4"></div>
          <div class="bento-content">
            <div class="bento-icon">🔖</div>
            <h3 class="bento-title">收藏管理</h3>
          </div>
        </div>
        <div class="bento-item small" data-aos="fade-up" data-aos-delay="500" @click="goToLogin">
          <div class="bento-background bg-5"></div>
          <div class="bento-content">
            <div class="bento-icon">📈</div>
            <h3 class="bento-title">成长画像</h3>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA 底部 -->
    <section class="cta-section">
      <div class="cta-background"></div>
      <div class="cta-content" data-aos="zoom-in">
        <h2 class="cta-title">准备好开始了吗？</h2>
        <p class="cta-desc">立即注册，体验 AI 驱动的个性化学习</p>
        <button class="cta-button-large" @click="goToLogin">
          <span>免费开始</span>
          <svg class="cta-arrow" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10.293 5.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L12.586 11H5a1 1 0 110-2h7.586l-2.293-2.293a1 1 0 010-1.414z" clip-rule="evenodd" />
          </svg>
        </button>
        <p class="cta-note">无需信用卡 · 随时取消</p>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <p>© 2025 EduVision - 智能学习推荐平台</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AOS from 'aos'
import 'aos/dist/aos.css'

const router = useRouter()

const isScrolled = ref(false)
const scrollProgress = ref(0)
const parallaxOffset = ref(0)
const transformProgress = ref(0)
const transformSection = ref(null)

// 动画数字
const animatedUsers = ref(0)
const animatedKnowledge = ref(0)
const animatedMaterials = ref(0)

const targetUsers = 1000
const targetKnowledge = 5000
const targetMaterials = 2000

// 数字动画
const animateNumber = (start, end, duration, callback) => {
  let startTime = null
  const step = (timestamp) => {
    if (!startTime) startTime = timestamp
    const progress = Math.min((timestamp - startTime) / duration, 1)
    callback(Math.floor(progress * (end - start) + start))
    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }
  requestAnimationFrame(step)
}

// 滚动监听
const handleScroll = () => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  
  // 导航栏状态
  isScrolled.value = scrollTop > 50
  
  // 滚动进度
  scrollProgress.value = scrollTop / docHeight
  
  // 视差效果
  parallaxOffset.value = scrollTop * 0.5
  
  // Transform 区域动画
  if (transformSection.value) {
    const rect = transformSection.value.getBoundingClientRect()
    const windowHeight = window.innerHeight
    // 只有当区域进入视口50%以上才开始动画
    if (rect.top < windowHeight * 0.5 && rect.bottom > windowHeight * 0.5) {
      const progress = (windowHeight * 0.5 - rect.top) / (windowHeight * 0.5)
      transformProgress.value = Math.min(1, Math.max(0, progress))
    } else if (rect.top >= windowHeight * 0.5) {
      transformProgress.value = 0
    }
  }
}

const scrollToFeatures = () => {
  const section = document.querySelector('.features-section')
  if (section) {
    section.scrollIntoView({ behavior: 'smooth' })
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  // 初始化 AOS 动画库
  AOS.init({
    duration: 800,
    easing: 'ease-out-cubic',
    once: true,
    offset: 100
  })
  
  window.addEventListener('scroll', handleScroll)
  
  // 启动数字动画
  setTimeout(() => {
    animateNumber(0, targetUsers, 2000, (val) => animatedUsers.value = val)
    animateNumber(0, targetKnowledge, 2000, (val) => animatedKnowledge.value = val)
    animateNumber(0, targetMaterials, 2000, (val) => animatedMaterials.value = val)
  }, 500)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&family=Work+Sans:wght@300;400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.home-container {
  min-height: 100vh;
  background: #F0F9FF;
  font-family: 'Work Sans', sans-serif;
  overflow-x: hidden;
}

/* 导航栏 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.navbar.is-scrolled {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.08);
}

.scroll-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, #0EA5E9, #F97316);
  transition: width 0.1s ease;
}

.navbar-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #0EA5E9, #38BDF8);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  font-size: 20px;
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.3);
  transition: transform 0.3s ease;
}

.logo-icon:hover {
  transform: scale(1.05) rotate(-5deg);
}

.logo-text {
  font-family: 'Outfit', sans-serif;
  font-size: 26px;
  font-weight: 700;
  color: #0C4A6E;
  letter-spacing: -0.5px;
}

.cta-button {
  padding: 14px 32px;
  background: linear-gradient(135deg, #F97316, #FB923C);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
  display: flex;
  align-items: center;
  gap: 8px;
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(249, 115, 22, 0.4);
}

.arrow-icon {
  width: 18px;
  height: 18px;
  transition: transform 0.3s ease;
}

.cta-button:hover .arrow-icon {
  transform: translateX(4px);
}

/* Hero 区域 */
.hero-section {
  position: relative;
  padding: 180px 48px 120px;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1920&q=80');
  background-size: cover;
  background-position: center;
  opacity: 0.5;
  z-index: 0;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 900px;
  text-align: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid rgba(14, 165, 233, 0.3);
  border-radius: 50px;
  font-size: 14px;
  font-weight: 600;
  color: #0EA5E9;
  margin-bottom: 32px;
  backdrop-filter: blur(10px);
}

.badge-dot {
  width: 8px;
  height: 8px;
  background: #0EA5E9;
  border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.hero-title {
  font-family: 'Outfit', sans-serif;
  font-size: 72px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 24px;
  line-height: 1.1;
  letter-spacing: -2px;
}

.highlight-text {
  background: linear-gradient(135deg, #0EA5E9, #F97316);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 22px;
  color: #475569;
  margin-bottom: 48px;
  line-height: 1.6;
  font-weight: 400;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 60px;
}

.primary-button {
  padding: 18px 40px;
  background: linear-gradient(135deg, #0EA5E9, #38BDF8);
  color: #fff;
  border: none;
  border-radius: 14px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.3);
  display: flex;
  align-items: center;
  gap: 10px;
}

.primary-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.4);
}

.button-arrow {
  width: 20px;
  height: 20px;
  transition: transform 0.3s ease;
}

.primary-button:hover .button-arrow {
  transform: translateX(6px);
}

.secondary-button {
  padding: 18px 40px;
  background: transparent;
  color: #0C4A6E;
  border: 2px solid #0EA5E9;
  border-radius: 14px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.secondary-button:hover {
  background: rgba(14, 165, 233, 0.1);
  transform: translateY(-2px);
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 80px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-family: 'Outfit', sans-serif;
  font-size: 48px;
  font-weight: 700;
  color: #0EA5E9;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 15px;
  color: #64748B;
  font-weight: 500;
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.float-card {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 24px;
  background: rgba(14, 165, 233, 0.15);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(14, 165, 233, 0.2);
}

.card-1 {
  top: 15%;
  left: 10%;
  animation: float-1 6s ease-in-out infinite;
}

.card-2 {
  top: 60%;
  right: 15%;
  animation: float-2 7s ease-in-out infinite;
}

.card-3 {
  bottom: 20%;
  left: 20%;
  animation: float-3 8s ease-in-out infinite;
}

@keyframes float-1 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(20px, -20px) rotate(5deg); }
}

@keyframes float-2 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(-15px, 15px) rotate(-5deg); }
}

@keyframes float-3 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(15px, -25px) rotate(3deg); }
}

/* Transform 区域 */
.transformation-section {
  padding: 120px 48px;
  background: #FFFFFF;
}

.section-header {
  text-align: center;
  max-width: 800px;
  margin: 0 auto 80px;
}

.section-tag {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(14, 165, 233, 0.1);
  border-radius: 50px;
  font-size: 13px;
  font-weight: 700;
  color: #0EA5E9;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 20px;
}

.section-title {
  font-family: 'Outfit', sans-serif;
  font-size: 48px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 16px;
  line-height: 1.2;
  letter-spacing: -1px;
}

.section-desc {
  font-size: 18px;
  color: #64748B;
  line-height: 1.6;
}

.transformation-visual {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 60px;
  min-height: 400px;
  padding: 40px;
}

.material-preview {
  flex: 1;
  background: #FFFFFF;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 12px 40px rgba(14, 165, 233, 0.15);
  border: 2px solid #E0F2FE;
  transition: all 1.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-weight: 600;
  color: #0C4A6E;
  font-size: 16px;
}

.preview-icon {
  font-size: 32px;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-line {
  height: 12px;
  background: linear-gradient(90deg, #E0F2FE, #F0F9FF);
  border-radius: 6px;
}

.content-line:nth-child(2) {
  width: 85%;
}

.content-line:nth-child(3) {
  width: 92%;
}

.content-line:nth-child(4) {
  width: 78%;
}

.content-line:nth-child(5) {
  width: 88%;
}

.arrow-flow {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  transition: all 1s ease;
}

.arrow-flow.is-animated {
  animation: pulse-flow 1.5s ease-in-out infinite;
}

@keyframes pulse-flow {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.flow-arrow {
  width: 48px;
  height: 48px;
  color: #0EA5E9;
}

.flow-label {
  font-size: 14px;
  font-weight: 600;
  color: #0EA5E9;
  background: rgba(14, 165, 233, 0.1);
  padding: 6px 16px;
  border-radius: 20px;
}

.knowledge-result {
  flex: 1;
  transition: all 1.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.result-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.12);
  border: 2px solid #E0F2FE;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.result-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.2);
  border-color: #0EA5E9;
}

.card-icon {
  font-size: 28px;
  margin-bottom: 12px;
}

.card-title {
  font-weight: 600;
  color: #0C4A6E;
  font-size: 15px;
  margin-bottom: 8px;
}

.card-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(14, 165, 233, 0.1);
  border-radius: 12px;
  font-size: 12px;
  color: #0EA5E9;
  font-weight: 600;
}

/* 功能展示区 */
.features-section {
  padding: 120px 48px;
  background: #F0F9FF;
}

.bento-grid {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.bento-item {
  position: relative;
  border-radius: 24px;
  padding: 40px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.bento-item:hover {
  transform: translateY(-8px);
  border-color: #0EA5E9;
  box-shadow: 0 20px 48px rgba(14, 165, 233, 0.2);
}

.bento-item.large {
  grid-column: span 2;
  grid-row: span 2;
}

.bento-item.medium {
  grid-column: span 2;
  grid-row: span 1;
}

.bento-item.small {
  grid-column: span 1;
  grid-row: span 1;
}

.bento-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  opacity: 0.6;
  transition: all 0.4s ease;
  z-index: 0;
}

.bento-item:hover .bento-background {
  opacity: 0.8;
  transform: scale(1.05);
}

.bg-1 {
  background-image: url('https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800&q=80');
}

.bg-2 {
  background-image: url('https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&q=80');
}

.bg-3 {
  background-image: url('https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&q=80');
}

.bg-4 {
  background-image: url('https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800&q=80');
}

.bg-5 {
  background-image: url('https://images.unsplash.com/photo-1543286386-713bdd548da4?w=800&q=80');
}

.bento-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.bento-icon {
  font-size: 56px;
  margin-bottom: 20px;
}

.bento-title {
  font-family: 'Outfit', sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 12px;
}

.bento-desc {
  font-size: 16px;
  color: #64748B;
  line-height: 1.6;
  flex: 1;
}

.bento-tag {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 6px 16px;
  background: linear-gradient(135deg, #F97316, #FB923C);
  color: #fff;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* CTA 区域 */
.cta-section {
  position: relative;
  padding: 140px 48px;
  overflow: hidden;
  background: #FFFFFF;
}

.cta-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=1920&q=80');
  background-size: cover;
  background-position: center;
  opacity: 0.5;
  z-index: 0;
}

.cta-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin: 0 auto;
  text-align: center;
}

.cta-title {
  font-family: 'Outfit', sans-serif;
  font-size: 56px;
  font-weight: 700;
  color: #0C4A6E;
  margin-bottom: 20px;
  line-height: 1.2;
  letter-spacing: -1px;
}

.cta-desc {
  font-size: 20px;
  color: #64748B;
  margin-bottom: 48px;
  line-height: 1.6;
}

.cta-button-large {
  padding: 22px 56px;
  background: linear-gradient(135deg, #F97316, #FB923C);
  color: #fff;
  border: none;
  border-radius: 16px;
  font-weight: 700;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 12px 32px rgba(249, 115, 22, 0.4);
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.cta-button-large:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 16px 40px rgba(249, 115, 22, 0.5);
}

.cta-arrow {
  width: 22px;
  height: 22px;
  transition: transform 0.3s ease;
}

.cta-button-large:hover .cta-arrow {
  transform: translateX(6px);
}

.cta-note {
  margin-top: 24px;
  font-size: 14px;
  color: #94A3B8;
  font-weight: 500;
}

/* Footer */
.footer {
  padding: 48px;
  text-align: center;
  background: #FFFFFF;
  border-top: 1px solid #E0F2FE;
}

.footer p {
  font-size: 14px;
  color: #94A3B8;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 1024px) {
  .hero-title {
    font-size: 56px;
  }
  
  .bento-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .bento-item.large,
  .bento-item.medium {
    grid-column: span 2;
  }
  
  .transformation-visual {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 42px;
  }
  
  .hero-stats {
    flex-direction: column;
    gap: 32px;
  }
  
  .bento-grid {
    grid-template-columns: 1fr;
  }
  
  .bento-item.large,
  .bento-item.medium,
  .bento-item.small {
    grid-column: span 1;
    grid-row: span 1;
  }
  
  .section-title {
    font-size: 36px;
  }
  
  .cta-title {
    font-size: 42px;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
