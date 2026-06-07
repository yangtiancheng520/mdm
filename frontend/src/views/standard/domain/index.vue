<script setup lang="ts">
/**
 * 值域管理页面
 */

import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getValueDomainList,
  createValueDomain,
  updateValueDomain,
  updateValueDomainOptions,
  deleteValueDomain,
  batchDeleteValueDomain,
  validateOptionValue,
  type ValueDomain,
  type ValueDomainForm,
  type DomainStatus,
  type DataType,
  type DomainOption,
  DATA_TYPE_OPTIONS,
  DOMAIN_STATUS_OPTIONS
} from '../../../api/standard/valueDomain'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

// 表格数据
const tableData = ref<ValueDomain[]>([])
const loading = ref(false)
const selectedRows = ref<ValueDomain[]>([])

// 搜索表单
const searchForm = ref({
  domainCode: '',
  domainName: '',
  dataType: '' as DataType | '',
  status: '' as DomainStatus | ''
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增值域')
const optionsDialogVisible = ref(false)

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单数据
const form = ref<ValueDomainForm>({
  domainCode: '',
  domainName: '',
  dataType: 'string',
  dataLength: null,
  options: [],
  status: '启用',
  description: ''
})

// 当前编辑的选项
const editingOptions = ref<DomainOption[]>([])
// 当前编辑的值域ID
const editingDomainId = ref<number | null>(null)

// 拖拽相关
const draggedIndex = ref<number | null>(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 计算类型长度是否需要显示
const needLength = computed(() => {
  return form.value.dataType === 'string'
})

// 获取类型标签
function getDataTypeLabel(type: DataType) {
  const option = DATA_TYPE_OPTIONS.find(o => o.value === type)
  return option?.label || type
}

// 获取状态标签
function getStatusLabel(status: DomainStatus) {
  const option = DOMAIN_STATUS_OPTIONS.find(o => o.value === status)
  return option?.label || status
}

// 获取状态样式
const getStatusClass = (status: DomainStatus) => {
  return status === '启用' ? 'status-active' : 'status-inactive'
}

// 获取值域列表
async function fetchData() {
  loading.value = true
  try {
    const params = {
      ...searchForm.value,
      page: currentPage.value,
      size: pageSize.value
    }
    const res = await getValueDomainList(params)
    if (res && res.data) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
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
    domainCode: '',
    domainName: '',
    dataType: '',
    status: ''
  }
  currentPage.value = 1
  fetchData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增值域'
  form.value = {
    domainCode: '',
    domainName: '',
    dataType: 'string',
    dataLength: null,
    options: [],
    status: '启用',
    description: ''
  }
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: ValueDomain) {
  dialogTitle.value = '编辑值域'
  form.value = {
    id: row.id,
    domainCode: row.domainCode,
    domainName: row.domainName,
    dataType: row.dataType,
    dataLength: row.dataLength,
    options: row.options || [],
    status: row.status,
    description: row.description || ''
  }
  dialogVisible.value = true
}

// 保存（新增/编辑基础信息）
async function handleSave() {
  // 验证基础信息
  if (!form.value.domainCode) {
    ElMessage.warning('请输入值域编码')
    return
  }
  if (!form.value.domainName) {
    ElMessage.warning('请输入值域名称')
    return
  }
  if (needLength.value && !form.value.dataLength) {
    ElMessage.warning('请输入长度')
    return
  }

  // 编辑时校验已有选项值是否符合新的长度要求
  if (form.value.id && form.value.options && form.value.options.length > 0) {
    for (const opt of form.value.options) {
      const result = validateOptionValue(opt.value, form.value.dataType, form.value.dataLength)
      if (!result.valid) {
        ElMessage.warning(`选项值"${opt.value}"不符合要求：${result.message}`)
        return
      }
    }
  }

  try {
    if (form.value.id) {
      await updateValueDomain(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createValueDomain(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 维护值域项
function handleManageOptions(row: ValueDomain) {
  editingDomainId.value = row.id
  // 设置当前值域的数据类型和长度
  form.value.dataType = row.dataType
  form.value.dataLength = row.dataLength
  editingOptions.value = row.options && row.options.length > 0
    ? row.options.map((opt, idx) => ({ ...opt, sort: idx + 1 }))
    : [{ value: '', sort: 1 }]
  optionsDialogVisible.value = true
}

// 添加选项
function handleAddOption() {
  editingOptions.value.push({ value: '', sort: editingOptions.value.length + 1 })
}

// 删除选项
function handleRemoveOption(index: number) {
  editingOptions.value.splice(index, 1)
  // 重新计算排序号
  editingOptions.value.forEach((opt, idx) => opt.sort = idx + 1)
}

// 拖拽开始
function handleDragStart(index: number) {
  draggedIndex.value = index
}

// 拖拽经过
function handleDragOver(e: DragEvent, index: number) {
  e.preventDefault()
}

// 拖拽放下
function handleDrop(index: number) {
  if (draggedIndex.value === null || draggedIndex.value === index) return

  const item = editingOptions.value[draggedIndex.value]
  editingOptions.value.splice(draggedIndex.value, 1)
  editingOptions.value.splice(index, 0, item)

  // 重新计算排序号
  editingOptions.value.forEach((opt, idx) => opt.sort = idx + 1)
  draggedIndex.value = null
}

// 拖拽结束
function handleDragEnd() {
  draggedIndex.value = null
}

// 提交选项
async function handleSubmitOptions() {
  // 过滤空选项
  const validOptions = editingOptions.value.filter(opt => opt.value)

  if (validOptions.length === 0) {
    ElMessage.warning('请至少添加一个有效选项')
    return
  }

  // 校验所有选项值
  for (const opt of validOptions) {
    const result = validateOptionValue(opt.value, form.value.dataType, form.value.dataLength)
    if (!result.valid) {
      ElMessage.warning(`选项值"${opt.value}"不合法：${result.message}`)
      return
    }
  }

  try {
    // 使用专门的更新值域项接口
    await updateValueDomainOptions(editingDomainId.value!, validOptions)
    ElMessage.success('保存成功')
    optionsDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 删除
function handleDelete(row: ValueDomain) {
  confirmMessage.value = `确定要删除值域「${row.domainName}」吗？`
  confirmAction.value = async () => {
    try {
      await deleteValueDomain(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
  confirmVisible.value = true
}

// 批量删除
function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的值域')
    return
  }

  confirmMessage.value = `确定要删除选中的 ${selectedRows.value.length} 个值域吗？`
  confirmAction.value = async () => {
    try {
      await batchDeleteValueDomain(selectedRows.value.map(r => r.id))
      ElMessage.success('删除成功')
      selectedRows.value = []
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
  confirmVisible.value = true
}

// 确认删除
function handleConfirmDelete() {
  if (confirmAction.value) {
    confirmAction.value()
  }
}

// 选择变化
function handleSelectionChange(rows: ValueDomain[]) {
  selectedRows.value = rows
}

// 全选切换
function toggleSelectAll(e: Event) {
  const checked = (e.target as HTMLInputElement).checked
  if (checked) {
    selectedRows.value = [...tableData.value]
  } else {
    selectedRows.value = []
  }
}

// 检查是否全选
const isAllSelected = computed(() => {
  return tableData.value.length > 0 && selectedRows.value.length === tableData.value.length
})

// 检查行是否选中
function isRowSelected(row: ValueDomain) {
  return selectedRows.value.some(r => r.id === row.id)
}

// 切换行选择
function toggleRowSelect(row: ValueDomain) {
  const index = selectedRows.value.findIndex(r => r.id === row.id)
  if (index > -1) {
    selectedRows.value.splice(index, 1)
  } else {
    selectedRows.value.push(row)
  }
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

// 类型变化时重置长度
function handleDataTypeChange() {
  if (form.value.dataType === 'string') {
    form.value.dataLength = 1
  } else {
    form.value.dataLength = null
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="value-domain-page">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <input v-model="searchForm.domainCode" type="text" placeholder="值域编码" @keyup.enter="handleSearch" />
        </div>
        <div class="mdm-filter-item">
          <input v-model="searchForm.domainName" type="text" placeholder="值域名称" @keyup.enter="handleSearch" />
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option v-for="opt in DOMAIN_STATUS_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-red" @click="handleAdd">+ 新增</button>
        <button class="mdm-btn-outline" @click="handleSearch">查询</button>
        <button class="mdm-btn-outline" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="mdm-table-wrapper">
      <table class="mdm-data-table">
        <thead>
          <tr>
            <th style="width: 40px">
              <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" />
            </th>
            <th>值域编码</th>
            <th>值域名称</th>
            <th>类型</th>
            <th>长度</th>
            <th>选项数</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-loading="loading">
          <tr v-for="row in tableData" :key="row.id">
            <td>
              <input type="checkbox" :checked="isRowSelected(row)" @change="toggleRowSelect(row)" />
            </td>
            <td>{{ row.domainCode }}</td>
            <td>{{ row.domainName }}</td>
            <td>{{ getDataTypeLabel(row.dataType) }}</td>
            <td>{{ row.dataLength || '-' }}</td>
            <td>{{ row.options?.length || 0 }}</td>
            <td>
              <span class="custom-status-tag" :class="getStatusClass(row.status)">
                {{ getStatusLabel(row.status) }}
              </span>
            </td>
            <td>{{ row.createdAt }}</td>
            <td>
              <div class="mdm-action-buttons">
                <button class="mdm-action-btn" @click="handleEdit(row)">编辑</button>
                <button class="mdm-action-btn" @click="handleManageOptions(row)">值域项</button>
                <button class="mdm-action-btn delete" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="tableData.length === 0 && !loading">
            <td colspan="9" class="mdm-empty-data">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="mdm-pagination">
      <span class="mdm-pagination-total">共 {{ total }} 条</span>
      <button class="mdm-page-btn" @click="handlePageChange(1)">◀◀</button>
      <button class="mdm-page-btn" :disabled="currentPage === 1" @click="handlePageChange(currentPage - 1)">‹</button>
      <button class="mdm-page-btn active">{{ currentPage }}</button>
      <button class="mdm-page-btn" :disabled="currentPage >= Math.ceil(total / pageSize)" @click="handlePageChange(currentPage + 1)">›</button>
      <button class="mdm-page-btn" @click="handlePageChange(Math.ceil(total / pageSize))">▶▶</button>
      <select class="mdm-page-select" v-model="pageSize" @change="handleSizeChange(pageSize)">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>

    <!-- 新增/编辑弹窗 - 基础信息 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>值域编码</div>
        <input v-model="form.domainCode" class="mdm-input-yellow" placeholder="请输入值域编码" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>值域名称</div>
        <input v-model="form.domainName" class="mdm-input-yellow" placeholder="请输入值域名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>类型</div>
        <select v-model="form.dataType" class="mdm-select" @change="handleDataTypeChange">
          <option v-for="opt in DATA_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row" v-if="needLength">
        <div class="mdm-form-label required"><em>*</em>长度</div>
        <input v-model.number="form.dataLength" type="number" class="mdm-input-normal" placeholder="请输入长度" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">状态</div>
        <select v-model="form.status" class="mdm-select">
          <option v-for="opt in DOMAIN_STATUS_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="form.description" class="mdm-textarea" placeholder="请输入描述"></textarea>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSave">保存</button>
      </template>
    </MdmDialog>

    <!-- 值域项配置弹窗 -->
    <MdmDialog v-model="optionsDialogVisible" title="维护值域项" width="500px">
      <div class="options-table">
        <div class="options-header">
          <span class="col-index"></span>
          <span class="col-value">值</span>
          <span class="col-action">操作</span>
        </div>
        <div class="options-body">
          <div
            class="options-row"
            v-for="(opt, index) in editingOptions"
            :key="index"
            draggable="true"
            @dragstart="handleDragStart(index)"
            @dragover="handleDragOver($event, index)"
            @drop="handleDrop(index)"
            @dragend="handleDragEnd"
          >
            <span class="col-index-num">{{ index + 1 }}</span>
            <input v-model="opt.value" class="col-value-input" placeholder="值" />
            <div class="col-action-btns">
              <span class="drag-handle" title="拖拽排序">⋮⋮</span>
              <button class="mdm-btn-outline-sm" @click="handleRemoveOption(index)">删除</button>
            </div>
          </div>
        </div>
        <button class="mdm-btn-outline" @click="handleAddOption" style="margin-top: 10px;">+ 添加选项</button>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="optionsDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSubmitOptions">保存</button>
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

<style scoped lang="scss">
@use '../../../assets/styles/mdm-common.scss' as *;

.value-domain-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

// 状态标签样式
.custom-status-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
  border: 1px solid;
}

.status-active {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
}

.status-inactive {
  background: #fff5f5;
  border-color: #ffa39e;
  color: #ed2b33;
}

// 选项配置样式
.options-table {
  .options-header {
    display: flex;
    padding: 10px 0;
    border-bottom: 1px solid #e8e8e8;
    font-weight: 600;
    color: #333;

    .col-index { width: 40px; padding-left: 5px; }
    .col-value { flex: 1; padding-left: 10px; }
    .col-action { width: 100px; text-align: center; }
  }

  .options-body {
    max-height: 300px;
    overflow-y: auto;
  }

  .options-row {
    display: flex;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
    align-items: center;
    cursor: move;

    &:hover {
      background: #fafafa;
    }

    .col-index-num {
      width: 40px;
      text-align: center;
      color: #666;
      font-size: 13px;
    }

    .col-value-input {
      flex: 1;
      padding: 6px 8px;
      margin-right: 10px;
    }

    .col-action-btns {
      width: 100px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }

    .drag-handle {
      color: #999;
      cursor: grab;
      font-size: 14px;
    }
  }
}

// 按钮间距
.mdm-right-group {
  gap: 8px;
}

// 分页按钮禁用样式
.mdm-page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.mdm-btn-outline-sm {
  padding: 4px 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: #fff;
  color: #666;
  cursor: pointer;
  font-size: 12px;

  &:hover {
    border-color: #ed2b33;
    color: #ed2b33;
  }
}
</style>
