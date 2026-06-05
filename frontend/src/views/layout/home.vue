<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import api from '../../api/index'

const router = useRouter()
const userStore = useUserStore()

const currentDate = computed(() => {
  const now = new Date()
  const options: Intl.DateTimeFormatOptions = {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }
  return now.toLocaleDateString('zh-CN', options)
})

const currentTime = computed(() => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
})

// 功能模块数据
const modules = ref([
  {
    id: 'masterdata',
    name: '主数据管理',
    icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z',
    color: '#e62934',
    bgColor: 'rgba(230, 41, 52, 0.1)',
    path: '/masterdata/type',
    stats: { label: '数据类型', value: 0 },
    children: [
      { name: '数据类型管理', path: '/masterdata/type' },
      { name: '数据实例', path: '/masterdata/instance' },
      { name: '数据生命周期', path: '/masterdata/lifecycle' },
      { name: '数据关系', path: '/masterdata/relation' }
    ]
  },
  {
    id: 'standard',
    name: '数据标准',
    icon: 'M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zM6 20V4h7v5h5v11H6z',
    color: '#1890ff',
    bgColor: 'rgba(24, 144, 255, 0.1)',
    path: '/standard/field',
    stats: { label: '标准字段', value: 0 },
    children: [
      { name: '字段标准库', path: '/standard/field' },
      { name: '数据标准视图', path: '/standard/view' },
      { name: '编码规则', path: '/standard/encoding' },
      { name: '值域管理', path: '/standard/domain' }
    ]
  },
  {
    id: 'quality',
    name: '数据质量',
    icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
    color: '#52c41a',
    bgColor: 'rgba(82, 196, 26, 0.1)',
    path: '/quality/rule',
    stats: { label: '质量规则', value: 0 },
    children: [
      { name: '质量规则', path: '/quality/rule' },
      { name: '质量问题', path: '/quality/issue' },
      { name: '质量检查', path: '/quality/check' },
      { name: '质量仪表板', path: '/quality/dashboard' }
    ]
  },
  {
    id: 'workflow',
    name: '流程管理',
    icon: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z',
    color: '#fa8c16',
    bgColor: 'rgba(250, 140, 22, 0.1)',
    path: '/workflow/define',
    stats: { label: '待办任务', value: 0 },
    children: [
      { name: '流程定义', path: '/workflow/define' },
      { name: '待办任务', path: '/workflow/todo' },
      { name: '已办任务', path: '/workflow/done' },
      { name: '流程监控', path: '/workflow/monitor' }
    ]
  },
  {
    id: 'distribution',
    name: '数据分发',
    icon: 'M17 20.41L18.41 19 15 15.59 13.59 17 17 20.41zM7.5 8H11v5.59L5.59 19 7 20.41l6-6V8h3.5L12 3.5 7.5 8z',
    color: '#722ed1',
    bgColor: 'rgba(114, 46, 209, 0.1)',
    path: '/distribution/monitor',
    stats: { label: '分发任务', value: 0 },
    children: [
      { name: '分发监控', path: '/distribution/monitor' },
      { name: '数据追踪', path: '/distribution/trace' },
      { name: '主题管理', path: '/distribution/topic' },
      { name: '订阅管理', path: '/distribution/subscription' }
    ]
  },
  {
    id: 'form',
    name: '表单设计',
    icon: 'M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-1 9h-4v4h-2v-4H9V9h4V5h2v4h4v2z',
    color: '#13c2c2',
    bgColor: 'rgba(19, 194, 194, 0.1)',
    path: '/form/manage',
    stats: { label: '表单数量', value: 0 },
    children: [
      { name: '表单管理', path: '/form/manage' },
      { name: '表单设计器', path: '/form/designer' },
      { name: '导入导出模板', path: '/form/template' }
    ]
  },
  {
    id: 'version',
    name: '版本管理',
    icon: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z',
    color: '#eb2f96',
    bgColor: 'rgba(235, 47, 150, 0.1)',
    path: '/version/manage',
    stats: { label: '版本数量', value: 0 },
    children: [
      { name: '版本管理', path: '/version/manage' },
      { name: '版本审计', path: '/version/audit' },
      { name: '版本对比', path: '/version/compare' }
    ]
  },
  {
    id: 'system',
    name: '系统管理',
    icon: 'M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z',
    color: '#595959',
    bgColor: 'rgba(89, 89, 89, 0.1)',
    path: '/user',
    stats: { label: '用户数量', value: 0 },
    children: [
      { name: '用户管理', path: '/user' },
      { name: '角色管理', path: '/role' },
      { name: '菜单管理', path: '/permission' },
      { name: '组织管理', path: '/organization' }
    ]
  }
])

// 快速统计数据
const quickStats = ref([
  { label: '今日待办', value: 5, icon: '📋', color: '#e62934' },
  { label: '质量预警', value: 3, icon: '⚠️', color: '#fa8c16' },
  { label: '数据变更', value: 12, icon: '📊', color: '#1890ff' },
  { label: '分发任务', value: 8, icon: '🔄', color: '#722ed1' }
])

// 最近活动
const recentActivities = ref([
  { time: '10分钟前', action: '用户 admin 更新了客户主数据', type: 'edit' },
  { time: '1小时前', action: '完成了数据质量检查任务', type: 'success' },
  { time: '2小时前', action: '新增了供应商主数据类型', type: 'add' },
  { time: '3小时前', action: '审批通过了数据标准变更申请', type: 'approval' },
  { time: '5小时前', action: '数据分发任务执行完成', type: 'success' }
])

// 获取统计数据
async function fetchStats() {
  try {
    // 这里可以添加实际的API调用
    // const res = await api.get('/stats/home')
  } catch (error) {
    console.error(error)
  }
}

function navigateTo(path: string) {
  router.push(path)
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="home-container">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-header">
      <div class="welcome-content">
        <div class="welcome-left">
          <h1 class="welcome-title">
            <span class="greeting-time">下午好</span>
            <span class="user-name">{{ userStore.userInfo?.name }}</span>
          </h1>
          <p class="welcome-subtitle">MDM 主数据管理平台 · 让数据管理更简单高效</p>
        </div>
        <div class="welcome-right">
          <div class="datetime-card">
            <div class="time">{{ currentTime }}</div>
            <div class="date">{{ currentDate }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速统计卡片 -->
    <div class="quick-stats">
      <div v-for="stat in quickStats" :key="stat.label" class="stat-item">
        <div class="stat-icon">{{ stat.icon }}</div>
        <div class="stat-info">
          <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 功能模块区域 -->
    <div class="modules-section">
      <div class="section-header">
        <h2 class="section-title">功能模块</h2>
        <p class="section-desc">选择模块快速进入相关功能</p>
      </div>
      <div class="modules-grid">
        <div
          v-for="module in modules"
          :key="module.id"
          class="module-card"
          @click="navigateTo(module.path)"
        >
          <div class="module-header">
            <div class="module-icon" :style="{ background: module.bgColor }">
              <svg viewBox="0 0 24 24" width="28" height="28">
                <path :fill="module.color" :d="module.icon" />
              </svg>
            </div>
            <div class="module-info">
              <h3 class="module-name">{{ module.name }}</h3>
              <div class="module-stats">
                <span class="stats-value">{{ module.stats.value }}</span>
                <span class="stats-label">{{ module.stats.label }}</span>
              </div>
            </div>
          </div>
          <div class="module-children">
            <div
              v-for="child in module.children"
              :key="child.path"
              class="child-item"
              @click.stop="navigateTo(child.path)"
            >
              <span class="child-dot"></span>
              <span class="child-name">{{ child.name }}</span>
            </div>
          </div>
          <div class="module-footer">
            <span class="footer-text">点击进入</span>
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path fill="currentColor" d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部信息区域 -->
    <div class="bottom-section">
      <!-- 最近活动 -->
      <div class="activity-section">
        <div class="section-header">
          <h2 class="section-title">最近活动</h2>
        </div>
        <div class="activity-list">
          <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
            <div class="activity-dot" :class="activity.type"></div>
            <div class="activity-content">
              <div class="activity-text">{{ activity.action }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="shortcuts-section">
        <div class="section-header">
          <h2 class="section-title">快捷入口</h2>
        </div>
        <div class="shortcuts-grid">
          <div class="shortcut-item" @click="navigateTo('/workflow/todo')">
            <div class="shortcut-icon">📝</div>
            <div class="shortcut-text">待办任务</div>
          </div>
          <div class="shortcut-item" @click="navigateTo('/quality/dashboard')">
            <div class="shortcut-icon">📊</div>
            <div class="shortcut-text">质量看板</div>
          </div>
          <div class="shortcut-item" @click="navigateTo('/distribution/monitor')">
            <div class="shortcut-icon">🔄</div>
            <div class="shortcut-text">分发监控</div>
          </div>
          <div class="shortcut-item" @click="navigateTo('/version/manage')">
            <div class="shortcut-icon">📦</div>
            <div class="shortcut-text">版本管理</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  padding: 0;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* 欢迎区域 */
.welcome-header {
  background: linear-gradient(135deg, #e62934 0%, #ff6b4a 100%);
  padding: 40px 32px;
  color: white;
  position: relative;
  overflow: hidden;
}

.welcome-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.welcome-header::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: 10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
}

.greeting-time {
  font-weight: 400;
  opacity: 0.95;
}

.user-name {
  margin-left: 12px;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.datetime-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px 32px;
  text-align: right;
}

.time {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 4px;
}

.date {
  font-size: 14px;
  opacity: 0.9;
}

/* 快速统计卡片 */
.quick-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 24px 32px;
  margin-top: -32px;
  position: relative;
  z-index: 2;
}

.stat-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 36px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
}

/* 功能模块区域 */
.modules-section {
  padding: 24px 32px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.module-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.module-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.module-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.module-name {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 4px 0;
}

.module-stats {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stats-value {
  font-size: 20px;
  font-weight: 700;
  color: #e62934;
}

.stats-label {
  font-size: 12px;
  color: #8c8c8c;
}

.module-children {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.child-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.2s;
}

.child-item:hover {
  background: #f5f5f5;
}

.child-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #e62934;
  flex-shrink: 0;
}

.child-name {
  font-size: 13px;
  color: #595959;
}

.module-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  color: #8c8c8c;
  font-size: 13px;
}

/* 底部区域 */
.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  padding: 0 32px 32px;
}

.activity-section,
.shortcuts-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

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
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 5px;
  flex-shrink: 0;
}

.activity-dot.edit {
  background: #1890ff;
}

.activity-dot.success {
  background: #52c41a;
}

.activity-dot.add {
  background: #e62934;
}

.activity-dot.approval {
  background: #fa8c16;
}

.activity-text {
  font-size: 14px;
  color: #262626;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #8c8c8c;
}

.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f5f5f5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.shortcut-item:hover {
  background: #e62934;
  transform: translateY(-4px);
}

.shortcut-item:hover .shortcut-text {
  color: white;
}

.shortcut-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.shortcut-text {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
  transition: color 0.3s;
}

/* 响应式 */
@media (max-width: 1600px) {
  .modules-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1200px) {
  .modules-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .bottom-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }

  .datetime-card {
    text-align: center;
  }

  .modules-grid {
    grid-template-columns: 1fr;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }

  .shortcuts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
