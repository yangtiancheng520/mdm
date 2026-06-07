<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getViewList,
  getViewDetail,
  createView,
  updateView,
  publishView,
  disableView,
  enableView,
  reviseView,
  deleteView,
  getVersionHistory,
  type ViewDefinition,
  type ViewEntity
} from '@/api/standard/viewDefinition'
import {
  getViewCategoryTree,
  createViewCategory,
  updateViewCategory,
  deleteViewCategory,
  type ViewCategory
} from '@/api/standard/viewCategory'
import MdmDialog from '@/components/MdmDialog.vue'
import MdmConfirmDialog from '@/components/MdmConfirmDialog.vue'
import TreeNode from './TreeNode.vue'

const router = useRouter()
const route = useRoute()

// 状态
const loading = ref(false)
const tableData = ref<ViewDefinition[]>([])
const selectedRows = ref<ViewDefinition[]>([])
const selectedRow = ref<ViewDefinition | null>(null) // 单选发布用的变量

// 分类树数据
const categoryTreeData = ref<ViewCategory[]>([])
const selectedCategoryId = ref<number | null>(null)

// 查询参数
const queryParams = reactive({
  keyword: '',
  status: '',
  categoryId: undefined as number | undefined
})

// 分类列表（扁平化）
const categories = ref<ViewCategory[]>([])

// 视图对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增视图')
const isEdit = ref(false)
const isViewOnly = ref(false) // 是否为查看模式
const editViewStatus = ref('') // 编辑时的视图状态
const formData = ref<ViewDefinition>({
  viewCode: '',
  viewName: '',
  categoryId: undefined,
  description: ''
})

// 查看详情
const viewDialogVisible = ref(false)
const viewData = ref<ViewDefinition>({})

// 历史版本
const historyDialogVisible = ref(false)
const historyViewCode = ref('')
const historyList = ref<ViewDefinition[]>([])

// 删除确认
const confirmVisible = ref(false)
const confirmMessage = ref('')
const pendingDeleteView = ref<ViewDefinition | null>(null)

// 展开的分类ID列表
const expandedIds = ref<number[]>([])

// 展开/折叠
const toggleExpand = (id: number) => {
  const index = expandedIds.value.indexOf(id)
  if (index > -1) {
    expandedIds.value.splice(index, 1)
  } else {
    expandedIds.value.push(id)
  }
}

// 分类对话框
const categoryDialogVisible = ref(false)
const categoryDialogTitle = ref('新增分类')
const isCategoryEdit = ref(false)
const categoryForm = ref<ViewCategory>({
  categoryCode: '',
  categoryName: '',
  parentId: undefined,
  sort: 0,
  status: 'active',
  description: ''
})

// 获取分类树
const fetchCategoryTree = async () => {
  try {
    const res = await getViewCategoryTree()
    if (res.code === 200) {
      categoryTreeData.value = res.data || []
      categories.value = flattenTree(res.data || [])
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 扁平化树
const flattenTree = (tree: ViewCategory[]): ViewCategory[] => {
  const result: ViewCategory[] = []
  const flatten = (nodes: ViewCategory[], level = 0) => {
    nodes.forEach(node => {
      result.push({ ...node, level } as any)
      if (node.children && node.children.length > 0) {
        flatten(node.children, level + 1)
      }
    })
  }
  flatten(tree)
  return result
}

// 选择分类
const handleCategorySelect = (category: ViewCategory | null) => {
  selectedCategoryId.value = category?.id || null
  queryParams.categoryId = category?.id || undefined
  loadData()
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
const handleSelectRow = (row: ViewDefinition) => {
  const index = selectedRows.value.findIndex(r => r.id === row.id)
  if (index > -1) {
    selectedRows.value.splice(index, 1)
  } else {
    selectedRows.value.push(row)
  }
}

// 新增分类
const handleAddCategory = (parent?: ViewCategory) => {
  isCategoryEdit.value = false
  categoryDialogTitle.value = '新增分类'
  categoryForm.value = {
    categoryCode: '',
    categoryName: '',
    parentId: parent?.id || undefined,
    sort: 0,
    status: 'active',
    description: ''
  }
  categoryDialogVisible.value = true
}

// 编辑分类
const handleEditCategory = (category: ViewCategory) => {
  isCategoryEdit.value = true
  categoryDialogTitle.value = '编辑分类'
  categoryForm.value = { ...category }
  categoryDialogVisible.value = true
}

// 删除分类
const handleDeleteCategory = async (category: ViewCategory) => {
  try {
    if (category.children && category.children.length > 0) {
      ElMessage.warning('该分类下有子分类，不能删除')
      return
    }
    if (category.viewCount && category.viewCount > 0) {
      ElMessage.warning(`该分类下有 ${category.viewCount} 个视图，不能删除`)
      return
    }
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', { type: 'warning' })
    const res = await deleteViewCategory(category.id!)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCategoryTree()
      if (selectedCategoryId.value === category.id) {
        selectedCategoryId.value = null
        queryParams.categoryId = undefined
        loadData()
      }
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    // 取消操作
  }
}

// 提交分类表单
const handleSubmitCategory = async () => {
  if (!categoryForm.value.categoryCode) {
    ElMessage.warning('请输入分类编码')
    return
  }
  if (!categoryForm.value.categoryName) {
    ElMessage.warning('请输入分类名称')
    return
  }

  const currentUser = localStorage.getItem('username') || 'admin'
  categoryForm.value.createdBy = currentUser
  categoryForm.value.updatedBy = currentUser

  try {
    let res
    if (isCategoryEdit.value) {
      res = await updateViewCategory(categoryForm.value.id!, categoryForm.value)
    } else {
      res = await createViewCategory(categoryForm.value)
    }

    if (res.code === 200) {
      ElMessage.success(isCategoryEdit.value ? '更新成功' : '创建成功')
      categoryDialogVisible.value = false
      fetchCategoryTree()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getViewList({
      keyword: queryParams.keyword || undefined,
      status: queryParams.status || undefined,
      categoryId: queryParams.categoryId
    })
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  loadData()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = ''
  queryParams.categoryId = selectedCategoryId.value || undefined
  loadData()
}

// 新增视图
const handleAdd = () => {
  isEdit.value = false
  isViewOnly.value = false
  editViewStatus.value = ''
  dialogTitle.value = '新增视图'
  formData.value = {
    viewCode: '',
    viewName: '',
    categoryId: undefined,
    description: ''
  }
  dialogVisible.value = true
}

// 编辑视图（基本信息）
const handleEdit = async (row: ViewDefinition) => {
  isEdit.value = true
  isViewOnly.value = false
  editViewStatus.value = row.status || ''
  dialogTitle.value = '编辑视图'
  try {
    const res = await getViewDetail(row.id!)
    if (res.code === 200) {
      formData.value = {
        id: res.data.id,
        viewCode: res.data.viewCode,
        viewName: res.data.viewName,
        categoryId: res.data.categoryId,
        description: res.data.description
      }
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败', error)
  }
}

// 提交视图表单
const handleSubmitView = async () => {
  if (!formData.value.viewCode) {
    ElMessage.warning('请输入视图编码')
    return
  }
  if (!formData.value.viewName) {
    ElMessage.warning('请输入视图名称')
    return
  }
  if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(formData.value.viewCode)) {
    ElMessage.warning('视图编码只能包含字母、数字、下划线，且必须以字母开头')
    return
  }

  const currentUser = localStorage.getItem('username') || 'admin'
  formData.value.createdBy = currentUser
  formData.value.updatedBy = currentUser

  try {
    let res
    if (isEdit.value) {
      res = await updateView(formData.value.id!, formData.value)
    } else {
      res = await createView(formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error: any) {
    console.error('提交失败', error)
    ElMessage.error(error.response?.data?.message || error.message || '操作失败')
  }
}

// 设计视图（实体、分组、字段）
const handleDesign = (row: ViewDefinition) => {
  router.push(`/standard/view/design/${row.id}`)
}

// 查看视图
const handleView = async (row: ViewDefinition) => {
  try {
    const res = await getViewDetail(row.id!)
    if (res.code === 200) {
      viewData.value = res.data
      viewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取详情失败', error)
  }
}

// 删除视图
const handleDelete = (row: ViewDefinition) => {
  pendingDeleteView.value = row
  if (row.status === 'revising') {
    confirmMessage.value = '确定要放弃该修订吗？放弃后主干版本保持不变。'
  } else {
    confirmMessage.value = '确定要删除该视图吗？'
  }
  confirmVisible.value = true
}

// 确认删除
const handleConfirmDelete = async () => {
  if (!pendingDeleteView.value) return
  try {
    const res = await deleteView(pendingDeleteView.value.id!)
    if (res.code === 200) {
      ElMessage.success(pendingDeleteView.value.status === 'revising' ? '已放弃修订' : '删除成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('删除失败', error)
  } finally {
    pendingDeleteView.value = null
  }
}

// 查看数据
const handlePublish = async () => {
  // 校验：只能选择一条
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要发布的视图')
    return
  }
  if (selectedRows.value.length > 1) {
    ElMessage.warning('只能选择一个视图进行发布')
    return
  }

  const row = selectedRows.value[0]

  // 校验：状态必须是草稿或修订中
  if (row.status === 'published') {
    ElMessage.warning('该视图已发布，请创建新版本后再发布')
    return
  }
  if (row.status === 'disabled') {
    ElMessage.warning('该视图已停用，请先启用')
    return
  }

  // 获取视图详情，检查数据有效性
  try {
    const res = await getViewDetail(row.id!)
    if (res.code !== 200 || !res.data) {
      ElMessage.error('获取视图详情失败')
      return
    }

    const viewDetail = res.data

    // 校验：主表必须有字段
    const mainEntity = viewDetail.entities?.find(e => e.entityType === 'main')
    if (!mainEntity) {
      ElMessage.warning('视图缺少主表，请先设计主表')
      return
    }
    if (!mainEntity.fields || mainEntity.fields.length === 0) {
      ElMessage.warning('主表没有字段，请先添加字段')
      return
    }

    // 准备确认信息
    const isRevising = viewDetail.status === 'revising'
    const mainTableName = `mdm_${viewDetail.viewCode?.toLowerCase()}`

    // 检查哪些表需要新建，哪些需要更新
    const checkTableExists = async (tableName: string) => {
      // 这里简化处理，实际应该调用后端接口检查
      // 修订版本默认主表和继承的子表已存在，新增的子表需要创建
      return isRevising
    }

    let confirmMessage = `确定要发布视图【${viewDetail.viewName}】吗？\n\n`

    if (isRevising) {
      // 修订版本发布
      const inheritedEntities = viewDetail.entities?.filter(e => e.isInherited) || []
      const newEntities = viewDetail.entities?.filter(e => !e.isInherited) || []

      confirmMessage += `此操作将：\n\n`

      // 更新的表
      if (inheritedEntities.length > 0 || viewDetail.entities?.some(e => e.entityType === 'main')) {
        confirmMessage += `📋 更新已有表：\n`
        confirmMessage += `  • ${mainTableName}（主表）\n`
        inheritedEntities.forEach(entity => {
          if (entity.entityType === 'sub') {
            confirmMessage += `  • mdm_${viewDetail.viewCode?.toLowerCase()}_${entity.entityCode?.toLowerCase()}（${entity.entityName}）\n`
          }
        })
        confirmMessage += `\n`
      }

      // 新建的表
      if (newEntities.length > 0) {
        confirmMessage += `➕ 新建表：\n`
        newEntities.forEach(entity => {
          if (entity.entityType === 'sub') {
            confirmMessage += `  • mdm_${viewDetail.viewCode?.toLowerCase()}_${entity.entityCode?.toLowerCase()}（${entity.entityName}）\n`
          }
        })
        confirmMessage += `\n`
      }

      confirmMessage += `发布后将成为新版本V${(viewDetail.version || 1) + 1}，原版本将变为历史版本。`
    } else {
      // 新视图发布
      const subTableNames = viewDetail.entities
        ?.filter(e => e.entityType === 'sub')
        .map(sub => `mdm_${viewDetail.viewCode?.toLowerCase()}_${sub.entityCode?.toLowerCase()}`) || []

      confirmMessage += `将创建以下物理表：\n`
      confirmMessage += `• ${mainTableName}（主表）\n`
      if (subTableNames.length > 0) {
        confirmMessage += subTableNames.map(t => `• ${t}（子表）`).join('\n')
      }
    }

    // 确认弹窗
    await ElMessageBox.confirm(
      confirmMessage,
      '发布确认',
      {
        type: 'warning',
        confirmButtonText: '确定发布',
        cancelButtonText: '取消'
      }
    )

    // 执行发布
    const publishRes = await publishView(row.id!)
    if (publishRes.code === 200) {
      ElMessage.success('发布成功')
      selectedRows.value = [] // 清空选中
      loadData()
    } else {
      ElMessage.error(publishRes.message || '发布失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('发布失败', error)
      ElMessage.error(error.response?.data?.message || error.message || '发布失败')
    }
  }
}

// 查看历史版本
const handleHistory = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择一个视图查看历史版本')
    return
  }
  if (selectedRows.value.length > 1) {
    ElMessage.warning('只能选择一个视图')
    return
  }

  const row = selectedRows.value[0]
  try {
    const res = await getViewDetail(row.id!)
    if (res.code === 200 && res.data) {
      historyViewCode.value = res.data.viewCode || ''
      // 调用后端接口获取历史版本列表
      const historyRes = await getVersionHistory(historyViewCode.value)
      if (historyRes.code === 200) {
        historyList.value = historyRes.data || []
      } else {
        historyList.value = [res.data]
      }
      historyDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取历史版本失败', error)
  }
}

// 查看视图
const handleViewData = (row: ViewDefinition) => {
  // 跳转到设计页面，但是只读模式
  router.push(`/standard/view/design/${row.id}?mode=view`)
}

// 修订视图
const handleRevise = async (row: ViewDefinition) => {
  try {
    await ElMessageBox.confirm(
      `确定要修订视图【${row.viewName}】吗？\n将创建新版本进行修改，当前版本保持不变。`,
      '修订确认',
      { type: 'info', confirmButtonText: '确定', cancelButtonText: '取消' }
    )

    const res = await reviseView(row.id!)
    if (res.code === 200) {
      ElMessage.success('已创建新版本，请进行设计')
      selectedRows.value = []
      router.push(`/standard/view/design/${res.data.id}`)
    } else {
      ElMessage.error(res.message || '修订失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '修订失败')
    }
  }
}

// 停用视图
const handleDisable = async (row: ViewDefinition) => {
  try {
    await ElMessageBox.confirm(
      `确定要停用视图【${row.viewName}】吗？\n停用后将无法录入数据，但可以重新启用。`,
      '停用确认',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )

    const res = await disableView(row.id!)
    if (res.code === 200) {
      ElMessage.success('停用成功')
      selectedRows.value = []
      loadData()
    } else {
      ElMessage.error(res.message || '停用失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '停用失败')
    }
  }
}

// 启用视图
const handleEnable = async (row: ViewDefinition) => {
  try {
    await ElMessageBox.confirm(
      `确定要启用视图【${row.viewName}】吗？`,
      '启用确认',
      { type: 'info', confirmButtonText: '确定', cancelButtonText: '取消' }
    )

    const res = await enableView(row.id!)
    if (res.code === 200) {
      ElMessage.success('启用成功')
      selectedRows.value = []
      loadData()
    } else {
      ElMessage.error(res.message || '启用失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '启用失败')
    }
  }
}

// 格式化日期时间
const formatDateTime = (datetime: string | undefined) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN')
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    draft: '草稿',
    revising: '修订中',
    published: '已发布',
    disabled: '已停用',
    history: '历史版本'
  }
  return labels[status] || status
}

// 获取实体数量
const getEntityCount = (entities: ViewEntity[] | undefined) => {
  if (!entities) return 0
  return entities.length
}

// 获取字段数量
const getFieldCount = (entities: ViewEntity[] | undefined) => {
  if (!entities) return 0
  return entities.reduce((sum, e) => sum + (e.fields?.length || 0), 0)
}

// 初始化
onMounted(() => {
  fetchCategoryTree()
  loadData()
})
</script>

<template>
  <div class="view-definition-page">
    <!-- 左侧分类树 -->
    <div class="left-panel">
      <div class="panel-header">
        <h3>视图分类</h3>
        <div class="header-actions">
          <button class="add-btn" @click="handleAddCategory()">+ 新增</button>
        </div>
      </div>
      <div class="panel-content">
        <!-- 全部分类 -->
        <div
          class="tree-node root"
          :class="{ active: selectedCategoryId === null }"
          @click="handleCategorySelect(null)"
        >
          <span class="node-icon">📁</span>
          <span class="node-label">全部分类</span>
        </div>

        <!-- 分类树 -->
        <div class="tree-list">
          <template v-for="category in categoryTreeData" :key="category.id">
            <div class="tree-item">
              <TreeNode
                :node="category"
                :selected-id="selectedCategoryId"
                :expanded-ids="expandedIds"
                :level="0"
                @select="handleCategorySelect"
                @add="handleAddCategory"
                @edit="handleEditCategory"
                @delete="handleDeleteCategory"
                @toggle="toggleExpand"
              />
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 右侧列表 -->
    <div class="right-panel">
      <!-- 顶部操作栏 -->
      <div class="mdm-top-bar">
        <div class="mdm-filter-row">
          <div class="mdm-filter-item">
            <input v-model="queryParams.keyword" type="text" placeholder="视图编码/名称" @keyup.enter="handleSearch" />
          </div>
          <div class="mdm-filter-item">
            <select v-model="queryParams.status" @change="handleSearch">
              <option value="">全部状态</option>
              <option value="draft">草稿</option>
              <option value="published">已发布</option>
              <option value="archived">已归档</option>
            </select>
          </div>
        </div>
        <div class="mdm-right-group">
          <button class="mdm-btn-outline" @click="handlePublish">发布</button>
          <span class="btn-divider"></span>
          <button class="mdm-btn-red" @click="handleAdd">+ 新增</button>
          <button class="mdm-btn-outline" @click="handleSearch">查询</button>
          <button class="mdm-btn-outline" @click="handleReset">重置</button>
          <button class="mdm-btn-outline" @click="handleHistory">历史版本</button>
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
              <th>视图编码</th>
              <th>视图名称</th>
              <th>分类</th>
              <th>状态</th>
              <th>版本</th>
              <th>更新时间</th>
              <th style="text-align: center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in tableData" :key="row.id">
              <td><input type="checkbox" :checked="selectedRows.some(r => r.id === row.id)" @change="handleSelectRow(row)" /></td>
              <td>{{ row.viewCode }}</td>
              <td>{{ row.viewName }}</td>
              <td>{{ row.categoryName || '-' }}</td>
              <td>
                <span :class="['status-tag', row.status]">{{ getStatusLabel(row.status!) }}</span>
              </td>
              <td>
                <span>V{{ row.version }}</span>
                <span v-if="row.status === 'revising'" class="version-suffix">(修订中)</span>
              </td>
              <td>{{ formatDateTime(row.updatedAt) }}</td>
              <td>
                <div class="action-btns">
                  <button v-if="row.status === 'draft'" class="action-btn design" @click="handleDesign(row)">设计</button>
                  <button v-if="row.status === 'revising'" class="action-btn design" @click="handleDesign(row)">继续设计</button>
                  <button v-if="row.status === 'draft' || row.status === 'revising'" class="action-btn edit" @click="handleEdit(row)">编辑</button>
                  <button v-if="row.status === 'published' || row.status === 'disabled'" class="action-btn view" @click="handleViewData(row)">查看</button>
                  <button v-if="row.status === 'published'" class="action-btn revise" @click="handleRevise(row)">修订</button>
                  <button v-if="row.status === 'published'" class="action-btn disable" @click="handleDisable(row)">停用</button>
                  <button v-if="row.status === 'disabled'" class="action-btn enable" @click="handleEnable(row)">启用</button>
                  <button v-if="row.status === 'draft'" class="action-btn delete" @click="handleDelete(row)">删除</button>
                  <button v-if="row.status === 'revising'" class="action-btn delete" @click="handleDelete(row)">放弃修订</button>
                </div>
              </td>
            </tr>
            <tr v-if="tableData.length === 0">
              <td colspan="8" class="empty-row">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 视图对话框 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>视图编码</div>
        <input v-model="formData.viewCode" class="mdm-input-yellow" :disabled="isEdit && editViewStatus !== 'draft'" placeholder="请输入视图编码" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>视图名称</div>
        <input v-model="formData.viewName" class="mdm-input-yellow" placeholder="请输入视图名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">所属分类</div>
        <select v-model="formData.categoryId" class="mdm-select">
          <option :value="undefined">请选择分类</option>
          <option v-for="cat in categoryTreeData" :key="cat.id" :value="cat.id">
            {{ cat.categoryName }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述说明</div>
        <textarea v-model="formData.description" class="mdm-textarea" rows="3" placeholder="请输入描述"></textarea>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSubmitView">确定</button>
      </template>
    </MdmDialog>

    <!-- 分类对话框 -->
    <MdmDialog v-model="categoryDialogVisible" :title="categoryDialogTitle" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>分类编码</div>
        <input v-model="categoryForm.categoryCode" class="mdm-input-yellow" :disabled="isCategoryEdit" placeholder="请输入分类编码" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>分类名称</div>
        <input v-model="categoryForm.categoryName" class="mdm-input-yellow" placeholder="请输入分类名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">排序</div>
        <input v-model.number="categoryForm.sort" type="number" class="mdm-input-yellow" placeholder="排序号" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">状态</div>
        <select v-model="categoryForm.status" class="mdm-select">
          <option value="active">启用</option>
          <option value="inactive">停用</option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="categoryForm.description" class="mdm-textarea" rows="2" placeholder="请输入描述"></textarea>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="categoryDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSubmitCategory">确定</button>
      </template>
    </MdmDialog>

    <!-- 历史版本对话框 -->
    <MdmDialog v-model="historyDialogVisible" title="历史版本" width="700px">
      <div class="history-header">
        <span>视图：<strong>{{ historyList[0]?.viewName }}</strong> ({{ historyViewCode }})</span>
      </div>
      <div class="history-timeline">
        <div v-for="(item, index) in historyList" :key="item.id" class="timeline-item">
          <div class="timeline-line">
            <div class="timeline-dot" :class="item.status"></div>
            <div v-if="index < historyList.length - 1" class="timeline-connector"></div>
          </div>
          <div class="timeline-content">
            <div class="timeline-header">
              <span class="version-tag">V{{ item.version }}</span>
              <span :class="['status-tag', item.status]">{{ getStatusLabel(item.status!) }}</span>
              <span v-if="item.status === 'published'" class="current-badge">当前使用</span>
            </div>
            <div class="timeline-body">
              <div class="info-row">
                <span class="label">物理表：</span>
                <span class="value table-name">{{ item.tableName || `mdm_${item.viewCode?.toLowerCase()}` }}</span>
              </div>
              <div class="info-row">
                <span class="label">发布时间：</span>
                <span class="value">{{ formatDateTime(item.publishTime) || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">更新人：</span>
                <span class="value">{{ item.updatedBy || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">字段数：</span>
                <span class="value">{{ getFieldCount(item.entities) }} 个</span>
              </div>
            </div>
            <div class="timeline-footer">
              <button class="action-btn view" @click="handleView(item)">查看详情</button>
              <button v-if="item.status === 'published'" class="action-btn data" @click="handleViewData(item)">查看数据</button>
            </div>
          </div>
        </div>
        <div v-if="historyList.length === 0" class="empty-history">
          暂无历史版本
        </div>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="historyDialogVisible = false">关闭</button>
      </template>
    </MdmDialog>

    <!-- 查看详情对话框 -->
    <MdmDialog v-model="viewDialogVisible" title="视图详情" width="800px">
      <div class="detail-section">
        <div class="detail-row">
          <span class="detail-label">视图编码：</span>
          <span class="detail-value">{{ viewData.viewCode }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">视图名称：</span>
          <span class="detail-value">{{ viewData.viewName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">分类：</span>
          <span class="detail-value">{{ viewData.categoryName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态：</span>
          <span :class="['status-tag', viewData.status]">{{ getStatusLabel(viewData.status!) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">版本：</span>
          <span class="detail-value">V{{ viewData.version }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">描述：</span>
          <span class="detail-value">{{ viewData.description || '-' }}</span>
        </div>
      </div>

      <!-- 实体列表 -->
      <h4 style="margin: 16px 0 8px; font-size: 14px;">实体结构</h4>
      <div v-for="entity in viewData.entities" :key="entity.id" class="entity-section">
        <div class="entity-header">
          <span class="entity-type" :class="entity.entityType">{{ entity.entityType === 'main' ? '主实体' : '子实体' }}</span>
          <span class="entity-name">{{ entity.entityName }}</span>
          <span class="entity-code">({{ entity.entityCode }})</span>
        </div>
        <div class="entity-fields" v-if="entity.fields && entity.fields.length > 0">
          <span class="field-count">共 {{ entity.fields.length }} 个字段</span>
          <div class="field-list">
            <span v-for="field in entity.fields" :key="field.id" class="field-tag">
              {{ field.fieldName }}
              <span v-if="field.isRequired" class="required">*</span>
            </span>
          </div>
        </div>
        <div v-else class="no-fields">暂无字段</div>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="viewDialogVisible = false">关闭</button>
      </template>
    </MdmDialog>

    <!-- 删除确认对话框 -->
    <MdmConfirmDialog
      v-model="confirmVisible"
      :message="confirmMessage"
      @confirm="handleConfirmDelete"
    />
  </div>
</template>

<style scoped lang="scss">
@import '@/assets/styles/mdm-common.scss';

.view-definition-page {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
  overflow-x: auto;
}

// 左侧分类树面板
.left-panel {
  width: 240px;
  min-width: 180px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;

  .panel-header {
    padding: 12px 12px;
    border-bottom: 1px solid #e8e8e8;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }

    .header-actions {
      display: flex;
      gap: 6px;
    }

    .add-btn {
      padding: 4px 10px;
      background: #ed2b33;
      border: none;
      border-radius: 4px;
      color: white;
      font-weight: 500;
      cursor: pointer;
      transition: 0.2s;
      font-size: 12px;

      &:hover {
        background: #c81e2c;
      }
    }
  }

  .panel-content {
    padding: 12px;
    max-height: calc(100vh - 160px);
    overflow-y: auto;
  }

  .tree-list {
    margin-top: 8px;
  }

  .tree-item {
    position: relative;
  }

  .tree-node.root {
    background: #f8f9fa;
    margin-bottom: 8px;
    padding: 8px 10px;
    cursor: pointer;
    border-radius: 4px;
    display: flex;
    align-items: center;

    &.active {
      background: #fef9f5;
      border-left: 3px solid #ed2b33;

      .node-label {
        color: #ed2b33;
        font-weight: 500;
      }
    }

    .node-icon {
      font-size: 16px;
      margin-right: 6px;
    }

    .node-label {
      font-size: 13px;
      font-weight: 500;
      color: #333;
    }
  }
}

// 右侧列表
.right-panel {
  flex: 1;
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

// 版本后缀
.version-suffix {
  display: inline-block;
  margin-left: 4px;
  font-size: 12px;
  color: #faad14;
}

  &.revising {
    background: #f9f0ff;
    color: #722ed1;
  }

  &.published {
    background: #f0f9eb;
    color: #67c23a;
  }

  &.disabled {
    background: #fff7e6;
    color: #faad14;
  }

  &.history {
    background: #f4f4f5;
    color: #909399;
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
    &.revise { color: #722ed1; }
    &.disable { color: #faad14; }
    &.enable { color: #52c41a; }
    &.delete { color: #f56c6c; }

    &:hover {
      background: #f5f5f5;
    }
  }
}

// 详情区域
.detail-section {
  .detail-row {
    display: flex;
    margin-bottom: 8px;
    font-size: 13px;

    .detail-label {
      width: 80px;
      color: #666;
      flex-shrink: 0;
    }

    .detail-value {
      flex: 1;
      color: #333;
    }
  }
}

// 实体区域
.entity-section {
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 8px;

  .entity-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;

    .entity-type {
      padding: 2px 8px;
      border-radius: 2px;
      font-size: 12px;
      margin-right: 8px;

      &.main {
        background: #e6f7ff;
        color: #1890ff;
      }

      &.sub {
        background: #f6ffed;
        color: #52c41a;
      }
    }

    .entity-name {
      font-weight: 500;
      color: #333;
    }

    .entity-code {
      font-size: 12px;
      color: #999;
      margin-left: 4px;
    }
  }

  .entity-fields {
    .field-count {
      font-size: 12px;
      color: #666;
      margin-bottom: 6px;
      display: block;
    }

    .field-list {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      .field-tag {
        padding: 2px 8px;
        background: #fff;
        border: 1px solid #d9d9d9;
        border-radius: 2px;
        font-size: 12px;
        color: #333;

        .required {
          color: #f56c6c;
        }
      }
    }
  }

  .no-fields {
    font-size: 12px;
    color: #999;
  }
}

// 空行
.empty-row {
  text-align: center;
  color: #999;
  padding: 20px;
}

// 历史版本弹窗
.history-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
  font-size: 14px;
  color: #666;

  strong {
    color: #333;
    font-size: 15px;
  }
}

// 时间轴
.history-timeline {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px 0;
}

.timeline-item {
  display: flex;
  gap: 16px;
  margin-bottom: 0;
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 20px;
  flex-shrink: 0;
}

.timeline-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #d9d9d9;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #d9d9d9;
  flex-shrink: 0;

  &.draft {
    background: #909399;
    box-shadow: 0 0 0 2px #909399;
  }

  &.published {
    background: #67c23a;
    box-shadow: 0 0 0 2px #67c23a;
  }

  &.archived, &.history {
    background: #e6a23c;
    box-shadow: 0 0 0 2px #e6a23c;
  }
}

.timeline-connector {
  width: 2px;
  flex: 1;
  background: #e8e8e8;
  min-height: 20px;
}

.timeline-content {
  flex: 1;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 14px;
  margin-bottom: 16px;

  &:hover {
    border-color: #ed2b33;
  }
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;

  .version-tag {
    display: inline-block;
    padding: 2px 10px;
    background: #f0f0f0;
    border-radius: 10px;
    font-size: 13px;
    font-weight: 600;
    color: #333;
  }

  .current-badge {
    padding: 2px 8px;
    background: #e6f7ff;
    color: #1890ff;
    border-radius: 2px;
    font-size: 12px;
  }
}

.timeline-body {
  .info-row {
    display: flex;
    margin-bottom: 6px;
    font-size: 13px;

    .label {
      width: 70px;
      color: #999;
      flex-shrink: 0;
    }

    .value {
      color: #333;

      &.table-name {
        font-family: monospace;
        background: #f5f5f5;
        padding: 2px 6px;
        border-radius: 3px;
        color: #ed2b33;
      }
    }
  }
}

.timeline-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 10px;

  .action-btn {
    padding: 4px 12px;
    font-size: 13px;
    border: 1px solid #d9d9d9;
    background: #fff;
    border-radius: 4px;
    cursor: pointer;

    &.view {
      color: #1890ff;
      border-color: #1890ff;

      &:hover {
        background: #e6f7ff;
      }
    }

    &.data {
      color: #67c23a;
      border-color: #67c23a;

      &:hover {
        background: #f0f9eb;
      }
    }
  }
}

.empty-history {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}
</style>
