<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
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
import { getViewList, type ViewDefinition } from '../../../api/standard/viewDefinition'
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
  viewId: 0,
  entityId: 0,
  entityType: 'main',
  tableName: '',
  fieldId: undefined,
  fieldCode: '',
  fieldName: '',
  ruleConfig: '',
  threshold: 100,
  severity: 'warning',
  status: 'active',
  description: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 规则类型配置选项
const ruleTypeConfigs: Record<string, { label: string; options: { value: string; label: string }[] }> = {
  completeness: {
    label: '完整性检查',
    options: [
      { value: 'not_null', label: '非空检查' },
      { value: 'not_empty', label: '非空字符串' }
    ]
  },
  uniqueness: {
    label: '唯一性检查',
    options: [
      { value: 'unique', label: '值唯一' },
      { value: 'unique_in_main', label: '主表范围内唯一（子表）' }
    ]
  },
  accuracy: {
    label: '准确性检查',
    options: [
      { value: 'regex', label: '正则匹配' },
      { value: 'domain', label: '值域检查' },
      { value: 'range', label: '范围检查' },
      { value: 'length', label: '长度检查' }
    ]
  }
}

// 当前规则类型的配置选项
const currentCheckTypes = computed(() => {
  return ruleTypeConfigs[form.value.ruleType]?.options || []
})

// 获取规则列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getRuleList(searchForm.value)
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
    viewId: 0,
    entityId: 0,
    entityType: 'main',
    tableName: '',
    fieldId: undefined,
    fieldCode: '',
    fieldName: '',
    ruleConfig: '',
    threshold: 100,
    severity: 'warning',
    status: 'active',
    description: ''
  }
  selectedView.value = null
  selectedEntity.value = null
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: QualityRule) {
  dialogTitle.value = '编辑规则'
  form.value = { ...row }

  // 加载视图实体信息
  if (row.viewId) {
    fetchViewEntities(row.viewId)
  }

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
    // 重置实体和字段选择
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
      // 重置字段选择
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

  try {
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

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="700px">
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

      <div class="mdm-form-row">
        <div class="mdm-form-label">严重级别</div>
        <div class="mdm-radio-group">
          <label v-for="item in SEVERITY_OPTIONS" :key="item.value" class="mdm-radio-item">
            <input type="radio" v-model="form.severity" :value="item.value" />
            {{ item.label }}
          </label>
        </div>
      </div>

      <div class="mdm-form-row">
        <div class="mdm-form-label">合格阈值</div>
        <div class="threshold-input">
          <input v-model.number="form.threshold" type="number" class="mdm-input-yellow" style="width: 100px" />
          <span class="threshold-unit">%</span>
        </div>
      </div>

      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="form.description" class="mdm-textarea" placeholder="请输入规则描述"></textarea>
      </div>

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
</style>
