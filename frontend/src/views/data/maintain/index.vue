<template>
  <div class="data-maintain-page">
    <!-- 左侧分类树 -->
    <div class="category-panel">
      <div class="panel-header">
        <span>数据分类</span>
      </div>
      <div class="panel-body">
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{ children: 'children', label: 'name' }"
          node-key="id"
          default-expand-all
          :highlight-current="true"
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <span class="tree-node">
              <el-icon v-if="data.type === 'folder'"><Folder /></el-icon>
              <el-icon v-else><Document /></el-icon>
              <span>{{ data.name }}</span>
            </span>
          </template>
        </el-tree>

        <el-empty v-if="treeData.length === 0" description="暂无分类" :image-size="60" />
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="content-panel">
      <!-- 未选中状态 -->
      <div v-if="!selectedNode" class="empty-state">
        <el-icon size="48"><Guide /></el-icon>
        <p>请从左侧选择分类或表单</p>
      </div>

      <!-- 文件夹内容 -->
      <template v-else-if="selectedNode.type === 'folder'">
        <div class="content-header">
          <span class="title">{{ selectedNode.name }}</span>
        </div>
        <div class="content-body">
          <div v-if="selectedNode.children && selectedNode.children.length > 0" class="items-grid">
            <div
              v-for="item in selectedNode.children"
              :key="item.id"
              class="item-card"
              @click="handleItemClick(item)"
            >
              <el-icon class="item-icon" :class="item.type">
                <Folder v-if="item.type === 'folder'" />
                <Document v-else />
              </el-icon>
              <span class="item-name">{{ item.name }}</span>
              <span v-if="item.type === 'form'" class="item-tag">表单</span>
            </div>
          </div>
          <el-empty v-else description="该文件夹下暂无内容" />
        </div>
      </template>

      <!-- 表单填写 -->
      <template v-else-if="selectedNode.type === 'form'">
        <div class="content-header">
          <span class="title">{{ selectedNode.name }}</span>
          <div class="header-actions">
            <el-button type="primary" @click="showDataForm = true" v-if="!showDataForm">
              + 新增数据
            </el-button>
            <el-button v-else @click="showDataForm = false">返回列表</el-button>
          </div>
        </div>
        <div class="content-body">
          <!-- 数据填写表单 -->
          <div v-if="showDataForm" class="form-container">
            <FormRenderer
              v-if="formDesign"
              :design="formDesign"
              v-model="formData"
              mode="edit"
            />
            <div class="form-actions">
              <el-button @click="handleCancelForm">取消</el-button>
              <el-button type="primary" @click="handleSaveForm" :loading="saving">保存</el-button>
            </div>
          </div>

          <!-- 数据列表 -->
          <div v-else class="data-list">
            <table class="mdm-data-table">
              <thead>
                <tr>
                  <th>序号</th>
                  <th>创建时间</th>
                  <th>创建人</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in dataList" :key="row.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ formatDateTime(row.createdAt) }}</td>
                  <td>{{ row.createdBy || '-' }}</td>
                  <td>
                    <span :class="['status-tag', row.status]">
                      {{ row.status === 'active' ? '生效' : '作废' }}
                    </span>
                  </td>
                  <td>
                    <div class="action-btns">
                      <button class="action-btn view" @click="handleView(row)">查看</button>
                      <button class="action-btn edit" @click="handleEdit(row)">编辑</button>
                      <button class="action-btn delete" @click="handleDelete(row)">删除</button>
                    </div>
                  </td>
                </tr>
                <tr v-if="dataList.length === 0">
                  <td colspan="5" class="empty-row">暂无数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>
    </div>

    <!-- 查看数据弹窗 -->
    <MdmDialog v-model="viewDialogVisible" title="查看数据" width="600px">
      <div v-if="viewData" class="view-content">
        <div v-for="(value, key) in viewData" :key="key" class="view-item">
          <span class="label">{{ key }}:</span>
          <span class="value">{{ value || '-' }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </MdmDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Document, Guide } from '@element-plus/icons-vue'
import MdmDialog from '@/components/MdmDialog.vue'
import { getCategoryTree, type DataCategoryDto } from '@/api/data/category'
import {
  getInstanceList,
  saveInstance,
  updateInstance,
  deleteInstance,
  type DataInstanceDto
} from '@/api/data/instance'
import { getFormDesign, type FormDesignRequest } from '@/api/form'

// 树数据
const treeData = ref<DataCategoryDto[]>([])
const treeRef = ref()
const selectedNode = ref<DataCategoryDto | null>(null)

// 表单相关
const formDesign = ref<FormDesignRequest | null>(null)
const formData = ref<Record<string, any>>({})
const showDataForm = ref(false)
const editingInstance = ref<DataInstanceDto | null>(null)
const saving = ref(false)

// 数据列表
const dataList = ref<DataInstanceDto[]>([])

// 查看弹窗
const viewDialogVisible = ref(false)
const viewData = ref<Record<string, any> | null>(null)

// 加载分类树
async function loadTree() {
  try {
    const res = await getCategoryTree()
    treeData.value = res.data || []
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

// 加载表单设计
async function loadFormDesign(formId: number) {
  try {
    const res = await getFormDesign(formId)
    formDesign.value = res.data
  } catch (error) {
    ElMessage.error('加载表单失败')
  }
}

// 加载数据列表
async function loadDataList() {
  if (!selectedNode.value || selectedNode.value.type !== 'form') return

  try {
    const res = await getInstanceList({
      categoryId: selectedNode.value.id,
      formId: selectedNode.value.formId!
    })
    dataList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 点击树节点
async function handleNodeClick(data: DataCategoryDto) {
  selectedNode.value = data
  showDataForm.value = false
  editingInstance.value = null

  if (data.type === 'form' && data.formId) {
    await Promise.all([
      loadFormDesign(data.formId),
      loadDataList()
    ])
  }
}

// 点击文件夹下的项目
function handleItemClick(item: DataCategoryDto) {
  if (item.type === 'form') {
    // 找到这个节点并触发点击
    handleNodeClick(item)
  }
}

// 取消表单
function handleCancelForm() {
  showDataForm.value = false
  editingInstance.value = null
  formData.value = {}
}

// 保存表单
async function handleSaveForm() {
  if (!selectedNode.value) return

  saving.value = true
  try {
    if (editingInstance.value) {
      await updateInstance(editingInstance.value.id, formData.value)
      ElMessage.success('更新成功')
    } else {
      await saveInstance({
        categoryId: selectedNode.value.id,
        formId: selectedNode.value.formId!,
        data: formData.value
      })
      ElMessage.success('保存成功')
    }
    showDataForm.value = false
    editingInstance.value = null
    formData.value = {}
    loadDataList()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 查看数据
function handleView(row: DataInstanceDto) {
  try {
    viewData.value = JSON.parse(row.dataJson)
    viewDialogVisible.value = true
  } catch {
    ElMessage.warning('数据格式错误')
  }
}

// 编辑数据
function handleEdit(row: DataInstanceDto) {
  try {
    formData.value = JSON.parse(row.dataJson)
    editingInstance.value = row
    showDataForm.value = true
  } catch {
    ElMessage.warning('数据格式错误')
  }
}

// 删除数据
async function handleDelete(row: DataInstanceDto) {
  try {
    await ElMessageBox.confirm('确定要删除此数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteInstance(row.id)
    ElMessage.success('删除成功')
    loadDataList()
  } catch {
    // 用户取消
  }
}

// 格式化日期时间
const formatDateTime = (dateStr: string | undefined) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/mdm-common.scss';

.data-maintain-page {
  display: flex;
  height: calc(100vh - 60px);
  background: #f5f5f5;
}

.category-panel {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 500;
}

.panel-body {
  flex: 1;
  overflow: auto;
  padding: 12px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
}

.content-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .title {
    font-size: 16px;
    font-weight: 500;
  }
}

.content-body {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.item-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
  }
}

.item-icon {
  font-size: 32px;
  color: #909399;

  &.folder {
    color: #e6a23c;
  }

  &.form {
    color: #409eff;
  }
}

.item-name {
  font-size: 14px;
  text-align: center;
}

.item-tag {
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 2px;
}

.form-container {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 2px;
  font-size: 12px;

  &.active {
    background: #f0f9eb;
    color: #67c23a;
  }

  &.obsolete {
    background: #f4f4f5;
    color: #909399;
  }
}

.action-btns {
  display: flex;
  gap: 6px;
  justify-content: center;

  .action-btn {
    padding: 4px 10px;
    font-size: 14px;
    border: none;
    background: none;
    cursor: pointer;
    border-radius: 4px;

    &.view { color: #1890ff; }
    &.edit { color: #52c41a; }
    &.delete { color: #f56c6c; }

    &:hover {
      background: #f5f5f5;
    }
  }
}

.empty-row {
  text-align: center;
  color: #94a3b8;
  padding: 40px;
}

.view-content {
  .view-item {
    display: flex;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 120px;
      color: #909399;
      flex-shrink: 0;
    }

    .value {
      flex: 1;
    }
  }
}
</style>
