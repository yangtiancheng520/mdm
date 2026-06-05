<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import api from '../../api/index'

const userStore = useUserStore()

const stats = ref({
  userCount: 0,
  roleCount: 0,
  permissionCount: 0,
  menuCount: 0
})

async function fetchStats() {
  try {
    const [userRes, roleRes, permissionRes] = await Promise.all([
      api.get('/user/list'),
      api.get('/role/list'),
      api.get('/permission/list')
    ])

    stats.value.userCount = userRes.data.total || 0
    stats.value.roleCount = roleRes.data.total || 0
    stats.value.permissionCount = permissionRes.data.total || 0

    // 计算菜单数量（type为menu的权限）
    const permissions = permissionRes.data.list || []
    stats.value.menuCount = permissions.filter((p: any) => p.type === 'menu').length
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <h2>欢迎回来，{{ userStore.userInfo?.name }}！</h2>
      <p>这是一个基于 Vue 3 + Element Plus + json-server 的后台管理系统</p>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon :size="40" color="#409EFF"><User /></el-icon>
            <div>
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon :size="40" color="#67C23A"><UserFilled /></el-icon>
            <div>
              <div class="stat-value">{{ stats.roleCount }}</div>
              <div class="stat-label">角色总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon :size="40" color="#E6A23C"><Lock /></el-icon>
            <div>
              <div class="stat-value">{{ stats.permissionCount }}</div>
              <div class="stat-label">权限总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon :size="40" color="#F56C6C"><Document /></el-icon>
            <div>
              <div class="stat-value">{{ stats.menuCount }}</div>
              <div class="stat-label">菜单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.home-container {
  height: 100%;
}

.welcome-card h2 {
  margin: 0 0 10px;
  color: #303133;
}

.welcome-card p {
  margin: 0;
  color: #909399;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>
