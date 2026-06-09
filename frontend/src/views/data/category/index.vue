<template>
  <div class="data-category-page">
    <!-- 顶部工具栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <span class="page-title">表单视图映射管理</span>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-primary" @click="handleAddRoot">+ 新增</button>
      </div>
    </div>

    <!-- 分类树 -->
    <div class="tree-container">
      <div class="tree-content">
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ children: 'children', label: 'name' }"
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
          draggable
          :allow-drop="allowDrop"
          :allow-drag="allowDrag"
          @node-drop="handleDrop"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <span class="node-icon">
                <el-icon v-if="data.type === 'folder'"><Folder /></el-icon>
                <el-icon v-else><Document /></el-icon>
              </span>
              <span class="node-label">{{ data.name }}</span>
              <span v-if="data.type === 'form'" class="node-tag">表单</span>
              <span class="node-actions">
                <el-button link size="small" class="action-add" @click.stop="handleAdd(data)">添加</el-button>
                <el-button link size="small" class="action-edit" @click.stop="handleEdit(data)">编辑</el-button>
                <el-button link size="small" class="action-delete" @click.stop="handleDelete(data)">删除</el-button>
              </span>
            </div>
          </template>
        </el-tree>

        <el-empty v-if="treeData.length === 0" description="暂无分类，请新增" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>类型</div>
        <div class="type-radio-group">
          <label class="type-radio" :class="{ active: formData.type === 'folder' }">
            <input type="radio" v-model="formData.type" value="folder" :disabled="isEdit" />
            <el-icon><Folder /></el-icon>
            <span>文件夹</span>
          </label>
          <label class="type-radio" :class="{ active: formData.type === 'form' }">
            <input type="radio" v-model="formData.type" value="form" :disabled="isEdit" />
            <el-icon><Document /></el-icon>
            <span>表单</span>
          </label>
        </div>
      </div>

      <div class="mdm-form-row" v-if="formData.type === 'folder'">
        <div class="mdm-form-label required"><em>*</em>文件夹名称</div>
        <input v-model="formData.name" class="mdm-input-yellow" placeholder="请输入文件夹名称" />
      </div>

      <div class="mdm-form-row" v-if="formData.type === 'form'">
        <div class="mdm-form-label required"><em>*</em>选择表单</div>
        <select v-model="formData.formId" class="mdm-select">
          <option :value="undefined">请选择表单</option>
          <option v-for="form in publishedForms" :key="form.id" :value="form.id">
            {{ form.formName }}
          </option>
        </select>
      </div>

      <div class="mdm-form-row">
        <div class="mdm-form-label">上级文件夹</div>
        <select v-model="formData.parentId" class="mdm-select">
          <option :value="null">顶级目录</option>
          <option
            v-for="folder in availableParentFolders"
            :key="folder.id"
            :value="folder.id"
            :disabled="isEdit && formData.id === folder.id"
          >
            {{ folder.name }}
          </option>
        </select>
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
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, Document } from '@element-plus/icons-vue'
import MdmDialog from '@/components/MdmDialog.vue'
import MdmConfirmDialog from '@/components/MdmConfirmDialog.vue'
import {
  getCategoryTree,
  createFolder,
  addForm,
  updateCategory,
  deleteCategory,
  updateSort,
  type DataCategoryDto
} from '@/api/data/category'
import { getFormList, type FormDto } from '@/api/form'

// 树数据
const treeData = ref<DataCategoryDto[]>([])
const treeRef = ref()

// 文件夹列表（用于下拉选择）
const folderList = computed(() => {
  const folders: DataCategoryDto[] = []
  const collect = (items: DataCategoryDto[]) => {
    items.forEach(item => {
      if (item.type === 'folder') {
        folders.push(item)
        if (item.children) collect(item.children)
      }
    })
  }
  collect(treeData.value)
  return folders
})

// 可选择的父级文件夹（排除自己和自己的子节点）
const availableParentFolders = computed(() => {
  if (!isEdit.value || !formData.value.id) {
    return folderList.value
  }

  // 收集当前节点及其所有子孙节点ID
  const excludeIds = new Set<number>()
  const collectDescendants = (id: number) => {
    excludeIds.add(id)
    const findChildren = (items: DataCategoryDto[]) => {
      items.forEach(item => {
        if (item.parentId === id || excludeIds.has(item.parentId || 0)) {
          excludeIds.add(item.id)
          if (item.children) findChildren(item.children)
        }
      })
    }
    findChildren(treeData.value)
  }
  collectDescendants(formData.value.id)

  return folderList.value.filter(f => !excludeIds.has(f.id))
})

// 已发布表单列表
const publishedForms = ref<FormDto[]>([])

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑' : '新增')
const formData = ref({
  id: undefined as number | undefined,
  type: 'folder' as 'folder' | 'form',
  name: '',
  formId: undefined as number | undefined,
  parentId: null as number | null
})
const submitting = ref(false)

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const pendingData = ref<DataCategoryDto | null>(null)

// 加载数据
async function loadData() {
  try {
    const [treeRes, formRes] = await Promise.all([
      getCategoryTree(),
      getFormList({ status: 'published' })
    ])
    treeData.value = treeRes.data || []
    publishedForms.value = formRes.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 新增根节点
function handleAddRoot() {
  isEdit.value = false
  formData.value = {
    id: undefined,
    type: 'folder',
    name: '',
    formId: undefined,
    parentId: null
  }
  dialogVisible.value = true
}

// 新增子节点
function handleAdd(data: DataCategoryDto) {
  isEdit.value = false
  formData.value = {
    id: undefined,
    type: 'folder',
    name: '',
    formId: undefined,
    parentId: data.type === 'folder' ? data.id : data.parentId
  }
  dialogVisible.value = true
}

// 编辑
function handleEdit(data: DataCategoryDto) {
  isEdit.value = true
  formData.value = {
    id: data.id,
    type: data.type as 'folder' | 'form',
    name: data.name,
    formId: data.formId || undefined,
    parentId: data.parentId
  }
  dialogVisible.value = true
}

// 提交
async function handleSubmit() {
  // 验证
  if (formData.value.type === 'folder' && !formData.value.name.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  if (formData.value.type === 'form' && !formData.value.formId) {
    ElMessage.warning('请选择表单')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && formData.value.id) {
      // 编辑模式
      if (formData.value.type === 'folder') {
        await updateCategory(formData.value.id, { name: formData.value.name })
      } else {
        // 表单类型编辑：需要删除旧的，创建新的
        await deleteCategory(formData.value.id)
        await addForm({
          parentId: formData.value.parentId || undefined,
          formId: formData.value.formId!
        })
      }
      ElMessage.success('修改成功')
    } else {
      // 新增模式
      if (formData.value.type === 'folder') {
        await createFolder({
          name: formData.value.name,
          parentId: formData.value.parentId || undefined
        })
        ElMessage.success('创建成功')
      } else {
        await addForm({
          parentId: formData.value.parentId || undefined,
          formId: formData.value.formId!
        })
        ElMessage.success('添加成功')
      }
    }
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '修改失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

// 删除
function handleDelete(data: DataCategoryDto) {
  pendingData.value = data
  if (data.type === 'folder' && data.children && data.children.length > 0) {
    confirmMessage.value = `文件夹"${data.name}"下有子项，确定要删除吗？子项将一并删除。`
  } else {
    confirmMessage.value = `确定要删除"${data.name}"吗？`
  }
  confirmVisible.value = true
}

// 确认删除
async function handleConfirm() {
  if (!pendingData.value) return

  try {
    // 如果是文件夹且有子项，递归删除
    if (pendingData.value.type === 'folder' && pendingData.value.children && pendingData.value.children.length > 0) {
      await deleteCategoryRecursive(pendingData.value)
    } else {
      await deleteCategory(pendingData.value.id)
    }
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  } finally {
    pendingData.value = null
  }
}

// 递归删除文件夹及其子项
async function deleteCategoryRecursive(category: DataCategoryDto) {
  // 先删除所有子项
  if (category.children && category.children.length > 0) {
    for (const child of category.children) {
      await deleteCategoryRecursive(child)
    }
  }
  // 再删除自己
  await deleteCategory(category.id)
}

// 拖拽控制
function allowDrag(draggingNode: any) {
  return true
}

function allowDrop(draggingNode: any, dropNode: any, type: string) {
  // 不能拖到自己下面
  if (draggingNode.data.id === dropNode.data.id) {
    return false
  }
  // 只能拖拽到文件夹内部
  if (type === 'inner') {
    return dropNode.data.type === 'folder'
  }
  return true
}

// 拖拽完成
async function handleDrop(draggingNode: any, dropNode: any, dropType: string, ev: any) {
  const sortItems: { id: number; parentId: number | null; sort: number }[] = []

  // 获取目标父节点ID
  let targetParentId: number | null = null
  if (dropType === 'inner') {
    // 拖入节点内部
    targetParentId = dropNode.data.id
  } else if (dropType === 'before' || dropType === 'after') {
    // 拖到节点前后
    targetParentId = dropNode.data.parentId
  }

  // 从el-tree获取最新的节点数据
  const tree = treeRef.value
  if (!tree) return

  // 获取同级节点
  let siblings: any[] = []
  if (targetParentId === null) {
    // 顶级节点
    siblings = treeData.value
  } else {
    // 找到父节点
    const parentNode = tree.getNode(targetParentId)
    if (parentNode && parentNode.childNodes) {
      siblings = parentNode.childNodes.map((node: any) => node.data)
    }
  }

  // 构建排序数据
  siblings.forEach((item: any, index: number) => {
    sortItems.push({
      id: item.id,
      parentId: targetParentId,
      sort: index
    })
  })

  if (sortItems.length === 0) return

  try {
    await updateSort(sortItems)
    ElMessage.success('排序已保存')
    // 重新加载数据确保同步
    loadData()
  } catch (error) {
    ElMessage.error('排序保存失败')
    loadData()
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/mdm-common.scss';

.data-category-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
}

.tree-container {
  background: #fff;
  border-radius: 4px;
  flex: 1;
  overflow: auto;
}

.tree-content {
  padding: 20px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding: 4px 0;
}

.node-icon {
  color: #909399;
  font-size: 16px;
}

.node-label {
  flex: 1;
}

.node-tag {
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 2px;
}

.node-actions {
  opacity: 0;
  transition: opacity 0.2s;

  .action-add {
    color: #409eff;
  }

  .action-edit {
    color: #e6a23c;
  }

  .action-delete {
    color: #f56c6c;
  }
}

.tree-node:hover .node-actions {
  opacity: 1;
}

// 类型单选样式
.type-radio-group {
  display: flex;
  gap: 16px;
}

.type-radio {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;

  input[type="radio"] {
    display: none;
  }

  .el-icon {
    font-size: 20px;
    color: #909399;
  }

  span {
    font-size: 14px;
    color: #606266;
  }

  &:hover {
    border-color: #f59a23;
  }

  &.active {
    border-color: #f59a23;
    background: #fff8eb;

    .el-icon {
      color: #f59a23;
    }

    span {
      color: #f59a23;
    }
  }

  input[type="radio"]:disabled + .el-icon,
  input[type="radio"]:disabled ~ span {
    opacity: 0.5;
  }
}
</style>
