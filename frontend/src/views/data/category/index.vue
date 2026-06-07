<template>
  <div class="data-category-page">
    <!-- 顶部工具栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <span class="page-title">数据分类管理</span>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-primary" @click="handleAddFolder">+ 新建文件夹</button>
        <button class="mdm-btn-outline" @click="handleAddForm">+ 添加表单</button>
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
                <el-button v-if="data.type === 'folder'" link size="small" @click.stop="handleEdit(data)">编辑</el-button>
                <el-button v-if="data.type === 'folder'" link size="small" @click.stop="handleDelete(data)">删除</el-button>
                <el-button v-if="data.type === 'form'" link size="small" type="danger" @click.stop="handleRemoveForm(data)">移除</el-button>
              </span>
            </div>
          </template>
        </el-tree>

        <el-empty v-if="treeData.length === 0" description="暂无分类，请新建文件夹" />
      </div>
    </div>

    <!-- 新建/编辑文件夹弹窗 -->
    <MdmDialog v-model="folderDialogVisible" :title="folderDialogTitle" width="450px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>文件夹名称</div>
        <input v-model="folderForm.name" class="mdm-input-yellow" placeholder="请输入文件夹名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">上级文件夹</div>
        <select v-model="folderForm.parentId" class="mdm-select">
          <option :value="null">顶级目录</option>
          <option v-for="folder in folderList" :key="folder.id" :value="folder.id">
            {{ folder.name }}
          </option>
        </select>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="folderDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleFolderSubmit" :disabled="submitting">确定</button>
      </template>
    </MdmDialog>

    <!-- 添加表单弹窗 -->
    <MdmDialog v-model="formDialogVisible" title="添加表单" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>选择文件夹</div>
        <select v-model="formSelect.parentId" class="mdm-select">
          <option :value="null">顶级目录</option>
          <option v-for="folder in folderList" :key="folder.id" :value="folder.id">
            {{ folder.name }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>选择表单</div>
        <select v-model="formSelect.formId" class="mdm-select">
          <option :value="undefined">请选择表单</option>
          <option v-for="form in publishedForms" :key="form.id" :value="form.id">
            {{ form.formName }}
          </option>
        </select>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="formDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleFormSubmit" :disabled="submitting">确定</button>
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
import { ref, computed, onMounted } from 'vue'
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

// 已发布表单列表
const publishedForms = ref<FormDto[]>([])

// 文件夹弹窗
const folderDialogVisible = ref(false)
const folderDialogTitle = computed(() => editingFolder.value ? '编辑文件夹' : '新建文件夹')
const editingFolder = ref<DataCategoryDto | null>(null)
const folderForm = ref({
  name: '',
  parentId: null as number | null
})
const submitting = ref(false)

// 表单选择弹窗
const formDialogVisible = ref(false)
const formSelect = ref({
  parentId: null as number | null,
  formId: undefined as number | undefined
})

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const pendingAction = ref<'delete' | 'remove' | null>(null)
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

// 新建文件夹
function handleAddFolder() {
  editingFolder.value = null
  folderForm.value = { name: '', parentId: null }
  folderDialogVisible.value = true
}

// 编辑文件夹
function handleEdit(data: DataCategoryDto) {
  editingFolder.value = data
  folderForm.value = {
    name: data.name,
    parentId: data.parentId
  }
  folderDialogVisible.value = true
}

// 提交文件夹
async function handleFolderSubmit() {
  if (!folderForm.value.name.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }

  submitting.value = true
  try {
    if (editingFolder.value) {
      await updateCategory(editingFolder.value.id, { name: folderForm.value.name })
      ElMessage.success('修改成功')
    } else {
      await createFolder({
        name: folderForm.value.name,
        parentId: folderForm.value.parentId || undefined
      })
      ElMessage.success('创建成功')
    }
    folderDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(editingFolder.value ? '修改失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// 添加表单
function handleAddForm() {
  formSelect.value = { parentId: null, formId: undefined }
  formDialogVisible.value = true
}

// 提交表单
async function handleFormSubmit() {
  if (!formSelect.value.formId) {
    ElMessage.warning('请选择表单')
    return
  }

  submitting.value = true
  try {
    await addForm({
      parentId: formSelect.value.parentId || undefined,
      formId: formSelect.value.formId
    })
    ElMessage.success('添加成功')
    formDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    submitting.value = false
  }
}

// 删除文件夹
function handleDelete(data: DataCategoryDto) {
  pendingAction.value = 'delete'
  pendingData.value = data
  confirmMessage.value = `确定要删除文件夹"${data.name}"吗？`
  confirmVisible.value = true
}

// 移除表单
function handleRemoveForm(data: DataCategoryDto) {
  pendingAction.value = 'remove'
  pendingData.value = data
  confirmMessage.value = `确定要移除表单"${data.name}"吗？`
  confirmVisible.value = true
}

// 确认操作
async function handleConfirm() {
  if (!pendingData.value) return

  try {
    await deleteCategory(pendingData.value.id)
    ElMessage.success(pendingAction.value === 'delete' ? '删除成功' : '移除成功')
    loadData()
  } catch (error) {
    ElMessage.error(pendingAction.value === 'delete' ? '删除失败' : '移除失败')
  } finally {
    pendingAction.value = null
    pendingData.value = null
  }
}

// 拖拽控制
function allowDrag(draggingNode: any) {
  // 所有节点都可以拖拽
  return true
}

function allowDrop(draggingNode: any, dropNode: any, type: string) {
  // 只能拖拽到文件夹节点内或前后
  if (type === 'inner') {
    return dropNode.data.type === 'folder'
  }
  return true
}

// 拖拽完成
async function handleDrop(draggingNode: any, dropNode: any, dropType: string, ev: any) {
  // 收集需要更新排序的节点
  const sortItems: { id: number; parentId: number | null; sort: number }[] = []

  // 获取目标父节点
  let targetParentId: number | null = null
  if (dropType === 'inner') {
    targetParentId = dropNode.data.id
  } else {
    targetParentId = dropNode.data.parentId
  }

  // 获取同级节点列表
  const siblings = getSiblings(treeData.value, targetParentId)

  // 更新排序
  siblings.forEach((item, index) => {
    sortItems.push({
      id: item.id,
      parentId: targetParentId,
      sort: index
    })
  })

  try {
    await updateSort(sortItems)
    ElMessage.success('排序已保存')
  } catch (error) {
    ElMessage.error('排序保存失败')
    loadData() // 重新加载恢复原状
  }
}

// 获取同级节点
function getSiblings(data: DataCategoryDto[], parentId: number | null): DataCategoryDto[] {
  if (parentId === null) {
    return data
  }
  for (const item of data) {
    if (item.id === parentId && item.children) {
      return item.children
    }
    if (item.children) {
      const result = getSiblings(item.children, parentId)
      if (result.length > 0) return result
    }
  }
  return []
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
}

.tree-node:hover .node-actions {
  opacity: 1;
}
</style>
