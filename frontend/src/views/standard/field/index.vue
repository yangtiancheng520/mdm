<script setup lang="ts">
/**
 * 字段标准库管理页面
 */

import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getFieldStandardList,
  createFieldStandard,
  updateFieldStandard,
  deleteFieldStandard,
  batchDeleteFieldStandard,
  publishFieldStandard,
  archiveFieldStandard,
  type FieldStandard,
  type FieldStandardForm,
  type FieldStatus,
  type FieldType,
  FIELD_TYPE_OPTIONS,
  FIELD_STATUS_OPTIONS
} from '../../../api/standard/fieldStandard'
import {
  getActiveCategoryTree,
  type FieldCategory
} from '../../../api/standard/fieldCategory'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'
import CategoryTreeSelect from '../../../components/common/CategoryTreeSelect.vue'

// 表格数据
const tableData = ref<FieldStandard[]>([])
const loading = ref(false)
const selectedRows = ref<FieldStandard[]>([])

// 分类树数据
const categoryTreeData = ref<FieldCategory[]>([])

// 搜索表单
const searchForm = ref({
  fieldCode: '',
  fieldName: '',
  status: '' as FieldStatus | '',
  categoryId: null as number | null
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增字段')

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单数据
const form = ref<FieldStandardForm>({
  fieldCode: '',
  fieldName: '',
  fieldType: 'string',
  length: null,
  precision: null,
  defaultValue: '',
  required: false,
  categoryId: null,
  status: 'draft',
  description: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取分类树
async function fetchCategoryTree() {
  try {
    const res = await getActiveCategoryTree()
    categoryTreeData.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 获取分类名称
function getCategoryName(categoryId: number | null): string {
  if (!categoryId) return '-'

  // 递归查找分类名称
  function findCategory(categories: FieldCategory[], id: number): string | null {
    for (const cat of categories) {
      if (cat.id === id) return cat.categoryName
      if (cat.children) {
        const found = findCategory(cat.children, id)
        if (found) return found
      }
    }
    return null
  }

  return findCategory(categoryTreeData.value, categoryId) || '-'
}

// 计算当前状态标签样式
const getStatusClass = (status: FieldStatus) => {
  switch (status) {
    case 'draft':
      return 'status-draft'
    case 'published':
      return 'status-published'
    case 'archived':
      return 'status-archived'
    default:
      return ''
  }
}

// 获取状态标签文本
const getStatusLabel = (status: FieldStatus) => {
  const option = FIELD_STATUS_OPTIONS.find(o => o.value === status)
  return option?.label || status
}

// 获取字段类型标签
const getFieldTypeLabel = (type: FieldType) => {
  const option = FIELD_TYPE_OPTIONS.find(o => o.value === type)
  return option?.label || type
}

// 获取字段列表
async function fetchData() {
  loading.value = true
  try {
    const params = {
      ...searchForm.value,
      page: currentPage.value,
      size: pageSize.value
    }
    console.log('===== 开始请求 =====')
    console.log('请求参数:', params)

    const res = await getFieldStandardList(params)

    console.log('===== API返回 =====')
    console.log('完整响应:', res)
    console.log('res.data:', res.data)
    console.log('res.data.list:', res.data?.list)
    console.log('res.data.total:', res.data?.total)

    if (res && res.data) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
      console.log('===== 赋值后 =====')
      console.log('tableData.value:', tableData.value)
      console.log('tableData.value.length:', tableData.value.length)
      console.log('total.value:', total.value)
    } else {
      console.error('API返回数据格式不正确:', res)
    }
  } catch (error) {
    console.error('===== 获取数据失败 =====')
    console.error('错误:', error)
  } finally {
    loading.value = false
    console.log('===== 请求结束 =====')
    console.log('最终tableData:', tableData.value)
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
    fieldCode: '',
    fieldName: '',
    status: '',
    categoryId: null
  }
  currentPage.value = 1
  fetchData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增字段'
  form.value = {
    fieldCode: '',
    fieldName: '',
    fieldType: 'string',
    length: null,
    precision: null,
    defaultValue: '',
    required: false,
    categoryId: null,
    status: 'draft',
    description: ''
  }
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: FieldStandard) {
  dialogTitle.value = '编辑字段'
  form.value = {
    id: row.id,
    fieldCode: row.fieldCode,
    fieldName: row.fieldName,
    fieldType: row.fieldType,
    length: row.length,
    precision: row.precision,
    defaultValue: row.defaultValue || '',
    required: row.required,
    categoryId: row.categoryId,
    status: row.status,
    description: row.description || ''
  }
  dialogVisible.value = true
}

// 删除
function handleDelete(row: FieldStandard) {
  confirmMessage.value = `确定要删除字段「${row.fieldName}」吗？`
  confirmAction.value = async () => {
    try {
      await deleteFieldStandard(row.id)
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
    ElMessage.warning('请选择要删除的字段')
    return
  }

  confirmMessage.value = `确定要删除选中的 ${selectedRows.value.length} 个字段吗？`
  confirmAction.value = async () => {
    try {
      await batchDeleteFieldStandard(selectedRows.value.map(r => r.id))
      ElMessage.success('删除成功')
      selectedRows.value = []
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
  confirmVisible.value = true
}

// 发布
async function handlePublish(row: FieldStandard) {
  try {
    await publishFieldStandard(row.id)
    ElMessage.success('发布成功')
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '发布失败')
  }
}

// 归档
function handleArchive(row: FieldStandard) {
  confirmMessage.value = `确定要归档字段「${row.fieldName}」吗？`
  confirmAction.value = async () => {
    try {
      await archiveFieldStandard(row.id)
      ElMessage.success('归档成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '归档失败')
    }
  }
  confirmVisible.value = true
}

// 选择变化
function handleSelectionChange(rows: FieldStandard[]) {
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
function isRowSelected(row: FieldStandard) {
  return selectedRows.value.some(r => r.id === row.id)
}

// 切换行选择
function toggleRowSelect(row: FieldStandard) {
  const index = selectedRows.value.findIndex(r => r.id === row.id)
  if (index > -1) {
    selectedRows.value.splice(index, 1)
  } else {
    selectedRows.value.push(row)
  }
}

// 确认删除
function handleConfirmDelete() {
  if (confirmAction.value) {
    confirmAction.value()
  }
}

// 提交表单
async function handleSubmit() {
  // 表单验证
  if (!form.value.fieldCode) {
    ElMessage.warning('请输入字段编码')
    return
  }
  if (!form.value.fieldName) {
    ElMessage.warning('请输入字段名称')
    return
  }

  try {
    if (form.value.id) {
      await updateFieldStandard(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createFieldStandard(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
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

// 获取操作按钮
function getActionButtons(row: FieldStandard) {
  const buttons: { label: string; action: () => void; class?: string }[] = [
    { label: '编辑', action: () => handleEdit(row) },
    { label: '删除', action: () => handleDelete(row), class: 'delete' }
  ]

  // 草稿状态可以发布
  if (row.status === 'draft') {
    buttons.splice(1, 0, { label: '发布', action: () => handlePublish(row) })
  }

  // 已发布状态可以归档
  if (row.status === 'published') {
    buttons.splice(1, 0, { label: '归档', action: () => handleArchive(row) })
  }

  return buttons
}

onMounted(() => {
  console.log('字段标准库页面已挂载')
  fetchCategoryTree()
  fetchData()
})
</script>

<template>
  <div class="field-standard-page">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <input v-model="searchForm.fieldCode" type="text" placeholder="字段编码" @keyup.enter="handleSearch" />
        </div>
        <div class="mdm-filter-item">
          <input v-model="searchForm.fieldName" type="text" placeholder="字段名称" @keyup.enter="handleSearch" />
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option v-for="opt in FIELD_STATUS_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <CategoryTreeSelect
            v-model="searchForm.categoryId"
            placeholder="全部分类"
            :show-all="true"
            :show-search="true"
            @change="handleSearch"
          />
        </div>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-red" @click="handleAdd">+ 新增</button>
        <button class="mdm-btn-outline" :disabled="selectedRows.length === 0" @click="handleBatchDelete">删除</button>
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
            <th>字段编码</th>
            <th>字段名称</th>
            <th>字段类型</th>
            <th>长度</th>
            <th>分类</th>
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
            <td>{{ row.fieldCode }}</td>
            <td>{{ row.fieldName }}</td>
            <td>{{ getFieldTypeLabel(row.fieldType) }}</td>
            <td>{{ row.length || '-' }}</td>
            <td>{{ getCategoryName(row.categoryId) }}</td>
            <td>
              <span class="custom-status-tag" :class="getStatusClass(row.status)">
                {{ getStatusLabel(row.status) }}
              </span>
            </td>
            <td>{{ row.createdAt }}</td>
            <td>
              <div class="mdm-action-buttons">
                <template v-for="(btn, index) in getActionButtons(row)" :key="index">
                  <button
                    class="mdm-action-btn"
                    :class="btn.class"
                    @click="btn.action"
                  >
                    {{ btn.label }}
                  </button>
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
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>字段编码</div>
        <input v-model="form.fieldCode" class="mdm-input-yellow" placeholder="请输入字段编码" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>字段名称</div>
        <input v-model="form.fieldName" class="mdm-input-yellow" placeholder="请输入字段名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">字段类型</div>
        <select v-model="form.fieldType" class="mdm-select">
          <option v-for="opt in FIELD_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">长度</div>
        <input v-model.number="form.length" type="number" class="mdm-input-normal" placeholder="请输入长度" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">精度</div>
        <input v-model.number="form.precision" type="number" class="mdm-input-normal" placeholder="请输入精度" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">默认值</div>
        <input v-model="form.defaultValue" class="mdm-input-normal" placeholder="请输入默认值" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">是否必填</div>
        <div class="mdm-radio-group">
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.required" :value="true" />
            是
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.required" :value="false" />
            否
          </label>
        </div>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">字段分类</div>
        <CategoryTreeSelect
          v-model="form.categoryId"
          placeholder="请选择分类"
          :show-search="true"
        />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>状态</div>
        <div class="mdm-radio-group">
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.status" value="draft" />
            草稿
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.status" value="published" />
            已发布
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="form.status" value="archived" />
            已归档
          </label>
        </div>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="form.description" class="mdm-textarea" placeholder="请输入描述"></textarea>
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

<style scoped lang="scss">
@use '../../../assets/styles/mdm-common.scss' as *;

.field-standard-page {
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
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #8c8c8c;
}

.status-published {
  background: #fff5f5;
  border-color: #ffa39e;
  color: #ed2b33;
}

.status-archived {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #8c8c8c;
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
</style>
