<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTemplateList,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  publishTemplate,
  resetTemplate,
  type QualityRuleTemplate
} from '../../../api/quality/template'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

const tableData = ref<QualityRuleTemplate[]>([])
const loading = ref(false)

// 搜索
const searchForm = ref({
  templateType: '',
  status: ''
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增模板')
const isView = ref(false) // 是否查看模式

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单
const form = ref<QualityRuleTemplate>({
  templateCode: '',
  templateName: '',
  templateType: '',
  checkType: '',
  checkConfig: '',
  severity: 'warning',
  description: '',
  status: 'draft',
  category: '',
  tags: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 模板类型选项
const TEMPLATE_TYPES = [
  { value: 'completeness', label: '完整性' },
  { value: 'uniqueness', label: '唯一性' },
  { value: 'accuracy', label: '准确性' },
  { value: 'consistency', label: '一致性' },
  { value: 'timeliness', label: '及时性' }
]

// 状态选项
const STATUS_OPTIONS = [
  { value: '', label: '全部状态' },
  { value: 'draft', label: '草稿' },
  { value: 'published', label: '启用' }
]

// 获取模板列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getTemplateList(searchForm.value)
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

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchData()
}

// 重置搜索
function handleResetSearch() {
  searchForm.value = {
    templateType: '',
    status: ''
  }
  currentPage.value = 1
  fetchData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增模板'
  isView.value = false
  form.value = {
    templateCode: '',
    templateName: '',
    templateType: '',
    checkType: '',
    checkConfig: '',
    severity: 'warning',
    description: '',
    status: 'draft',
    category: '',
    tags: ''
  }
  dialogVisible.value = true
}

// 查看
function handleView(row: QualityRuleTemplate) {
  dialogTitle.value = '查看模板'
  isView.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: QualityRuleTemplate) {
  // active 表示启用状态，不能编辑
  if (row.status === 'active') {
    ElMessage.warning('启用状态的模板不能编辑')
    return
  }
  dialogTitle.value = '编辑模板'
  isView.value = false
  form.value = { ...row }
  dialogVisible.value = true
}

// 删除
function handleDelete(row: QualityRuleTemplate) {
  // active 表示启用状态，不能删除
  if (row.status === 'active') {
    ElMessage.warning('启用状态的模板不能删除')
    return
  }
  confirmMessage.value = `确定要删除模板「${row.templateName}」吗？`
  confirmAction.value = async () => {
    await deleteTemplate(row.id!)
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

// 发布（草稿 -> 启用）
function handlePublish(row: QualityRuleTemplate) {
  confirmMessage.value = `确定要发布模板「${row.templateName}」吗？发布后将启用。`
  confirmAction.value = async () => {
    try {
      await publishTemplate(row.id!)
      ElMessage.success('发布成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.message || '发布失败')
    }
  }
  confirmVisible.value = true
}

// 重置模板状态（启用 -> 草稿）
function handleResetStatus(row: QualityRuleTemplate) {
  confirmMessage.value = `确定要重置模板「${row.templateName}」为草稿状态吗？`
  confirmAction.value = async () => {
    try {
      await resetTemplate(row.id!)
      ElMessage.success('重置成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.message || '重置失败')
    }
  }
  confirmVisible.value = true
}

// 提交
async function handleSubmit() {
  // 表单验证
  if (!form.value.templateCode) {
    ElMessage.warning('请输入模板编码')
    return
  }
  if (!form.value.templateName) {
    ElMessage.warning('请输入模板名称')
    return
  }

  try {
    if (form.value.id) {
      await updateTemplate(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createTemplate(form.value)
      ElMessage.success('创建成功，已保存为草稿')
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

// 获取模板类型标签
function getTemplateTypeLabel(type: string) {
  const item = TEMPLATE_TYPES.find(t => t.value === type)
  return item?.label || type
}

// 获取状态标签
function getStatusLabel(status: string) {
  const labels: Record<string, string> = {
    draft: '草稿',
    published: '启用',
    active: '启用',
    inactive: '停用'
  }
  return labels[status] || status
}

// 获取状态样式类
function getStatusClass(status: string) {
  // 统一处理状态值
  const normalizedStatus = status === 'active' ? 'published' : (status === 'inactive' ? 'draft' : status)
  const classMap: Record<string, string> = {
    draft: 'status-draft',
    published: 'status-published'
  }
  return classMap[normalizedStatus] || 'status-draft'
}

// 获取严重级别标签
function getSeverityLabel(severity: string) {
  const labels: Record<string, string> = {
    warning: '警告',
    error: '错误',
    critical: '严重'
  }
  return labels[severity] || severity
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
})
</script>

<template>
  <div class="mdm-container">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <select v-model="searchForm.templateType" @change="handleSearch">
            <option value="">全部类型</option>
            <option v-for="item in TEMPLATE_TYPES" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-red" @click="handleAdd">+ 新增</button>
        <button class="mdm-btn-outline" @click="handleSearch">查询</button>
        <button class="mdm-btn-outline" @click="handleResetSearch">重置</button>
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
            <th>模板编码</th>
            <th>模板名称</th>
            <th>类型</th>
            <th>分类</th>
            <th>检查类型</th>
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
            <td>{{ row.templateCode }}</td>
            <td>
              {{ row.templateName }}
              <span v-if="row.tags" class="template-tags">{{ row.tags }}</span>
            </td>
            <td>
              <span class="template-type-tag">{{ getTemplateTypeLabel(row.templateType || '') }}</span>
            </td>
            <td>{{ row.category || '-' }}</td>
            <td>{{ row.checkType || '-' }}</td>
            <td>
              <span class="severity-tag" :class="getSeverityClass(row.severity || 'warning')">
                {{ getSeverityLabel(row.severity || 'warning') }}
              </span>
            </td>
            <td>
              <span class="status-tag" :class="getStatusClass(row.status || 'draft')">
                {{ getStatusLabel(row.status || 'draft') }}
              </span>
            </td>
            <td>
              <div class="mdm-action-buttons">
                <!-- 草稿状态(inactive)：查看、编辑、删除、发布 -->
                <template v-if="row.status !== 'active'">
                  <button class="mdm-action-btn" @click="handleView(row)">查看</button>
                  <button class="mdm-action-btn" @click="handleEdit(row)">编辑</button>
                  <button class="mdm-action-btn delete" @click="handleDelete(row)">删除</button>
                  <button class="mdm-action-btn publish" @click="handlePublish(row)">发布</button>
                </template>
                <!-- 启用状态(active)：查看、重置 -->
                <template v-else>
                  <button class="mdm-action-btn" @click="handleView(row)">查看</button>
                  <button class="mdm-action-btn reset" @click="handleResetStatus(row)">重置</button>
                </template>
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

    <!-- 新增/编辑/查看弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>模板编码</div>
        <input v-model="form.templateCode" class="mdm-input-yellow" placeholder="请输入模板编码" :readonly="isView" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>模板名称</div>
        <input v-model="form.templateName" class="mdm-input-yellow" placeholder="请输入模板名称" :readonly="isView" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">模板类型</div>
        <select v-model="form.templateType" class="mdm-select" :disabled="isView">
          <option value="">请选择</option>
          <option v-for="item in TEMPLATE_TYPES" :key="item.value" :value="item.value">{{ item.label }}</option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">检查类型</div>
        <input v-model="form.checkType" class="mdm-input-yellow" placeholder="请输入检查类型" :readonly="isView" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">检查配置</div>
        <textarea v-model="form.checkConfig" class="mdm-textarea" placeholder="请输入检查配置JSON" :readonly="isView"></textarea>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">严重级别</div>
        <div class="mdm-radio-group">
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.severity" value="warning" :disabled="isView" />
            警告
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.severity" value="error" :disabled="isView" />
            错误
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.severity" value="critical" :disabled="isView" />
            严重
          </label>
        </div>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="form.description" class="mdm-textarea" placeholder="请输入描述" :readonly="isView"></textarea>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</button>
        <button v-if="!isView" class="mdm-btn-primary" @click="handleSubmit">保存为草稿</button>
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

.template-type-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 12px;
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.status-draft {
  background: #f5f5f5;
  color: #8c8c8c;
}

.status-tag.status-published {
  background: #f6ffed;
  color: #52c41a;
}

/* 模板标签 */
.template-tags {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #8c8c8c;
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

/* 发布按钮 */
.mdm-action-btn.publish {
  background: #f6ffed;
  color: #52c41a;
}

.mdm-action-btn.publish:hover {
  background: #d9f7be;
}

/* 重置按钮 */
.mdm-action-btn.reset {
  background: #fff7e6;
  color: #fa8c16;
}

.mdm-action-btn.reset:hover {
  background: #ffd591;
}
</style>
