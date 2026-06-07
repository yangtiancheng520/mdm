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
                <button v-if="row.status === 'draft'" class="action-btn delete" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="tableData.length === 0">
            <td colspan="7" class="empty-row">暂无数据</td>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MdmDialog from '@/components/MdmDialog.vue'
import MdmConfirmDialog from '@/components/MdmConfirmDialog.vue'
import {
  getFormList,
  createForm,
  updateForm,
  deleteForm,
  publishForm,
  type FormDto,
  type FormDesignRequest
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
const pendingAction = ref<'publish' | 'delete' | null>(null)
const pendingRow = ref<FormDto | null>(null)

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

    // 获取视图名称
    data.forEach(item => {
      const view = viewList.value.find(v => v.id === item.viewId)
      item.viewName = view?.viewName || '-'
    })

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
    } else if (pendingAction.value === 'delete') {
      await deleteForm(pendingRow.value.id)
      ElMessage.success('删除成功')
    }
    loadData()
  } catch (error) {
    ElMessage.error(pendingAction.value === 'publish' ? '发布失败' : '删除失败')
  } finally {
    pendingAction.value = null
    pendingRow.value = null
  }
}

onMounted(() => {
  loadViewList().then(() => {
    loadData()
  })
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
    &.delete { color: #f56c6c; }

    &:hover {
      background: #f5f5f5;
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
