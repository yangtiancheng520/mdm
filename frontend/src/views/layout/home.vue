<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { useTabsStore } from '../../store/tabs'
import {
  getDashboardOverview,
  getRealtimeActivities,
  type DashboardOverview,
  type RealtimeActivity
} from '../../api/dashboard'

const router = useRouter()
const userStore = useUserStore()
const tabsStore = useTabsStore()

// 当前时间
const currentTime = ref(new Date())
let timeInterval: number | null = null

// 格式化时间
const formattedTime = computed(() => {
  const now = currentTime.value
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
})

const formattedDate = computed(() => {
  const now = currentTime.value
  const options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }
  return now.toLocaleDateString('zh-CN', options)
})

const greeting = computed(() => {
  const hour = currentTime.value.getHours()
  if (hour >= 6 && hour < 9) return '早上好'
  if (hour >= 9 && hour < 12) return '上午好'
  if (hour >= 12 && hour < 14) return '中午好'
  if (hour >= 14 && hour < 18) return '下午好'
  if (hour >= 18 && hour < 22) return '晚上好'
  return '夜深了'
})

// 监控数据
const overview = ref<DashboardOverview | null>(null)
const activities = ref<RealtimeActivity[]>([])
const loading = ref(true)

// 用户待办任务
const pendingTasks = ref([
  { id: 1, title: '客户主数据变更审批', type: '审批', priority: 'high', time: '10分钟前', from: '张三' },
  { id: 2, title: '供应商数据质量整改', type: '整改', priority: 'medium', time: '1小时前', from: '系统' },
  { id: 3, title: '物料标准字段审核', type: '审核', priority: 'high', time: '2小时前', from: '李四' },
  { id: 4, title: '数据分发异常处理', type: '异常', priority: 'critical', time: '3小时前', from: '系统' },
  { id: 5, title: '员工主数据导入确认', type: '确认', priority: 'low', time: '今天', from: '王五' },
])

// 快速入口
const quickEntries = ref([
  { name: '字段标准库', path: '/standard/field', icon: 'book', color: '#e62934' },
  { name: '数据模型', path: '/standard/view', icon: 'grid', color: '#ff6b35' },
  { name: '表单设计', path: '/form/manage', icon: 'edit', color: '#ffd700' },
  { name: '数据维护', path: '/data/maintain', icon: 'database', color: '#1890ff' },
  { name: '质量检测', path: '/quality/check', icon: 'check-circle', color: '#52c41a' },
  { name: '分发监控', path: '/distribution/monitor', icon: 'share-alt', color: '#722ed1' },
])

// 模块监控数据
const modules = computed(() => {
  if (!overview.value) return []
  return [
    {
      id: 'dictionary',
      name: '数据字典',
      icon: 'book',
      color: '#e62934',
      stats: [
        { label: '字段标准', value: overview.value.fieldStandardTotal, sub: `${overview.value.fieldStandardActive} 启用` },
        { label: '值域管理', value: overview.value.domainTotal },
        { label: '字段分类', value: overview.value.categoryTotal },
      ],
      path: '/standard/field'
    },
    {
      id: 'model',
      name: '数据模型',
      icon: 'grid',
      color: '#ff6b35',
      stats: [
        { label: '视图模型', value: overview.value.viewModelTotal, sub: `${overview.value.viewModelPublished} 已发布` },
        { label: '数据实体', value: overview.value.entityTotal },
        { label: '模型字段', value: overview.value.fieldTotal },
      ],
      path: '/standard/view'
    },
    {
      id: 'form',
      name: '表单设计',
      icon: 'edit',
      color: '#ffd700',
      stats: [
        { label: '表单总数', value: overview.value.formTotal, sub: `${overview.value.formPublished} 已发布` },
        { label: '草稿表单', value: overview.value.formDraft },
      ],
      path: '/form/manage'
    },
    {
      id: 'data',
      name: '数据维护',
      icon: 'database',
      color: '#1890ff',
      stats: [
        { label: '数据总量', value: overview.value.dataInstanceTotal },
        { label: '今日新增', value: overview.value.dataTodayNew },
        { label: '今日更新', value: overview.value.dataTodayUpdate },
      ],
      path: '/data/maintain'
    },
    {
      id: 'quality',
      name: '质量管理',
      icon: 'check-circle',
      color: '#52c41a',
      stats: [
        { label: '质量规则', value: overview.value.qualityRuleTotal, sub: `${overview.value.qualityRuleActive} 启用` },
        { label: '通过率', value: `${overview.value.qualityPassRate}%` },
        { label: '待处理问题', value: overview.value.qualityIssuePending },
      ],
      path: '/quality/rule'
    },
    {
      id: 'distribution',
      name: '数据分发',
      icon: 'share-alt',
      color: '#722ed1',
      stats: [
        { label: '分发配置', value: overview.value.distributionConfigTotal },
        { label: '成功率', value: `${overview.value.distributionSuccessRate}%` },
        { label: '今日分发', value: overview.value.distributionTaskToday },
      ],
      path: '/distribution/monitor'
    },
  ]
})

// 核心指标
const coreMetrics = computed(() => {
  if (!overview.value) return []
  return [
    {
      label: '主数据总量',
      value: overview.value.dataInstanceTotal,
      unit: '条',
      trend: overview.value.dataTodayNew > 0 ? 'up' : 'stable',
      trendValue: `+${overview.value.dataTodayNew}`,
      color: '#e62934',
      icon: 'database'
    },
    {
      label: '质量通过率',
      value: overview.value.qualityPassRate,
      unit: '%',
      trend: overview.value.qualityPassRate >= 95 ? 'up' : 'down',
      trendValue: overview.value.qualityPassRate >= 95 ? '优秀' : '待提升',
      color: '#52c41a',
      icon: 'check-circle'
    },
    {
      label: '分发成功率',
      value: overview.value.distributionSuccessRate,
      unit: '%',
      trend: overview.value.distributionSuccessRate >= 98 ? 'up' : 'down',
      trendValue: overview.value.distributionSuccessRate >= 98 ? '正常' : '异常',
      color: '#1890ff',
      icon: 'share-alt'
    },
    {
      label: '待办任务',
      value: pendingTasks.value.length,
      unit: '项',
      trend: pendingTasks.value.length > 5 ? 'warning' : 'stable',
      trendValue: pendingTasks.value.filter(t => t.priority === 'critical' || t.priority === 'high').length + ' 紧急',
      color: '#fa8c16',
      icon: 'clock-circle'
    },
  ]
})

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    // 模拟数据 - 实际使用时调用真实API
    overview.value = {
      fieldStandardTotal: 386,
      fieldStandardActive: 312,
      fieldStandardDraft: 74,
      categoryTotal: 24,
      domainTotal: 45,
      viewModelTotal: 56,
      viewModelPublished: 48,
      viewModelDraft: 8,
      entityTotal: 128,
      fieldTotal: 1246,
      formTotal: 42,
      formPublished: 36,
      formDraft: 6,
      dataInstanceTotal: 12586,
      dataTodayNew: 248,
      dataTodayUpdate: 156,
      dataTodayDelete: 23,
      qualityRuleTotal: 86,
      qualityRuleActive: 72,
      qualityCheckTotal: 156,
      qualityCheckToday: 12,
      qualityPassRate: 96.8,
      qualityIssueTotal: 128,
      qualityIssuePending: 15,
      distributionConfigTotal: 18,
      distributionConfigActive: 15,
      distributionTaskTotal: 2856,
      distributionTaskToday: 324,
      distributionSuccessRate: 98.5,
      distributionPending: 12,
    }

    activities.value = [
      { id: 1, time: '刚刚', module: '数据维护', action: '新增客户主数据记录', type: 'success', user: 'admin' },
      { id: 2, time: '2分钟前', module: '质量管理', action: '完成数据质量检测任务', type: 'success', user: 'system' },
      { id: 3, time: '5分钟前', module: '数据分发', action: '分发任务执行成功', type: 'success', user: 'system' },
      { id: 4, time: '10分钟前', module: '数据模型', action: '发布新版本视图模型', type: 'info', user: '张三' },
      { id: 5, time: '15分钟前', module: '质量管理', action: '发现3条质量问题', type: 'warning', user: 'system' },
      { id: 6, time: '20分钟前', module: '数据字典', action: '更新字段标准定义', type: 'info', user: '李四' },
      { id: 7, time: '30分钟前', module: '数据分发', action: '分发任务执行失败', type: 'error', user: 'system' },
      { id: 8, time: '1小时前', module: '表单设计', action: '新增表单模板', type: 'success', user: '王五' },
    ]

    // 实际API调用
    // const [overviewRes, activitiesRes] = await Promise.all([
    //   getDashboardOverview(),
    //   getRealtimeActivities(20)
    // ])
    // overview.value = overviewRes.data
    // activities.value = activitiesRes.data
  } catch (error) {
    console.error('获取监控数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 数字动画
const animatedNumbers = ref<Record<string, number>>({})
function animateNumber(key: string, target: number) {
  const duration = 1500
  const startTime = Date.now()
  const startValue = 0

  function update() {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    animatedNumbers.value[key] = Math.floor(startValue + (target - startValue) * easeProgress)

    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }

  update()
}

// 页面标题映射
const pageTitleMap: Record<string, { title: string; icon: string }> = {
  '/standard/field': { title: '字段标准库', icon: 'Document' },
  '/standard/view': { title: '数据标准视图', icon: 'Grid' },
  '/form/manage': { title: '表单管理', icon: 'Edit' },
  '/data/maintain': { title: '数据维护', icon: 'Coin' },
  '/quality/rule': { title: '质量规则', icon: 'CircleCheck' },
  '/quality/check': { title: '质量检测', icon: 'Checked' },
  '/quality/dashboard': { title: '质量看板', icon: 'DataAnalysis' },
  '/distribution/monitor': { title: '分发监控', icon: 'Share' },
  '/distribution/topic': { title: '分发主题', icon: 'Share' },
  '/distribution/subscription': { title: '订阅管理', icon: 'Share' },
  '/workflow/todo': { title: '待办任务', icon: 'Clock' },
  '/workflow/define': { title: '流程定义', icon: 'Connection' },
  '/masterdata/type': { title: '主数据类型', icon: 'Box' },
  '/masterdata/instance': { title: '主数据实例', icon: 'Box' },
  '/version/manage': { title: '版本管理', icon: 'Clock' },
}

// 导航 - 打开新TAB
function navigateTo(path: string) {
  const pageInfo = pageTitleMap[path] || { title: path.split('/').pop() || '页面', icon: 'Document' }

  // 添加TAB
  tabsStore.addTab({
    path: path,
    name: path.split('/').filter(Boolean).map((s, i) => i === 0 ? s.charAt(0).toUpperCase() + s.slice(1) : s).join(''),
    title: pageInfo.title,
    icon: pageInfo.icon
  })

  // 路由跳转
  router.push(path)
}

// 获取任务优先级颜色
function getPriorityColor(priority: string) {
  const colors: Record<string, string> = {
    critical: '#e62934',
    high: '#ff6b35',
    medium: '#fa8c16',
    low: '#8c8c8c'
  }
  return colors[priority] || '#8c8c8c'
}

// 获取活动类型颜色
function getActivityColor(type: string) {
  const colors: Record<string, string> = {
    success: '#52c41a',
    warning: '#fa8c16',
    error: '#e62934',
    info: '#1890ff'
  }
  return colors[type] || '#8c8c8c'
}

// 生命周期
onMounted(() => {
  fetchData()

  // 时间更新
  timeInterval = window.setInterval(() => {
    currentTime.value = new Date()
  }, 1000)

  // 数字动画
  setTimeout(() => {
    if (overview.value) {
      animateNumber('dataTotal', overview.value.dataInstanceTotal)
      animateNumber('qualityRate', overview.value.qualityPassRate * 10)
      animateNumber('distributionRate', overview.value.distributionSuccessRate * 10)
    }
  }, 300)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<template>
  <div class="dashboard-container">
    <!-- 顶部状态栏 -->
    <header class="status-bar">
      <div class="status-left">
        <div class="brand">
          <div class="brand-logo">
            <svg viewBox="0 0 40 40" width="32" height="32">
              <circle cx="12" cy="14" r="3" fill="currentColor"/>
              <circle cx="28" cy="14" r="3" fill="currentColor"/>
              <circle cx="20" cy="26" r="4" fill="currentColor"/>
              <path d="M12 14 L20 26 L28 14" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/>
              <line x1="16" y1="20" x2="24" y2="20" stroke="currentColor" stroke-width="1.5" stroke-dasharray="2 1"/>
            </svg>
          </div>
          <div class="brand-text">
            <span class="brand-name">MDM</span>
            <span class="brand-sub">数据驾驶舱</span>
          </div>
        </div>
      </div>

      <div class="status-center">
        <div class="time-display">
          <span class="time-value">{{ formattedTime }}</span>
          <span class="date-value">{{ formattedDate }}</span>
        </div>
      </div>

      <div class="status-right">
        <div class="user-greeting">
          <span class="greeting-text">{{ greeting }}，</span>
          <span class="user-name">{{ userStore.userInfo?.name || '用户' }}</span>
        </div>
        <div class="system-status">
          <span class="status-dot pulse"></span>
          <span class="status-text">系统运行正常</span>
        </div>
      </div>
    </header>

    <!-- 核心指标栏 -->
    <section class="core-metrics">
      <div
        v-for="metric in coreMetrics"
        :key="metric.label"
        class="metric-card"
        :style="{ '--accent-color': metric.color }"
      >
        <div class="metric-icon">
          <svg viewBox="0 0 24 24" width="28" height="28">
            <path v-if="metric.icon === 'database'" fill="currentColor" d="M12 3C7.58 3 4 4.79 4 7v10c0 2.21 3.58 4 8 4s8-1.79 8-4V7c0-2.21-3.58-4-8-4zm0 2c3.87 0 6 1.5 6 2s-2.13 2-6 2-6-1.5-6-2 2.13-2 6-2zm6 12c0 .5-2.13 2-6 2s-6-1.5-6-2v-2.23c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23V17zm0-5c0 .5-2.13 2-6 2s-6-1.5-6-2V9.77c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23V12z"/>
            <path v-else-if="metric.icon === 'check-circle'" fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            <path v-else-if="metric.icon === 'share-alt'" fill="currentColor" d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z"/>
            <path v-else-if="metric.icon === 'clock-circle'" fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
          </svg>
        </div>
        <div class="metric-content">
          <div class="metric-value">
            <span class="value">{{ metric.value }}</span>
            <span class="unit">{{ metric.unit }}</span>
          </div>
          <div class="metric-label">{{ metric.label }}</div>
        </div>
        <div class="metric-trend" :class="metric.trend">
          <span class="trend-value">{{ metric.trendValue }}</span>
        </div>
      </div>
    </section>

    <!-- 主内容区 -->
    <main class="dashboard-main">
      <!-- 左侧：用户待办 + 实时动态 -->
      <aside class="left-panel">
        <!-- 用户待办任务 -->
        <section class="panel user-tasks">
          <div class="panel-header">
            <div class="panel-title">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14l-5-5 1.41-1.41L12 14.17l4.59-4.58L18 11l-6 6z"/>
              </svg>
              <span>我的待办</span>
            </div>
            <span class="task-count">{{ pendingTasks.length }}</span>
          </div>
          <div class="panel-body">
            <div class="tasks-list">
              <div
                v-for="task in pendingTasks"
                :key="task.id"
                class="task-item"
                :style="{ '--priority-color': getPriorityColor(task.priority) }"
              >
                <div class="task-priority" :class="task.priority"></div>
                <div class="task-content">
                  <div class="task-title">{{ task.title }}</div>
                  <div class="task-meta">
                    <span class="task-type">{{ task.type }}</span>
                    <span class="task-from">来自: {{ task.from }}</span>
                    <span class="task-time">{{ task.time }}</span>
                  </div>
                </div>
                <button class="task-action" @click="navigateTo('/workflow/todo')">
                  <svg viewBox="0 0 24 24" width="16" height="16">
                    <path fill="currentColor" d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
                  </svg>
                </button>
              </div>
            </div>
            <div class="panel-footer">
              <button class="view-all-btn" @click="navigateTo('/workflow/todo')">
                查看全部待办
                <svg viewBox="0 0 24 24" width="14" height="14">
                  <path fill="currentColor" d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
                </svg>
              </button>
            </div>
          </div>
        </section>

        <!-- 实时动态 -->
        <section class="panel realtime-activity">
          <div class="panel-header">
            <div class="panel-title">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
              </svg>
              <span>实时动态</span>
            </div>
            <span class="live-indicator">
              <span class="live-dot"></span>
              LIVE
            </span>
          </div>
          <div class="panel-body">
            <div class="activity-timeline">
              <div
                v-for="(activity, index) in activities"
                :key="activity.id"
                class="activity-item"
                :style="{ '--activity-color': getActivityColor(activity.type) }"
              >
                <div class="activity-dot" :class="activity.type"></div>
                <div class="activity-line" v-if="index < activities.length - 1"></div>
                <div class="activity-content">
                  <div class="activity-header">
                    <span class="activity-module">{{ activity.module }}</span>
                    <span class="activity-time">{{ activity.time }}</span>
                  </div>
                  <div class="activity-action">{{ activity.action }}</div>
                  <div class="activity-user" v-if="activity.user">
                    <svg viewBox="0 0 24 24" width="12" height="12">
                      <path fill="currentColor" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                    {{ activity.user }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </aside>

      <!-- 中央：模块监控 -->
      <section class="center-panel">
        <div class="modules-grid">
          <div
            v-for="module in modules"
            :key="module.id"
            class="module-card"
            :style="{ '--module-color': module.color }"
            @click="navigateTo(module.path)"
          >
            <div class="module-header">
              <div class="module-icon">
                <svg viewBox="0 0 24 24" width="32" height="32">
                  <path v-if="module.icon === 'book'" fill="currentColor" d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/>
                  <path v-else-if="module.icon === 'grid'" fill="currentColor" d="M4 4h4v4H4V4zm6 0h4v4h-4V4zm6 0h4v4h-4V4zM4 10h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4zM4 16h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4z"/>
                  <path v-else-if="module.icon === 'edit'" fill="currentColor" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                  <path v-else-if="module.icon === 'database'" fill="currentColor" d="M12 3C7.58 3 4 4.79 4 7v10c0 2.21 3.58 4 8 4s8-1.79 8-4V7c0-2.21-3.58-4-8-4zm0 2c3.87 0 6 1.5 6 2s-2.13 2-6 2-6-1.5-6-2 2.13-2 6-2zm6 12c0 .5-2.13 2-6 2s-6-1.5-6-2v-2.23c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23V17zm0-5c0 .5-2.13 2-6 2s-6-1.5-6-2V9.77c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23V12z"/>
                  <path v-else-if="module.icon === 'check-circle'" fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  <path v-else-if="module.icon === 'share-alt'" fill="currentColor" d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z"/>
                </svg>
              </div>
              <div class="module-name">{{ module.name }}</div>
            </div>
            <div class="module-stats">
              <div
                v-for="(stat, index) in module.stats"
                :key="index"
                class="stat-item"
              >
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-sub" v-if="stat.sub">{{ stat.sub }}</div>
              </div>
            </div>
            <div class="module-footer">
              <span class="view-detail">点击查看详情</span>
              <svg viewBox="0 0 24 24" width="16" height="16">
                <path fill="currentColor" d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z"/>
              </svg>
            </div>
          </div>
        </div>
      </section>

      <!-- 右侧：快速入口 -->
      <aside class="right-panel">
        <section class="panel quick-entry">
          <div class="panel-header">
            <div class="panel-title">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/>
              </svg>
              <span>快速入口</span>
            </div>
          </div>
          <div class="panel-body">
            <div class="entry-grid">
              <div
                v-for="entry in quickEntries"
                :key="entry.path"
                class="entry-item"
                :style="{ '--entry-color': entry.color }"
                @click="navigateTo(entry.path)"
              >
                <div class="entry-icon">
                  <svg viewBox="0 0 24 24" width="24" height="24">
                    <path v-if="entry.icon === 'book'" fill="currentColor" d="M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z"/>
                    <path v-else-if="entry.icon === 'grid'" fill="currentColor" d="M4 4h4v4H4V4zm6 0h4v4h-4V4zm6 0h4v4h-4V4zM4 10h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4zM4 16h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4z"/>
                    <path v-else-if="entry.icon === 'edit'" fill="currentColor" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                    <path v-else-if="entry.icon === 'database'" fill="currentColor" d="M12 3C7.58 3 4 4.79 4 7v10c0 2.21 3.58 4 8 4s8-1.79 8-4V7c0-2.21-3.58-4-8-4zm0 2c3.87 0 6 1.5 6 2s-2.13 2-6 2-6-1.5-6-2 2.13-2 6-2zm6 12c0 .5-2.13 2-6 2s-6-1.5-6-2v-2.23c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23V17zm0-5c0 .5-2.13 2-6 2s-6-1.5-6-2V9.77c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23V12z"/>
                    <path v-else-if="entry.icon === 'check-circle'" fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                    <path v-else-if="entry.icon === 'share-alt'" fill="currentColor" d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z"/>
                  </svg>
                </div>
                <div class="entry-name">{{ entry.name }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 数据概览 -->
        <section class="panel data-overview">
          <div class="panel-header">
            <div class="panel-title">
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
              </svg>
              <span>数据概览</span>
            </div>
          </div>
          <div class="panel-body">
            <div class="overview-chart">
              <div class="chart-item">
                <div class="chart-label">字段标准</div>
                <div class="chart-bar">
                  <div class="chart-fill" style="--width: 80%; --color: #e62934;"></div>
                </div>
                <div class="chart-value">386</div>
              </div>
              <div class="chart-item">
                <div class="chart-label">视图模型</div>
                <div class="chart-bar">
                  <div class="chart-fill" style="--width: 65%; --color: #ff6b35;"></div>
                </div>
                <div class="chart-value">56</div>
              </div>
              <div class="chart-item">
                <div class="chart-label">表单数量</div>
                <div class="chart-bar">
                  <div class="chart-fill" style="--width: 50%; --color: #ffd700;"></div>
                </div>
                <div class="chart-value">42</div>
              </div>
              <div class="chart-item">
                <div class="chart-label">质量规则</div>
                <div class="chart-bar">
                  <div class="chart-fill" style="--width: 70%; --color: #52c41a;"></div>
                </div>
                <div class="chart-value">86</div>
              </div>
              <div class="chart-item">
                <div class="chart-label">分发配置</div>
                <div class="chart-bar">
                  <div class="chart-fill" style="--width: 35%; --color: #722ed1;"></div>
                </div>
                <div class="chart-value">18</div>
              </div>
            </div>
          </div>
        </section>
      </aside>
    </main>
  </div>
</template>

<style scoped>
.dashboard-container {
  min-height: calc(100vh - 60px);
  background: linear-gradient(180deg, #0d0d1a 0%, #1a1a2e 50%, #16213e 100%);
  padding: 0;
  color: #fff;
  position: relative;
  overflow-x: hidden;
}

/* 背景网格效果 */
.dashboard-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(90deg, rgba(230, 41, 52, 0.03) 1px, transparent 1px),
    linear-gradient(rgba(230, 41, 52, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
  z-index: 0;
}

/* 顶部状态栏 */
.status-bar {
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(26, 26, 46, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(230, 41, 52, 0.2);
}

.status-left {
  display: flex;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #e62934 0%, #ff4757 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 0 20px rgba(230, 41, 52, 0.4);
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  color: #e62934;
  letter-spacing: 2px;
}

.brand-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 1px;
}

.status-center {
  display: flex;
  align-items: center;
}

.time-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 24px;
  background: rgba(230, 41, 52, 0.1);
  border: 1px solid rgba(230, 41, 52, 0.3);
  border-radius: 8px;
}

.time-value {
  font-size: 28px;
  font-weight: 700;
  color: #e62934;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
  text-shadow: 0 0 20px rgba(230, 41, 52, 0.5);
}

.date-value {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.status-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-greeting {
  display: flex;
  align-items: center;
  gap: 4px;
}

.greeting-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(82, 196, 26, 0.1);
  border: 1px solid rgba(82, 196, 26, 0.3);
  border-radius: 20px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
}

.status-dot.pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(82, 196, 26, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(82, 196, 26, 0);
  }
}

.status-text {
  font-size: 12px;
  color: #52c41a;
}

/* 核心指标栏 */
.core-metrics {
  position: relative;
  z-index: 10;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 24px;
}

.metric-card {
  position: relative;
  background: rgba(30, 30, 50, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s;
  overflow: hidden;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--accent-color);
}

.metric-card:hover {
  transform: translateY(-4px);
  border-color: var(--accent-color);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3), 0 0 40px color-mix(in srgb, var(--accent-color) 30%, transparent);
}

.metric-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-color);
}

.metric-content {
  flex: 1;
}

.metric-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.metric-value .value {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  font-family: 'Courier New', monospace;
}

.metric-value .unit {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.metric-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
}

.metric-trend {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.metric-trend.up {
  background: rgba(82, 196, 26, 0.2);
  color: #52c41a;
}

.metric-trend.down {
  background: rgba(230, 41, 52, 0.2);
  color: #e62934;
}

.metric-trend.stable {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
}

.metric-trend.warning {
  background: rgba(250, 140, 22, 0.2);
  color: #fa8c16;
}

/* 主内容区 */
.dashboard-main {
  position: relative;
  z-index: 10;
  display: grid;
  grid-template-columns: 320px 1fr 280px;
  gap: 16px;
  padding: 0 24px 24px;
}

/* 面板通用样式 */
.panel {
  background: rgba(30, 30, 50, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.panel-title svg {
  color: #e62934;
}

.panel-body {
  padding: 16px;
}

/* 用户待办任务 */
.user-tasks {
  margin-bottom: 16px;
}

.task-count {
  background: rgba(230, 41, 52, 0.2);
  color: #e62934;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 280px;
  overflow-y: auto;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border-left: 3px solid var(--priority-color);
  transition: all 0.2s;
  cursor: pointer;
}

.task-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.task-priority {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--priority-color);
  flex-shrink: 0;
}

.task-priority.critical {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-title {
  font-size: 13px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.task-type {
  padding: 1px 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.task-action {
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.05);
  border: none;
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.task-action:hover {
  background: rgba(230, 41, 52, 0.2);
  color: #e62934;
}

.panel-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.view-all-btn {
  width: 100%;
  padding: 8px;
  background: rgba(230, 41, 52, 0.1);
  border: 1px solid rgba(230, 41, 52, 0.3);
  border-radius: 6px;
  color: #e62934;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.2s;
}

.view-all-btn:hover {
  background: rgba(230, 41, 52, 0.2);
}

/* 实时动态 */
.live-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  font-weight: 600;
  color: #52c41a;
  letter-spacing: 1px;
}

.live-dot {
  width: 6px;
  height: 6px;
  background: #52c41a;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

.activity-timeline {
  display: flex;
  flex-direction: column;
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  position: relative;
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
}

.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--activity-color);
  flex-shrink: 0;
  z-index: 1;
}

.activity-line {
  position: absolute;
  left: 4.5px;
  top: 14px;
  width: 1px;
  height: calc(100% - 4px);
  background: rgba(255, 255, 255, 0.1);
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.activity-module {
  font-size: 11px;
  padding: 2px 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  color: rgba(255, 255, 255, 0.7);
}

.activity-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
}

.activity-action {
  font-size: 13px;
  color: #fff;
  line-height: 1.4;
}

.activity-user {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

/* 模块监控区 */
.center-panel {
  display: flex;
  flex-direction: column;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.module-card {
  position: relative;
  background: rgba(30, 30, 50, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
}

.module-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--module-color);
  opacity: 0.5;
  transition: opacity 0.3s;
}

.module-card:hover {
  transform: translateY(-4px);
  border-color: var(--module-color);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 40px color-mix(in srgb, var(--module-color) 20%, transparent);
}

.module-card:hover::before {
  opacity: 1;
}

.module-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.module-icon {
  width: 48px;
  height: 48px;
  background: color-mix(in srgb, var(--module-color) 15%, transparent);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--module-color);
}

.module-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.module-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  flex: 1;
  min-width: 80px;
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  font-family: 'Courier New', monospace;
}

.stat-item .stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
}

.stat-item .stat-sub {
  font-size: 11px;
  color: var(--module-color);
  margin-top: 2px;
}

.module-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.module-card:hover .module-footer {
  color: var(--module-color);
}

/* 快速入口 */
.quick-entry {
  margin-bottom: 16px;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 12px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.entry-item:hover {
  background: color-mix(in srgb, var(--entry-color) 10%, transparent);
  border-color: var(--entry-color);
  transform: translateY(-2px);
}

.entry-icon {
  width: 40px;
  height: 40px;
  background: color-mix(in srgb, var(--entry-color) 15%, transparent);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--entry-color);
  margin-bottom: 8px;
}

.entry-name {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
}

/* 数据概览 */
.data-overview .panel-body {
  padding: 12px 16px;
}

.overview-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-label {
  width: 60px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}

.chart-bar {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.chart-fill {
  height: 100%;
  width: var(--width);
  background: var(--color);
  border-radius: 4px;
  transition: width 1s ease;
  box-shadow: 0 0 10px color-mix(in srgb, var(--color) 50%, transparent);
}

.chart-value {
  width: 40px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  font-family: 'Courier New', monospace;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 2px;
}

::-webkit-scrollbar-thumb {
  background: rgba(230, 41, 52, 0.3);
  border-radius: 2px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(230, 41, 52, 0.5);
}

/* 响应式 */
@media (max-width: 1600px) {
  .dashboard-main {
    grid-template-columns: 280px 1fr 240px;
  }

  .modules-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1200px) {
  .dashboard-main {
    grid-template-columns: 1fr;
  }

  .core-metrics {
    grid-template-columns: repeat(2, 1fr);
  }

  .left-panel,
  .right-panel {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .left-panel .panel,
  .right-panel .panel {
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .core-metrics {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .modules-grid {
    grid-template-columns: 1fr;
  }

  .left-panel,
  .right-panel {
    grid-template-columns: 1fr;
  }

  .status-bar {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
