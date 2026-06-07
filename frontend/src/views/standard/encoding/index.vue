<script setup lang="ts">
/**
 * 编码规则管理页面
 */

import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
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
  status: 'active',
  example: '',
  description: ''
})

// 当前编辑的规则段
const currentSegment = ref<SegmentConfig | null>(null)
const currentSegmentIndex = ref(-1)

// 预览数据
const previewData = ref<Record<string, any>>({})
const previewResult = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取状态标签
const getStatusLabel = (status: string) => {
  return status === 'active' ? '启用' : '停用'
}

// 获取状态样式
const getStatusClass = (status: string) => {
  return status === 'active' ? 'status-active' : 'status-inactive'
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
    status: 'active',
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
        field: '',
        rulesJson: '[\n  {"when": "值1", "then": "结果1"},\n  {"when": "值2", "then": "结果2"}\n]',
        default: ''
      }
    } else if (currentSegment.value.type === 'category') {
      currentSegment.value.config = {
        field: '',
        mappingJson: '{\n  "分类值1": "编码1",\n  "分类值2": "编码2"\n}',
        default: ''
      }
    } else if (currentSegment.value.type === 'field') {
      currentSegment.value.config = {
        source: '',
        length: null,
        padding: ''
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
  if (currentSegment.value.type === 'conditional' && currentSegment.value.config.rules) {
    currentSegment.value.config.rulesJson = JSON.stringify(currentSegment.value.config.rules, null, 2)
  }
  // 分类编码：将mapping对象转为JSON字符串便于编辑
  if (currentSegment.value.type === 'category' && currentSegment.value.config.mapping) {
    currentSegment.value.config.mappingJson = JSON.stringify(currentSegment.value.config.mapping, null, 2)
  }

  currentSegmentIndex.value = index
  segmentDialogVisible.value = true
}

// 删除规则段
function handleDeleteSegment(index: number) {
  form.value.ruleDefinition.segments.splice(index, 1)
}

// 上移规则段
function handleMoveUpSegment(index: number) {
  if (index > 0) {
    const segments = form.value.ruleDefinition.segments
    const temp = segments[index]
    segments[index] = segments[index - 1]
    segments[index - 1] = temp
  }
}

// 下移规则段
function handleMoveDownSegment(index: number) {
  const segments = form.value.ruleDefinition.segments
  if (index < segments.length - 1) {
    const temp = segments[index]
    segments[index] = segments[index + 1]
    segments[index + 1] = temp
  }
}

// 保存规则段
function handleSaveSegment() {
  if (!currentSegment.value) return

  if (!currentSegment.value.name) {
    ElMessage.warning('请输入段名称')
    return
  }

  // 条件值：解析JSON字符串为数组
  if (currentSegment.value.type === 'conditional' && currentSegment.value.config.rulesJson) {
    try {
      currentSegment.value.config.rules = JSON.parse(currentSegment.value.config.rulesJson)
      delete currentSegment.value.config.rulesJson
    } catch (e) {
      ElMessage.warning('条件规则JSON格式错误')
      return
    }
  }

  // 分类编码：解析JSON字符串为对象
  if (currentSegment.value.type === 'category' && currentSegment.value.config.mappingJson) {
    try {
      currentSegment.value.config.mapping = JSON.parse(currentSegment.value.config.mappingJson)
      delete currentSegment.value.config.mappingJson
    } catch (e) {
      ElMessage.warning('映射配置JSON格式错误')
      return
    }
  }

  if (currentSegmentIndex.value === -1) {
    form.value.ruleDefinition.segments.push(currentSegment.value)
  } else {
    form.value.ruleDefinition.segments[currentSegmentIndex.value] = currentSegment.value
  }

  segmentDialogVisible.value = false
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
})
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
                <button class="mdm-action-btn" @click="handleEdit(row)">编辑</button>
                <button class="mdm-action-btn delete" @click="handleDelete(row)">删除</button>
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
          <option value="active">启用</option>
          <option value="inactive">停用</option>
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
          <div class="mdm-form-label required"><em>*</em>条件字段</div>
          <input v-model="currentSegment.config.field" class="mdm-input-normal" placeholder="业务字段名，如: materialType" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">条件规则</div>
          <textarea v-model="currentSegment.config.rulesJson" class="mdm-textarea" rows="4" placeholder='[{"when":"产成品","then":"5"}]'></textarea>
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">默认值</div>
          <input v-model="currentSegment.config.default" class="mdm-input-normal" placeholder="未匹配时的默认值" />
        </div>
      </template>

      <!-- 字段引用配置 -->
      <template v-if="currentSegment?.type === 'field'">
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>引用字段</div>
          <input v-model="currentSegment.config.source" class="mdm-input-normal" placeholder="业务字段名" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">长度限制</div>
          <input v-model.number="currentSegment.config.length" type="number" class="mdm-input-normal" placeholder="不限制请留空" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">填充字符</div>
          <input v-model="currentSegment.config.padding" class="mdm-input-normal" placeholder="不足时填充" />
        </div>
      </template>

      <!-- 分类编码配置 -->
      <template v-if="currentSegment?.type === 'category'">
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>分类字段</div>
          <input v-model="currentSegment.config.field" class="mdm-input-normal" placeholder="分类字段名，如: materialGroup" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">映射配置</div>
          <textarea v-model="currentSegment.config.mappingJson" class="mdm-textarea" rows="4" placeholder='{"分类值":"编码"}'></textarea>
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">默认值</div>
          <input v-model="currentSegment.config.default" class="mdm-input-normal" placeholder="未匹配时的默认值" />
        </div>
      </template>

      <template #footer>
        <button class="mdm-btn-cancel" @click="segmentDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSaveSegment">保存</button>
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
</style>
