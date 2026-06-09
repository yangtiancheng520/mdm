<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTaskList,
  executeCheck,
  deleteTask,
  getResults,
  TASK_STATUS_OPTIONS,
  type QualityCheckTask,
  type QualityCheckResult
} from '../../../api/quality/check'
import { getViewList, type ViewDefinition } from '../../../api/standard/viewDefinition'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

const tableData = ref<QualityCheckTask[]>([])
const loading = ref(false)

// 搜索
const searchForm = ref({
  viewId: null as number | null,
  status: ''
})

// 视图列表
const viewList = ref<ViewDefinition[]>([])

// 弹窗
const dialogVisible = ref(false)

// 结果弹窗
const resultDialogVisible = ref(false)
const resultLoading = ref(false)
const resultList = ref<QualityCheckResult[]>([])
const currentTask = ref<QualityCheckTask | null>(null)

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单
const form = ref<QualityCheckTask>({
  taskCode: '',
  taskName: '',
  viewId: 0,
  status: 'pending'
})

// 执行中
const executing = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取任务列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getTaskList(searchForm.value)
    tableData.value = res.data || []
    total.value = tableData.value.length
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取视图列表
async function fetchViewList() {
  try {
    const res = await getViewList({ status: 'published' })
    viewList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchData()
}

// 重置
function handleReset() {
  searchForm.value = {
    viewId: null,
    status: ''
  }
  currentPage.value = 1
  fetchData()
}

// 新增任务
function handleAdd() {
  form.value = {
    taskCode: '',
    taskName: '',
    viewId: 0,
    status: 'pending'
  }
  dialogVisible.value = true
}

// 查看结果
async function handleViewResult(row: QualityCheckTask) {
  currentTask.value = row
  resultDialogVisible.value = true
  resultLoading.value = true

  try {
    const res = await getResults(row.id!)
    resultList.value = res.data || []
  } catch (error) {
    console.error(error)
    resultList.value = []
  } finally {
    resultLoading.value = false
  }
}

// 删除
function handleDelete(row: QualityCheckTask) {
  confirmMessage.value = `确定要删除任务「${row.taskName}」吗？`
  confirmAction.value = async () => {
    await deleteTask(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  }
  confirmVisible.value = true
}

// 确认删除
function handleConfirmDelete() {
  if (confirmAction.value) {
    confirmAction.value()
  }
}

// 提交执行
async function handleSubmit() {
  if (!form.value.viewId) {
    ElMessage.warning('请选择视图')
    return
  }

  executing.value = true
  try {
    const res = await executeCheck(form.value)
    ElMessage.success('检测任务执行完成')
    dialogVisible.value = false
    fetchData()

    // 显示结果
    if (res.data) {
      currentTask.value = res.data
      resultDialogVisible.value = true
      resultLoading.value = true
      const resultRes = await getResults(res.data.id!)
      resultList.value = resultRes.data || []
      resultLoading.value = false
    }
  } catch (error: any) {
    ElMessage.error(error.message || '执行失败')
  } finally {
    executing.value = false
  }
}

// 获取状态标签
function getStatusLabel(status: string) {
  const item = TASK_STATUS_OPTIONS.find(s => s.value === status)
  return item?.label || status
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

// 获取问题级别样式类
function getIssueLevelClass(level?: string) {
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

// 分页
function handlePageChange(page: number) {
  currentPage.value = page
  fetchData()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

onMounted(() => {
  fetchData()
  fetchViewList()
})
</script>

<template>
  <div class="mdm-container">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <select v-model="searchForm.viewId" @change="handleSearch">
            <option :value="null">全部视图</option>
            <option v-for="view in viewList" :key="view.id" :value="view.id">{{ view.viewName }}</option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option v-for="item in TASK_STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-red" @click="handleAdd">+ 执行检测</button>
        <button class="mdm-btn-outline" @click="handleSearch">查询</button>
        <button class="mdm-btn-outline" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="mdm-table-wrapper">
      <table class="mdm-data-table">
        <thead>
          <tr>
            <th>任务编码</th>
            <th>任务名称</th>
            <th>视图</th>
            <th>状态</th>
            <th>检测记录</th>
            <th>通过/失败</th>
            <th>通过率</th>
            <th>执行耗时</th>
            <th>执行时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-loading="loading">
          <tr v-for="row in tableData" :key="row.id">
            <td>{{ row.taskCode }}</td>
            <td>{{ row.taskName }}</td>
            <td>{{ row.viewName || '-' }}</td>
            <td>
              <span class="status-tag" :class="getStatusClass(row.status)">
                {{ getStatusLabel(row.status) }}
              </span>
            </td>
            <td>{{ row.totalRecords || 0 }}</td>
            <td>
              <span class="pass-count">{{ row.passCount || 0 }}</span> /
              <span class="fail-count">{{ row.failCount || 0 }}</span>
            </td>
            <td>
              <span class="pass-rate" :class="{ 'rate-low': (row.passRate || 100) < 80 }">
                {{ row.passRate != null ? row.passRate.toFixed(1) + '%' : '-' }}
              </span>
            </td>
            <td>{{ formatDuration(row.durationMs) }}</td>
            <td>{{ row.startTime || '-' }}</td>
            <td>
              <div class="mdm-action-buttons">
                <button class="mdm-action-btn" @click="handleViewResult(row)">查看结果</button>
                <button class="mdm-action-btn delete" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="tableData.length === 0 && !loading">
            <td colspan="10" class="mdm-empty-data">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="mdm-pagination">
      <span class="mdm-pagination-total">共 {{ total }} 条</span>
      <button class="mdm-page-btn" @click="handlePageChange(1)">◀◀</button>
      <button class="mdm-page-btn" @click="handlePageChange(currentPage - 1)">‹</button>
      <button class="mdm-page-btn active">{{ currentPage }}</button>
      <button class="mdm-page-btn" @click="handlePageChange(currentPage + 1)">›</button>
      <button class="mdm-page-btn" @click="handlePageChange(Math.ceil(total / pageSize))">▶▶</button>
      <select class="mdm-page-select" v-model="pageSize" @change="handleSizeChange(pageSize)">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>

    <!-- 新增任务弹窗 -->
    <MdmDialog v-model="dialogVisible" title="执行质量检测" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label">任务名称</div>
        <input v-model="form.taskName" class="mdm-input-yellow" placeholder="请输入任务名称（可选）" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>选择视图</div>
        <select v-model="form.viewId" class="mdm-select">
          <option :value="0">请选择视图</option>
          <option v-for="view in viewList" :key="view.id" :value="view.id">{{ view.viewName }}</option>
        </select>
      </div>
      <div class="form-tip">
        提示：将对所选视图下的所有启用的质量规则进行检测
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSubmit" :disabled="executing">
          {{ executing ? '执行中...' : '执行检测' }}
        </button>
      </template>
    </MdmDialog>

    <!-- 检测结果弹窗 -->
    <MdmDialog v-model="resultDialogVisible" :title="'检测结果 - ' + (currentTask?.taskName || '')" width="900px">
      <!-- 统计信息 -->
      <div class="result-summary" v-if="currentTask">
        <div class="summary-item">
          <span class="summary-label">检测记录</span>
          <span class="summary-value">{{ currentTask.totalRecords || 0 }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">通过</span>
          <span class="summary-value pass">{{ currentTask.passCount || 0 }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">失败</span>
          <span class="summary-value fail">{{ currentTask.failCount || 0 }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">通过率</span>
          <span class="summary-value" :class="{ 'rate-low': (currentTask.passRate || 100) < 80 }">
            {{ currentTask.passRate != null ? currentTask.passRate.toFixed(1) + '%' : '-' }}
          </span>
        </div>
      </div>

      <!-- 结果列表 -->
      <div class="result-table-wrapper">
        <table class="mdm-data-table" v-loading="resultLoading">
          <thead>
            <tr>
              <th>规则</th>
              <th>实体</th>
              <th>字段</th>
              <th>当前值</th>
              <th>结果</th>
              <th>问题级别</th>
              <th>问题描述</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in resultList" :key="item.id">
              <td>{{ item.ruleName || '-' }}</td>
              <td>
                <span class="entity-tag" :class="item.entityType">
                  {{ item.entityType === 'main' ? '主表' : '子表' }}
                </span>
              </td>
              <td>{{ item.fieldName || item.fieldCode || '-' }}</td>
              <td class="field-value">{{ item.fieldValue || '-' }}</td>
              <td>
                <span class="result-tag" :class="item.isPassed ? 'passed' : 'failed'">
                  {{ item.isPassed ? '通过' : '失败' }}
                </span>
              </td>
              <td>
                <span v-if="item.issueLevel" class="level-tag" :class="getIssueLevelClass(item.issueLevel)">
                  {{ item.issueLevel }}
                </span>
                <span v-else>-</span>
              </td>
              <td>{{ item.issueMessage || '-' }}</td>
            </tr>
            <tr v-if="resultList.length === 0 && !resultLoading">
              <td colspan="7" class="mdm-empty-data">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="resultDialogVisible = false">关闭</button>
      </template>
    </MdmDialog>

    <!-- 确认对话框 -->
    <MdmConfirmDialog
      v-model="confirmVisible"
      :message="confirmMessage"
      @confirm="handleConfirmDelete"
    />
  </div>
</template>

<style scoped>
@import '../../../assets/styles/mdm-common.scss';

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

.pass-count {
  color: #52c41a;
}

.fail-count {
  color: #ff4d4f;
}

.pass-rate {
  font-weight: 600;
  color: #52c41a;
}

.pass-rate.rate-low {
  color: #ff4d4f;
}

.entity-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.entity-tag.main {
  background: #f6ffed;
  color: #52c41a;
}

.entity-tag.sub {
  background: #fff7e6;
  color: #fa8c16;
}

.result-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.result-tag.passed {
  background: #f6ffed;
  color: #52c41a;
}

.result-tag.failed {
  background: #fff2f0;
  color: #ff4d4f;
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

.field-value {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.form-tip {
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.result-summary {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-label {
  font-size: 12px;
  color: #909399;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
}

.summary-value.pass {
  color: #52c41a;
}

.summary-value.fail {
  color: #ff4d4f;
}

.summary-value.rate-low {
  color: #ff4d4f;
}

.result-table-wrapper {
  max-height: 400px;
  overflow-y: auto;
}
</style>
