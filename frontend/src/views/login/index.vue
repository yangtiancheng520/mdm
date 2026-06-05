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
  <div class="tinper-login-container">
    <!-- 左侧品牌区域 -->
    <div class="tinper-login-left">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">M</div>
          <h1 class="logo-text">MDM 管理系统</h1>
        </div>
        <p class="brand-desc">企业级主数据管理平台</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span>统一数据管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span>权限精细控制</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span>多端协同办公</span>
          </div>
        </div>
      </div>
      <div class="brand-footer">
        <p>© 2024 MDM Platform. All rights reserved.</p>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="tinper-login-right">
      <div class="login-form-wrapper">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号和密码</p>
        </div>

        <form class="tinper-login-form" @submit.prevent="handleLogin">
          <div class="tinper-form-item">
            <label class="tinper-form-label">账号</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" width="16" height="16">
                  <path fill="currentColor" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </span>
              <input
                v-model="form.account"
                type="text"
                class="tinper-input"
                placeholder="请输入账号"
                autocomplete="username"
              />
            </div>
          </div>

          <div class="tinper-form-item">
            <label class="tinper-form-label">密码</label>
            <div class="input-wrapper">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" width="16" height="16">
                  <path fill="currentColor" d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
                </svg>
              </span>
              <input
                v-model="form.password"
                type="password"
                class="tinper-input"
                placeholder="请输入密码"
                autocomplete="current-password"
                @keyup.enter="handleLogin"
              />
            </div>
          </div>

          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" />
              <span>记住账号</span>
            </label>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>

          <button
            type="submit"
            class="tinper-button-primary login-button"
            :disabled="loading"
          >
            <span v-if="loading" class="loading-spinner"></span>
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </form>

        <div class="login-tips">
          <p>测试账号：<code>admin</code> / <code>123456</code></p>
          <p>普通用户：<code>user</code> / <code>123456</code></p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tinper-login-container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

/* 左侧品牌区域 */
.tinper-login-left {
  flex: 1;
  background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  position: relative;
}

.brand-content {
  max-width: 480px;
  text-align: center;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 24px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  backdrop-filter: blur(10px);
}

.logo-text {
  font-size: 36px;
  font-weight: 600;
  margin: 0;
}

.brand-desc {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
}

.feature-icon {
  width: 24px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.brand-footer {
  position: absolute;
  bottom: 24px;
  font-size: 12px;
  opacity: 0.7;
}

/* 右侧登录区域 */
.tinper-login-right {
  width: 520px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.login-form-wrapper {
  width: 100%;
  max-width: 360px;
}

.login-header {
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 表单样式 */
.tinper-login-form {
  width: 100%;
}

.tinper-form-item {
  margin-bottom: 24px;
}

.tinper-form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  z-index: 1;
}

.tinper-input {
  width: 100%;
  padding: 12px 12px 12px 40px;
  font-size: 14px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  background: white;
  color: #333;
  transition: all 0.3s;
  outline: none;
}

.tinper-input:hover {
  border-color: #C0C4CC;
}

.tinper-input:focus {
  border-color: #1E88E5;
  box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.2);
}

.tinper-input::placeholder {
  color: #C0C4CC;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 13px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  cursor: pointer;
}

.remember-me input {
  cursor: pointer;
}

.forgot-password {
  color: #1E88E5;
  text-decoration: none;
  transition: color 0.3s;
}

.forgot-password:hover {
  color: #42A5F5;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 4px;
  border: none;
  background: #1E88E5;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-button:hover:not(:disabled) {
  background: #42A5F5;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.4);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 提示信息 */
.login-tips {
  margin-top: 32px;
  padding: 16px;
  background: #F5F7FA;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.login-tips p {
  margin: 4px 0;
}

.login-tips code {
  background: white;
  padding: 2px 6px;
  border-radius: 2px;
  color: #1E88E5;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .tinper-login-left {
    display: none;
  }

  .tinper-login-right {
    width: 100%;
  }
}

@media (max-width: 520px) {
  .tinper-login-right {
    padding: 40px 24px;
  }
}
</style>
