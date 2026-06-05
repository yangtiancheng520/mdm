<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, batchDeleteUsers, type User, type UserForm } from '../../../api/user'
import { getAllRoles, type Role } from '../../../api/role'

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
const formRef = ref()

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

// 获取用户列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList(searchForm.value)
    tableData.value = res.data.list
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
  fetchData()
}

// 重置
function handleReset() {
  searchForm.value = { account: '', name: '', status: '' }
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
  ElMessageBox.confirm(`确定要删除用户「${row.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

// 批量删除
function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await batchDeleteUsers(selectedRows.value.map(r => r.id))
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

// 选择变化
function handleSelectionChange(rows: User[]) {
  selectedRows.value = rows
}

// 提交表单
async function handleSubmit() {
  try {
    if (form.value.id) {
      // 编辑时：如果密码为空则不发送密码字段（保留原密码）
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

onMounted(() => {
  fetchData()
  fetchRoles()
})
</script>

<template>
  <div class="user-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="账号">
          <el-input v-model="searchForm.account" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 100px">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>用户列表</span>
          <div>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">
              批量删除
            </el-button>
            <el-button type="primary" @click="handleAdd">新增用户</el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="账号" required>
          <el-input v-model="form.account" :disabled="!!form.id" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" :required="!form.id">
          <el-input v-model="form.password" type="password" :placeholder="form.id ? '留空则不修改密码' : '请输入密码'" show-password />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" required>
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roles" multiple placeholder="请选择角色" style="width: 100%;">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-container {
  height: 100%;
}

.search-card {
  margin-bottom: 20px;
}
</style>
