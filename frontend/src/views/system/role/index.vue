<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getAllRoles, type Role, type RoleForm } from '../../../api/role'
import { getPermissionTree, type PermissionTree } from '../../../api/permission'

const tableData = ref<Role[]>([])
const loading = ref(false)

// 搜索
const searchName = ref('')

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')

// 表单
const form = ref<RoleForm>({
  name: '',
  code: '',
  description: '',
  permissions: [],
  status: 'active'
})

const permissionTree = ref<PermissionTree[]>([])
const treeRef = ref()

// 获取角色列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getRoleList({ name: searchName.value })
    tableData.value = res.data.list
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取权限树
async function fetchPermissions() {
  try {
    const res = await getPermissionTree()
    permissionTree.value = res.data
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
  searchName.value = ''
  fetchData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增角色'
  form.value = {
    name: '',
    code: '',
    description: '',
    permissions: [],
    status: 'active'
  }
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: Role) {
  dialogTitle.value = '编辑角色'
  form.value = {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description,
    permissions: [...row.permissions],
    status: row.status
  }
  dialogVisible.value = true
}

// 删除
function handleDelete(row: Role) {
  ElMessageBox.confirm(`确定要删除角色「${row.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

// 提交
async function handleSubmit() {
  try {
    // 从树组件获取选中的权限
    const checkedKeys = treeRef.value?.getCheckedKeys() || []
    form.value.permissions = checkedKeys

    if (form.value.id) {
      await updateRole(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createRole(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

// 监听弹窗打开，设置树的选中状态
watch(dialogVisible, (val) => {
  if (val && form.value.permissions.length > 0) {
    nextTick(() => {
      treeRef.value?.setCheckedKeys(form.value.permissions)
    })
  }
})

onMounted(() => {
  fetchData()
  fetchPermissions()
})
</script>

<template>
  <div class="role-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="角色名称">
          <el-input v-model="searchName" placeholder="请输入角色名称" clearable />
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
          <span>角色列表</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" />
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

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="form.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="权限">
          <el-tree
            ref="treeRef"
            :data="permissionTree"
            :props="{ label: 'name', children: 'children' }"
            show-checkbox
            node-key="id"
            :default-expand-all="true"
          />
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
.role-container {
  height: 100%;
}

.search-card {
  margin-bottom: 20px;
}
</style>
