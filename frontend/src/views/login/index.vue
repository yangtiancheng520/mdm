<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { useUserStore } from '../../store/user'
import { useTabsStore } from '../../store/tabs'

const router = useRouter()
const userStore = useUserStore()
const tabsStore = useTabsStore()

const form = ref({
  account: '',
  password: ''
})

const loading = ref(false)

async function handleLogin() {
  if (!form.value.account || !form.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const res = await login(form.value)
    tabsStore.clearTabs()
    userStore.setToken(res.data.token)
    userStore.setUserInfo({
      id: res.data.user.id,
      account: res.data.user.account,
      name: res.data.user.name,
      avatar: res.data.user.avatar,
      email: '',
      phone: '',
      roles: res.data.user.roles,
      permissions: res.data.user.permissions
    })

    // 获取用户菜单
    await userStore.fetchMenus()

    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="yonbip-login">
    <!-- 右上角LOGO -->
    <div class="top-logo">
      <img src="/智远工创.png" alt="智远工创" />
    </div>

    <!-- 左侧文案区域 -->
    <div class="left-text-box">
      <div class="main-title">
        <span>MDM</span> 主数据管理平台
      </div>
      <div class="sub-title">统一数据 · 精细管理 · 高效协同</div>

      <!-- 数据可视化图形 -->
      <div class="data-visualization">
        <!-- 数据流背景 -->
        <div class="data-flow">
          <!-- 中心数据节点 -->
          <div class="central-hub">
            <div class="hub-ring hub-ring-1"></div>
            <div class="hub-ring hub-ring-2"></div>
            <div class="hub-ring hub-ring-3"></div>
            <div class="hub-core">
              <svg viewBox="0 0 24 24" width="36" height="36">
                <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
              </svg>
            </div>
          </div>

          <!-- 第一层：主要功能节点 -->
          <div class="data-node primary-node node-1">
            <div class="node-pulse"></div>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
            </svg>
            <span class="node-label">主数据</span>
          </div>

          <div class="data-node primary-node node-2">
            <div class="node-pulse"></div>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zM6 20V4h7v5h5v11H6z"/>
            </svg>
            <span class="node-label">元数据</span>
          </div>

          <div class="data-node primary-node node-3">
            <div class="node-pulse"></div>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
            <span class="node-label">质量</span>
          </div>

          <div class="data-node primary-node node-4">
            <div class="node-pulse"></div>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/>
            </svg>
            <span class="node-label">安全</span>
          </div>

          <div class="data-node primary-node node-5">
            <div class="node-pulse"></div>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
            </svg>
            <span class="node-label">标准</span>
          </div>

          <div class="data-node primary-node node-6">
            <div class="node-pulse"></div>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-4v3.5l-2 1.5v2l3.5-1 3.5 1v-2l-2-1.5V12l8 4z"/>
            </svg>
            <span class="node-label">服务</span>
          </div>

          <div class="data-node primary-node node-7">
            <div class="node-pulse"></div>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
            </svg>
            <span class="node-label">分析</span>
          </div>

          <div class="data-node primary-node node-8">
            <div class="node-pulse"></div>
            <svg viewBox="0 0 24 24" width="22" height="22">
              <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
            </svg>
            <span class="node-label">治理</span>
          </div>

          <!-- 第二层：辅助节点 -->
          <div class="data-node secondary-node sec-node-1">
            <div class="node-pulse-small"></div>
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
            </svg>
            <span class="node-label-small">采集</span>
          </div>

          <div class="data-node secondary-node sec-node-2">
            <div class="node-pulse-small"></div>
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor" d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.24-2.92 3.84-5.65 3.84-3.25 0-5.84-2.59-5.84-5.84s2.59-5.84 5.84-5.84c1.61 0 3.07.64 4.14 1.69L13 10h9V1l-4.35 5.35z"/>
            </svg>
            <span class="node-label-small">清洗</span>
          </div>

          <div class="data-node secondary-node sec-node-3">
            <div class="node-pulse-small"></div>
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor" d="M2 20h20v-4H2v4zm2-3h2v2H4v-2zM2 4v4h20V4H2zm4 3H4V5h2v2zm-4 7h20v-4H2v4zm2-3h2v2H4v-2z"/>
            </svg>
            <span class="node-label-small">存储</span>
          </div>

          <div class="data-node secondary-node sec-node-4">
            <div class="node-pulse-small"></div>
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor" d="M17 20.41L18.41 19 15 15.59 13.59 17 17 20.41zM7.5 8H11v5.59L5.59 19 7 20.41l6-6V8h3.5L12 3.5 7.5 8z"/>
            </svg>
            <span class="node-label-small">分发</span>
          </div>

          <div class="data-node secondary-node sec-node-5">
            <div class="node-pulse-small"></div>
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 8c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm6 6H6v-1.5c0-2 4-3.1 6-3.1s6 1.1 6 3.1V17z"/>
            </svg>
            <span class="node-label-small">应用</span>
          </div>

          <div class="data-node secondary-node sec-node-6">
            <div class="node-pulse-small"></div>
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor" d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
            </svg>
            <span class="node-label-small">监控</span>
          </div>

          <div class="data-node secondary-node sec-node-7">
            <div class="node-pulse-small"></div>
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor" d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
            </svg>
            <span class="node-label-small">共享</span>
          </div>

          <div class="data-node secondary-node sec-node-8">
            <div class="node-pulse-small"></div>
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path fill="currentColor" d="M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-1 9h-4v4h-2v-4H9V9h4V5h2v4h4v2z"/>
            </svg>
            <span class="node-label-small">集成</span>
          </div>

          <!-- 连接线 SVG -->
          <svg class="flow-lines" viewBox="0 0 500 500">
            <!-- 中心到第一层节点的连接线 -->
            <path class="flow-line primary-line" d="M250 250 L 250 80" />
            <path class="flow-line primary-line" d="M250 250 L 370 120" />
            <path class="flow-line primary-line" d="M250 250 L 420 250" />
            <path class="flow-line primary-line" d="M250 250 L 370 380" />
            <path class="flow-line primary-line" d="M250 250 L 250 420" />
            <path class="flow-line primary-line" d="M250 250 L 130 380" />
            <path class="flow-line primary-line" d="M250 250 L 80 250" />
            <path class="flow-line primary-line" d="M250 250 L 130 120" />

            <!-- 第一层到第二层的连接线 -->
            <path class="flow-line secondary-line" d="M250 80 L 200 40" />
            <path class="flow-line secondary-line" d="M370 120 L 420 60" />
            <path class="flow-line secondary-line" d="M420 250 L 470 200" />
            <path class="flow-line secondary-line" d="M370 380 L 420 440" />
            <path class="flow-line secondary-line" d="M250 420 L 300 460" />
            <path class="flow-line secondary-line" d="M130 380 L 80 440" />
            <path class="flow-line secondary-line" d="M80 250 L 30 300" />
            <path class="flow-line secondary-line" d="M130 120 L 80 60" />

            <!-- 数据流动粒子 -->
            <circle class="data-particle particle-p1" r="4" fill="#e62934"/>
            <circle class="data-particle particle-p2" r="4" fill="#e62934"/>
            <circle class="data-particle particle-p3" r="4" fill="#e62934"/>
            <circle class="data-particle particle-p4" r="4" fill="#e62934"/>
            <circle class="data-particle particle-p5" r="4" fill="#e62934"/>
            <circle class="data-particle particle-p6" r="4" fill="#e62934"/>
            <circle class="data-particle particle-p7" r="4" fill="#e62934"/>
            <circle class="data-particle particle-p8" r="4" fill="#e62934"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 右侧登录卡片 -->
    <div class="login-card">
      <div class="card-head">
        <div class="logo-brand">
          <div class="logo-icon">
            <!-- MDM 品牌图标 -->
            <svg viewBox="0 0 60 60" width="36" height="36" fill="none">
              <circle cx="18" cy="20" r="4" fill="white"/>
              <circle cx="42" cy="20" r="4" fill="white"/>
              <circle cx="30" cy="42" r="5" fill="white"/>
              <path d="M18 20 L30 42 L42 20" stroke="white" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="24" y1="32" x2="36" y2="32" stroke="white" stroke-width="2" stroke-dasharray="3 2"/>
              <circle cx="12" cy="34" r="2.5" fill="white" fill-opacity="0.8"/>
              <circle cx="48" cy="34" r="2.5" fill="white" fill-opacity="0.8"/>
            </svg>
          </div>
          <div class="brand-name"><span>MDM</span> 主数据管理平台</div>
        </div>
      </div>

      <div class="form-wrap">
        <form @submit.prevent="handleLogin">
          <div class="input-item">
            <div class="input-icon-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                <path fill="currentColor" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
              <input
                v-model="form.account"
                type="text"
                placeholder="请输入账号"
                autocomplete="username"
              />
            </div>
          </div>

          <div class="input-item">
            <div class="input-icon-wrapper">
              <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18">
                <path fill="currentColor" d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
              </svg>
              <input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                autocomplete="current-password"
                @keyup.enter="handleLogin"
              />
            </div>
          </div>

          <div class="row-helper">
            <label>
              <input type="checkbox" checked />
              记住账号
            </label>
            <span class="forget-pwd">忘记密码?</span>
          </div>

          <button type="submit" class="login-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="copyright">
      Copyright © 2024 MDM 主数据管理平台
    </div>
  </div>
</template>

<style scoped>
.yonbip-login {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 50%, #f5f7fa 100%);
  position: relative;
}

/* 右上角LOGO */
.top-logo {
  position: absolute;
  top: 24px;
  right: 32px;
  z-index: 100;
}

.top-logo img {
  height: 28px;
  width: auto;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.1));
  transition: transform 0.3s ease;
}

.top-logo img:hover {
  transform: scale(1.05);
}

/* 左侧文案区域 */
.left-text-box {
  position: absolute;
  top: 50%;
  left: 12%;
  transform: translateY(-50%);
  color: #1a2a3a;
  z-index: 2;
  text-align: center;
}

.main-title {
  font-size: 52px;
  font-weight: 700;
  letter-spacing: 4px;
  color: #1a2a3a;
  margin: 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.main-title span {
  color: #e62934;
  font-weight: 800;
}

.sub-title {
  font-size: 22px;
  margin-top: 16px;
  font-weight: 500;
  color: #5a6a7a;
  letter-spacing: 3px;
  opacity: 0.95;
}

/* 数据可视化图形 */
.data-visualization {
  margin-top: 40px;
  width: 500px;
  height: 500px;
  position: relative;
}

.data-flow {
  position: relative;
  width: 100%;
  height: 100%;
}

/* 中心数据节点 */
.central-hub {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hub-ring {
  position: absolute;
  border-radius: 50%;
  border: 2px solid rgba(230, 41, 52, 0.3);
  animation: pulse-ring 3s ease-out infinite;
}

.hub-ring-1 {
  width: 100%;
  height: 100%;
  animation-delay: 0s;
}

.hub-ring-2 {
  width: 130%;
  height: 130%;
  animation-delay: 1s;
}

.hub-ring-3 {
  width: 160%;
  height: 160%;
  animation-delay: 2s;
}

@keyframes pulse-ring {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }
  100% {
    transform: scale(1.2);
    opacity: 0;
  }
}

.hub-core {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #e62934, #ff6b4a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 6px 24px rgba(230, 41, 52, 0.3);
  z-index: 10;
  animation: hub-breathe 3s ease-in-out infinite;
}

@keyframes hub-breathe {
  0%, 100% {
    box-shadow: 0 8px 30px rgba(230, 41, 52, 0.4);
  }
  50% {
    box-shadow: 0 12px 40px rgba(230, 41, 52, 0.6);
  }
}

/* 数据节点 - 第一层主要节点 */
.data-node {
  position: absolute;
  background: rgba(240, 245, 250, 0.95);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(230, 41, 52, 0.1);
}

.data-node svg {
  color: #e62934;
  filter: drop-shadow(0 2px 4px rgba(230, 41, 52, 0.3));
}

/* 第一层节点样式 */
.primary-node {
  width: 70px;
  height: 70px;
}

.primary-node .node-label {
  font-size: 11px;
  color: #2c3e50;
  margin-top: 2px;
  font-weight: 600;
  white-space: nowrap;
}

/* 第二层节点样式 */
.secondary-node {
  width: 50px;
  height: 50px;
}

.secondary-node .node-label-small {
  font-size: 9px;
  color: #3a4c5d;
  margin-top: 1px;
  font-weight: 600;
  white-space: nowrap;
}

/* 节点悬停效果 */
.data-node:hover {
  transform: scale(1.15);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12), 0 0 30px rgba(230, 41, 52, 0.15);
  background: rgba(255, 255, 255, 1);
  border-color: rgba(230, 41, 52, 0.2);
}

.data-node:hover svg {
  color: #c81e2c;
}

/* 第一层节点位置 - 围绕中心均匀分布 */
.primary-node.node-1 { top: 50px; left: 50%; transform: translateX(-50%); }
.primary-node.node-2 { top: 90px; right: 80px; }
.primary-node.node-3 { top: 50%; right: 40px; transform: translateY(-50%); }
.primary-node.node-4 { bottom: 90px; right: 80px; }
.primary-node.node-5 { bottom: 50px; left: 50%; transform: translateX(-50%); }
.primary-node.node-6 { bottom: 90px; left: 80px; }
.primary-node.node-7 { top: 50%; left: 40px; transform: translateY(-50%); }
.primary-node.node-8 { top: 90px; left: 80px; }

/* 第一层悬停保持居中 */
.primary-node.node-1:hover { transform: translateX(-50%) scale(1.15); }
.primary-node.node-3:hover { transform: translateY(-50%) scale(1.15); }
.primary-node.node-5:hover { transform: translateX(-50%) scale(1.15); }
.primary-node.node-7:hover { transform: translateY(-50%) scale(1.15); }

/* 第二层节点位置 - 外围分布 */
.secondary-node.sec-node-1 { top: 10px; left: 150px; }
.secondary-node.sec-node-2 { top: 30px; right: 50px; }
.secondary-node.sec-node-3 { top: 130px; right: 10px; }
.secondary-node.sec-node-4 { bottom: 130px; right: 10px; }
.secondary-node.sec-node-5 { bottom: 30px; right: 50px; }
.secondary-node.sec-node-6 { bottom: 10px; left: 250px; }
.secondary-node.sec-node-7 { bottom: 130px; left: 10px; }
.secondary-node.sec-node-8 { top: 130px; left: 10px; }

/* 节点脉冲动画 */
.node-pulse {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(230, 41, 52, 0.1);
  animation: node-pulse 2s ease-out infinite;
}

.node-pulse-small {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(230, 41, 52, 0.08);
  animation: node-pulse 2.5s ease-out infinite;
}

@keyframes node-pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.6);
    opacity: 0;
  }
}

/* 连接线 SVG */
.flow-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.flow-line {
  fill: none;
  stroke-linecap: round;
}

.primary-line {
  stroke: rgba(230, 41, 52, 0.5);
  stroke-width: 2.5;
  stroke-dasharray: 8, 8;
  animation: flow-dash 2s linear infinite;
  filter: drop-shadow(0 0 4px rgba(230, 41, 52, 0.3));
}

.secondary-line {
  stroke: rgba(230, 41, 52, 0.3);
  stroke-width: 1.5;
  stroke-dasharray: 6, 6;
  animation: flow-dash 3s linear infinite;
}

@keyframes flow-dash {
  to {
    stroke-dashoffset: -28;
  }
}

/* 数据流动粒子 */
.data-particle {
  animation: flow-particle 3s ease-in-out infinite;
}

.particle-p1 {
  animation-delay: 0s;
  offset-path: path('M250 250 L 250 80');
  offset-distance: 0%;
}

.particle-p2 {
  animation-delay: 0.375s;
  offset-path: path('M250 250 L 370 120');
  offset-distance: 0%;
}

.particle-p3 {
  animation-delay: 0.75s;
  offset-path: path('M250 250 L 420 250');
  offset-distance: 0%;
}

.particle-p4 {
  animation-delay: 1.125s;
  offset-path: path('M250 250 L 370 380');
  offset-distance: 0%;
}

.particle-p5 {
  animation-delay: 1.5s;
  offset-path: path('M250 250 L 250 420');
  offset-distance: 0%;
}

.particle-p6 {
  animation-delay: 1.875s;
  offset-path: path('M250 250 L 130 380');
  offset-distance: 0%;
}

.particle-p7 {
  animation-delay: 2.25s;
  offset-path: path('M250 250 L 80 250');
  offset-distance: 0%;
}

.particle-p8 {
  animation-delay: 2.625s;
  offset-path: path('M250 250 L 130 120');
  offset-distance: 0%;
}

@keyframes flow-particle {
  0% {
    offset-distance: 0%;
    opacity: 1;
    r: 4;
  }
  100% {
    offset-distance: 100%;
    opacity: 0.2;
    r: 2;
  }
}

/* 右侧登录卡片 */
.login-card {
  position: absolute;
  right: 8%;
  top: 50%;
  transform: translateY(-50%);
  width: 440px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  z-index: 2;
}

.card-head {
  padding: 20px 40px 20px;
  text-align: center;
  border-bottom: 1px solid #f0f2f5;
}

.logo-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #e31b23, #b01018);
  border-radius: 0;
  box-shadow: 0 4px 12px rgba(227, 27, 35, 0.25);
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.brand-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.brand-name span {
  font-size: 26px;
  color: #e62934;
  font-weight: 700;
  margin-right: 4px;
}


/* 表单区域 */
.form-wrap {
  padding: 32px 40px 40px;
  background: #ffffff;
}

.input-item {
  margin-bottom: 20px;
}

.input-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: #a0a8b3;
  pointer-events: none;
}

.input-item input {
  width: 100%;
  height: 44px;
  border: 1px solid #dce5ef;
  border-radius: 0;
  padding: 0 14px 0 42px;
  font-size: 14px;
  background: #fafbfc;
  transition: all 0.2s;
  outline: none;
  color: #1e2a3e;
}

.input-item input:focus {
  border-color: #e62934;
  background: white;
  box-shadow: 0 0 0 3px rgba(230, 41, 52, 0.08);
}

.input-item input::placeholder {
  color: #a0a8b3;
}

.row-helper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 26px;
  font-size: 13px;
}

.row-helper label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #5a6c7d;
  cursor: pointer;
}

.row-helper input[type="checkbox"] {
  width: 15px;
  height: 15px;
  cursor: pointer;
  accent-color: #e62934;
}

.forget-pwd {
  color: #e62934;
  cursor: pointer;
  transition: color 0.2s;
}

.forget-pwd:hover {
  color: #c81e2c;
}

.login-btn {
  width: 100%;
  height: 46px;
  background: linear-gradient(135deg, #e62934 0%, #ff4d5a 100%);
  border: none;
  color: white;
  font-size: 16px;
  font-weight: 600;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(230, 41, 52, 0.25);
}

.login-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #c81e2c 0%, #e62934 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(230, 41, 52, 0.35);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 底部版权 */
.copyright {
  position: absolute;
  bottom: 20px;
  width: 100%;
  text-align: center;
  color: #8a9bb0;
  font-size: 12px;
  z-index: 2;
}

/* 响应式 */
@media (max-width: 1200px) {
  .top-logo img {
    height: 32px;
  }

  .left-text-box {
    left: 6%;
  }

  .main-title {
    font-size: 40px;
  }

  .sub-title {
    font-size: 20px;
  }

  .data-visualization {
    width: 400px;
    height: 400px;
    margin-top: 30px;
  }

  .primary-node {
    width: 60px;
    height: 60px;
  }

  .secondary-node {
    width: 45px;
    height: 45px;
  }

  .primary-node .node-label {
    font-size: 10px;
  }

  .secondary-node .node-label-small {
    font-size: 8px;
  }
}

@media (max-width: 992px) {
  .top-logo {
    top: 16px;
    right: 20px;
  }

  .top-logo img {
    height: 28px;
  }

  .left-text-box {
    display: none;
  }

  .login-card {
    position: relative;
    right: auto;
    top: auto;
    transform: none;
    margin: auto;
    margin-top: 15vh;
  }
}

@media (max-width: 576px) {
  .login-card {
    width: 90%;
    max-width: 440px;
    border-radius: 24px;
  }

  .card-head {
    padding: 20px 30px 16px;
  }

  .form-wrap {
    padding: 24px 30px 32px;
  }

  .card-head {
    padding: 20px 20px 16px;
  }

  .form-wrap {
    padding: 20px 20px 24px;
  }

  .login-btn {
    height: 44px;
    font-size: 15px;
  }
}
</style>
