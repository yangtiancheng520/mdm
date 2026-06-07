<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getPermissionTree, createPermission, updatePermission, deletePermission, updatePermissionSort, type PermissionTree, type PermissionForm } from '../../../api/permission'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

const treeData = ref<PermissionTree[]>([])
const loading = ref(false)
const treeRef = ref()

// 当前选中的节点
const selectedNode = ref<PermissionTree | null>(null)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增目录')

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')

// 表单
const form = ref<PermissionForm>({
  name: '',
  code: '',
  type: 'menu',
  parentId: null,
  path: '',
  icon: '',
  sort: 0,
  status: 'active'
})

// 获取权限树
async function fetchTree() {
  loading.value = true
  try {
    const res = await getPermissionTree()
    treeData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 选择节点
function handleNodeClick(node: PermissionTree) {
  selectedNode.value = node
}

// 新增根节点
function handleAddRoot() {
  dialogTitle.value = '新增一级菜单'
  form.value = {
    name: '',
    code: '',
    type: 'menu',
    parentId: null,
    path: '',
    icon: '',
    sort: 0,
    status: 'active'
  }
  dialogVisible.value = true
}

// 新增子节点
function handleAddChild(node: PermissionTree) {
  dialogTitle.value = '新增子菜单'
  form.value = {
    name: '',
    code: '',
    type: node.type === 'menu' ? 'button' : 'button',
    parentId: node.id,
    path: '',
    icon: '',
    sort: 0,
    status: 'active'
  }
  dialogVisible.value = true
}

// 编辑节点
function handleEdit(node: PermissionTree) {
  dialogTitle.value = '编辑菜单'
  form.value = {
    id: node.id,
    name: node.name,
    code: node.code,
    type: node.type,
    parentId: node.parentId,
    path: node.path,
    icon: node.icon,
    sort: node.sort,
    status: node.status
  }
  dialogVisible.value = true
}

// 删除节点
function handleDelete(node: PermissionTree) {
  const hasChildren = node.children && node.children.length > 0
  if (hasChildren) {
    ElMessage.warning('该菜单下有子菜单，不能删除')
    return
  }

  selectedNode.value = node
  confirmMessage.value = `确定要删除「${node.name}」吗？`
  confirmVisible.value = true
}

// 确认删除
async function handleConfirmDelete() {
  if (!selectedNode.value) return

  try {
    await deletePermission(selectedNode.value.id)
    ElMessage.success('删除成功')
    selectedNode.value = null
    await fetchTree()
  } catch (error) {
    console.error(error)
  }
}

// 提交
async function handleSubmit() {
  if (!form.value.name) {
    ElMessage.warning('请输入名称')
    return
  }
  if (!form.value.code) {
    ElMessage.warning('请输入编码')
    return
  }

  try {
    if (form.value.id) {
      await updatePermission(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createPermission(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchTree()
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 获取类型标签
function getTypeLabel(type: string) {
  return type === 'menu' ? '菜单' : '按钮'
}

// 拖拽控制
function allowDrag(draggingNode: any) {
  return true
}

function allowDrop(draggingNode: any, dropNode: any, type: string) {
  // 只允许拖拽到菜单节点内或前后
  if (type === 'inner') {
    return dropNode.data.type === 'menu'
  }
  return true
}

// 拖拽完成
async function handleDrop(draggingNode: any, dropNode: any, dropType: string, ev: any) {
  const sortItems: { id: number; parentId: number | null; sort: number }[] = []

  let targetParentId: number | null = null
  if (dropType === 'inner') {
    targetParentId = dropNode.data.id
  } else {
    targetParentId = dropNode.data.parentId
  }

  const siblings = getSiblings(treeData.value, targetParentId)

  siblings.forEach((item, index) => {
    sortItems.push({
      id: item.id,
      parentId: targetParentId,
      sort: index
    })
  })

  try {
    await updatePermissionSort(sortItems)
    ElMessage.success('排序已保存')
  } catch (error) {
    ElMessage.error('排序保存失败')
    fetchTree()
  }
}

// 获取同级节点
function getSiblings(data: PermissionTree[], parentId: number | null): PermissionTree[] {
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
  fetchTree()
})
</script>

<template>
  <div class="menu-management-page">
    <!-- 左侧树形结构 -->
    <div class="left-panel">
      <div class="panel-header">
        <h3>菜单结构</h3>
        <button class="add-root-btn" @click="handleAddRoot">+ 新增一级菜单</button>
      </div>
      <div class="panel-content" v-loading="loading">
        <div v-if="treeData.length === 0" class="empty-tree">
          <p>暂无菜单数据</p>
          <button class="mdm-btn-primary" @click="handleAddRoot">创建第一个菜单</button>
        </div>

        <!-- 树形节点 -->
        <el-tree
          v-else
          ref="treeRef"
          :data="treeData"
          :props="{ children: 'children', label: 'name' }"
          node-key="id"
          :highlight-current="true"
          :default-expand-all="false"
          draggable
          :allow-drop="allowDrop"
          :allow-drag="allowDrag"
          @node-click="handleNodeClick"
          @node-drop="handleDrop"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <span class="node-icon">{{ data.type === 'menu' ? '📁' : '🔘' }}</span>
              <span class="node-name">{{ data.name }}</span>
              <span class="node-code">({{ data.code }})</span>
              <span class="node-actions" @click.stop>
                <button v-if="data.type === 'menu'" class="action-btn" @click="handleAddChild(data)" title="添加子菜单">+</button>
                <button class="action-btn" @click="handleEdit(data)" title="编辑">✎</button>
                <button class="action-btn delete" @click="handleDelete(data)" title="删除">×</button>
              </span>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 右侧详情 -->
    <div class="right-panel">
      <div class="panel-header">
        <h3>菜单详情</h3>
      </div>
      <div class="panel-content">
        <div v-if="!selectedNode" class="empty-detail">
          <p>请选择一个菜单查看详情</p>
        </div>

        <div v-else class="detail-content">
          <div class="detail-section">
            <div class="section-title">基本信息</div>
            <div class="detail-row">
              <span class="label">名称：</span>
              <span class="value">{{ selectedNode.name }}</span>
            </div>
            <div class="detail-row">
              <span class="label">编码：</span>
              <span class="value code">{{ selectedNode.code }}</span>
            </div>
            <div class="detail-row">
              <span class="label">类型：</span>
              <span :class="['type-tag', selectedNode.type]">{{ getTypeLabel(selectedNode.type) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">路径：</span>
              <span class="value">{{ selectedNode.path || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">图标：</span>
              <span class="value">{{ selectedNode.icon || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">排序：</span>
              <span class="value">{{ selectedNode.sort }}</span>
            </div>
            <div class="detail-row">
              <span class="label">状态：</span>
              <div class="mdm-status-badge">
                <span :class="selectedNode.status === 'active' ? 'mdm-green-dot' : 'mdm-red-dot'"></span>
                {{ selectedNode.status === 'active' ? '启用' : '禁用' }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>名称</div>
        <input v-model="form.name" class="mdm-input-yellow" placeholder="请输入名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>编码</div>
        <input v-model="form.code" class="mdm-input-yellow" placeholder="请输入编码，如：user:view" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>类型</div>
        <select v-model="form.type" class="mdm-select">
          <option value="menu">菜单</option>
          <option value="button">按钮</option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">路径</div>
        <input v-model="form.path" class="mdm-input-normal" placeholder="请输入路径，如：/user/manage" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">图标</div>
        <input v-model="form.icon" class="mdm-input-normal" placeholder="请输入图标名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">排序</div>
        <input v-model.number="form.sort" type="number" class="mdm-input-normal" placeholder="请输入排序号" />
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

.menu-management-page {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

// 左侧树形面板
.left-panel {
  width: 400px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 12px 16px;
    border-bottom: 1px solid #e8e8e8;
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }

    .add-root-btn {
      padding: 4px 12px;
      background: #ed2b33;
      border: none;
      border-radius: 4px;
      color: white;
      font-size: 13px;
      cursor: pointer;

      &:hover {
        background: #c81e2c;
      }
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
  }

  .empty-tree {
    text-align: center;
    padding: 40px 20px;
    color: #999;

    p {
      margin-bottom: 16px;
    }
  }
}

// 树节点样式
.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  padding: 2px 0;

  .node-icon {
    font-size: 16px;
  }

  .node-name {
    color: #333;
  }

  .node-code {
    font-size: 12px;
    color: #999;
  }

  .node-actions {
    margin-left: auto;
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: 0.2s;

    .action-btn {
      padding: 2px 6px;
      border: none;
      background: transparent;
      color: #666;
      cursor: pointer;
      border-radius: 2px;
      font-size: 14px;

      &:hover {
        background: #e8e8e8;
        color: #333;
      }

      &.delete:hover {
        color: #f56c6c;
      }
    }
  }
}

.tree-node:hover .node-actions {
  opacity: 1;
}

// 右侧详情面板
.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 12px 16px;
    border-bottom: 1px solid #e8e8e8;

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
  }

  .empty-detail {
    text-align: center;
    padding: 60px 20px;
    color: #999;
  }
}

.detail-content {
  .detail-section {
    margin-bottom: 24px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
      margin-bottom: 12px;
      padding-bottom: 8px;
      border-bottom: 1px solid #e8e8e8;
    }

    .detail-row {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      font-size: 13px;

      .label {
        width: 80px;
        color: #666;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        color: #333;

        &.code {
          font-family: monospace;
          background: #f5f5f5;
          padding: 2px 8px;
          border-radius: 3px;
          color: #ed2b33;
        }
      }

      .type-tag {
        display: inline-block;
        padding: 2px 8px;
        border-radius: 2px;
        font-size: 12px;

        &.menu {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.button {
          background: #f6ffed;
          color: #52c41a;
        }
      }
    }
  }
}
</style>
