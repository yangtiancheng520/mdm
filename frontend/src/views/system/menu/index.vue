<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getPermissionTree, createPermission, updatePermission, deletePermission, type PermissionTree, type PermissionForm } from '../../../api/permission'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

const treeData = ref<PermissionTree[]>([])
const loading = ref(false)
const treeRef = ref()

// 当前选中的节点
const selectedNode = ref<PermissionTree | null>(null)
const expandedKeys = ref<number[]>([])

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
    // 默认全部折叠，不展开任何节点
    expandedKeys.value = []
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
    type: node.type === 'menu' ? 'button' : 'button', // 如果父节点是菜单，默认子节点是按钮
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
    // 保存当前展开的节点
    const currentExpandedKeys = [...expandedKeys.value]

    await deletePermission(selectedNode.value.id)
    ElMessage.success('删除成功')

    // 清空选中节点
    selectedNode.value = null

    // 重新加载树，并恢复展开状态
    await fetchTree()

    // 恢复之前展开的节点（排除被删除的节点）
    expandedKeys.value = currentExpandedKeys.filter(key => {
      // 检查节点是否还存在
      const nodeExists = findNodeInTree(treeData.value, key)
      return nodeExists
    })
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
    // 保存当前展开的节点
    const currentExpandedKeys = [...expandedKeys.value]

    if (form.value.id) {
      await updatePermission(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createPermission(form.value)
      ElMessage.success('创建成功')

      // 如果是新增子菜单，自动展开父节点
      if (form.value.parentId) {
        currentExpandedKeys.push(form.value.parentId)
      }
    }
    dialogVisible.value = false

    // 重新加载树，并恢复展开状态
    await fetchTree()

    // 恢复之前展开的节点
    expandedKeys.value = currentExpandedKeys
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 展开/折叠
function toggleExpand(key: number) {
  const index = expandedKeys.value.indexOf(key)
  if (index > -1) {
    expandedKeys.value.splice(index, 1)
  } else {
    expandedKeys.value.push(key)
  }
}

// 判断是否展开
function isExpanded(key: number) {
  return expandedKeys.value.includes(key)
}

// 获取类型标签
function getTypeLabel(type: string) {
  return type === 'menu' ? '菜单' : '按钮'
}

// 获取类型图标
function getTypeIcon(type: string) {
  return type === 'menu' ? '📁' : '🔘'
}

// 计算子节点数量
function getChildCount(node: PermissionTree): number {
  if (!node.children || node.children.length === 0) return 0
  return node.children.length
}

// 在树中查找节点是否存在
function findNodeInTree(nodes: PermissionTree[], targetId: number): boolean {
  for (const node of nodes) {
    if (node.id === targetId) {
      return true
    }
    if (node.children && node.children.length > 0) {
      if (findNodeInTree(node.children, targetId)) {
        return true
      }
    }
  }
  return false
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
        <div v-else class="tree-container">
          <div v-for="node in treeData" :key="node.id" class="tree-node-wrapper">
            <!-- 一级节点 -->
            <div
              class="tree-node level-1"
              :class="{ active: selectedNode?.id === node.id }"
              @click="handleNodeClick(node)"
            >
              <div class="node-content">
                <span class="expand-icon" @click.stop="toggleExpand(node.id)">
                  <template v-if="node.children && node.children.length > 0">
                    {{ isExpanded(node.id) ? '▼' : '▶' }}
                  </template>
                  <template v-else>
                    <span style="width: 14px; display: inline-block;"></span>
                  </template>
                </span>
                <span class="node-icon">{{ getTypeIcon(node.type) }}</span>
                <span class="node-name">{{ node.name }}</span>
                <span class="node-code">({{ node.code }})</span>
                <span v-if="getChildCount(node) > 0" class="child-count">{{ getChildCount(node) }}</span>
              </div>
              <div class="node-actions" @click.stop>
                <button class="action-btn" @click="handleAddChild(node)" title="添加子菜单">+</button>
                <button class="action-btn" @click="handleEdit(node)" title="编辑">✎</button>
                <button class="action-btn delete" @click="handleDelete(node)" title="删除">×</button>
              </div>
            </div>

            <!-- 二级节点 -->
            <div v-if="isExpanded(node.id) && node.children" class="children-container">
              <div
                v-for="child in node.children"
                :key="child.id"
                class="child-node-wrapper"
              >
                <div
                  class="tree-node level-2"
                  :class="{ active: selectedNode?.id === child.id }"
                  @click="handleNodeClick(child)"
                >
                  <div class="node-content">
                    <span class="expand-icon" @click.stop="toggleExpand(child.id)">
                      <template v-if="child.children && child.children.length > 0">
                        {{ isExpanded(child.id) ? '▼' : '▶' }}
                      </template>
                      <template v-else>
                        <span style="width: 14px; display: inline-block;"></span>
                      </template>
                    </span>
                    <span class="node-icon">{{ getTypeIcon(child.type) }}</span>
                    <span class="node-name">{{ child.name }}</span>
                    <span class="node-code">({{ child.code }})</span>
                    <span v-if="getChildCount(child) > 0" class="child-count">{{ getChildCount(child) }}</span>
                  </div>
                  <div class="node-actions" @click.stop>
                    <button class="action-btn" @click="handleAddChild(child)" title="添加子菜单">+</button>
                    <button class="action-btn" @click="handleEdit(child)" title="编辑">✎</button>
                    <button class="action-btn delete" @click="handleDelete(child)" title="删除">×</button>
                  </div>
                </div>

                <!-- 三级节点 -->
                <div v-if="isExpanded(child.id) && child.children" class="children-container level-3-container">
                  <div
                    v-for="grandchild in child.children"
                    :key="grandchild.id"
                    class="tree-node level-3"
                    :class="{ active: selectedNode?.id === grandchild.id }"
                    @click="handleNodeClick(grandchild)"
                  >
                    <div class="node-content">
                      <span class="node-icon">{{ getTypeIcon(grandchild.type) }}</span>
                      <span class="node-name">{{ grandchild.name }}</span>
                      <span class="node-code">({{ grandchild.code }})</span>
                    </div>
                    <div class="node-actions" @click.stop>
                      <button class="action-btn" @click="handleEdit(grandchild)" title="编辑">✎</button>
                      <button class="action-btn delete" @click="handleDelete(grandchild)" title="删除">×</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
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
  overflow-x: auto;
}

// 左侧树形面板
.left-panel {
  width: 400px;
  min-width: 300px;
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
      transition: 0.2s;

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

// 树形容器
.tree-container {
  .tree-node-wrapper {
    margin-bottom: 4px;
  }

  .child-node-wrapper {
    width: 100%;
  }
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: 0.2s;
  margin-bottom: 2px;

  &:hover {
    background: #f5f5f5;
  }

  &.active {
    background: #fef9f5;
    border-left: 3px solid #ed2b33;

    .node-name {
      color: #ed2b33;
      font-weight: 500;
    }
  }

  &.level-1 {
    background: #fafafa;
    font-weight: 500;

    &:hover {
      background: #f0f0f0;
    }
  }

  &.level-2 {
    font-weight: normal;
  }

  &.level-3 {
    font-weight: normal;
    font-size: 13px;
  }

  .node-content {
    display: flex;
    align-items: center;
    gap: 6px;
    flex: 1;

    .expand-icon {
      width: 14px;
      font-size: 10px;
      color: #999;
      cursor: pointer;

      &:hover {
        color: #333;
      }
    }

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

    .child-count {
      display: inline-block;
      padding: 0 6px;
      background: #ed2b33;
      color: white;
      border-radius: 10px;
      font-size: 11px;
      min-width: 18px;
      text-align: center;
    }
  }

  .node-actions {
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

  &:hover .node-actions {
    opacity: 1;
  }
}

.children-container {
  margin-left: 20px;

  &.level-3-container {
    margin-left: 20px;
  }
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
