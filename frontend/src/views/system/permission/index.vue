<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPermissionList, createPermission, updatePermission, deletePermission, type Permission, type PermissionForm } from '../../../api/permission'

const tableData = ref<Permission[]>([])
const loading = ref(false)

// 搜索
const searchName = ref('')

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增权限')

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

// 权限类型选项
const typeOptions = [
  { label: '菜单', value: 'menu' },
  { label: '按钮', value: 'button' }
]

// 获取权限列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getPermissionList({ name: searchName.value })
    tableData.value = res.data.list
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
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
  dialogTitle.value = '新增权限'
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

// 编辑
function handleEdit(row: Permission) {
  dialogTitle.value = '编辑权限'
  form.value = {
    id: row.id,
    name: row.name,
    code: row.code,
    type: row.type,
    parentId: row.parentId,
    path: row.path,
    icon: row.icon,
    sort: row.sort,
    status: row.status
  }
  dialogVisible.value = true
}

// 删除
function handleDelete(row: Permission) {
  ElMessageBox.confirm(`确定要删除权限「${row.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

// 提交
async function handleSubmit() {
  try {
    if (form.value.id) {
      await updatePermission(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createPermission(form.value)
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
})
</script>

<template>
  <div class="permission-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="权限名称">
          <el-input v-model="searchName" placeholder="请输入权限名称" clearable />
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
          <span>权限列表</span>
          <el-button type="primary" @click="handleAdd">新增权限</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" border row-key="id">
        <el-table-column prop="name" label="权限名称" width="150" />
        <el-table-column prop="code" label="权限编码" width="150" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 'menu' ? 'primary' : 'warning'">
              {{ row.type === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" width="120" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
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
        <el-form-item label="权限名称" required>
          <el-input v-model="form.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" required>
          <el-input v-model="form.code" placeholder="请输入权限编码，如：user:view" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" placeholder="请输入路径" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
.permission-container {
  height: 100%;
}

.search-card {
  margin-bottom: 20px;
}
</style>
