<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getAllRoles, type Role, type RoleForm } from '../../../api/role'
import { getPermissionTree, type PermissionTree } from '../../../api/permission'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

const tableData = ref<Role[]>([])
const loading = ref(false)

// 搜索
const searchName = ref('')

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

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

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取角色列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getRoleList({ name: searchName.value })
    tableData.value = res.data.list
    total.value = res.data.total || res.data.list.length
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
  currentPage.value = 1
  fetchData()
}

// 重置
function handleReset() {
  searchName.value = ''
  currentPage.value = 1
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
  confirmMessage.value = `确定要删除角色「${row.name}」吗？`
  confirmAction.value = async () => {
    await deleteRole(row.id)
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

// 提交
async function handleSubmit() {
  try {
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

// 监听弹窗打开
watch(dialogVisible, (val) => {
  if (val && form.value.permissions.length > 0) {
    nextTick(() => {
      treeRef.value?.setCheckedKeys(form.value.permissions)
    })
  }
})

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
  fetchPermissions()
})
</script>

<template>
  <div class="mdm-container">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <input v-model="searchName" type="text" placeholder="角色名称" @keyup.enter="handleSearch" />
        </div>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-red" @click="handleAdd">+ 新增</button>
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
            <th>角色名称</th>
            <th>角色编码</th>
            <th>描述</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-loading="loading">
          <tr v-for="row in tableData" :key="row.id">
            <td>
              <input type="checkbox" />
            </td>
            <td>{{ row.name }}</td>
            <td>{{ row.code }}</td>
            <td>{{ row.description }}</td>
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
            <td colspan="7" class="mdm-empty-data">暂无数据</td>
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

    <!-- 弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>角色名称</div>
        <input v-model="form.name" class="mdm-input-yellow" placeholder="请输入角色名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>角色编码</div>
        <input v-model="form.code" class="mdm-input-yellow" placeholder="请输入角色编码" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="form.description" class="mdm-textarea" placeholder="请输入描述"></textarea>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">权限</div>
        <div style="flex: 1;">
          <el-tree
            ref="treeRef"
            :data="permissionTree"
            :props="{ label: 'name', children: 'children' }"
            show-checkbox
            node-key="id"
            :default-expand-all="true"
          />
        </div>
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
