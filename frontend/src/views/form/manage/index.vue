<template>
  <div class="form-manage-page">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <input v-model="searchForm.formCode" type="text" placeholder="表单编码" @keyup.enter="handleSearch" />
        </div>
        <div class="mdm-filter-item">
          <input v-model="searchForm.formName" type="text" placeholder="表单名称" @keyup.enter="handleSearch" />
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="draft">草稿</option>
            <option value="published">已发布</option>
          </select>
        </div>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-red" @click="handleAdd">+ 新增</button>
        <button class="mdm-btn-outline" @click="handleSearch">查询</button>
        <button class="mdm-btn-outline" @click="handleReset">重置</button>
        <button class="mdm-btn-outline" @click="handleHistorySelected">历史版本</button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="mdm-table-wrapper">
      <table class="mdm-data-table">
        <thead>
          <tr>
            <th style="width: 40px">
              <input type="checkbox" @change="handleSelectAll" :checked="selectedRows.length === tableData.length && tableData.length > 0" />
            </th>
            <th>表单编码</th>
            <th>表单名称</th>
            <th>关联视图</th>
            <th>版本</th>
            <th>状态</th>
            <th>更新时间</th>
            <th style="text-align: center">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in tableData" :key="row.id">
            <td><input type="checkbox" :checked="selectedRows.some(r => r.id === row.id)" @change="handleSelectRow(row)" /></td>
            <td>{{ row.formCode }}</td>
            <td>{{ row.formName }}</td>
            <td>{{ row.viewName || '-' }}</td>
            <td><span class="version-tag">V{{ row.version || 1 }}</span></td>
            <td>
              <span :class="['status-tag', row.status]">{{ row.status === 'published' ? '已发布' : '草稿' }}</span>
            </td>
            <td>{{ formatDateTime(row.updatedAt) }}</td>
            <td>
              <div class="action-btns">
                <button v-if="row.status === 'draft'" class="action-btn design" @click="handleDesign(row)">设计</button>
                <button v-if="row.status === 'draft'" class="action-btn edit" @click="handleEdit(row)">编辑</button>
                <button v-if="row.status === 'published'" class="action-btn view" @click="handleView(row)">查看</button>
                <button v-if="row.status === 'draft'" class="action-btn publish" @click="handlePublish(row)">发布</button>
                <button v-if="row.status === 'published'" class="action-btn unpublish" @click="handleUnpublish(row)">取消发布</button>
                <button v-if="row.status === 'draft'" class="action-btn delete" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="tableData.length === 0">
            <td colspan="8" class="empty-row">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>表单编码</div>
        <input v-model="form.formCode" class="mdm-input-yellow" :disabled="isEdit" placeholder="请输入表单编码（字母、数字、下划线）" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>表单名称</div>
        <input v-model="form.formName" class="mdm-input-yellow" placeholder="请输入表单名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>关联视图</div>
        <select v-model="form.viewId" class="mdm-select">
          <option :value="undefined">请选择关联视图</option>
          <option v-for="view in viewList" :key="view.id" :value="view.id">
            {{ view.viewName }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="form.description" class="mdm-textarea" rows="3" placeholder="请输入描述"></textarea>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSubmit" :disabled="submitting">确定</button>
      </template>
    </MdmDialog>

    <!-- 确认对话框 -->
    <MdmConfirmDialog
      v-model="confirmVisible"
      :message="confirmMessage"
      @confirm="handleConfirm"
    />

    <!-- 历史版本弹窗 -->
    <MdmDialog v-model="logDialogVisible" title="历史版本" width="700px">
      <div class="log-timeline-container">
        <!-- 当前版本信息 -->
        <div v-if="currentFormRow" class="current-version-info">
          <span class="label">当前版本：</span>
          <el-tag type="success" size="default">V{{ currentFormRow.version || 1 }}</el-tag>
          <span class="status-text">（{{ currentFormRow.status === 'published' ? '已发布' : '草稿' }}）</span>
        </div>
        <el-divider />
        <el-timeline v-if="formLogs.length > 0">
          <el-timeline-item
            v-for="log in formLogs"
            :key="log.id"
            :timestamp="formatDateTime(log.createdAt)"
            :type="getLogColor(log.operationType)"
            :icon="getLogIcon(log.operationType)"
            placement="top"
          >
            <div
              class="log-item"
              :class="{ 'log-item-selected': selectedLogId === log.id }"
              @click="selectedLogId = log.id"
            >
              <!-- 用户 + 操作 -->
              <div class="log-header">
                <span class="log-user">{{ log.createdBy }}</span>
                <span class="log-action">{{ getActionText(log) }}</span>
              </div>

              <!-- 版本变更 -->
              <div v-if="log.versionDisplay" class="log-version">
                <el-tag type="info" size="small">{{ log.versionDisplay }}</el-tag>
              </div>

              <!-- 状态变更 -->
              <div v-if="log.statusDisplay" class="log-status-change">
                <el-tag :type="getStatusColor(log.fromStatus)" size="small">
                  {{ getStatusLabel(log.fromStatus) }}
                </el-tag>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                <el-tag :type="getStatusColor(log.toStatus)" size="small">
                  {{ getStatusLabel(log.toStatus) }}
                </el-tag>
              </div>

              <!-- 变更详情 -->
              <div v-if="log.changeData && log.operationType === 'update'" class="log-detail">
                <div class="detail-text">{{ getChangeSummary(log.changeData) }}</div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无操作历史" :image-size="80" />
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="logDialogVisible = false">关闭</button>
      </template>
    </MdmDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Check, Close, Edit, Delete, Plus, Promotion, DocumentChecked } from '@element-plus/icons-vue'
import MdmDialog from '@/components/MdmDialog.vue'
import MdmConfirmDialog from '@/components/MdmConfirmDialog.vue'
import {
  getFormList,
  createForm,
  updateForm,
  deleteForm,
  publishForm,
  unpublishForm,
  getFormLogs,
  type FormDto,
  type FormDesignRequest,
  type FormLogDto
} from '@/api/form'
import { getViewList, type ViewDefinition } from '@/api/standard/viewDefinition'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  formCode: '',
  formName: '',
  status: ''
})

// 表格数据
const tableData = ref<FormDto[]>([])
const selectedRows = ref<FormDto[]>([])

// 视图列表
const viewList = ref<ViewDefinition[]>([])

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑表单' : '新增表单')
const isEdit = ref(false)
const submitting = ref(false)

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const pendingAction = ref<'publish' | 'unpublish' | 'delete' | null>(null)
const pendingRow = ref<FormDto | null>(null)

// 日志弹窗
const logDialogVisible = ref(false)
const formLogs = ref<FormLogDto[]>([])
const selectedLogId = ref<number | null>(null)
const currentFormRow = ref<FormDto | null>(null)

// 表单数据
const form = reactive<FormDesignRequest>({
  id: undefined,
  formCode: '',
  formName: '',
  formType: 'create',
  viewId: undefined,
  description: ''
})

// 格式化日期时间
const formatDateTime = (dateStr: string | undefined) => {
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

// 全选/取消全选
const handleSelectAll = () => {
  if (selectedRows.value.length === tableData.value.length) {
    selectedRows.value = []
  } else {
    selectedRows.value = [...tableData.value]
  }
}

// 选择单行
const handleSelectRow = (row: FormDto) => {
  const index = selectedRows.value.findIndex(r => r.id === row.id)
  if (index > -1) {
    selectedRows.value.splice(index, 1)
  } else {
    selectedRows.value.push(row)
  }
}

// 加载视图列表
async function loadViewList() {
  try {
    const res = await getViewList({ status: 'published' })
    viewList.value = res.data || []
  } catch (error) {
    console.error('加载视图列表失败', error)
  }
}

// 加载表单列表
async function loadData() {
  try {
    const res = await getFormList({
      formName: searchForm.formName || undefined,
      status: searchForm.status || undefined
    })
    let data = res.data || []

    // 前端过滤表单编码
    if (searchForm.formCode) {
      data = data.filter(item => item.formCode.includes(searchForm.formCode))
    }

    // 后端已返回视图名称，无需前端匹配
    tableData.value = data
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 搜索
function handleSearch() {
  loadData()
}

// 重置
function handleReset() {
  searchForm.formCode = ''
  searchForm.formName = ''
  searchForm.status = ''
  loadData()
}

// 新增
function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: FormDto) {
  isEdit.value = true
  resetForm()
  form.id = row.id
  form.formCode = row.formCode
  form.formName = row.formName
  form.formType = row.formType || 'create'
  form.viewId = row.viewId
  form.description = row.description
  dialogVisible.value = true
}

// 设计表单
function handleDesign(row: FormDto) {
  router.push(`/form/design/${row.id}`)
}

// 查看表单
function handleView(row: FormDto) {
  router.push(`/form/design/${row.id}?mode=view`)
}

// 重置表单
function resetForm() {
  form.id = undefined
  form.formCode = ''
  form.formName = ''
  form.formType = 'create'
  form.viewId = undefined
  form.description = ''
}

// 提交
async function handleSubmit() {
  // 表单验证
  if (!form.formCode) {
    ElMessage.warning('请输入表单编码')
    return
  }
  if (!/^[A-Za-z0-9_]+$/.test(form.formCode)) {
    ElMessage.warning('表单编码只能包含字母、数字和下划线')
    return
  }
  if (!form.formName) {
    ElMessage.warning('请输入表单名称')
    return
  }
  if (!form.viewId) {
    ElMessage.warning('请选择关联视图')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateForm(form.id!, form)
      ElMessage.success('修改成功')
    } else {
      await createForm(form)
      ElMessage.success('新增成功，表单已保存为草稿')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(isEdit.value ? '修改失败' : '新增失败')
  } finally {
    submitting.value = false
  }
}

// 发布
function handlePublish(row: FormDto) {
  pendingAction.value = 'publish'
  pendingRow.value = row
  confirmMessage.value = '确定要发布此表单吗？'
  confirmVisible.value = true
}

// 取消发布
function handleUnpublish(row: FormDto) {
  pendingAction.value = 'unpublish'
  pendingRow.value = row
  confirmMessage.value = '确定要取消发布此表单吗？取消后将退回草稿状态。'
  confirmVisible.value = true
}

// 删除
function handleDelete(row: FormDto) {
  pendingAction.value = 'delete'
  pendingRow.value = row
  confirmMessage.value = '确定要删除此表单吗？'
  confirmVisible.value = true
}

// 确认操作
async function handleConfirm() {
  if (!pendingRow.value) return

  try {
    if (pendingAction.value === 'publish') {
      await publishForm(pendingRow.value.id)
      ElMessage.success('发布成功')
    } else if (pendingAction.value === 'unpublish') {
      await unpublishForm(pendingRow.value.id)
      ElMessage.success('已取消发布，表单退回草稿状态')
    } else if (pendingAction.value === 'delete') {
      await deleteForm(pendingRow.value.id)
      ElMessage.success('删除成功')
    }
    loadData()
  } catch (error) {
    ElMessage.error(pendingAction.value === 'publish' ? '发布失败' : pendingAction.value === 'unpublish' ? '取消发布失败' : '删除失败')
  } finally {
    pendingAction.value = null
    pendingRow.value = null
  }
}

// 查看历史版本（需要先选择一条数据）
function handleHistorySelected() {
  if (selectedRows.value.length !== 1) {
    ElMessage.warning('请先选择一条表单记录')
    return
  }
  handleHistory(selectedRows.value[0])
}

// 查看历史
async function handleHistory(row: FormDto) {
  try {
    const res = await getFormLogs(row.id)
    formLogs.value = res.data || []
    selectedLogId.value = null
    currentFormRow.value = row
    logDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载操作历史失败')
  }
}

// 获取操作动作文本
const getActionText = (log: FormLogDto): string => {
  switch (log.operationType) {
    case 'create':
      return '创建了表单'
    case 'update':
      return '更新了表单基本信息'
    case 'save_design':
      return '保存了表单设计'
    case 'publish':
      return '发布了表单'
    case 'unpublish':
      return '取消了发布'
    case 'delete':
      return '删除了表单'
    default:
      return log.operationDetail || '操作了表单'
  }
}

// 获取日志颜色
const getLogColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'create': 'primary',
    'update': 'primary',
    'save_design': 'primary',
    'delete': 'danger',
    'publish': 'success',
    'unpublish': 'warning'
  }
  return colorMap[type] || 'primary'
}

// 获取日志图标
const getLogIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    'create': Plus,
    'update': Edit,
    'save_design': Edit,
    'delete': Delete,
    'publish': Promotion,
    'unpublish': Close
  }
  return iconMap[type] || Check
}

// 获取状态颜色
const getStatusColor = (status: string | null) => {
  if (!status) return 'info'
  const colorMap: Record<string, string> = {
    'draft': 'info',
    'published': 'success'
  }
  return colorMap[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status: string | null) => {
  if (!status) return '-'
  const labelMap: Record<string, string> = {
    'draft': '草稿',
    'published': '已发布'
  }
  return labelMap[status] || status
}

// 获取变更摘要
const getChangeSummary = (changeData: string): string => {
  if (!changeData) return ''
  try {
    const data = JSON.parse(changeData)
    const fields: string[] = []

    if (data.formName) fields.push('表单名称')
    if (data.formType) fields.push('表单类型')
    if (data.viewId) fields.push('关联视图')
    if (data.description) fields.push('描述')

    if (fields.length === 0) return ''
    return `修改了：${fields.join('、')}`
  } catch {
    return ''
  }
}

onMounted(() => {
  loadViewList() // 用于新增/编辑弹窗的选择框
  loadData()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/mdm-common.scss';

.form-manage-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 分隔线
.btn-divider {
  display: inline-block;
  width: 1px;
  height: 20px;
  background: #d9d9d9;
  margin: 0 12px;
}

// 状态标签
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 2px;
  font-size: 12px;

  &.draft {
    background: #f4f4f5;
    color: #909399;
  }

  &.published {
    background: #f0f9eb;
    color: #67c23a;
  }
}

// 版本标签
.version-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 2px;
  font-size: 12px;
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

// 操作按钮
.action-btns {
  display: flex;
  gap: 6px;
  justify-content: center;

  .action-btn {
    padding: 4px 10px;
    font-size: 14px;
    border: none;
    background: none;
    cursor: pointer;
    border-radius: 4px;

    &.design { color: #ed2b33; font-weight: 500; }
    &.view { color: #1890ff; }
    &.edit { color: #1890ff; }
    &.publish { color: #52c41a; }
    &.unpublish { color: #faad14; }
    &.delete { color: #f56c6c; }

    &:hover {
      background: #f5f5f5;
    }
  }
}

// 日志时间轴容器
.log-timeline-container {
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;

  .current-version-info {
    padding: 12px 16px;
    background: #f0f9eb;
    border-radius: 6px;
    margin-bottom: 16px;

    .label {
      font-weight: 600;
      color: #606266;
      margin-right: 8px;
    }

    .status-text {
      margin-left: 8px;
      color: #909399;
    }
  }

  .log-item {
    padding: 12px 16px;
    background: #f8fafc;
    border-radius: 6px;
    border-left: 3px solid #409eff;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #f0f7ff;
      border-left-color: #66b1ff;
    }

    &.log-item-selected {
      background: #ecf5ff;
      border-left-color: #ed2b33;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

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

    .log-version {
      margin-top: 8px;
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
  }
}

// 空数据行
.empty-row {
  text-align: center;
  color: #94a3b8;
  padding: 40px;
}
</style>
