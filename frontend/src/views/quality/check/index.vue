<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTaskList,
  executeCheck,
  deleteTask,
  getResults,
  TASK_STATUS_OPTIONS,
  type QualityCheckTask,
  type QualityCheckResult,
  type CheckRecord
} from '../../../api/quality/check'
import { getViewList, getViewDetail, type ViewDefinition } from '../../../api/standard/viewDefinition'
import { getFormList, type FormDto } from '../../../api/form'
import { getInstanceList } from '../../../api/data/instance'
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

// 表单列表
const formList = ref<FormDto[]>([])

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

// 执行检测相关
const selectedFormId = ref<number | null>(null)
const selectedViewDetail = ref<ViewDefinition | null>(null)  // 当前选中表单关联的视图详情
const dataLoading = ref(false)
const dataList = ref<any[]>([])

// 数据筛选
const dataSearchForm = ref({
  status: '',
  keyword: ''
})

// 已选中的记录ID
const selectedRecordIds = ref<number[]>([])

// 待检列表
const pendingCheckList = ref<CheckRecord[]>([])

// 任务名称
const taskName = ref('')

// 执行中
const executing = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 数据状态选项
const DATA_STATUS_OPTIONS = [
  { value: 'PENDING_QC', label: '待质检' },
  { value: 'ACTIVE_QUALIFIED', label: '已生效-合格' },
  { value: 'ACTIVE_UNQUALIFIED', label: '已生效-不合格' },
  { value: 'DRAFT', label: '草稿' },
  { value: 'OBSOLETE', label: '已作废' }
]

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

// 获取表单列表
async function fetchFormList() {
  try {
    const res = await getFormList({ status: 'published' })
    formList.value = res.data || []
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
  selectedFormId.value = null
  selectedViewDetail.value = null
  dataList.value = []
  selectedRecordIds.value = []
  pendingCheckList.value = []
  taskName.value = ''
  dataSearchForm.value = { status: '', keyword: '' }
  dialogVisible.value = true
}

// 获取选中表单的视图ID
function getSelectedViewId(): number | null {
  const form = formList.value.find(f => f.id === selectedFormId.value)
  return form?.viewId || null
}

// 选择表单后，加载视图详情并设置默认任务名称
async function handleFormChange() {
  console.log('handleFormChange 被调用, selectedFormId:', selectedFormId.value)
  console.log('formList:', formList.value.map(f => ({ id: f.id, formName: f.formName })))

  dataList.value = []
  selectedRecordIds.value = []
  selectedViewDetail.value = null

  if (selectedFormId.value) {
    // 设置默认任务名称：表单名+日期
    const form = formList.value.find(f => f.id === selectedFormId.value)
    console.log('找到的表单:', form)
    if (form) {
      taskName.value = `${form.formName}_${new Date().toISOString().slice(0, 10)}`
      console.log('设置任务名称:', taskName.value)
    }

    // 加载视图详情（获取实体和表名信息）
    const viewId = getSelectedViewId()
    if (viewId) {
      try {
        const res = await getViewDetail(viewId)
        selectedViewDetail.value = res.data || null
        console.log('视图详情:', selectedViewDetail.value)
      } catch (error) {
        console.error('加载视图详情失败', error)
      }
    }
  } else {
    taskName.value = ''
  }
}

// 加载表单数据
async function loadDataList() {
  if (!selectedFormId.value) {
    ElMessage.warning('请先选择表单')
    return
  }

  dataLoading.value = true
  try {
    // 使用 formId 查询数据
    const res = await getInstanceList({
      formId: selectedFormId.value,
      status: dataSearchForm.value.status || undefined
    })
    let data = res.data || []

    // 关键字搜索
    if (dataSearchForm.value.keyword) {
      const keyword = dataSearchForm.value.keyword.toLowerCase()
      data = data.filter((item: any) =>
        (item.code && String(item.code).toLowerCase().includes(keyword)) ||
        (item.name && String(item.name).toLowerCase().includes(keyword))
      )
    }

    dataList.value = data

    if (data.length === 0) {
      ElMessage.info('该表单没有数据')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
    dataList.value = []
  } finally {
    dataLoading.value = false
  }
}

// 全选/取消全选
function toggleSelectAll() {
  if (selectedRecordIds.value.length === dataList.value.length) {
    selectedRecordIds.value = []
  } else {
    selectedRecordIds.value = dataList.value.map((d: any) => d.id)
  }
}

// 获取主表表名
function getMainTableName(): string {
  if (selectedViewDetail.value?.entities) {
    const mainEntity = selectedViewDetail.value.entities.find(e => e.entityType === 'main')
    return mainEntity?.tableName || ''
  }
  return selectedViewDetail.value?.tableName || ''
}

// 添加到待检列表
function addToCheckList() {
  if (selectedRecordIds.value.length === 0) {
    ElMessage.warning('请先勾选要检测的数据')
    return
  }

  const viewId = getSelectedViewId()
  if (!viewId) {
    ElMessage.warning('无法获取视图信息')
    return
  }

  // 获取主表表名
  const tableName = getMainTableName()
  const form = formList.value.find(f => f.id === selectedFormId.value)
  console.log('添加待检列表 - 表单:', form?.formName, '视图ID:', viewId, '主表:', tableName)

  let addedCount = 0
  selectedRecordIds.value.forEach(recordId => {
    // 检查是否已存在
    const exists = pendingCheckList.value.some(
      r => r.viewId === viewId && r.recordId === recordId
    )
    if (!exists) {
      pendingCheckList.value.push({
        viewId: viewId,
        tableName: tableName,
        recordId: recordId
      })
      addedCount++
    }
  })

  if (addedCount > 0) {
    ElMessage.success(`已添加 ${addedCount} 条数据到待检列表`)
    selectedRecordIds.value = []
  } else {
    ElMessage.info('所选数据已存在于待检列表中')
  }
}

// 从待检列表移除
function removeFromCheckList(index: number) {
  pendingCheckList.value.splice(index, 1)
}

// 清空待检列表
function clearCheckList() {
  pendingCheckList.value = []
}

// 获取记录在待检列表中的显示名称
function getRecordDisplay(item: CheckRecord): string {
  // 通过viewId找到对应的表单
  const form = formList.value.find(f => f.viewId === item.viewId)
  if (form) return form.formName
  // 找不到表单则显示视图名称
  const view = viewList.value.find(v => v.id === item.viewId)
  return view?.viewName || `视图${item.viewId}`
}

// 获取记录编码
function getRecordCode(recordId: number): string {
  const record = dataList.value.find((d: any) => d.id === recordId)
  return record?.code || String(recordId)
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
  if (pendingCheckList.value.length === 0) {
    ElMessage.warning('请添加要检测的数据')
    return
  }

  // 调试日志
  console.log('===== 提交检测 =====')
  console.log('待检列表:', JSON.stringify(pendingCheckList.value, null, 2))
  console.log('视图详情:', selectedViewDetail.value)

  // 生成默认任务名称：表单名+日期
  const form = formList.value.find(f => f.id === selectedFormId.value)
  const defaultTaskName = form
    ? `${form.formName}_${new Date().toISOString().slice(0, 10)}`
    : `质量检测_${new Date().toISOString().slice(0, 10)}`

  executing.value = true
  try {
    const task: QualityCheckTask = {
      taskCode: '',
      taskName: taskName.value || defaultTaskName,
      status: 'pending',
      records: pendingCheckList.value
    }

    console.log('提交的任务:', JSON.stringify(task, null, 2))

    const res = await executeCheck(task)
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

// 获取数据状态标签
function getDataStatusLabel(status: string) {
  const item = DATA_STATUS_OPTIONS.find(s => s.value === status)
  return item?.label || status
}

// 获取数据状态样式类
function getDataStatusClass(status: string) {
  const classMap: Record<string, string> = {
    PENDING_QC: 'status-pending-qc',
    ACTIVE_QUALIFIED: 'status-qualified',
    ACTIVE_UNQUALIFIED: 'status-unqualified',
    DRAFT: 'status-draft',
    OBSOLETE: 'status-obsolete'
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

// 获取视图名称
function getViewName(viewId: number | undefined): string {
  if (!viewId) return '-'
  const view = viewList.value.find(v => v.id === viewId)
  return view?.viewName || '-'
}

onMounted(() => {
  fetchData()
  fetchViewList()
  fetchFormList()
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
            <td>{{ getViewName(row.viewId) }}</td>
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

    <!-- 执行检测弹窗 -->
    <MdmDialog v-model="dialogVisible" title="执行质量检测" width="900px">
      <!-- 步骤1：选择表单和数据 -->
      <div class="check-step">
        <div class="step-title">步骤1：选择表单和数据</div>
        <div class="step-content">
          <div class="view-select-row">
            <select v-model="selectedFormId" class="mdm-select" style="width: 200px" @change="handleFormChange">
              <option :value="null">请选择表单</option>
              <option v-for="form in formList" :key="form.id" :value="form.id">{{ form.formName }}</option>
            </select>
            <button class="mdm-btn-primary" @click="loadDataList" :disabled="!selectedFormId || dataLoading">
              {{ dataLoading ? '加载中...' : '加载数据' }}
            </button>
          </div>

          <!-- 数据筛选和列表 -->
          <div v-if="dataList.length > 0" class="data-section">
            <div class="data-filter-row">
              <select v-model="dataSearchForm.status" class="mdm-select" style="width: 120px">
                <option value="">全部状态</option>
                <option v-for="item in DATA_STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
              <input
                v-model="dataSearchForm.keyword"
                class="mdm-input-yellow"
                style="width: 180px"
                placeholder="搜索编码/名称"
                @keyup.enter="loadDataList"
              />
              <button class="mdm-btn-outline" @click="loadDataList">查询</button>
            </div>

            <div class="data-table-wrapper">
              <table class="mdm-data-table">
                <thead>
                  <tr>
                    <th style="width: 40px">
                      <input type="checkbox" :checked="selectedRecordIds.length === dataList.length && dataList.length > 0" @change="toggleSelectAll" />
                    </th>
                    <th>记录ID</th>
                    <th>编码</th>
                    <th>名称</th>
                    <th>状态</th>
                    <th>创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in dataList" :key="item.id">
                    <td><input type="checkbox" :value="item.id" v-model="selectedRecordIds" /></td>
                    <td>{{ item.id }}</td>
                    <td>{{ item.code || '-' }}</td>
                    <td>{{ item.name || '-' }}</td>
                    <td>
                      <span class="status-tag" :class="getDataStatusClass(item.status)">
                        {{ getDataStatusLabel(item.status) }}
                      </span>
                    </td>
                    <td>{{ item.created_at ? item.created_at.slice(0, 10) : '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="data-footer">
              <span class="selected-count">已选 {{ selectedRecordIds.length }} 条</span>
              <button class="mdm-btn-primary" @click="addToCheckList" :disabled="selectedRecordIds.length === 0">
                添加到待检列表
              </button>
            </div>
          </div>

          <!-- 无数据提示 -->
          <div v-else-if="selectedFormId && !dataLoading" class="empty-tip">
            请点击"加载数据"查看表单数据
          </div>
        </div>
      </div>

      <!-- 步骤2：待检列表 -->
      <div class="check-step">
        <div class="step-title">步骤2：待检列表（共 {{ pendingCheckList.length }} 条）</div>
        <div class="step-content">
          <div v-if="pendingCheckList.length > 0" class="pending-table-wrapper">
            <table class="mdm-data-table">
              <thead>
                <tr>
                  <th>表单名称</th>
                  <th>记录ID</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in pendingCheckList" :key="index">
                  <td>{{ getRecordDisplay(item) }}</td>
                  <td>{{ item.recordId }}</td>
                  <td>
                    <button class="mdm-action-btn delete" @click="removeFromCheckList(index)">移除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-tip">请从上方添加要检测的数据</div>
          <div v-if="pendingCheckList.length > 0" class="pending-footer">
            <button class="mdm-btn-outline" @click="clearCheckList">清空待检列表</button>
          </div>
        </div>
      </div>

      <!-- 任务名称 -->
      <div class="mdm-form-row" style="margin-top: 16px">
        <div class="mdm-form-label">任务名称</div>
        <input v-model="taskName" class="mdm-input-yellow" placeholder="请输入任务名称（可选）" />
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSubmit" :disabled="executing || pendingCheckList.length === 0">
          {{ executing ? '检测中...' : '开始检测' }}
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

/* 数据状态样式 */
.status-tag.status-pending-qc {
  background: #fff7e6;
  color: #fa8c16;
}

.status-tag.status-qualified {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.status-unqualified {
  background: #fff2f0;
  color: #ff4d4f;
}

.status-tag.status-draft {
  background: #f0f0f0;
  color: #666;
}

.status-tag.status-obsolete {
  background: #f5f5f5;
  color: #999;
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

/* 执行检测弹窗样式 */
.check-step {
  margin-bottom: 16px;
}

.step-title {
  font-weight: 600;
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #1890ff;
}

.step-content {
  padding: 12px;
  background: #f9fafb;
  border-radius: 4px;
}

.view-select-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.data-section {
  margin-top: 12px;
}

.data-filter-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.data-table-wrapper {
  max-height: 250px;
  overflow-y: auto;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.data-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e8e8e8;
}

.selected-count {
  color: #666;
  font-size: 13px;
}

.pending-table-wrapper {
  max-height: 200px;
  overflow-y: auto;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.pending-footer {
  margin-top: 12px;
  text-align: right;
}

.empty-tip {
  text-align: center;
  padding: 24px;
  color: #999;
  font-size: 13px;
}
</style>
