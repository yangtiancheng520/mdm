<script setup lang="ts">
/**
 * 字段分类管理页面 - 使用 Element Plus Tree 组件
 */

import { ref, onMounted } from 'vue'
import { ElMessage, ElIcon } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory,
  getFieldCountByCategory,
  type FieldCategory,
  type FieldCategoryForm
} from '../../../api/standard/fieldCategory'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

// 树形数据
const treeData = ref<FieldCategory[]>([])
const loading = ref(false)
const filterText = ref('')
const treeRef = ref()

// 当前选中的节点
const currentNode = ref<FieldCategory | null>(null)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单数据
const form = ref<FieldCategoryForm>({
  categoryCode: '',
  categoryName: '',
  parentId: null,
  sort: 0,
  status: 'active',
  description: ''
})

// 获取分类树
async function fetchTree() {
  loading.value = true
  try {
    const res = await getCategoryTree()
    treeData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 过滤节点
function filterNode(value: string, data: FieldCategory): boolean {
  if (!value) return true
  return data.categoryName.includes(value) || data.categoryCode.includes(value)
}

// 搜索
function handleSearch() {
  treeRef.value?.filter(filterText.value)
}

// 新增
function handleAdd(parent?: FieldCategory) {
  dialogTitle.value = '新增分类'
  form.value = {
    categoryCode: '',
    categoryName: '',
    parentId: parent?.id || null,
    sort: 0,
    status: 'active',
    description: ''
  }
  currentNode.value = null
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: FieldCategory) {
  dialogTitle.value = '编辑分类'
  currentNode.value = row
  form.value = {
    id: row.id,
    categoryCode: row.categoryCode,
    categoryName: row.categoryName,
    parentId: row.parentId,
    sort: row.sort,
    status: row.status,
    description: row.description
  }
  dialogVisible.value = true
}

// 删除
async function handleDelete(row: FieldCategory) {
  // 检查是否有子节点
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该分类下存在子分类，无法删除')
    return
  }

  // 检查是否有关联字段
  try {
    const res = await getFieldCountByCategory(row.id)
    const fieldCount = res.data || 0
    if (fieldCount > 0) {
      ElMessage.warning(`该分类下有 ${fieldCount} 个字段，请先迁移字段`)
      return
    }
  } catch (error) {
    console.error('获取字段数量失败', error)
  }

  confirmMessage.value = `确定要删除分类「${row.categoryName}」吗？`
  confirmAction.value = async () => {
    try {
      console.log('删除分类ID:', row.id, '类型:', typeof row.id)
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      fetchTree()
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

// 提交表单
async function handleSubmit() {
  // 表单验证
  if (!form.value.categoryCode) {
    ElMessage.warning('请输入分类编码')
    return
  }
  if (!form.value.categoryName) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    if (form.value.id) {
      await updateCategory(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createCategory(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchTree()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 状态标签
function getStatusLabel(status: string): string {
  return status === 'active' ? '启用' : '禁用'
}

onMounted(() => {
  fetchTree()
})
</script>

<template>
  <div class="category-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">字段分类管理</h2>
      <button class="mdm-btn-red" @click="handleAdd()">+ 新增顶级分类</button>
    </div>

    <!-- 分类树 -->
    <div class="category-tree-container">
      <!-- 搜索框 -->
      <div class="search-wrapper">
        <el-input
          v-model="filterText"
          placeholder="搜索分类名称/编码"
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>

      <!-- 树形结构 -->
      <div v-loading="loading" class="tree-content">
        <el-empty v-if="treeData.length === 0" description="暂无分类数据">
          <el-button type="primary" @click="handleAdd()">新增分类</el-button>
        </el-empty>

        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{
            label: 'categoryName',
            children: 'children'
          }"
          :node-key="'id'"
          :default-expand-all="true"
          :highlight-current="true"
          :filter-node-method="filterNode"
          :expand-on-click-node="false"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="node-info">
                <span class="node-name">{{ data.categoryName }}</span>
                <span class="node-code">({{ data.categoryCode }})</span>
                <span
                  class="custom-tag"
                  :class="data.status === 'active' ? 'status-active' : 'status-inactive'"
                >
                  {{ getStatusLabel(data.status) }}
                </span>
              </div>
              <div class="node-actions">
                <button class="action-btn add" @click.stop="handleAdd(data)">
                  <el-icon><Plus /></el-icon>
                  <span>新增</span>
                </button>
                <button class="action-btn edit" @click.stop="handleEdit(data)">
                  <el-icon><Edit /></el-icon>
                  <span>编辑</span>
                </button>
                <button class="action-btn delete" @click.stop="handleDelete(data)">
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>分类编码</div>
        <input v-model="form.categoryCode" class="mdm-input-yellow" placeholder="请输入分类编码" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>分类名称</div>
        <input v-model="form.categoryName" class="mdm-input-yellow" placeholder="请输入分类名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">排序</div>
        <input v-model.number="form.sort" type="number" class="mdm-input-normal" placeholder="请输入排序" />
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
            禁用
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
@import '../../../assets/styles/mdm-common.scss';

.category-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 4px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.category-tree-container {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  min-height: 600px;
}

.search-wrapper {
  margin-bottom: 16px;
}

.tree-content {
  min-height: 500px;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;

  .node-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .node-name {
      font-weight: 500;
      color: #333;
    }

    .node-code {
      color: #999;
      font-size: 12px;
    }
  }

  .node-actions {
    display: none;
    gap: 8px;
  }

  &:hover .node-actions {
    display: flex;
  }
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: 1px solid #d4d7dc;
  background: white;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
  transition: all 0.2s;
  color: #333;

  span {
    font-size: 13px;
    color: #333;
  }

  .el-icon {
    font-size: 14px;
  }
}

.action-btn:hover {
  background: #f8fafc;
  border-color: #b0b5bd;
}

.action-btn.add:hover {
  background: #e6f7ff;
  border-color: #91d5ff;
  color: #1890ff;

  .el-icon, span {
    color: #1890ff;
  }
}

.action-btn.edit:hover {
  background: #fffbe6;
  border-color: #ffe58f;
  color: #faad14;

  .el-icon, span {
    color: #faad14;
  }
}

.action-btn.delete {
  border-color: #ed2b33;
  color: #ed2b33;

  .el-icon, span {
    color: #ed2b33;
  }
}

.action-btn.delete:hover {
  background: #ed2b33;
  border-color: #c81e2c;
  color: white;

  .el-icon, span {
    color: white;
  }
}

// 自定义标签样式
.custom-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
  border: 1px solid;
}

.custom-tag.status-active {
  background: #fff5f5;
  border-color: #ffa39e;
  color: #ed2b33;
}

.custom-tag.status-inactive {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #8c8c8c;
}

// 状态标签样式 - 使用 :deep 穿透 Element Plus 样式
:deep(.status-active) {
  background: #fff5f5 !important;
  border-color: #ffa39e !important;
  color: #ed2b33 !important;
}

:deep(.status-inactive) {
  background: #f5f5f5 !important;
  border-color: #d9d9d9 !important;
  color: #8c8c8c !important;
}

// Element Plus Tree 样式覆盖
:deep(.el-tree) {
  background: transparent;

  .el-tree-node__content {
    height: 40px;
    border-radius: 4px;

    &:hover {
      background-color: #f5f7fa;
    }
  }

  .el-tree-node.is-current > .el-tree-node__content {
    background-color: #e8f4ff;
    color: #1890ff;
    font-weight: 500;
  }
}

// 表单样式
.mdm-textarea {
  width: 100%;
  min-height: 80px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  outline: none;

  &:focus {
    border-color: #c41a1a;
  }
}
</style>
