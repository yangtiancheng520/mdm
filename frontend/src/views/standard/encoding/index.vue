<script setup lang="ts">
/**
 * 编码规则管理页面
 */

import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getEncodingRuleList,
  createEncodingRule,
  updateEncodingRule,
  deleteEncodingRule,
  batchDeleteEncodingRule,
  previewCode,
  type EncodingRule,
  type EncodingRuleForm,
  type RuleDefinition,
  type SegmentConfig,
  SEGMENT_TYPE_OPTIONS,
  RESET_CYCLE_OPTIONS
} from '../../../api/standard/encodingRule'
import {
  getActiveFieldStandardList,
  type FieldStandard
} from '../../../api/standard/fieldStandard'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

// 表格数据
const tableData = ref<EncodingRule[]>([])
const loading = ref(false)
const selectedRows = ref<EncodingRule[]>([])

// 搜索表单
const searchForm = ref({
  ruleCode: '',
  ruleName: '',
  status: ''
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增编码规则')
const segmentDialogVisible = ref(false)
const fieldSearchDialogVisible = ref(false)  // 字段搜索弹窗
const fieldSearchType = ref('')  // 当前搜索的字段类型

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单数据
const form = ref<EncodingRuleForm>({
  ruleCode: '',
  ruleName: '',
  ruleDefinition: {
    segments: []
  },
  status: 'draft',  // 新建时默认为草稿
  example: '',
  description: ''
})

// 当前编辑的规则段
const currentSegment = ref<SegmentConfig | null>(null)
const currentSegmentIndex = ref(-1)

// 预览数据
const previewData = ref<Record<string, any>>({})
const previewResult = ref('')

// 字段标准库列表（用于选择字段）
const fieldStandardList = ref<FieldStandard[]>([])
const fieldSearchKeyword = ref('')

// 过滤后的字段列表
const filteredFieldList = computed(() => {
  if (!fieldSearchKeyword.value) {
    return fieldStandardList.value
  }
  const keyword = fieldSearchKeyword.value.toLowerCase()
  return fieldStandardList.value.filter(field =>
    field.fieldName?.toLowerCase().includes(keyword) ||
    field.fieldCode?.toLowerCase().includes(keyword)
  )
})

// 根据ID获取字段名称
function getFieldNameById(fieldId: number): string {
  const field = fieldStandardList.value.find(f => f.id === fieldId)
  return field ? `${field.fieldName} (${field.fieldCode})` : ''
}

// 打开字段搜索弹窗
function openFieldSearchDialog(type: string) {
  fieldSearchType.value = type
  fieldSearchKeyword.value = ''
  fieldSearchDialogVisible.value = true
}

// 选择字段
function handleSelectField(field: FieldStandard) {
  if (currentSegment.value) {
    currentSegment.value.config.fieldId = field.id
    currentSegment.value.config.fieldCode = field.fieldCode
    currentSegment.value.config.fieldName = field.fieldName
  }
  fieldSearchDialogVisible.value = false
}

// 清除已选字段
function clearSelectedField() {
  if (currentSegment.value) {
    currentSegment.value.config.fieldId = null
    currentSegment.value.config.fieldCode = null
    currentSegment.value.config.fieldName = null
  }
}

// 复制条件规则样例
function copyConditionExample() {
  const example = `[
  {"when":"产成品","then":"5"},
  {"when":"原材料","then":"1"},
  {"when":"半成品","then":"3"}
]`
  navigator.clipboard.writeText(example).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载值域列表
async function loadDomainList() {
  try {
    const res = await getActiveValueDomainList()
    domainList.value = res.data || []
  } catch (error) {
    console.error('加载值域列表失败:', error)
  }
}

// 加载字段标准库列表
async function loadFieldStandardList() {
  try {
    const res = await getActiveFieldStandardList()
    fieldStandardList.value = res.data || []
  } catch (error) {
    console.error('加载字段标准库列表失败:', error)
  }
}

// 获取值域选项
async function loadDomainOptions(domainId: number) {
  if (!domainId || domainOptionsMap.value.has(domainId)) return
  try {
    const res = await getValueDomainById(domainId)
    domainOptionsMap.value.set(domainId, res.data?.options || [])
  } catch (error) {
    console.error('加载值域选项失败:', error)
  }
}

// 获取值域选项列表
function getDomainOptionsById(domainId: number): DomainOption[] {
  return domainOptionsMap.value.get(domainId) || []
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'draft': return '草稿'
    case 'active': return '启用'
    default: return status
  }
}

// 获取状态样式
const getStatusClass = (status: string) => {
  switch (status) {
    case 'draft': return 'status-draft'
    case 'active': return 'status-active'
    default: return ''
  }
}

// 获取规则段类型标签
const getSegmentTypeLabel = (type: string) => {
  const option = SEGMENT_TYPE_OPTIONS.find(o => o.value === type)
  return option?.label || type
}

// 获取列表
async function fetchData() {
  loading.value = true
  try {
    const params = {
      ...searchForm.value,
      page: currentPage.value,
      size: pageSize.value
    }
    const res = await getEncodingRuleList(params)
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
    ruleCode: '',
    ruleName: '',
    status: ''
  }
  currentPage.value = 1
  fetchData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增编码规则'
  form.value = {
    ruleCode: '',
    ruleName: '',
    ruleDefinition: {
      segments: []
    },
    status: 'draft',  // 新建时默认为草稿
    example: '',
    description: ''
  }
  previewData.value = {}
  previewResult.value = ''
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: EncodingRule) {
  dialogTitle.value = '编辑编码规则'
  form.value = {
    id: row.id,
    ruleCode: row.ruleCode,
    ruleName: row.ruleName,
    ruleDefinition: row.ruleDefinition ? JSON.parse(JSON.stringify(row.ruleDefinition)) : { segments: [] },
    scopeType: row.scopeType,
    scopeConfig: row.scopeConfig,
    status: row.status,
    example: row.example,
    description: row.description || ''
  }
  previewData.value = {}
  previewResult.value = ''
  dialogVisible.value = true
}

// 发布规则
async function handlePublish(row: EncodingRule) {
  try {
    await ElMessageBox.confirm('确定要发布该编码规则吗？发布后将无法删除。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateEncodingRule(row.id, { ...row, status: 'active' })
    ElMessage.success('发布成功')
    fetchData()
  } catch {
    // 用户取消
  }
}

// 保存
async function handleSave() {
  if (!form.value.ruleCode) {
    ElMessage.warning('请输入规则编码')
    return
  }
  if (!form.value.ruleName) {
    ElMessage.warning('请输入规则名称')
    return
  }

  try {
    if (form.value.id) {
      await updateEncodingRule(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createEncodingRule(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 删除
function handleDelete(row: EncodingRule) {
  // 启用状态不能删除
  if (row.status === 'active') {
    ElMessage.warning('启用状态的规则不能删除')
    return
  }
  confirmMessage.value = `确定要删除编码规则「${row.ruleName}」吗？`
  confirmAction.value = async () => {
    try {
      await deleteEncodingRule(row.id)
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
    ElMessage.warning('请选择要删除的规则')
    return
  }

  confirmMessage.value = `确定要删除选中的 ${selectedRows.value.length} 个规则吗？`
  confirmAction.value = async () => {
    try {
      await batchDeleteEncodingRule(selectedRows.value.map(r => r.id))
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
function handleSelectionChange(rows: EncodingRule[]) {
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
function isRowSelected(row: EncodingRule) {
  return selectedRows.value.some(r => r.id === row.id)
}

// 切换行选择
function toggleRowSelect(row: EncodingRule) {
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

// ========== 规则段配置 ==========

// 当前选中的类型信息
const currentSegmentType = computed(() => {
  if (!currentSegment.value) return null
  return SEGMENT_TYPE_OPTIONS.find(o => o.value === currentSegment.value?.type)
})

// 类型变更时重置配置
function handleSegmentTypeChange() {
  if (currentSegment.value) {
    currentSegment.value.config = {}
    // 设置默认值
    if (currentSegment.value.type === 'sequence') {
      currentSegment.value.config = {
        length: 4,
        padding: '0',
        start: 1,
        resetCycle: 'never'
      }
    } else if (currentSegment.value.type === 'date') {
      currentSegment.value.config = {
        format: 'yyyyMMdd'
      }
    } else if (currentSegment.value.type === 'conditional') {
      currentSegment.value.config = {
        fieldId: null,
        fieldCode: null,
        fieldName: null,
        rulesJson: '',
        length: 2,
        padding: '0'
      }
    }
  }
}

// 添加规则段
function handleAddSegment() {
  currentSegment.value = {
    type: 'fixed',
    name: '',
    config: {}
  }
  currentSegmentIndex.value = -1
  segmentDialogVisible.value = true
}

// 编辑规则段
function handleEditSegment(index: number) {
  const segment = form.value.ruleDefinition.segments[index]
  currentSegment.value = JSON.parse(JSON.stringify(segment))

  // 条件值：将rules数组转为JSON字符串便于编辑
  if (currentSegment.value.type === 'conditional') {
    if (currentSegment.value.config.rules) {
      currentSegment.value.config.rulesJson = JSON.stringify(currentSegment.value.config.rules, null, 2)
    } else {
      currentSegment.value.config.rulesJson = ''
    }
    // 确保编辑时也有默认值
    if (!currentSegment.value.config.length || currentSegment.value.config.length < 1) {
      currentSegment.value.config.length = 2
    }
    if (!currentSegment.value.config.padding) {
      currentSegment.value.config.padding = '0'
    }
  }

  currentSegmentIndex.value = index
  segmentDialogVisible.value = true
}

// 保存规则段
function handleSaveSegment() {
  if (!currentSegment.value) return

  if (!currentSegment.value.name) {
    ElMessage.warning('请输入段名称')
    return
  }

  // 条件值：必须选择字段且设置条件规则
  if (currentSegment.value.type === 'conditional') {
    if (!currentSegment.value.config.fieldId) {
      ElMessage.warning('请选择字段')
      return
    }
    // 必须输入输出长度且大于0
    if (!currentSegment.value.config.length || currentSegment.value.config.length < 1) {
      ElMessage.warning('输出长度必须大于0')
      return
    }
    // 必须输入默认值
    if (!currentSegment.value.config.defaultValue || String(currentSegment.value.config.defaultValue).trim() === '') {
      ElMessage.warning('请输入默认值')
      return
    }
    // 验证默认值长度不能大于输出长度
    if (currentSegment.value.config.defaultValue) {
      const defaultLen = String(currentSegment.value.config.defaultValue).length
      if (defaultLen > currentSegment.value.config.length) {
        ElMessage.warning(`默认值长度(${defaultLen})不能大于输出长度(${currentSegment.value.config.length})`)
        return
      }
    }
    // 必须输入条件规则
    if (!currentSegment.value.config.rulesJson || currentSegment.value.config.rulesJson.trim() === '') {
      ElMessage.warning('请设置条件规则')
      return
    }
    // 解析JSON字符串为数组
    try {
      const rules = JSON.parse(currentSegment.value.config.rulesJson)
      if (!Array.isArray(rules) || rules.length === 0) {
        ElMessage.warning('条件规则不能为空')
        return
      }
      // 验证每个规则格式
      for (const rule of rules) {
        if (!rule.when || rule.then === undefined || rule.then === null || rule.then === '') {
          ElMessage.warning('条件规则格式错误：每条规则必须包含 when 和 then')
          return
        }
        // 验证then值的长度不能大于输出长度
        const thenLen = String(rule.then).length
        if (thenLen > currentSegment.value.config.length) {
          ElMessage.warning(`条件规则中"${rule.when}"的输出值长度(${thenLen})不能大于输出长度(${currentSegment.value.config.length})`)
          return
        }
      }
      currentSegment.value.config.rules = rules
    } catch (e) {
      ElMessage.warning('条件规则JSON格式错误')
      return
    }
  }

  if (currentSegmentIndex.value === -1) {
    form.value.ruleDefinition.segments.push(currentSegment.value)
  } else {
    form.value.ruleDefinition.segments[currentSegmentIndex.value] = currentSegment.value
  }

  segmentDialogVisible.value = false

  // 实时更新预览
  updatePreview()
}

// 实时预览 - 本地模拟生成
function updatePreview() {
  if (!form.value.ruleDefinition.segments || form.value.ruleDefinition.segments.length === 0) {
    form.value.example = ''
    return
  }

  const result: string[] = []
  const now = new Date()

  for (const segment of form.value.ruleDefinition.segments) {
    const segmentResult = simulateSegment(segment, now)
    result.push(segmentResult)
  }

  form.value.example = result.join('')
}

// 模拟单个规则段的输出
function simulateSegment(segment: SegmentConfig, now: Date): string {
  const config = segment.config || {}

  switch (segment.type) {
    case 'fixed':
      return config.value || ''

    case 'date':
      const format = config.format || 'yyyyMMdd'
      return formatDate(now, format)

    case 'sequence':
      const length = config.length || 4
      const padding = config.padding || '0'
      const start = config.start || 1
      return String(start).padStart(length, padding)

    case 'conditional':
      // 模拟第一个条件结果
      let condResult = ''
      if (config.rules && Array.isArray(config.rules) && config.rules.length > 0) {
        condResult = String(config.rules[0].then || '')
      } else if (config.defaultValue) {
        // 如果没有规则但有默认值，使用默认值
        condResult = String(config.defaultValue)
      }
      // 如果配置了长度，则补齐
      if (config.length && config.length > 0) {
        const padding = config.padding || '0'
        condResult = condResult.padStart(config.length, padding)
      }
      return condResult

    case 'random':
      const randLength = config.length || 4
      return generateRandomString(randLength)

    default:
      return ''
  }
}

// 格式化日期
function formatDate(date: Date, format: string): string {
  const year = date.getFullYear()
  const yearShort = String(year).slice(-2)  // 两位年份
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return format
    .replace('yyyy', String(year))
    .replace('YYYY', String(year))
    .replace('yy', yearShort)
    .replace('YY', yearShort)
    .replace('MM', month)
    .replace('dd', day)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// 生成随机字符串
function generateRandomString(length: number): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

// 删除规则段时也更新预览
function handleDeleteSegment(index: number) {
  form.value.ruleDefinition.segments.splice(index, 1)
  updatePreview()
}

// 上移规则段时也更新预览
function handleMoveUpSegment(index: number) {
  if (index > 0) {
    const segments = form.value.ruleDefinition.segments
    const temp = segments[index]
    segments[index] = segments[index - 1]
    segments[index - 1] = temp
    updatePreview()
  }
}

// 下移规则段时也更新预览
function handleMoveDownSegment(index: number) {
  const segments = form.value.ruleDefinition.segments
  if (index < segments.length - 1) {
    const temp = segments[index]
    segments[index] = segments[index + 1]
    segments[index + 1] = temp
    updatePreview()
  }
}

// 预览编码
async function handlePreview() {
  if (!form.value.ruleCode) {
    ElMessage.warning('请先输入规则编码')
    return
  }

  try {
    const res = await previewCode(form.value.ruleCode, previewData.value)
    previewResult.value = res.data || ''
    form.value.example = previewResult.value
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '预览失败')
  }
}

onMounted(() => {
  fetchData()
  loadFieldStandardList()  // 加载字段标准库列表
})

// 点击外部关闭下拉框
function handleClickOutside(e: MouseEvent) {
  // 不再需要
}
</script>

<template>
  <div class="encoding-rule-page">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <input v-model="searchForm.ruleCode" type="text" placeholder="规则编码" @keyup.enter="handleSearch" />
        </div>
        <div class="mdm-filter-item">
          <input v-model="searchForm.ruleName" type="text" placeholder="规则名称" @keyup.enter="handleSearch" />
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="draft">草稿</option>
            <option value="active">启用</option>
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
            <th>规则编码</th>
            <th>规则名称</th>
            <th>规则段数</th>
            <th>示例</th>
            <th>创建时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-loading="loading">
          <tr v-for="row in tableData" :key="row.id">
            <td>
              <input type="checkbox" :checked="isRowSelected(row)" @change="toggleRowSelect(row)" />
            </td>
            <td>{{ row.ruleCode }}</td>
            <td>{{ row.ruleName }}</td>
            <td>{{ row.ruleDefinition?.segments?.length || 0 }}</td>
            <td>{{ row.example || '-' }}</td>
            <td>{{ row.createdAt }}</td>
            <td>
              <span class="custom-status-tag" :class="getStatusClass(row.status)">
                {{ getStatusLabel(row.status) }}
              </span>
            </td>
            <td>
              <div class="mdm-action-buttons">
                <!-- 草稿状态：编辑、发布、删除 -->
                <template v-if="row.status === 'draft'">
                  <button class="mdm-action-btn" @click="handleEdit(row)">编辑</button>
                  <button class="mdm-action-btn publish" @click="handlePublish(row)">发布</button>
                  <button class="mdm-action-btn delete" @click="handleDelete(row)">删除</button>
                </template>
                <!-- 启用状态：编辑（不能删除） -->
                <template v-else-if="row.status === 'active'">
                  <button class="mdm-action-btn" @click="handleEdit(row)">编辑</button>
                </template>
              </div>
            </td>
          </tr>
          <tr v-if="tableData.length === 0 && !loading">
            <td colspan="8" class="mdm-empty-data">暂无数据</td>
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

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>规则编码</div>
        <input v-model="form.ruleCode" class="mdm-input-yellow" placeholder="请输入规则编码" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>规则名称</div>
        <input v-model="form.ruleName" class="mdm-input-yellow" placeholder="请输入规则名称" />
      </div>

      <!-- 规则段配置 -->
      <div class="segment-section">
        <div class="segment-header">
          <span class="segment-title">规则段配置</span>
          <button class="mdm-btn-outline-sm" @click="handleAddSegment">+ 添加规则段</button>
        </div>

        <div class="segment-list" v-if="form.ruleDefinition.segments.length > 0">
          <div class="segment-item" v-for="(segment, index) in form.ruleDefinition.segments" :key="index">
            <div class="segment-info">
              <span class="segment-index">{{ index + 1 }}</span>
              <span class="segment-type">{{ getSegmentTypeLabel(segment.type) }}</span>
              <span class="segment-name">{{ segment.name }}</span>
            </div>
            <div class="segment-actions">
              <button class="mdm-btn-outline-xs" @click="handleMoveUpSegment(index)" :disabled="index === 0">↑</button>
              <button class="mdm-btn-outline-xs" @click="handleMoveDownSegment(index)" :disabled="index === form.ruleDefinition.segments.length - 1">↓</button>
              <button class="mdm-btn-outline-xs" @click="handleEditSegment(index)">编辑</button>
              <button class="mdm-btn-outline-xs delete" @click="handleDeleteSegment(index)">删除</button>
            </div>
          </div>
        </div>
        <div class="segment-empty" v-else>
          暂无规则段，请点击"添加规则段"按钮添加
        </div>
      </div>

      <div class="mdm-form-row">
        <div class="mdm-form-label">示例</div>
        <input v-model="form.example" class="mdm-input-normal" placeholder="编码示例" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">状态</div>
        <select v-model="form.status" class="mdm-select">
          <option value="draft">草稿</option>
          <option value="active">启用</option>
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

    <!-- 规则段配置弹窗 -->
    <MdmDialog v-model="segmentDialogVisible" title="配置规则段" width="550px">
      <div class="mdm-form-row" v-if="currentSegment">
        <div class="mdm-form-label required"><em>*</em>段名称</div>
        <input v-model="currentSegment.name" class="mdm-input-yellow" placeholder="请输入段名称" />
      </div>

      <div class="mdm-form-row" v-if="currentSegment">
        <div class="mdm-form-label required"><em>*</em>段类型</div>
        <select v-model="currentSegment.type" class="mdm-select" @change="handleSegmentTypeChange">
          <option v-for="opt in SEGMENT_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>

      <!-- 类型说明 -->
      <div class="segment-tip" v-if="currentSegmentType">
        <div class="tip-row">
          <span class="tip-icon">💡</span>
          <span class="tip-label">使用说明：</span>
          <span class="tip-value">{{ currentSegmentType.description }}</span>
        </div>
        <div class="tip-row">
          <span class="tip-icon">📝</span>
          <span class="tip-label">配置示例：</span>
          <span class="tip-value">{{ currentSegmentType.example }}</span>
        </div>
        <div class="tip-row">
          <span class="tip-icon">📤</span>
          <span class="tip-label">输出示例：</span>
          <span class="tip-value example-result">{{ currentSegmentType.exampleResult }}</span>
        </div>
      </div>

      <!-- 固定值配置 -->
      <div class="mdm-form-row" v-if="currentSegment?.type === 'fixed'">
        <div class="mdm-form-label required"><em>*</em>固定值</div>
        <input v-model="currentSegment.config.value" class="mdm-input-normal" placeholder="请输入固定值" />
      </div>

      <!-- 日期配置 -->
      <template v-if="currentSegment?.type === 'date'">
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>日期格式</div>
          <input v-model="currentSegment.config.format" class="mdm-input-normal" placeholder="如: yyyyMMdd" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">偏移天数</div>
          <input v-model.number="currentSegment.config.offsetDays" type="number" class="mdm-input-normal" placeholder="默认: 0" />
        </div>
      </template>

      <!-- 序列号配置 -->
      <template v-if="currentSegment?.type === 'sequence'">
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>长度</div>
          <input v-model.number="currentSegment.config.length" type="number" class="mdm-input-normal" placeholder="如: 4" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">填充字符</div>
          <input v-model="currentSegment.config.padding" class="mdm-input-normal" placeholder="默认: 0" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">起始值</div>
          <input v-model.number="currentSegment.config.start" type="number" class="mdm-input-normal" placeholder="默认: 1" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">重置周期</div>
          <select v-model="currentSegment.config.resetCycle" class="mdm-select">
            <option v-for="opt in RESET_CYCLE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
      </template>

      <!-- 条件值配置 -->
      <template v-if="currentSegment?.type === 'conditional'">
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>选择字段</div>
          <div class="field-select-box">
            <template v-if="currentSegment.config.fieldId">
              <span class="selected-field-text">{{ getFieldNameById(currentSegment.config.fieldId) }}</span>
              <button class="clear-field-btn" @click="clearSelectedField">×</button>
            </template>
            <button v-else class="select-field-btn" @click="openFieldSearchDialog('conditional')">选择字段</button>
          </div>
        </div>
        <div class="mdm-form-row" v-if="currentSegment.config.fieldId">
          <div class="mdm-form-label">条件规则</div>
          <div class="condition-rules-wrapper">
            <textarea v-model="currentSegment.config.rulesJson" class="mdm-textarea" rows="4" placeholder='[{"when":"产成品","then":"5"}]'></textarea>
            <div class="condition-example">
              <div class="example-header">
                <span>样例（点击复制）</span>
              </div>
              <pre class="example-code" @click="copyConditionExample">[
  {"when":"产成品","then":"5"},
  {"when":"原材料","then":"1"},
  {"when":"半成品","then":"3"}
]</pre>
            </div>
          </div>
          <div class="field-tip">格式：JSON数组，when为字段值，then为输出编码</div>
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>输出长度</div>
          <input v-model.number="currentSegment.config.length" type="number" class="mdm-input-yellow" placeholder="如: 2" min="1" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>默认值</div>
          <input v-model="currentSegment.config.defaultValue" class="mdm-input-yellow" placeholder="条件不匹配时的默认值" />
          <div class="field-tip" v-if="currentSegment.config.defaultValue && currentSegment.config.length && currentSegment.config.defaultValue.length > currentSegment.config.length" style="color: #ff4d4f;">
            ⚠️ 默认值长度({{ currentSegment.config.defaultValue.length }})不能大于输出长度({{ currentSegment.config.length }})
          </div>
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">填充字符</div>
          <input v-model="currentSegment.config.padding" class="mdm-input-normal" placeholder="默认: 0" maxlength="1" />
        </div>
      </template>

      <template #footer>
        <button class="mdm-btn-cancel" @click="segmentDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSaveSegment">保存</button>
      </template>
    </MdmDialog>

    <!-- 字段搜索弹窗 -->
    <MdmDialog v-model="fieldSearchDialogVisible" title="选择字段" width="500px">
      <div class="field-search-box">
        <input
          v-model="fieldSearchKeyword"
          class="field-search-input"
          placeholder="输入字段名称或编码搜索..."
          autofocus
        />
        <div class="field-search-list">
          <div
            class="field-search-item"
            v-for="field in filteredFieldList"
            :key="field.id"
            @click="handleSelectField(field)"
          >
            <div class="field-info">
              <span class="field-name">{{ field.fieldName }}</span>
              <span class="field-code">{{ field.fieldCode }}</span>
            </div>
            <div class="field-type">{{ field.fieldType }}</div>
          </div>
          <div class="field-search-empty" v-if="filteredFieldList.length === 0">
            <template v-if="fieldSearchKeyword">
              未找到匹配的字段
            </template>
            <template v-else>
              暂无字段数据
            </template>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="fieldSearchDialogVisible = false">取消</button>
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

.encoding-rule-page {
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

.status-draft {
  background: #fffbe6;
  border-color: #ffe58f;
  color: #faad14;
}

.status-active {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
}

// 操作按钮样式
.mdm-action-buttons {
  display: flex;
  gap: 6px;
}

.mdm-action-btn {
  background: none;
  border: none;
  color: #5b6e8c;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 10px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: #ed2b33;
    color: white;
  }

  &.publish {
    color: #1890ff;

    &:hover {
      background: #1890ff;
      color: white;
    }
  }

  &.delete:hover {
    background: #ef4444;
  }
}

// 规则段配置区域
.segment-section {
  margin: 15px 0;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.segment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.segment-title {
  font-weight: 600;
  color: #333;
}

.segment-list {
  padding: 10px 15px;
}

.segment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  margin-bottom: 5px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;

  &:hover {
    background: #fafafa;
  }
}

.segment-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.segment-index {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ed2b33;
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
}

.segment-type {
  padding: 2px 8px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 12px;
}

.segment-name {
  color: #333;
}

.segment-actions {
  display: flex;
  gap: 5px;
}

.segment-empty {
  padding: 20px;
  text-align: center;
  color: #999;
}

// 全局设置区域
.settings-section {
  margin: 15px 0;
  padding: 15px;
  background: #fafafa;
  border-radius: 4px;
}

.settings-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.settings-row {
  display: flex;
  gap: 20px;
}

.settings-item {
  display: flex;
  align-items: center;
  gap: 8px;

  label {
    color: #666;
  }
}

// 规则段类型提示
.segment-tip {
  margin: 10px 0 15px;
  padding: 12px 15px;
  background: #f0f7ff;
  border: 1px solid #d0e5ff;
  border-radius: 4px;

  .tip-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 6px;
    font-size: 13px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .tip-icon {
    margin-right: 6px;
  }

  .tip-label {
    color: #666;
    min-width: 70px;
  }

  .tip-value {
    color: #333;

    &.example-result {
      color: #1890ff;
      font-weight: 500;
    }
  }
}

// 按钮样式
.mdm-btn-outline-sm {
  padding: 4px 12px;
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

.mdm-btn-outline-xs {
  padding: 2px 8px;
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

  &.delete:hover {
    border-color: #ed2b33;
    color: #ed2b33;
    background: #fff5f5;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.mdm-select-sm {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
}

.mdm-input-sm {
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;
  width: 80px;
}

// 分页按钮禁用样式
.mdm-page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

// 条件规则配置样式
.condition-rules {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 8px;
  background: #fafafa;
}

.condition-rule-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;

  &:last-child {
    margin-bottom: 0;
  }
}

.rule-when {
  flex: 1;
  color: #333;
  font-size: 13px;
}

.rule-arrow {
  margin: 0 10px;
  color: #999;
}

.rule-then-input {
  width: 80px;
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;

  &:focus {
    border-color: #409eff;
    outline: none;
  }
}

.condition-empty {
  padding: 20px;
  text-align: center;
  color: #999;
}

// 分类映射配置样式
.category-mapping {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 8px;
  background: #fafafa;
}

.mapping-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  margin-bottom: 4px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;

  &:last-child {
    margin-bottom: 0;
  }
}

.mapping-key {
  flex: 1;
  color: #333;
  font-size: 13px;
}

.mapping-arrow {
  margin: 0 10px;
  color: #999;
}

.mapping-value-input {
  width: 80px;
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 13px;

  &:focus {
    border-color: #409eff;
    outline: none;
  }
}

.mapping-empty {
  padding: 20px;
  text-align: center;
  color: #999;
}

// 字段选择样式
.field-select-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-field-text {
  flex: 1;
  padding: 6px 12px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  color: #333;
  font-size: 13px;
}

.select-field-btn {
  flex: 1;
  padding: 6px 12px;
  background: #fff;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  font-size: 13px;

  &:hover {
    border-color: #1890ff;
    color: #1890ff;
  }
}

.clear-field-btn {
  padding: 2px 8px;
  background: #ff4d4f;
  border: none;
  border-radius: 4px;
  color: #fff;
  cursor: pointer;
  font-size: 12px;

  &:hover {
    background: #ff7875;
  }
}

.field-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

// 条件规则包装器
.condition-rules-wrapper {
  flex: 1;
  display: flex;
  gap: 12px;

  .mdm-textarea {
    flex: 1;
  }
}

.condition-example {
  width: 200px;
  flex-shrink: 0;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
}

.example-header {
  padding: 6px 10px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  font-size: 12px;
  color: #666;
}

.example-code {
  margin: 0;
  padding: 10px;
  background: #f5f5f5;
  font-size: 11px;
  line-height: 1.5;
  color: #333;
  cursor: pointer;
  overflow-x: auto;
  white-space: pre;
  font-family: Consolas, Monaco, monospace;

  &:hover {
    background: #e8e8e8;
  }
}

// 字段搜索弹窗样式
.field-search-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-search-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;

  &:focus {
    border-color: #1890ff;
    outline: none;
  }
}

.field-search-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.field-search-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #e6f7ff;
  }
}

.field-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.field-search-item .field-name {
  font-size: 14px;
  color: #333;
}

.field-search-item .field-code {
  font-size: 12px;
  color: #999;
}

.field-type {
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.field-search-empty {
  padding: 40px;
  text-align: center;
  color: #999;
  font-size: 14px;
}
</style>
