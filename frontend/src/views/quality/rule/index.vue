<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getRuleList,
  createRule,
  updateRule,
  deleteRule,
  toggleRuleStatus,
  getViewEntities,
  RULE_TYPES,
  SEVERITY_OPTIONS,
  type QualityRule
} from '../../../api/quality/rule'
import { getTemplateList, type QualityRuleTemplate } from '../../../api/quality/template'
import { testRule } from '../../../api/quality/test'
import { getViewList, type ViewDefinition } from '../../../api/standard/viewDefinition'
import { getActiveValueDomainList, type ValueDomain } from '../../../api/standard/valueDomain'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

const tableData = ref<QualityRule[]>([])
const loading = ref(false)

// 搜索
const searchForm = ref({
  viewId: null as number | null,
  ruleType: '',
  status: ''
})

// 视图列表
const viewList = ref<ViewDefinition[]>([])

// 模板列表
const templateList = ref<QualityRuleTemplate[]>([])

// 值域列表
const domainList = ref<ValueDomain[]>([])

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 当前选中的视图（用于获取实体和字段）
const selectedView = ref<any>(null)
const selectedEntity = ref<any>(null)

// 表单
const form = ref<QualityRule>({
  ruleCode: '',
  ruleName: '',
  ruleType: 'completeness',
  checkType: '',
  checkConfig: '',
  viewId: 0,
  entityId: 0,
  entityType: 'main',
  tableName: '',
  fieldId: undefined,
  fieldCode: '',
  fieldName: '',
  templateId: null,
  threshold: 100,
  severity: 'warning',
  status: 'active',
  description: ''
})

// 检查配置（动态）
const checkConfig = ref<any>({
  checkType: '',
  pattern: '',
  minLength: null,
  maxLength: null,
  minValue: null,
  maxValue: null,
  minOperator: '>=',
  maxOperator: '<=',
  domainCode: '',
  allowNull: true,
  trimWhitespace: true,
  allowZero: false,
  countType: 'char',
  errorMessage: ''
})

// 测试相关
const testingRule = ref(false)
const testRuleResult = ref<any>(null)
const testValue = ref('')
const testResult = ref<boolean | null>(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 检查类型选项
const CHECK_TYPES = {
  completeness: [
    { value: 'not_null', label: '非空检查' },
    { value: 'not_empty', label: '非空字符串' }
  ],
  uniqueness: [
    { value: 'unique_global', label: '全局唯一' },
    { value: 'unique_in_main', label: '主表范围唯一' }
  ],
  accuracy: [
    { value: 'regex', label: '正则表达式' },
    { value: 'length', label: '长度检查' },
    { value: 'range', label: '范围检查' },
    { value: 'domain', label: '值域检查' }
  ],
  consistency: [
    { value: 'reference', label: '关联检查' }
  ],
  timeliness: [
    { value: 'timeliness', label: '时效检查' }
  ]
}

// 当前可用的检查类型
const availableCheckTypes = computed(() => {
  return CHECK_TYPES[form.value.ruleType] || []
})

// 获取规则列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getRuleList(searchForm.value)
    const allData = res.data || []
    total.value = allData.length

    // 前端分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = allData.slice(start, end)
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

// 获取模板列表
async function fetchTemplateList() {
  try {
    const res = await getTemplateList({ status: 'active' })
    templateList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 获取值域列表
async function fetchDomainList() {
  try {
    const res = await getActiveValueDomainList()
    domainList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 获取视图的实体信息
async function fetchViewEntities(viewId: number) {
  try {
    const res = await getViewEntities(viewId)
    selectedView.value = res.data
  } catch (error) {
    console.error(error)
    selectedView.value = null
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
    ruleType: '',
    status: ''
  }
  currentPage.value = 1
  fetchData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增规则'
  form.value = {
    ruleCode: '',
    ruleName: '',
    ruleType: 'completeness',
    checkType: '',
    checkConfig: '',
    viewId: 0,
    entityId: 0,
    entityType: 'main',
    tableName: '',
    fieldId: undefined,
    fieldCode: '',
    fieldName: '',
    templateId: null,
    threshold: 100,
    severity: 'warning',
    status: 'active',
    description: ''
  }
  checkConfig.value = {
    checkType: '',
    pattern: '',
    minLength: null,
    maxLength: null,
    minValue: null,
    maxValue: null,
    minOperator: '>=',
    maxOperator: '<=',
    domainCode: '',
    allowNull: true,
    trimWhitespace: true,
    allowZero: false,
    countType: 'char',
    errorMessage: ''
  }
  selectedView.value = null
  selectedEntity.value = null
  testRuleResult.value = null
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: QualityRule) {
  dialogTitle.value = '编辑规则'
  form.value = { ...row }

  // 解析checkConfig
  if (row.checkConfig) {
    try {
      checkConfig.value = JSON.parse(row.checkConfig)
    } catch (e) {
      console.error('解析检查配置失败', e)
    }
  }

  // 加载视图实体信息
  if (row.viewId) {
    fetchViewEntities(row.viewId)
  }

  testRuleResult.value = null
  dialogVisible.value = true
}

// 删除
function handleDelete(row: QualityRule) {
  confirmMessage.value = `确定要删除规则「${row.ruleName}」吗？`
  confirmAction.value = async () => {
    await deleteRule(row.id!)
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

// 切换状态
async function handleToggleStatus(row: QualityRule) {
  try {
    await toggleRuleStatus(row.id!)
    ElMessage.success('状态更新成功')
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

// 视图选择变化
async function handleViewChange(viewId: number) {
  if (viewId) {
    await fetchViewEntities(viewId)
    form.value.entityId = 0
    form.value.entityType = 'main'
    form.value.fieldId = undefined
    form.value.fieldCode = ''
    form.value.fieldName = ''
    selectedEntity.value = null
  } else {
    selectedView.value = null
  }
}

// 实体选择变化
function handleEntityChange(entityId: number) {
  if (selectedView.value && selectedView.value.entities) {
    const entity = selectedView.value.entities.find((e: any) => e.id === entityId)
    if (entity) {
      selectedEntity.value = entity
      form.value.entityType = entity.entityType
      form.value.tableName = entity.tableName
      form.value.fieldId = undefined
      form.value.fieldCode = ''
      form.value.fieldName = ''
    }
  }
}

// 字段选择变化
function handleFieldChange(fieldId: number) {
  if (selectedEntity.value && selectedEntity.value.fields) {
    const field = selectedEntity.value.fields.find((f: any) => f.id === fieldId)
    if (field) {
      form.value.fieldCode = field.fieldCode
      form.value.fieldName = field.fieldName
    }
  }
}

// 规则类型变化
watch(() => form.value.ruleType, (newType) => {
  // 重置检查类型
  form.value.checkType = ''
  checkConfig.value = {
    checkType: '',
    pattern: '',
    minLength: null,
    maxLength: null,
    minValue: null,
    maxValue: null,
    minOperator: '>=',
    maxOperator: '<=',
    domainCode: '',
    allowNull: true,
    trimWhitespace: true,
    allowZero: false,
    countType: 'char',
    errorMessage: ''
  }
})

// 应用模板
async function applyTemplate() {
  if (!form.value.templateId) {
    return
  }

  const template = templateList.value.find(t => t.id === form.value.templateId)
  if (template) {
    form.value.checkType = template.checkType || ''
    form.value.severity = template.severity || 'warning'

    if (template.checkConfig) {
      try {
        checkConfig.value = JSON.parse(template.checkConfig)
      } catch (e) {
        console.error('解析模板配置失败', e)
      }
    }

    ElMessage.success('已应用模板配置')
  }
}

// 检查类型变化
watch(() => form.value.checkType, (newType) => {
  checkConfig.value.checkType = newType
})

// 测试正则表达式
function testRegex() {
  if (!checkConfig.value.pattern || !testValue.value) {
    ElMessage.warning('请输入正则表达式和测试值')
    return
  }

  try {
    const regex = new RegExp(checkConfig.value.pattern)
    testResult.value = regex.test(testValue.value)
  } catch (e: any) {
    ElMessage.error('正则表达式格式错误：' + e.message)
    testResult.value = null
  }
}

// 测试规则
async function handleTestRule() {
  if (!form.value.viewId || !form.value.fieldId) {
    ElMessage.warning('请先选择视图、实体和字段')
    return
  }

  if (!form.value.checkType) {
    ElMessage.warning('请选择检查类型')
    return
  }

  testingRule.value = true
  testRuleResult.value = null

  try {
    // 构建检查配置
    const config = { ...checkConfig.value, checkType: form.value.checkType }

    const testData = {
      ...form.value,
      checkConfig: JSON.stringify(config)
    }

    const res = await testRule(testData)
    if (res.data) {
      testRuleResult.value = res.data
      if (res.data.success) {
        ElMessage.success('规则测试完成')
      } else {
        ElMessage.error('规则测试失败：' + res.data.error)
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '测试失败')
  } finally {
    testingRule.value = false
  }
}

// 提交
async function handleSubmit() {
  // 表单验证
  if (!form.value.ruleCode) {
    ElMessage.warning('请输入规则编码')
    return
  }
  if (!form.value.ruleName) {
    ElMessage.warning('请输入规则名称')
    return
  }
  if (!form.value.viewId) {
    ElMessage.warning('请选择视图')
    return
  }
  if (!form.value.entityId) {
    ElMessage.warning('请选择实体')
    return
  }
  if (!form.value.checkType) {
    ElMessage.warning('请选择检查类型')
    return
  }

  try {
    // 构建检查配置JSON
    const config = { ...checkConfig.value, checkType: form.value.checkType }
    form.value.checkConfig = JSON.stringify(config)

    if (form.value.id) {
      await updateRule(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createRule(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
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

// 获取规则类型标签
function getRuleTypeLabel(type: string) {
  const item = RULE_TYPES.find(t => t.value === type)
  return item?.label || type
}

// 获取检查类型标签
function getCheckTypeLabel(checkType: string) {
  const labels: Record<string, string> = {
    not_null: '非空检查',
    not_empty: '非空字符串',
    regex: '正则表达式',
    length: '长度检查',
    range: '范围检查',
    domain: '值域检查',
    unique_global: '全局唯一',
    unique_in_main: '主表范围唯一',
    reference: '关联检查',
    timeliness: '时效检查'
  }
  return labels[checkType] || checkType
}

// 获取严重级别标签
function getSeverityLabel(severity: string) {
  const item = SEVERITY_OPTIONS.find(s => s.value === severity)
  return item?.label || severity
}

// 获取严重级别样式类
function getSeverityClass(severity: string) {
  const classMap: Record<string, string> = {
    warning: 'severity-warning',
    error: 'severity-error',
    critical: 'severity-critical'
  }
  return classMap[severity] || ''
}

onMounted(() => {
  fetchData()
  fetchViewList()
  fetchTemplateList()
  fetchDomainList()
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
          <select v-model="searchForm.ruleType" @change="handleSearch">
            <option value="">全部类型</option>
            <option v-for="item in RULE_TYPES" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="active">启用</option>
            <option value="inactive">停用</option>
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
              <input type="checkbox" />
            </th>
            <th>规则编码</th>
            <th>规则名称</th>
            <th>规则类型</th>
            <th>检查类型</th>
            <th>所属视图</th>
            <th>实体</th>
            <th>检查字段</th>
            <th>严重级别</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-loading="loading">
          <tr v-for="row in tableData" :key="row.id">
            <td>
              <input type="checkbox" />
            </td>
            <td>{{ row.ruleCode }}</td>
            <td>{{ row.ruleName }}</td>
            <td>
              <span class="rule-type-tag">{{ getRuleTypeLabel(row.ruleType) }}</span>
            </td>
            <td>{{ row.checkType || '-' }}</td>
            <td>{{ row.viewName || '-' }}</td>
            <td>
              <span class="entity-tag" :class="row.entityType">
                {{ row.entityType === 'main' ? '主表' : '子表' }}
              </span>
              {{ row.entityName || '-' }}
            </td>
            <td>{{ row.fieldName || '-' }}</td>
            <td>
              <span class="severity-tag" :class="getSeverityClass(row.severity || 'warning')">
                {{ getSeverityLabel(row.severity || 'warning') }}
              </span>
            </td>
            <td>
              <div class="mdm-status-badge" @click="handleToggleStatus(row)" style="cursor: pointer">
                <span :class="row.status === 'active' ? 'mdm-green-dot' : 'mdm-red-dot'"></span>
                {{ row.status === 'active' ? '启用' : '停用' }}
              </div>
            </td>
            <td>
              <div class="mdm-action-buttons">
                <button class="mdm-action-btn" @click="handleEdit(row)">编辑</button>
                <button class="mdm-action-btn delete" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="tableData.length === 0 && !loading">
            <td colspan="11" class="mdm-empty-data">暂无数据</td>
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

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="900px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>规则编码</div>
        <input v-model="form.ruleCode" class="mdm-input-yellow" placeholder="请输入规则编码，如：RULE001" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>规则名称</div>
        <input v-model="form.ruleName" class="mdm-input-yellow" placeholder="请输入规则名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>规则类型</div>
        <select v-model="form.ruleType" class="mdm-select">
          <option v-for="item in RULE_TYPES" :key="item.value" :value="item.value">{{ item.label }}</option>
        </select>
      </div>

      <div class="form-section-title">关联目标</div>

      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>选择视图</div>
        <select v-model="form.viewId" class="mdm-select" @change="handleViewChange(form.viewId)">
          <option :value="0">请选择视图</option>
          <option v-for="view in viewList" :key="view.id" :value="view.id">{{ view.viewName }}</option>
        </select>
      </div>

      <div class="mdm-form-row" v-if="selectedView && selectedView.entities">
        <div class="mdm-form-label required"><em>*</em>选择实体</div>
        <select v-model="form.entityId" class="mdm-select" @change="handleEntityChange(form.entityId)">
          <option :value="0">请选择实体</option>
          <option v-for="entity in selectedView.entities" :key="entity.id" :value="entity.id">
            {{ entity.entityName }}
            <template v-if="entity.entityType === 'main'">(主表)</template>
            <template v-else>(子表)</template>
          </option>
        </select>
      </div>

      <div class="mdm-form-row" v-if="selectedEntity && selectedEntity.fields">
        <div class="mdm-form-label">检查字段</div>
        <select v-model="form.fieldId" class="mdm-select" @change="handleFieldChange(form.fieldId)">
          <option :value="undefined">请选择字段</option>
          <option v-for="field in selectedEntity.fields" :key="field.id" :value="field.id">
            {{ field.fieldName }} ({{ field.fieldCode }})
          </option>
        </select>
      </div>

      <div class="form-section-title">规则配置</div>

      <!-- 规则模板选择 -->
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>规则模板</div>
        <select v-model="form.templateId" @change="applyTemplate" class="mdm-select">
          <option :value="null">请选择模板</option>
          <option v-for="tpl in templateList" :key="tpl.id" :value="tpl.id">
            {{ tpl.templateName }}（{{ tpl.category || '未分类' }}）
          </option>
        </select>
      </div>

      <!-- 检查类型显示（只读，由模板自动填充） -->
      <div class="mdm-form-row" v-if="form.checkType">
        <div class="mdm-form-label">检查类型</div>
        <input :value="getCheckTypeLabel(form.checkType)" class="mdm-input-yellow" readonly style="background: #f5f5f5" />
      </div>

      <!-- 动态配置区域 -->
      <div class="dynamic-config" v-if="form.checkType">

        <!-- 正则表达式配置 -->
        <div v-if="form.checkType === 'regex'" class="config-section">
          <div class="config-title">正则表达式配置</div>
          <div class="mdm-form-row">
            <div class="mdm-form-label required"><em>*</em>正则表达式</div>
            <input v-model="checkConfig.pattern" class="mdm-input-yellow"
                   placeholder="例如：^[A-Za-z0-9]+$" />
          </div>
          <div class="mdm-form-row">
            <div class="mdm-form-label">错误提示</div>
            <input v-model="checkConfig.errorMessage" class="mdm-input-yellow"
                   placeholder="格式不正确" />
          </div>
          <div class="mdm-form-row">
            <div class="mdm-form-label">测试验证</div>
            <input v-model="testValue" class="mdm-input-yellow" placeholder="输入测试值" style="width: 200px" />
            <button class="mdm-btn-outline" @click="testRegex" style="margin-left: 8px">测试</button>
            <span v-if="testResult !== null" :class="testResult ? 'success-tip' : 'error-tip'" style="margin-left: 8px">
              {{ testResult ? '✓ 匹配成功' : '✗ 匹配失败' }}
            </span>
          </div>
        </div>

        <!-- 长度检查配置 -->
        <div v-if="form.checkType === 'length'" class="config-section">
          <div class="config-title">长度检查配置</div>
          <div class="config-row">
            <div class="config-item">
              <div class="mdm-form-label">最小长度</div>
              <input v-model.number="checkConfig.minLength" type="number"
                     class="mdm-input-yellow" style="width: 120px" />
            </div>
            <div class="config-item">
              <div class="mdm-form-label">最大长度</div>
              <input v-model.number="checkConfig.maxLength" type="number"
                     class="mdm-input-yellow" style="width: 120px" />
            </div>
          </div>
          <div class="mdm-form-row">
            <div class="mdm-form-label">统计方式</div>
            <div class="mdm-radio-group">
              <label class="mdm-radio-item">
                <input type="radio" v-model="checkConfig.countType" value="char" />
                按字符
              </label>
              <label class="mdm-radio-item">
                <input type="radio" v-model="checkConfig.countType" value="byte" />
                按字节
              </label>
            </div>
          </div>
        </div>

        <!-- 范围检查配置 -->
        <div v-if="form.checkType === 'range'" class="config-section">
          <div class="config-title">范围检查配置</div>
          <div class="config-row">
            <div class="config-item">
              <div class="mdm-form-label">最小值</div>
              <input v-model.number="checkConfig.minValue" type="number"
                     class="mdm-input-yellow" />
            </div>
            <div class="config-item">
              <div class="mdm-form-label">比较符</div>
              <select v-model="checkConfig.minOperator" class="mdm-select" style="width: 120px">
                <option value=">=">大于等于 (≥)</option>
                <option value=">">大于 (>)</option>
              </select>
            </div>
          </div>
          <div class="config-row">
            <div class="config-item">
              <div class="mdm-form-label">最大值</div>
              <input v-model.number="checkConfig.maxValue" type="number"
                     class="mdm-input-yellow" />
            </div>
            <div class="config-item">
              <div class="mdm-form-label">比较符</div>
              <select v-model="checkConfig.maxOperator" class="mdm-select" style="width: 120px">
                <option value="<=">小于等于 (≤)</option>
                <option value="<">小于 (<)</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 值域检查配置 -->
        <div v-if="form.checkType === 'domain'" class="config-section">
          <div class="config-title">值域检查配置</div>
          <div class="mdm-form-row">
            <div class="mdm-form-label required"><em>*</em>关联值域</div>
            <select v-model="checkConfig.domainCode" class="mdm-select">
              <option value="">请选择值域</option>
              <option v-for="domain in domainList" :key="domain.domainCode"
                      :value="domain.domainCode">
                {{ domain.domainName }}
              </option>
            </select>
          </div>
          <div class="mdm-form-row">
            <div class="mdm-form-label">允许空值</div>
            <el-switch v-model="checkConfig.allowNull" />
          </div>
        </div>

        <!-- 非空检查配置 -->
        <div v-if="form.checkType === 'not_null'" class="config-section">
          <div class="config-title">非空检查配置</div>
          <div class="mdm-form-row">
            <div class="mdm-form-label">去除空白</div>
            <el-switch v-model="checkConfig.trimWhitespace" />
            <span class="config-tip">自动去除字段值前后的空白字符</span>
          </div>
          <div class="mdm-form-row">
            <div class="mdm-form-label">允许零值</div>
            <el-switch v-model="checkConfig.allowZero" />
            <span class="config-tip">值为0时视为有效值</span>
          </div>
        </div>

      </div>

      <!-- 严重级别 -->
      <div class="mdm-form-row">
        <div class="mdm-form-label">严重级别</div>
        <div class="mdm-radio-group">
          <label v-for="item in SEVERITY_OPTIONS" :key="item.value" class="mdm-radio-item">
            <input type="radio" v-model="form.severity" :value="item.value" />
            {{ item.label }}
          </label>
        </div>
      </div>

      <!-- 合格阈值 -->
      <div class="mdm-form-row">
        <div class="mdm-form-label">合格阈值</div>
        <div class="threshold-input">
          <input v-model.number="form.threshold" type="number" class="mdm-input-yellow" style="width: 100px" />
          <span class="threshold-unit">%</span>
        </div>
        <span class="config-tip">通过率需达到此阈值才算合格</span>
      </div>

      <!-- 规则测试 -->
      <div class="mdm-form-row" v-if="form.viewId && form.fieldId && form.checkType">
        <div class="mdm-form-label"></div>
        <button class="mdm-btn-primary" @click="handleTestRule" :disabled="testingRule">
          {{ testingRule ? '测试中...' : '🔍 测试规则' }}
        </button>
      </div>

      <!-- 测试结果 -->
      <div class="test-result-section" v-if="testRuleResult && testRuleResult.success">
        <div class="test-result-title">测试结果</div>
        <div class="test-result-grid">
          <div class="result-item">
            <span class="label">检测记录数</span>
            <span class="value">{{ testRuleResult.totalRecords }}</span>
          </div>
          <div class="result-item">
            <span class="label">通过数</span>
            <span class="value success">{{ testRuleResult.passCount }}</span>
          </div>
          <div class="result-item">
            <span class="label">失败数</span>
            <span class="value error">{{ testRuleResult.failCount }}</span>
          </div>
          <div class="result-item">
            <span class="label">通过率</span>
            <span class="value" :class="testRuleResult.passRate >= 90 ? 'success' : 'warning'">
              {{ testRuleResult.passRate.toFixed(1) }}%
            </span>
          </div>
        </div>
        <div class="fail-records" v-if="testRuleResult.failRecords && testRuleResult.failRecords.length > 0">
          <div class="fail-title">失败记录（前10条）</div>
          <div class="fail-list">
            <div v-for="(record, index) in testRuleResult.failRecords" :key="index" class="fail-item">
              <span class="record-id">ID: {{ record.recordId }}</span>
              <span class="field-value">值: {{ record.fieldValue || '(空)' }}</span>
              <span class="reason">{{ record.reason }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 描述 -->
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="form.description" class="mdm-textarea" placeholder="请输入规则描述"></textarea>
      </div>

      <!-- 状态 -->
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>状态</div>
        <div class="mdm-radio-group">
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.status" value="active" />
            启用
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.status" value="inactive" />
            停用
          </label>
        </div>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSubmit">确定</button>
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

.form-section-title {
  padding: 12px 0 8px;
  margin-top: 8px;
  border-top: 1px dashed #e4e7ed;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.rule-type-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 12px;
}

.entity-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 4px;
}

.entity-tag.main {
  background: #f6ffed;
  color: #52c41a;
}

.entity-tag.sub {
  background: #fff7e6;
  color: #fa8c16;
}

.severity-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.severity-tag.severity-warning {
  background: #fffbe6;
  color: #faad14;
}

.severity-tag.severity-error {
  background: #fff2f0;
  color: #ff4d4f;
}

.severity-tag.severity-critical {
  background: #ff4d4f;
  color: #fff;
}

.threshold-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.threshold-unit {
  color: #666;
}

.template-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #1890ff;
}

.config-section {
  margin: 12px 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.config-title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.config-row {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.config-item {
  flex: 1;
}

.config-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.success-tip {
  color: #52c41a;
  font-weight: 500;
}

.error-tip {
  color: #ff4d4f;
  font-weight: 500;
}

.test-result-section {
  margin: 16px 0;
  padding: 16px;
  background: #f0f5ff;
  border-radius: 4px;
  border: 1px solid #adc6ff;
}

.test-result-title {
  font-size: 14px;
  font-weight: 500;
  color: #1890ff;
  margin-bottom: 12px;
}

.test-result-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.result-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.result-item .label {
  font-size: 12px;
  color: #8c8c8c;
}

.result-item .value {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
}

.result-item .value.success {
  color: #52c41a;
}

.result-item .value.error {
  color: #ff4d4f;
}

.result-item .value.warning {
  color: #faad14;
}

.fail-records {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #d9d9d9;
}

.fail-title {
  font-size: 13px;
  font-weight: 500;
  color: #ff4d4f;
  margin-bottom: 8px;
}

.fail-list {
  max-height: 150px;
  overflow-y: auto;
}

.fail-item {
  padding: 6px 8px;
  margin-bottom: 4px;
  background: #fff;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  gap: 12px;
}

.fail-item .record-id {
  color: #1890ff;
  font-weight: 500;
}

.fail-item .field-value {
  color: #ff4d4f;
}

.fail-item .reason {
  color: #8c8c8c;
}
</style>
