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
    <!-- 左侧文案区域 -->
    <div class="left-text-box">
      <div class="main-title">MDM 主数据管理平台</div>
      <div class="sub-title">统一数据 · 精细管理 · 高效协同</div>

      <!-- 团队协作图 -->
      <div class="team-collaboration">
        <div class="collaboration-title">高效协同团队</div>
        <div class="team-visual">
          <!-- 中心节点 -->
          <div class="center-node">
            <div class="node-icon">📊</div>
            <div class="node-label">主数据</div>
          </div>

          <!-- 周围团队节点 -->
          <div class="team-node node-1">
            <div class="node-avatar">👥</div>
            <div class="node-label">数据团队</div>
          </div>
          <div class="team-node node-2">
            <div class="node-avatar">💼</div>
            <div class="node-label">业务团队</div>
          </div>
          <div class="team-node node-3">
            <div class="node-avatar">🔧</div>
            <div class="node-label">技术团队</div>
          </div>
          <div class="team-node node-4">
            <div class="node-avatar">📈</div>
            <div class="node-label">分析团队</div>
          </div>
          <div class="team-node node-5">
            <div class="node-avatar">🎯</div>
            <div class="node-label">管理团队</div>
          </div>

          <!-- 连接线 -->
          <svg class="connection-lines" viewBox="0 0 400 400">
            <line x1="200" y1="200" x2="100" y2="100" class="line" />
            <line x1="200" y1="200" x2="300" y2="100" class="line" />
            <line x1="200" y1="200" x2="100" y2="300" class="line" />
            <line x1="200" y1="200" x2="300" y2="300" class="line" />
            <line x1="200" y1="200" x2="50" y2="200" class="line" />
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
            <svg viewBox="0 0 60 60" width="42" height="42" fill="none">
              <circle cx="18" cy="20" r="4" fill="white"/>
              <circle cx="42" cy="20" r="4" fill="white"/>
              <circle cx="30" cy="42" r="5" fill="white"/>
              <path d="M18 20 L30 42 L42 20" stroke="white" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
              <line x1="24" y1="32" x2="36" y2="32" stroke="white" stroke-width="2" stroke-dasharray="3 2"/>
              <circle cx="12" cy="34" r="2.5" fill="white" fill-opacity="0.8"/>
              <circle cx="48" cy="34" r="2.5" fill="white" fill-opacity="0.8"/>
            </svg>
          </div>
          <div class="logo-text">
            <div class="brand-name"><span>MDM</span> 主数据管理平台</div>
          </div>
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
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 50%, #f0f2f5 100%);
  position: relative;
}

/* 左侧文案区域 */
.left-text-box {
  position: absolute;
  top: 50%;
  left: 12%;
  transform: translateY(-50%);
  color: #1e2a3e;
  z-index: 2;
  text-align: center;
}

.main-title {
  font-size: 42px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #1a1a1a;
  margin: 0;
}

.sub-title {
  font-size: 20px;
  margin-top: 12px;
  font-weight: 400;
  color: #666;
  opacity: 0.9;
}

/* 团队协作图 */
.team-collaboration {
  margin-top: 36px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.collaboration-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

.team-visual {
  position: relative;
  width: 260px;
  height: 260px;
  margin: 0 auto;
}

/* 中心节点 */
.center-node {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #e62934, #ff6b4a);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 20px rgba(230, 41, 52, 0.3);
  z-index: 10;
}

.center-node .node-icon {
  font-size: 26px;
  margin-bottom: 2px;
}

.center-node .node-label {
  font-size: 10px;
  color: white;
  font-weight: 600;
}

/* 团队节点 */
.team-node {
  position: absolute;
  width: 58px;
  height: 58px;
  background: white;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
}

.team-node:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.team-node .node-avatar {
  font-size: 20px;
  margin-bottom: 2px;
}

.team-node .node-label {
  font-size: 9px;
  color: #5a6c7d;
  font-weight: 500;
}

/* 节点位置 */
.node-1 { top: 0; left: 50%; transform: translateX(-50%); }
.node-2 { top: 25%; right: 0; }
.node-3 { bottom: 25%; right: 0; }
.node-4 { bottom: 0; left: 50%; transform: translateX(-50%); }
.node-5 { top: 50%; left: 0; transform: translateY(-50%); }

.node-1:hover { transform: translateX(-50%) scale(1.1); }
.node-4:hover { transform: translateX(-50%) scale(1.1); }
.node-5:hover { transform: translateY(-50%) scale(1.1); }

/* 连接线 */
.connection-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.connection-lines .line {
  stroke: rgba(230, 41, 52, 0.3);
  stroke-width: 2;
  stroke-dasharray: 5, 5;
  animation: dash 20s linear infinite;
}

@keyframes dash {
  to {
    stroke-dashoffset: -100;
  }
}

/* 右侧登录卡片 */
.login-card {
  position: absolute;
  right: 8%;
  top: 50%;
  transform: translateY(-50%);
  width: 360px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  z-index: 2;
}

.card-head {
  padding: 20px 24px 12px;
  text-align: center;
  border-bottom: 1px solid #f0f2f5;
}

.logo-text span:nth-child(1) {
  font-size: 28px;
  color: #e62934;
  font-weight: 700;
}


/* 表单区域 */
.form-wrap {
  padding: 20px 24px 28px;
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
  border-radius: 8px;
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
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(230, 41, 52, 0.2);
}

.login-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #c81e2c 0%, #e62934 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(230, 41, 52, 0.3);
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
  .left-text-box {
    left: 6%;
  }

  .main-title {
    font-size: 40px;
  }

  .sub-title {
    font-size: 20px;
  }

  .team-visual {
    width: 260px;
    height: 260px;
  }
}

@media (max-width: 992px) {
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
    max-width: 360px;
    border-radius: 10px;
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
