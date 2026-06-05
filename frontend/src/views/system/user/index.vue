<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, batchDeleteUsers, type User, type UserForm } from '../../../api/user'
import { getAllRoles, type Role } from '../../../api/role'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

const tableData = ref<User[]>([])
const loading = ref(false)
const selectedRows = ref<User[]>([])

// 搜索表单
const searchForm = ref({
  account: '',
  name: '',
  status: ''
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单数据
const form = ref<UserForm>({
  account: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  status: 'active',
  roles: []
})

const roleList = ref<Role[]>([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取用户列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList(searchForm.value)
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

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchData()
}

// 重置
function handleReset() {
  searchForm.value = { account: '', name: '', status: '' }
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
    roles: []
  }
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
    roles: row.roles
  }
  dialogVisible.value = true
}

// 删除
function handleDelete(row: User) {
  confirmMessage.value = `确定要删除用户「${row.name}」吗？`
  confirmAction.value = async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }
  confirmVisible.value = true
}

// 批量删除
function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  confirmMessage.value = `确定要删除选中的 ${selectedRows.value.length} 个用户吗？`
  confirmAction.value = async () => {
    await batchDeleteUsers(selectedRows.value.map(r => r.id))
    ElMessage.success('删除成功')
    fetchData()
  }
  confirmVisible.value = true
}

// 确认删除
function handleConfirmDelete() {
  if (confirmAction.value) {
    confirmAction.value()
  }
}

// 选择变化
function handleSelectionChange(rows: User[]) {
  selectedRows.value = rows
}

// 提交表单
async function handleSubmit() {
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
  } catch (error) {
    console.error(error)
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
  fetchData()
  fetchRoles()
})
</script>

<template>
  <div class="mdm-container">
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
            <td colspan="8" class="mdm-empty-data">暂无数据</td>
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
</style>
