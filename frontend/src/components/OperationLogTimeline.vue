<template>
  <div class="operation-log-timeline">
    <el-timeline v-if="logs.length > 0">
      <el-timeline-item
        v-for="log in logs"
        :key="log.id"
        :timestamp="formatDateTime(log.createdAt)"
        :type="getLogColor(log.operationType)"
        :icon="getLogIcon(log.operationType)"
        placement="top"
      >
        <div class="log-item">
          <!-- 用户 + 操作 -->
          <div class="log-header">
            <span class="log-user">{{ log.createdBy }}</span>
            <span class="log-action">{{ getActionText(log) }}</span>
          </div>

          <!-- 状态变更标签 -->
          <div v-if="log.fromStatus && log.toStatus" class="log-status-change">
            <el-tag :type="getStatusColor(log.fromStatus)" size="small">
              {{ getStatusLabel(log.fromStatus) }}
            </el-tag>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            <el-tag :type="getStatusColor(log.toStatus)" size="small">
              {{ getStatusLabel(log.toStatus) }}
            </el-tag>
          </div>

          <!-- 更新详情：显示变更的字段 -->
          <div v-if="log.operationType === 'update' && getChangeSummary(log)" class="log-detail">
            <div class="detail-text">{{ getChangeSummary(log) }}</div>
          </div>

          <!-- 质检不合格：显示原因 -->
          <div v-if="log.operationType === 'qc_fail'" class="log-quality-fail">
            <div class="fail-header">
              <el-icon><WarningFilled /></el-icon>
              <span>质检不合格原因</span>
            </div>
            <div class="fail-content">
              <div v-if="log.qualityScore" class="score-row">
                <span class="label">质检评分：</span>
                <el-progress
                  :percentage="Number(log.qualityScore)"
                  :color="Number(log.qualityScore) >= 80 ? '#67c23a' : '#f56c6c'"
                  :stroke-width="10"
                  style="flex: 1;"
                />
                <span class="score-value">{{ Number(log.qualityScore).toFixed(1) }}%</span>
              </div>
              <div v-if="log.qualityIssues && parseQualityIssues(log.qualityIssues).length > 0" class="issues-list">
                <div v-for="(issue, index) in parseQualityIssues(log.qualityIssues).slice(0, 5)" :key="index" class="issue-item">
                  • {{ issue.fieldName || issue.field || issue }}：{{ issue.issue || issue.message || '不符合规范' }}
                </div>
                <div v-if="parseQualityIssues(log.qualityIssues).length > 5" class="issue-more">
                  还有 {{ parseQualityIssues(log.qualityIssues).length - 5 }} 个问题...
                </div>
              </div>
            </div>
          </div>

          <!-- 质检合格：显示评分 -->
          <div v-if="log.operationType === 'qc_pass' && log.qualityScore" class="log-quality-pass">
            <div class="score-row">
              <span class="label">质检评分：</span>
              <el-progress
                :percentage="Number(log.qualityScore)"
                color="#67c23a"
                :stroke-width="10"
                style="flex: 1;"
              />
              <span class="score-value">{{ Number(log.qualityScore).toFixed(1) }}%</span>
            </div>
          </div>

          <!-- 驳回/作废原因 -->
          <div v-if="log.operationReason && (log.operationType === 'reject' || log.operationType === 'obsolete')" class="log-reason">
            <span class="label">原因：</span>
            <span class="reason-text">{{ log.operationReason }}</span>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>

    <el-empty v-else description="暂无操作日志" :image-size="80" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ArrowRight, Check, Close, Edit, Delete, Plus, RefreshLeft,
  DataAnalysis, Promotion, WarningFilled, DocumentChecked,
  CircleClose, RefreshRight
} from '@element-plus/icons-vue'
import { getOperationLogs, type OperationLog } from '@/api/data/operationLog'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  formId: number
  recordId: number
}>()

const logs = ref<OperationLog[]>([])
const loading = ref(false)

// 加载日志
async function loadLogs() {
  loading.value = true
  try {
    const res = await getOperationLogs(props.formId, props.recordId)
    logs.value = res.data || []
  } catch (error) {
    ElMessage.error('加载操作日志失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 获取操作动作文本
const getActionText = (log: OperationLog): string => {
  const type = log.operationType
  switch (type) {
    case 'create':
      return '创建了数据草稿'
    case 'update':
      return '更新了数据'
    case 'delete':
      return '删除了数据'
    case 'submit':
      return '提交了审批'
    case 'approve':
      return '通过了审批'
    case 'reject':
      return '驳回了审批'
    case 'qc_pass':
      return '质检合格'
    case 'qc_fail':
      return '质检不合格'
    case 'obsolete':
      return '作废了数据'
    case 'restore':
      return '恢复了数据'
    case 'distribute':
      return '分发到下游系统'
    default:
      return log.operationDetail || '操作了数据'
  }
}

// 获取变更摘要（用于更新日志）
const getChangeSummary = (log: OperationLog): string => {
  if (!log.operationData) return ''

  try {
    const data = JSON.parse(log.operationData)
    const changeCount = data.changeCount || 0

    if (changeCount === 0) return ''

    // 解析具体的变更字段
    const mainChanges = data.mainTable || {}
    const subChanges = data.subTables || {}

    const changeTexts: string[] = []

    // 主表变更字段（显示字段名称）
    const mainFields = Object.keys(mainChanges)
    if (mainFields.length > 0) {
      // 从变更数据中获取字段名称
      const fieldNames = mainFields.map(code => {
        const changeInfo = mainChanges[code]
        return changeInfo?.fieldName || code
      })
      const displayFields = fieldNames.slice(0, 3).join('、')
      const more = fieldNames.length > 3 ? `等${fieldNames.length}个字段` : ''
      changeTexts.push(`主表「${displayFields}」${more}`)
    }

    // 子表变更（显示子表名称）
    const subTables = Object.keys(subChanges)
    if (subTables.length > 0) {
      const subNames = subTables.map(code => {
        const subInfo = subChanges[code]
        return subInfo?.entityName || code
      })
      const displaySubs = subNames.slice(0, 2).join('、')
      const more = subNames.length > 2 ? `等${subNames.length}个子表` : ''
      changeTexts.push(`子表「${displaySubs}」${more}`)
    }

    return changeTexts.length > 0 ? `修改了${changeTexts.join('、')}` : ''
  } catch {
    return ''
  }
}

// 获取日志颜色
const getLogColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'create': 'primary',
    'update': 'primary',
    'delete': 'danger',
    'submit': 'warning',
    'approve': 'success',
    'reject': 'danger',
    'quality_check': 'primary',
    'qc_pass': 'success',
    'qc_fail': 'danger',
    'obsolete': 'info',
    'restore': 'success',
    'distribute': 'primary'
  }
  return colorMap[type] || 'primary'
}

// 获取日志图标
const getLogIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    'create': Plus,
    'update': Edit,
    'delete': Delete,
    'submit': Promotion,
    'approve': Check,
    'reject': Close,
    'qc_pass': DocumentChecked,
    'qc_fail': CircleClose,
    'restore': RefreshLeft,
    'quality_check': DataAnalysis,
    'obsolete': Delete,
    'distribute': Promotion
  }
  return iconMap[type] || Check
}

// 获取状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'PENDING_QC': 'primary',
    'ACTIVE_QUALIFIED': 'success',
    'ACTIVE_UNQUALIFIED': 'danger',
    'OBSOLETE': 'info'
  }
  return colorMap[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '审批中',
    'PENDING_QC': '待质检',
    'ACTIVE_QUALIFIED': '已生效-合格',
    'ACTIVE_UNQUALIFIED': '已生效-不合格',
    'OBSOLETE': '已作废'
  }
  return labelMap[status] || status
}

// 解析质检问题
const parseQualityIssues = (issuesJson: string) => {
  try {
    const parsed = JSON.parse(issuesJson)
    if (Array.isArray(parsed)) return parsed
    if (parsed.issues && Array.isArray(parsed.issues)) return parsed.issues
    return []
  } catch {
    return []
  }
}

onMounted(() => {
  loadLogs()
})

// 暴露刷新方法
defineExpose({
  refresh: loadLogs
})
</script>

<style scoped lang="scss">
.operation-log-timeline {
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;

  .log-item {
    padding: 12px 16px;
    background: #f8fafc;
    border-radius: 6px;
    border-left: 3px solid #409eff;

    .log-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .log-user {
        font-weight: 600;
        color: #409eff;
        font-size: 14px;
      }

      .log-action {
        color: #606266;
        font-size: 14px;
      }
    }

    .log-status-change {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;

      .arrow-icon {
        color: #909399;
      }
    }

    .log-detail {
      margin-top: 8px;
      padding: 8px 12px;
      background: #ecf5ff;
      border-radius: 4px;

      .detail-text {
        color: #409eff;
        font-size: 13px;
      }
    }

    .log-quality-fail {
      margin-top: 10px;
      padding: 12px;
      background: #fef0f0;
      border-radius: 6px;
      border: 1px solid #fbc4c4;

      .fail-header {
        display: flex;
        align-items: center;
        gap: 6px;
        color: #f56c6c;
        font-weight: 600;
        margin-bottom: 10px;
        font-size: 13px;
      }

      .fail-content {
        .score-row {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 10px;

          .label {
            color: #606266;
            font-size: 13px;
            white-space: nowrap;
          }

          .score-value {
            font-weight: 600;
            color: #f56c6c;
            font-size: 13px;
          }
        }

        .issues-list {
          .issue-item {
            color: #606266;
            font-size: 12px;
            line-height: 1.8;
            padding-left: 4px;
          }
          .issue-more {
            color: #909399;
            font-size: 12px;
            margin-top: 4px;
            font-style: italic;
          }
        }
      }
    }

    .log-quality-pass {
      margin-top: 8px;
      padding: 8px 12px;
      background: #f0f9eb;
      border-radius: 4px;

      .score-row {
        display: flex;
        align-items: center;
        gap: 10px;

        .label {
          color: #606266;
          font-size: 13px;
          white-space: nowrap;
        }

        .score-value {
          font-weight: 600;
          color: #67c23a;
          font-size: 13px;
        }
      }
    }

    .log-reason {
      margin-top: 8px;
      padding: 8px 12px;
      background: #fafafa;
      border-radius: 4px;

      .label {
        color: #909399;
        font-size: 13px;
      }

      .reason-text {
        color: #606266;
        font-size: 13px;
      }
    }
  }
}

// 不同类型日志的左边框颜色
:deep(.el-timeline-item) {
  .el-timeline-item__tail {
    border-left-color: #e4e7ed;
  }

  // 根据类型调整日志项样式
  .el-timeline-item__node--primary {
    background-color: #409eff;
  }
  .el-timeline-item__node--success {
    background-color: #67c23a;
  }
  .el-timeline-item__node--warning {
    background-color: #e6a23c;
  }
  .el-timeline-item__node--danger {
    background-color: #f56c6c;
  }
  .el-timeline-item__node--info {
    background-color: #909399;
  }
}
</style>
