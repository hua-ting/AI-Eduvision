<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-notification-provider>
        <n-dialog-provider>
          <n-loading-bar-provider>
            <router-view />
          </n-loading-bar-provider>
        </n-dialog-provider>
      </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { NConfigProvider, NMessageProvider, NNotificationProvider, NDialogProvider, NLoadingBarProvider } from 'naive-ui'

// 主题配置(EduSunny 青春活力)
const themeOverrides = {
  common: {
    primaryColor: '#4F46E5',
    primaryColorHover: '#6366F1',
    primaryColorPressed: '#4338CA',
    successColor: '#10B981',
    successColorHover: '#34D399',
    warningColor: '#F97316',
    errorColor: '#EF4444',
    infoColor: '#818CF8',
    textColorBase: '#1E1B4B',
    borderColor: '#C7D2FE',
    cardColor: '#ffffff',
    borderRadius: '16px'
  }
}
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500;600;700&family=Nunito:wght@300;400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Nunito', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #F8FAFC;
  color: #1E1B4B;
  min-height: 100vh;
}

#app {
  min-height: 100vh;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
