<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getIssueList,
  assignIssue,
  resolveIssue,
  ignoreIssue,
  closeIssue,
  ISSUE_STATUS_OPTIONS,
  ISSUE_LEVEL_OPTIONS,
  type QualityIssue
} from '../../../api/quality/issue'
import { getViewList, type ViewDefinition } from '../../../api/standard/viewDefinition'
import { getUserList, type User } from '../../../api/user'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

const tableData = ref<QualityIssue[]>([])
const loading = ref(false)

// 搜索
const searchForm = ref({
  status: '',
  issueLevel: '',
  viewId: null as number | null
})

// 视图列表
const viewList = ref<ViewDefinition[]>([])

// 用户列表
const userList = ref<User[]>([])

// 弹窗
const assignDialogVisible = ref(false)
const resolveDialogVisible = ref(false)
const ignoreDialogVisible = ref(false)

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 当前操作的问题
const currentIssue = ref<QualityIssue | null>(null)
const assignForm = ref({ assignee: '' })
const resolveForm = ref({ resolution: '' })
const ignoreForm = ref({ reason: '' })

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取问题列表
async function fetchData() {
  loading.value = true
  try {
    const res = await getIssueList(searchForm.value)
    const allData = res.data || []
    total.value = allData.length

    // 前端分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    tableData.value = allData.slice(start, end)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取视图列表
async function fetchViewList() {
  try {
    const res = await getViewList({ status: 'published' })
    viewList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 获取用户列表
async function fetchUserList() {
  try {
    const res = await getUserList({ status: 'active' })
    userList.value = res.data?.list || []
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
  searchForm.value = {
    status: '',
    issueLevel: '',
    viewId: null
  }
  currentPage.value = 1
  fetchData()
}

// 指派
function handleAssign(row: QualityIssue) {
  currentIssue.value = row
  assignForm.value.assignee = row.assignee || ''
  assignDialogVisible.value = true
}

// 确认指派
async function handleConfirmAssign() {
  if (!assignForm.value.assignee) {
    ElMessage.warning('请选择处理人')
    return
  }
  try {
    await assignIssue(currentIssue.value!.id!, assignForm.value.assignee)
    ElMessage.success('指派成功')
    assignDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '指派失败')
  }
}

// 解决
function handleResolve(row: QualityIssue) {
  currentIssue.value = row
  resolveForm.value.resolution = ''
  resolveDialogVisible.value = true
}

// 确认解决
async function handleConfirmResolve() {
  if (!resolveForm.value.resolution) {
    ElMessage.warning('请输入解决方案')
    return
  }
  try {
    await resolveIssue(currentIssue.value!.id!, resolveForm.value.resolution)
    ElMessage.success('已标记为解决')
    resolveDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 忽略
function handleIgnore(row: QualityIssue) {
  currentIssue.value = row
  ignoreForm.value.reason = ''
  ignoreDialogVisible.value = true
}

// 确认忽略
async function handleConfirmIgnore() {
  if (!ignoreForm.value.reason) {
    ElMessage.warning('请输入忽略原因')
    return
  }
  try {
    await ignoreIssue(currentIssue.value!.id!, ignoreForm.value.reason)
    ElMessage.success('已忽略')
    ignoreDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 关闭
function handleClose(row: QualityIssue) {
  confirmMessage.value = `确定要关闭问题「${row.issueCode}」吗？`
  confirmAction.value = async () => {
    await closeIssue(row.id!)
    ElMessage.success('已关闭')
    fetchData()
  }
  confirmVisible.value = true
}

// 确认删除
function handleConfirm() {
  if (confirmAction.value) {
    confirmAction.value()
  }
}

// 获取状态标签
function getStatusLabel(status: string) {
  const item = ISSUE_STATUS_OPTIONS.find(s => s.value === status)
  return item?.label || status
}

// 获取状态样式类
function getStatusClass(status: string) {
  const classMap: Record<string, string> = {
    open: 'status-open',
    processing: 'status-processing',
    resolved: 'status-resolved',
    ignored: 'status-ignored',
    closed: 'status-closed'
  }
  return classMap[status] || ''
}

// 获取级别样式类
function getLevelClass(level?: string) {
  if (!level) return ''
  const classMap: Record<string, string> = {
    warning: 'level-warning',
    error: 'level-error',
    critical: 'level-critical'
  }
  return classMap[level] || ''
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
  fetchViewList()
  fetchUserList()
})
</script>

<template>
  <div class="mdm-container">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option v-for="item in ISSUE_STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.issueLevel" @change="handleSearch">
            <option value="">全部级别</option>
            <option v-for="item in ISSUE_LEVEL_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.viewId" @change="handleSearch">
            <option :value="null">全部视图</option>
            <option v-for="view in viewList" :key="view.id" :value="view.id">{{ view.viewName }}</option>
          </select>
        </div>
      </div>
      <div class="mdm-right-group">
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
            <th>问题编号</th>
            <th>规则</th>
            <th>视图</th>
            <th>实体</th>
            <th>字段</th>
            <th>当前值</th>
            <th>问题级别</th>
            <th>问题描述</th>
            <th>状态</th>
            <th>处理人</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-loading="loading">
          <tr v-for="row in tableData" :key="row.id">
            <td>
              <input type="checkbox" />
            </td>
            <td>{{ row.issueCode }}</td>
            <td>{{ row.ruleName || '-' }}</td>
            <td>{{ row.viewName || '-' }}</td>
            <td>
              <span class="entity-tag" :class="row.entityType">
                {{ row.entityType === 'main' ? '主表' : '子表' }}
              </span>
            </td>
            <td>{{ row.fieldName || row.fieldCode || '-' }}</td>
            <td class="field-value">{{ row.fieldValue || '-' }}</td>
            <td>
              <span v-if="row.issueLevel" class="level-tag" :class="getLevelClass(row.issueLevel)">
                {{ row.issueLevel }}
              </span>
              <span v-else>-</span>
            </td>
            <td class="issue-desc">{{ row.issueDesc || '-' }}</td>
            <td>
              <span class="status-tag" :class="getStatusClass(row.status)">
                {{ getStatusLabel(row.status) }}
              </span>
            </td>
            <td>{{ row.assignee || '-' }}</td>
            <td>
              <div class="mdm-action-buttons">
                <button v-if="row.status === 'open'" class="mdm-action-btn" @click="handleAssign(row)">指派</button>
                <button v-if="row.status === 'open' || row.status === 'processing'" class="mdm-action-btn" @click="handleResolve(row)">解决</button>
                <button v-if="row.status === 'open' || row.status === 'processing'" class="mdm-action-btn" @click="handleIgnore(row)">忽略</button>
                <button v-if="row.status === 'resolved'" class="mdm-action-btn" @click="handleClose(row)">关闭</button>
              </div>
            </td>
          </tr>
          <tr v-if="tableData.length === 0 && !loading">
            <td colspan="12" class="mdm-empty-data">暂无数据</td>
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

    <!-- 指派弹窗 -->
    <MdmDialog v-model="assignDialogVisible" title="指派问题" width="400px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>处理人</div>
        <el-select
          v-model="assignForm.assignee"
          filterable
          clearable
          placeholder="请选择或搜索处理人"
          style="width: 100%"
        >
          <el-option
            v-for="user in userList"
            :key="user.account"
            :label="user.name + ' (' + user.account + ')'"
            :value="user.account"
          />
        </el-select>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="assignDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleConfirmAssign">确定</button>
      </template>
    </MdmDialog>

    <!-- 解决弹窗 -->
    <MdmDialog v-model="resolveDialogVisible" title="解决问题" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>解决方案</div>
        <textarea v-model="resolveForm.resolution" class="mdm-textarea" placeholder="请输入解决方案"></textarea>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="resolveDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleConfirmResolve">确定</button>
      </template>
    </MdmDialog>

    <!-- 忽略弹窗 -->
    <MdmDialog v-model="ignoreDialogVisible" title="忽略问题" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>忽略原因</div>
        <textarea v-model="ignoreForm.reason" class="mdm-textarea" placeholder="请输入忽略原因"></textarea>
      </div>
      <template #footer>
        <button class="mdm-btn-cancel" @click="ignoreDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleConfirmIgnore">确定</button>
      </template>
    </MdmDialog>

    <!-- 确认对话框 -->
    <MdmConfirmDialog
      v-model="confirmVisible"
      :message="confirmMessage"
      @confirm="handleConfirm"
    />
  </div>
</template>

<style scoped>
@import '../../../assets/styles/mdm-common.scss';

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.status-open {
  background: #fff2f0;
  color: #ff4d4f;
}

.status-tag.status-processing {
  background: #e6f7ff;
  color: #1890ff;
}

.status-tag.status-resolved {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.status-ignored {
  background: #f0f0f0;
  color: #666;
}

.status-tag.status-closed {
  background: #f5f5f5;
  color: #999;
}

.entity-tag {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.entity-tag.main {
  background: #f6ffed;
  color: #52c41a;
}

.entity-tag.sub {
  background: #fff7e6;
  color: #fa8c16;
}

.level-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.level-tag.level-warning {
  background: #fffbe6;
  color: #faad14;
}

.level-tag.level-error {
  background: #fff2f0;
  color: #ff4d4f;
}

.level-tag.level-critical {
  background: #ff4d4f;
  color: #fff;
}

.field-value {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.issue-desc {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>