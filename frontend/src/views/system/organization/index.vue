<script setup lang="ts">
/**
 * 组织管理页面
 */

import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getOrganizationTree,
  createOrganization,
  updateOrganization,
  deleteOrganization,
  type Organization,
  type OrganizationForm,
  ORG_TYPE_OPTIONS
} from '../../../api/system/organization'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'
import TreeSelect from '../../../components/common/TreeSelect.vue'

// 树形数据
const treeData = ref<Organization[]>([])
const loading = ref(false)

// 当前选中的节点
const currentNode = ref<Organization | null>(null)
const selectedId = ref<number | null>(null)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增组织')

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单数据
const form = ref<OrganizationForm>({
  orgCode: '',
  orgName: '',
  orgType: 'department',
  parentId: null,
  manager: '',
  phone: '',
  email: '',
  sort: 0,
  status: 'active',
  description: ''
})

// 父组织选项（排除当前节点及其子节点）
const parentOptions = computed(() => {
  if (!currentNode.value) return treeData.value

  // 过滤掉当前节点及其所有子节点
  const filterNode = (nodes: Organization[], excludeId: number): Organization[] => {
    return nodes
      .filter(n => n.id !== excludeId)
      .map(n => ({
        ...n,
        children: n.children ? filterNode(n.children, excludeId) : undefined
      }))
  }

  return filterNode(treeData.value, currentNode.value.id)
})

// 获取组织树
async function fetchTree() {
  loading.value = true
  try {
    const res = await getOrganizationTree()
    treeData.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 新增
function handleAdd(parent?: Organization) {
  dialogTitle.value = '新增组织'
  form.value = {
    orgCode: '',
    orgName: '',
    orgType: 'department',
    parentId: parent?.id || null,
    manager: '',
    phone: '',
    email: '',
    sort: 0,
    status: 'active',
    description: ''
  }
  currentNode.value = null
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: Organization) {
  dialogTitle.value = '编辑组织'
  currentNode.value = row
  form.value = {
    id: row.id,
    orgCode: row.orgCode,
    orgName: row.orgName,
    orgType: row.orgType,
    parentId: row.parentId,
    manager: row.manager,
    phone: row.phone,
    email: row.email,
    sort: row.sort,
    status: row.status,
    description: row.description
  }
  dialogVisible.value = true
}

// 删除
function handleDelete(row: Organization) {
  // 检查是否有子节点
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该组织下存在子组织，无法删除')
    return
  }

  confirmMessage.value = `确定要删除组织「${row.orgName}」吗？`
  confirmAction.value = async () => {
    await deleteOrganization(row.id)
    ElMessage.success('删除成功')
    fetchTree()
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
  if (!form.value.orgCode) {
    ElMessage.warning('请输入组织编码')
    return
  }
  if (!form.value.orgName) {
    ElMessage.warning('请输入组织名称')
    return
  }

  try {
    if (form.value.id) {
      await updateOrganization(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createOrganization(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchTree()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 获取组织类型标签
function getOrgTypeLabel(type: string): string {
  const option = ORG_TYPE_OPTIONS.find(o => o.value === type)
  return option?.label || type
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
  <div class="organization-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">组织管理</h2>
      <div class="page-actions">
        <button class="mdm-btn-red" @click="handleAdd()">
          <span>+ 新增</span>
        </button>
      </div>
    </div>

    <!-- 组织树 -->
    <div class="org-tree-container">
      <div v-if="loading" class="loading-wrapper">
        <span>加载中...</span>
      </div>

      <div v-else-if="treeData.length === 0" class="empty-wrapper">
        <span class="empty-icon">📭</span>
        <span class="empty-text">暂无数据</span>
        <button class="mdm-btn-red" @click="handleAdd()">新增顶级组织</button>
      </div>

      <div v-else class="org-tree">
        <!-- 递归树节点 -->
        <template v-for="node in treeData" :key="node.id">
          <div class="org-node">
            <!-- 节点内容 -->
            <div class="org-node-content">
              <div class="org-node-info">
                <span class="org-expand-icon">
                  {{ node.children && node.children.length > 0 ? '▼' : '•' }}
                </span>
                <span class="org-name">{{ node.orgName }}</span>
                <span class="org-code">({{ node.orgCode }})</span>
                <span class="org-type-tag">{{ getOrgTypeLabel(node.orgType) }}</span>
                <span class="org-status" :class="node.status">
                  {{ getStatusLabel(node.status) }}
                </span>
              </div>
              <div class="org-node-actions">
                <button class="action-btn add" @click="handleAdd(node)" title="新增子组织">
                  ➕
                </button>
                <button class="action-btn edit" @click="handleEdit(node)" title="编辑">
                  ✏️
                </button>
                <button class="action-btn delete" @click="handleDelete(node)" title="删除">
                  🗑️
                </button>
              </div>
            </div>

            <!-- 子节点 -->
            <div v-if="node.children && node.children.length > 0" class="org-children">
              <template v-for="child in node.children" :key="child.id">
                <div class="org-node">
                  <div class="org-node-content">
                    <div class="org-node-info">
                      <span class="org-expand-icon">
                        {{ child.children && child.children.length > 0 ? '▼' : '•' }}
                      </span>
                      <span class="org-name">{{ child.orgName }}</span>
                      <span class="org-code">({{ child.orgCode }})</span>
                      <span class="org-type-tag">{{ getOrgTypeLabel(child.orgType) }}</span>
                      <span class="org-status" :class="child.status">
                        {{ getStatusLabel(child.status) }}
                      </span>
                    </div>
                    <div class="org-node-actions">
                      <button class="action-btn add" @click="handleAdd(child)" title="新增子组织">
                        ➕
                      </button>
                      <button class="action-btn edit" @click="handleEdit(child)" title="编辑">
                        ✏️
                      </button>
                      <button class="action-btn delete" @click="handleDelete(child)" title="删除">
                        🗑️
                      </button>
                    </div>
                  </div>

                  <!-- 三级节点 -->
                  <div v-if="child.children && child.children.length > 0" class="org-children">
                    <template v-for="grandChild in child.children" :key="grandChild.id">
                      <div class="org-node">
                        <div class="org-node-content">
                          <div class="org-node-info">
                            <span class="org-expand-icon">•</span>
                            <span class="org-name">{{ grandChild.orgName }}</span>
                            <span class="org-code">({{ grandChild.orgCode }})</span>
                            <span class="org-type-tag">{{ getOrgTypeLabel(grandChild.orgType) }}</span>
                            <span class="org-status" :class="grandChild.status">
                              {{ getStatusLabel(grandChild.status) }}
                            </span>
                          </div>
                          <div class="org-node-actions">
                            <button class="action-btn edit" @click="handleEdit(grandChild)" title="编辑">
                              ✏️
                            </button>
                            <button class="action-btn delete" @click="handleDelete(grandChild)" title="删除">
                              🗑️
                            </button>
                          </div>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>组织编码</div>
        <input v-model="form.orgCode" class="mdm-input-yellow" placeholder="请输入组织编码" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>组织名称</div>
        <input v-model="form.orgName" class="mdm-input-yellow" placeholder="请输入组织名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>组织类型</div>
        <select v-model="form.orgType" class="mdm-select">
          <option v-for="opt in ORG_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">上级组织</div>
        <TreeSelect
          v-model="form.parentId"
          :data="parentOptions"
          placeholder="请选择上级组织"
          clearable
        />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">负责人</div>
        <input v-model="form.manager" class="mdm-input-normal" placeholder="请输入负责人" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">联系电话</div>
        <input v-model="form.phone" class="mdm-input-normal" placeholder="请输入联系电话" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">邮箱</div>
        <input v-model="form.email" class="mdm-input-normal" placeholder="请输入邮箱" />
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

<style scoped>
@import '../../../assets/styles/mdm-common.scss';

.organization-page {
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

.org-tree-container {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  min-height: 400px;
}

.loading-wrapper,
.empty-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #999;
  gap: 16px;
}

.empty-icon {
  font-size: 48px;
}

.empty-text {
  font-size: 14px;
}

.org-tree {
  font-size: 14px;
}

.org-node {
  margin-bottom: 4px;
}

.org-node-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 4px;
  transition: background 0.2s;
}

.org-node-content:hover {
  background: #f5f7fa;
}

.org-node-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.org-expand-icon {
  width: 16px;
  font-size: 10px;
  color: #999;
  text-align: center;
}

.org-name {
  font-weight: 500;
  color: #333;
}

.org-code {
  color: #999;
  font-size: 12px;
}

.org-type-tag {
  padding: 2px 8px;
  background: #e8f4ff;
  color: #1890ff;
  font-size: 12px;
  border-radius: 2px;
}

.org-status {
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 2px;
}

.org-status.active {
  background: #f6ffed;
  color: #52c41a;
}

.org-status.inactive {
  background: #fff2f0;
  color: #ff4d4f;
}

.org-node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.org-node-content:hover .org-node-actions {
  opacity: 1;
}

.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  transition: background 0.2s;
}

.action-btn:hover {
  background: #e8e8e8;
}

.action-btn.add:hover {
  background: #e6f7ff;
}

.action-btn.edit:hover {
  background: #fffbe6;
}

.action-btn.delete:hover {
  background: #fff2f0;
}

.org-children {
  margin-left: 24px;
  border-left: 1px dashed #d9d9d9;
  padding-left: 12px;
}

/* 表单样式 */
.mdm-textarea {
  width: 100%;
  min-height: 80px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  outline: none;
}

.mdm-textarea:focus {
  border-color: #c41a1a;
}
</style>
