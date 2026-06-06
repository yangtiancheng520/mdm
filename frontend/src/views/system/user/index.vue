<script setup lang="ts">
import { ref, onMounted, onActivated, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, batchDeleteUsers, type User, type UserForm } from '../../../api/user'
import { getAllRoles, type Role } from '../../../api/role'
import { getActiveOrganizationTree, type Organization } from '../../../api/system/organization'
import MdmDialog from '../../../components/MdmDialog.vue'
import OrganizationTreeSelect from '../../../components/common/OrganizationTreeSelect.vue'

const router = useRouter()
const route = useRoute()

const tableData = ref<User[]>([])
const loading = ref(false)
const selectedRows = ref<User[]>([])

// 组织树数据
const orgTreeData = ref<Organization[]>([])
const selectedOrgId = ref<number | null>(null)
const selectedOrgNode = ref<Organization | null>(null)

// 搜索表单
const searchForm = ref({
  account: '',
  name: '',
  status: '',
  orgId: null as number | null
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')

// 组织选择弹窗
const orgSelectVisible = ref(false)
const selectedOrgName = ref('')

// 表单数据
const form = ref<UserForm & { orgId?: number | null }>({
  account: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  status: 'active',
  roles: [],
  orgId: null
})

const roleList = ref<Role[]>([])

// 组织树组件引用
const orgTreeRef = ref()

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取组织树
async function fetchOrgTree() {
  try {
    const res = await getActiveOrganizationTree()
    orgTreeData.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 获取用户列表
async function fetchData() {
  loading.value = true
  try {
    const params = {
      ...searchForm.value,
      orgId: selectedOrgId.value || searchForm.value.orgId
    }
    const res = await getUserList(params)
    tableData.value = res.data.list
    total.value = res.data.total || res.data.list.length
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取角色列表
async function fetchRoles() {
  try {
    const res = await getAllRoles()
    roleList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

// 选择组织节点
function handleOrgSelect(orgId: number | null, node: Organization | null) {
  selectedOrgId.value = orgId
  selectedOrgNode.value = node
  searchForm.value.orgId = orgId
  currentPage.value = 1
  fetchData()
}

// 打开组织选择弹窗
function handleOpenOrgSelect() {
  orgSelectVisible.value = true
}

// 确认选择组织
function handleConfirmOrg(orgId: number | null, node: Organization | null) {
  form.value.orgId = orgId
  selectedOrgName.value = node?.orgName || ''
  orgSelectVisible.value = false
}

// 清空选择的组织
function handleClearOrg() {
  form.value.orgId = null
  selectedOrgName.value = ''
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchData()
}

// 重置
function handleReset() {
  searchForm.value = { account: '', name: '', status: '', orgId: selectedOrgId.value }
  currentPage.value = 1
  fetchData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增用户'
  form.value = {
    account: '',
    password: '',
    name: '',
    email: '',
    phone: '',
    status: 'active',
    roles: [],
    orgId: selectedOrgId.value // 默认选中当前组织
  }
  selectedOrgName.value = ''
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: User) {
  dialogTitle.value = '编辑用户'
  form.value = {
    id: row.id,
    account: row.account,
    password: '',
    name: row.name,
    email: row.email,
    phone: row.phone,
    status: row.status,
    roles: row.roles,
    orgId: (row as any).orgId || null
  }
  selectedOrgName.value = (row as any).orgName || ''
  dialogVisible.value = true
}

// 删除
async function handleDelete(row: User) {
  try {
    await ElMessageBox.confirm(`确定要删除用户「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 批量删除
async function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await batchDeleteUsers(selectedRows.value.map(r => r.id))
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 选择变化
function handleSelectionChange(rows: User[]) {
  selectedRows.value = rows
}

// 提交表单
async function handleSubmit() {
  // 表单验证
  if (!form.value.account) {
    ElMessage.warning('请输入账号')
    return
  }
  if (!form.value.id && !form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!form.value.name) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (!form.value.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  if (!form.value.phone) {
    ElMessage.warning('请输入手机号')
    return
  }

  try {
    if (form.value.id) {
      const updateData = { ...form.value }
      if (!updateData.password) {
        delete updateData.password
      }
      await updateUser(form.value.id, updateData)
      ElMessage.success('更新成功')
    } else {
      await createUser(form.value)
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

onMounted(() => {
  console.log('用户管理页面 onMounted 触发')
  // 进入页面时刷新组织树，确保显示最新的启用状态
  orgTreeRef.value?.refresh()
  fetchOrgTree()
  fetchData()
  fetchRoles()
})

// 组件被激活时（从其他TAB切换回来）刷新组织树
onActivated(() => {
  console.log('用户管理页面 onActivated 触发')
  // 刷新组织树，确保显示最新的启用状态
  orgTreeRef.value?.refresh()
  fetchOrgTree()
})
</script>

<template>
  <div class="user-management-page">
    <!-- 左侧组织树 -->
    <div class="left-panel">
      <div class="panel-header">
        <h3>组织架构</h3>
      </div>
      <div class="panel-content">
        <OrganizationTreeSelect
          ref="orgTreeRef"
          :model-value="selectedOrgId"
          :show-all="true"
          :multiple="false"
          @change="handleOrgSelect"
        />
      </div>
    </div>

    <!-- 右侧用户列表 -->
    <div class="right-panel">
      <!-- 顶部操作栏 -->
      <div class="mdm-top-bar">
        <div class="mdm-filter-row">
          <div class="mdm-filter-item">
            <input v-model="searchForm.account" type="text" placeholder="账号" @keyup.enter="handleSearch" />
          </div>
          <div class="mdm-filter-item">
            <input v-model="searchForm.name" type="text" placeholder="姓名" @keyup.enter="handleSearch" />
          </div>
          <div class="mdm-filter-item">
            <select v-model="searchForm.status" @change="handleSearch">
              <option value="">全部状态</option>
              <option value="active">启用</option>
              <option value="inactive">禁用</option>
            </select>
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
                <input type="checkbox" />
              </th>
              <th>账号</th>
              <th>姓名</th>
              <th>所属组织</th>
              <th>邮箱</th>
              <th>手机号</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody v-loading="loading">
            <tr v-for="row in tableData" :key="row.id">
              <td>
                <input type="checkbox" :checked="selectedRows.includes(row)" @change="(e) => e.target.checked ? selectedRows.push(row) : selectedRows.splice(selectedRows.indexOf(row), 1)" />
              </td>
              <td>{{ row.account }}</td>
              <td>{{ row.name }}</td>
              <td>{{ (row as any).orgName || '-' }}</td>
              <td>{{ row.email }}</td>
              <td>{{ row.phone }}</td>
              <td>
                <div class="mdm-status-badge">
                  <span :class="row.status === 'active' ? 'mdm-green-dot' : 'mdm-red-dot'"></span>
                  {{ row.status === 'active' ? '启用' : '禁用' }}
                </div>
              </td>
              <td>{{ row.createdAt }}</td>
              <td>
                <div class="mdm-action-buttons">
                  <button class="mdm-action-btn" @click="handleEdit(row)">编辑</button>
                  <button class="mdm-action-btn delete" @click="handleDelete(row)">删除</button>
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
    </div>

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>账号</div>
        <input v-model="form.account" class="mdm-input-yellow" :disabled="!!form.id" placeholder="请输入账号" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>密码</div>
        <input v-model="form.password" class="mdm-input-yellow" type="password" :placeholder="form.id ? '留空则不修改密码' : '请输入密码'" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>姓名</div>
        <input v-model="form.name" class="mdm-input-yellow" placeholder="请输入姓名" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">所属组织</div>
        <div class="org-select-display">
          <span class="org-name">{{ selectedOrgName || '未选择组织' }}</span>
          <div class="org-actions">
            <button class="mdm-btn-outline" @click="handleOpenOrgSelect">选择</button>
            <button v-if="selectedOrgName" class="mdm-btn-outline" @click="handleClearOrg">清空</button>
          </div>
        </div>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>邮箱</div>
        <input v-model="form.email" class="mdm-input-yellow" placeholder="请输入邮箱" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>手机号</div>
        <input v-model="form.phone" class="mdm-input-yellow" placeholder="请输入手机号" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">角色</div>
        <select v-model="form.roles" multiple class="mdm-select" style="height: auto; min-height: 36px;">
          <option v-for="role in roleList" :key="role.id" :value="role.id">{{ role.name }}</option>
        </select>
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

    <!-- 组织选择弹窗 -->
    <MdmDialog v-model="orgSelectVisible" title="选择所属组织" width="500px">
      <OrganizationTreeSelect
        v-model="form.orgId"
        :multiple="false"
        :show-search="true"
        @change="handleConfirmOrg"
      />
      <template #footer>
        <button class="mdm-btn-cancel" @click="orgSelectVisible = false">取消</button>
      </template>
    </MdmDialog>
  </div>
</template>

<style scoped lang="scss">
@import '../../../assets/styles/mdm-common.scss';

.user-management-page {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

// 左侧组织树面板
.left-panel {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;

  .panel-header {
    padding: 16px 20px;
    border-bottom: 1px solid #e8e8e8;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }

  .panel-content {
    padding: 12px;
    max-height: calc(100vh - 160px);
    overflow-y: auto;
  }
}

// 右侧用户列表
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 组织选择显示
.org-select-display {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: #f8fafc;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  min-height: 30px;

  .org-name {
    font-size: 13px;
    color: #333;

    &:empty::before {
      content: '未选择组织';
      color: #94a3b8;
    }
  }

  .org-actions {
    display: flex;
    gap: 8px;
  }
}

// 组织选择器包装
.org-select-wrapper {
  width: 100%;

  :deep(.org-tree-select) {
    .tree-wrapper {
      max-height: 200px;
    }
  }
}

// 按钮间距
.mdm-right-group {
  gap: 8px;
}
</style>
