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
  <div class="login-container">
    <div class="login-box">
      <h2 class="title">后台管理系统</h2>
      <el-form :model="form" label-width="0">
        <el-form-item>
          <el-input
            v-model="form.account"
            placeholder="请输入账号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="tips">
        <p>测试账号：admin / 123456</p>
        <p>普通用户：user / 123456</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.tips {
  margin-top: 20px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

.tips p {
  margin: 5px 0;
}
</style>
