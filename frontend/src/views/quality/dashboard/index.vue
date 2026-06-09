<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTaskList, type QualityCheckTask } from '../../../api/quality/check'
import { getIssueList, countByStatus, type QualityIssue } from '../../../api/quality/issue'
import { getRuleList, type QualityRule } from '../../../api/quality/rule'

// 统计数据
const stats = ref({
  totalRules: 0,
  totalTasks: 0,
  openIssues: 0,
  avgPassRate: 0
})

// 最近检测任务
const recentTasks = ref<QualityCheckTask[]>([])

// 待处理问题
const openIssues = ref<QualityIssue[]>([])

// 规则分布
const ruleDistribution = ref<{ type: string; count: number }[]>([])

// 加载中
const loading = ref(false)

// 获取统计数据
async function fetchStats() {
  loading.value = true
  try {
    // 获取规则数量
    const ruleRes = await getRuleList({ status: 'active' })
    stats.value.totalRules = ruleRes.data?.length || 0

    // 计算规则类型分布
    const rules = ruleRes.data || []
    const typeCount: Record<string, number> = {}
    rules.forEach((r: QualityRule) => {
      typeCount[r.ruleType] = (typeCount[r.ruleType] || 0) + 1
    })
    ruleDistribution.value = Object.entries(typeCount).map(([type, count]) => ({
      type: getRuleTypeLabel(type),
      count
    }))

    // 获取检测任务
    const taskRes = await getTaskList({})
    const tasks = taskRes.data || []
    stats.value.totalTasks = tasks.length

    // 计算平均通过率
    const completedTasks = tasks.filter((t: QualityCheckTask) => t.status === 'completed' && t.passRate != null)
    if (completedTasks.length > 0) {
      const totalRate = completedTasks.reduce((sum: number, t: QualityCheckTask) => sum + (t.passRate || 0), 0)
      stats.value.avgPassRate = Math.round(totalRate / completedTasks.length * 10) / 10
    }

    // 最近5条检测任务
    recentTasks.value = tasks.slice(0, 5)

    // 获取待处理问题
    const issueRes = await getIssueList({ status: 'open' })
    openIssues.value = (issueRes.data || []).slice(0, 5)
    stats.value.openIssues = (issueRes.data || []).length

  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取规则类型标签
function getRuleTypeLabel(type: string) {
  const labels: Record<string, string> = {
    completeness: '完整性',
    uniqueness: '唯一性',
    accuracy: '准确性',
    consistency: '一致性',
    timeliness: '及时性'
  }
  return labels[type] || type
}

// 获取状态标签
function getStatusLabel(status: string) {
  const labels: Record<string, string> = {
    pending: '待执行',
    running: '执行中',
    completed: '已完成',
    failed: '失败'
  }
  return labels[status] || status
}

// 获取状态样式类
function getStatusClass(status: string) {
  const classMap: Record<string, string> = {
    pending: 'status-pending',
    running: 'status-running',
    completed: 'status-completed',
    failed: 'status-failed'
  }
  return classMap[status] || ''
}

// 获取级别样式类
function getLevelClass(level?: string) {
  if (!level) return ''
  const classMap: Record<string, string> = {
    warning: 'level-warning',
    error: 'level-error',
    critical: 'level-critical'
  }
  return classMap[level] || ''
}

// 格式化执行时间
function formatDuration(ms?: number) {
  if (!ms) return '-'
  if (ms < 1000) return ms + 'ms'
  return (ms / 1000).toFixed(2) + 's'
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon rules">
          <svg viewBox="0 0 24 24" width="32" height="32">
            <path fill="currentColor" d="M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalRules }}</div>
          <div class="stat-label">质量规则</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon tasks">
          <svg viewBox="0 0 24 24" width="32" height="32">
            <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalTasks }}</div>
          <div class="stat-label">检测任务</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon issues">
          <svg viewBox="0 0 24 24" width="32" height="32">
            <path fill="currentColor" d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.openIssues }}</div>
          <div class="stat-label">待处理问题</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon rate">
          <svg viewBox="0 0 24 24" width="32" height="32">
            <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.avgPassRate }}%</div>
          <div class="stat-label">平均通过率</div>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="dashboard-content">
      <!-- 左侧：规则分布 + 最近检测 -->
      <div class="left-section">
        <!-- 规则分布 -->
        <div class="panel">
          <div class="panel-header">
            <h3>规则类型分布</h3>
          </div>
          <div class="panel-body">
            <div class="distribution-list">
              <div v-for="item in ruleDistribution" :key="item.type" class="distribution-item">
                <div class="distribution-label">{{ item.type }}</div>
                <div class="distribution-bar">
                  <div class="bar-fill" :style="{ width: (item.count / stats.totalRules * 100) + '%' }"></div>
                </div>
                <div class="distribution-count">{{ item.count }}</div>
              </div>
              <div v-if="ruleDistribution.length === 0" class="empty-tip">暂无规则</div>
            </div>
          </div>
        </div>

        <!-- 最近检测任务 -->
        <div class="panel">
          <div class="panel-header">
            <h3>最近检测任务</h3>
          </div>
          <div class="panel-body">
            <div class="task-list">
              <div v-for="task in recentTasks" :key="task.id" class="task-item">
                <div class="task-info">
                  <div class="task-name">{{ task.taskName }}</div>
                  <div class="task-time">{{ task.startTime }}</div>
                </div>
                <div class="task-stats">
                  <span class="status-tag" :class="getStatusClass(task.status)">
                    {{ getStatusLabel(task.status) }}
                  </span>
                  <span class="pass-rate" v-if="task.passRate != null">
                    {{ task.passRate.toFixed(1) }}%
                  </span>
                </div>
              </div>
              <div v-if="recentTasks.length === 0" class="empty-tip">暂无检测任务</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：待处理问题 -->
      <div class="right-section">
        <div class="panel">
          <div class="panel-header">
            <h3>待处理问题</h3>
          </div>
          <div class="panel-body">
            <div class="issue-list">
              <div v-for="issue in openIssues" :key="issue.id" class="issue-item">
                <div class="issue-header">
                  <span class="issue-code">{{ issue.issueCode }}</span>
                  <span class="level-tag" :class="getLevelClass(issue.issueLevel)">
                    {{ issue.issueLevel }}
                  </span>
                </div>
                <div class="issue-desc">{{ issue.issueDesc || issue.ruleName }}</div>
                <div class="issue-meta">
                  <span>{{ issue.viewName || '-' }}</span>
                  <span>·</span>
                  <span>{{ issue.fieldName || issue.fieldCode || '-' }}</span>
                </div>
              </div>
              <div v-if="openIssues.length === 0" class="empty-tip">暂无待处理问题</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.rules {
  background: rgba(230, 41, 52, 0.1);
  color: #e62934;
}

.stat-icon.tasks {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.stat-icon.issues {
  background: rgba(250, 140, 22, 0.1);
  color: #fa8c16;
}

.stat-icon.rate {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #262626;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-top: 4px;
}

/* 内容区域 */
.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.panel-body {
  padding: 16px 20px;
}

/* 规则分布 */
.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.distribution-label {
  width: 60px;
  font-size: 13px;
  color: #595959;
}

.distribution-bar {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #e62934, #ff6b6b);
  border-radius: 4px;
  transition: width 0.3s;
}

.distribution-count {
  width: 30px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #262626;
}

/* 任务列表 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}

.task-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.task-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.task-stats {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.status-pending {
  background: #f0f0f0;
  color: #666;
}

.status-tag.status-running {
  background: #e6f7ff;
  color: #1890ff;
}

.status-tag.status-completed {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.status-failed {
  background: #fff2f0;
  color: #ff4d4f;
}

.pass-rate {
  font-size: 14px;
  font-weight: 600;
  color: #52c41a;
}

/* 问题列表 */
.issue-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.issue-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}

.issue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.issue-code {
  font-size: 13px;
  font-weight: 500;
  color: #262626;
}

.level-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.level-tag.level-warning {
  background: #fffbe6;
  color: #faad14;
}

.level-tag.level-error {
  background: #fff2f0;
  color: #ff4d4f;
}

.level-tag.level-critical {
  background: #ff4d4f;
  color: #fff;
}

.issue-desc {
  font-size: 13px;
  color: #595959;
  margin-bottom: 8px;
}

.issue-meta {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  gap: 8px;
}

.empty-tip {
  text-align: center;
  padding: 20px;
  color: #8c8c8c;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-content {
    grid-template-columns: 1fr;
  }
}
</style>
