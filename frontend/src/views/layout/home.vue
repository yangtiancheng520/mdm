<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import api from '../../api/index'
import StatCard from '../../components/StatCard.vue'
import Card from '../../components/Card.vue'

const userStore = useUserStore()

const stats = ref({
  userCount: 0,
  roleCount: 0,
  permissionCount: 0,
  menuCount: 0
})

const recentActivities = ref([
  { time: '10分钟前', action: '用户 admin 登录系统', type: 'login' },
  { time: '1小时前', action: '修改了用户 user01 的信息', type: 'edit' },
  { time: '2小时前', action: '新增角色 数据管理员', type: 'add' },
  { time: '3小时前', action: '删除了权限 test:view', type: 'delete' }
])

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
  <div class="tinper-home">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎回来，{{ userStore.userInfo?.name }}！</h1>
          <p>MDM 主数据管理平台，为您提供统一的数据管理服务</p>
        </div>
        <div class="welcome-time">
          <div class="time-value">{{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <StatCard
        :value="stats.userCount"
        label="用户总数"
        icon-path="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
        bg-color="white"
        icon-bg-color="#1E88E5"
        :trend="12"
      />
      <StatCard
        :value="stats.roleCount"
        label="角色总数"
        icon-path="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.98 4-3.08 6-3.08 1.99 0 5.97 1.1 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"
        bg-color="white"
        icon-bg-color="#52C41A"
        :trend="8"
      />
      <StatCard
        :value="stats.permissionCount"
        label="权限总数"
        icon-path="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
        bg-color="white"
        icon-bg-color="#FAAD14"
        :trend="-5"
      />
      <StatCard
        :value="stats.menuCount"
        label="菜单总数"
        icon-path="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"
        bg-color="white"
        icon-bg-color="#F5222D"
        :trend="15"
      />
    </div>

    <!-- 内容区域 -->
    <div class="content-grid">
      <!-- 快捷操作 -->
      <Card title="快捷操作">
        <div class="quick-actions">
          <div class="action-item" @click="$router.push('/user')">
            <div class="action-icon" style="background: rgba(30, 136, 229, 0.1)">
              <svg viewBox="0 0 24 24" width="24" height="24">
                <path fill="#1E88E5" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
            </div>
            <div class="action-text">
              <div class="action-title">用户管理</div>
              <div class="action-desc">管理系统用户</div>
            </div>
          </div>

          <div class="action-item" @click="$router.push('/role')">
            <div class="action-icon" style="background: rgba(82, 196, 26, 0.1)">
              <svg viewBox="0 0 24 24" width="24" height="24">
                <path fill="#52C41A" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/>
              </svg>
            </div>
            <div class="action-text">
              <div class="action-title">角色管理</div>
              <div class="action-desc">配置系统角色</div>
            </div>
          </div>

          <div class="action-item" @click="$router.push('/permission')">
            <div class="action-icon" style="background: rgba(250, 173, 20, 0.1)">
              <svg viewBox="0 0 24 24" width="24" height="24">
                <path fill="#FAAD14" d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2z"/>
              </svg>
            </div>
            <div class="action-text">
              <div class="action-title">权限管理</div>
              <div class="action-desc">分配系统权限</div>
            </div>
          </div>

          <div class="action-item">
            <div class="action-icon" style="background: rgba(245, 34, 45, 0.1)">
              <svg viewBox="0 0 24 24" width="24" height="24">
                <path fill="#F5222D" d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z"/>
              </svg>
            </div>
            <div class="action-text">
              <div class="action-title">系统设置</div>
              <div class="action-desc">配置系统参数</div>
            </div>
          </div>
        </div>
      </Card>

      <!-- 最近活动 -->
      <Card title="最近活动">
        <div class="activity-list">
          <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
            <div class="activity-dot" :class="activity.type"></div>
            <div class="activity-content">
              <div class="activity-text">{{ activity.action }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.tinper-home {
  padding: 0;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 24px;
  color: white;
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.3);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-text p {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.time-value {
  font-size: 14px;
  opacity: 0.9;
  text-align: right;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  background: #F5F7FA;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #E8F4FF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 136, 229, 0.15);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #909399;
}

/* 活动列表 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.activity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.activity-dot.login {
  background: #1E88E5;
}

.activity-dot.edit {
  background: #FAAD14;
}

.activity-dot.add {
  background: #52C41A;
}

.activity-dot.delete {
  background: #F5222D;
}

.activity-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .time-value {
    text-align: center;
  }

  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>
