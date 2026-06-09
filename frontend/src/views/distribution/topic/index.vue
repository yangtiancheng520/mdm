<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTopicList,
  createTopic,
  updateTopic,
  deleteTopic,
  toggleTopicStatus,
  getSubscriptions,
  addSubscription,
  deleteSubscription
} from '../../../api/topic'
import { getConfigList } from '../../../api/distribution'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

// 数据类型选项 - 只保留测试通过的类型
const dataTypeOptions = [
  { value: 'MATERIAL', label: '物料' },
  { value: 'CUSTOMER', label: '客户' },
  { value: 'VENDOR', label: '供应商' },
  { value: 'EMPLOYEE', label: '员工' },
  { value: 'ORGANIZATION', label: '组织' }
]

// 分发模式选项
const distributeModeOptions = [
  { value: 'MANUAL', label: '手动触发' },
  { value: 'REALTIME', label: '实时分发' },
  { value: 'SCHEDULE', label: '定时分发' }
]

const tableData = ref<any[]>([])
const loading = ref(false)
const configList = ref<any[]>([])

// 搜索
const searchForm = ref({
  dataType: '',
  status: '',
  keyword: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增主题')
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单
const form = ref<any>({
  topicCode: '',
  topicName: '',
  dataType: 'MATERIAL',
  distributeMode: 'MANUAL',
  cronExpression: '',
  batchSize: 100,
  retryCount: 3,
  retryInterval: 5000,
  status: 'inactive',
  description: ''
})

// 订阅弹窗
const subscriptionVisible = ref(false)
const currentTopic = ref<any>(null)
const subscriptionList = ref<any[]>([])
const addSubscriptionVisible = ref(false)
const subscriptionForm = ref<any>({
  systemConfigId: null,
  priority: 0,
  syncMode: 'SYNC',
  enableCreate: 1,
  enableUpdate: 1,
  enableDelete: 0,
  status: 'active'
})

// 加载配置列表
const loadConfigList = async () => {
  try {
    const res = await getConfigList() as any
    configList.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    console.error(e)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getTopicList({
      dataType: searchForm.value.dataType || undefined,
      status: searchForm.value.status || undefined,
      keyword: searchForm.value.keyword || undefined,
      page: currentPage.value - 1,
      size: pageSize.value
    }) as any
    tableData.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    dataType: '',
    status: '',
    keyword: ''
  }
  currentPage.value = 1
  loadData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增主题'
  form.value = {
    topicCode: '',
    topicName: '',
    dataType: 'MATERIAL',
    distributeMode: 'MANUAL',
    cronExpression: '',
    batchSize: 100,
    retryCount: 3,
    retryInterval: 5000,
    status: 'inactive',
    description: ''
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑主题'
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存
const handleSave = async () => {
  if (!form.value.topicCode || !form.value.topicName) {
    ElMessage.warning('请填写主题编码和名称')
    return
  }

  try {
    form.value.dataTypeName = dataTypeOptions.find(o => o.value === form.value.dataType)?.label
    if (form.value.id) {
      await updateTopic(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createTopic(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

// 删除
const handleDelete = (row: any) => {
  confirmMessage.value = `确定删除主题"${row.topicName}"吗？删除后关联的订阅也将被删除。`
  confirmAction.value = async () => {
    try {
      await deleteTopic(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
  confirmVisible.value = true
}

// 切换状态
const handleToggle = (row: any) => {
  const action = row.status === 'active' ? '停用' : '启用'
  confirmMessage.value = `确定${action}主题"${row.topicName}"吗？`
  confirmAction.value = async () => {
    try {
      await toggleTopicStatus(row.id)
      ElMessage.success('操作成功')
      loadData()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }
  confirmVisible.value = true
}

// 管理订阅
const handleManageSubscriptions = async (row: any) => {
  currentTopic.value = row
  await loadSubscriptions(row.id)
  subscriptionVisible.value = true
}

// 加载订阅列表
const loadSubscriptions = async (topicId: number) => {
  try {
    const res = await getSubscriptions(topicId) as any
    subscriptionList.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    console.error(e)
  }
}

// 打开添加订阅弹窗
const handleAddSubscription = () => {
  subscriptionForm.value = {
    systemConfigId: null,
    priority: subscriptionList.value.length,
    syncMode: 'SYNC',
    enableCreate: 1,
    enableUpdate: 1,
    enableDelete: 0,
    status: 'active'
  }
  addSubscriptionVisible.value = true
}

// 保存订阅
const handleSaveSubscription = async () => {
  if (!subscriptionForm.value.systemConfigId) {
    ElMessage.warning('请选择目标系统')
    return
  }

  try {
    const config = configList.value.find(c => c.id === subscriptionForm.value.systemConfigId)
    await addSubscription(currentTopic.value.id, {
      ...subscriptionForm.value,
      systemConfigName: config?.configName,
      systemType: config?.systemType
    })
    ElMessage.success('添加成功')
    addSubscriptionVisible.value = false
    loadSubscriptions(currentTopic.value.id)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  }
}

// 删除订阅
const handleDeleteSubscription = (row: any) => {
  confirmMessage.value = `确定移除订阅"${row.systemConfigName}"吗？`
  confirmAction.value = async () => {
    try {
      await deleteSubscription(row.id)
      ElMessage.success('移除成功')
      loadSubscriptions(currentTopic.value.id)
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '移除失败')
    }
  }
  confirmVisible.value = true
}

// 获取数据类型名称
const getTypeName = (type: string) => {
  return dataTypeOptions.find(o => o.value === type)?.label || type
}

// 获取分发模式名称
const getModeName = (mode: string) => {
  return distributeModeOptions.find(o => o.value === mode)?.label || mode
}

// 分页
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

onMounted(() => {
  loadConfigList()
  loadData()
})
</script>

<template>
  <div class="topic-management-page">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <select v-model="searchForm.dataType" @change="handleSearch">
            <option value="">全部类型</option>
            <option v-for="item in dataTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="active">启用</option>
            <option value="inactive">停用</option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <input v-model="searchForm.keyword" type="text" placeholder="主题编码/名称" @keyup.enter="handleSearch" />
        </div>
      </div>
      <div class="mdm-right-group">
        <button class="mdm-btn-red" @click="handleAdd">+ 新增主题</button>
        <button class="mdm-btn-outline" @click="handleSearch">查询</button>
        <button class="mdm-btn-outline" @click="handleReset">重置</button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="mdm-table-wrapper">
      <table class="mdm-data-table">
        <thead>
          <tr>
            <th style="width: 50px">序号</th>
            <th>主题编码</th>
            <th>主题名称</th>
            <th style="width: 100px">数据类型</th>
            <th style="width: 100px">分发模式</th>
            <th style="width: 100px">Cron表达式</th>
            <th style="width: 80px">订阅数</th>
            <th style="width: 80px">状态</th>
            <th style="width: 150px">创建时间</th>
            <th style="width: 200px">操作</th>
          </tr>
        </thead>
        <tbody v-loading="loading">
          <tr v-for="(row, index) in tableData" :key="row.id">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td>{{ row.topicCode }}</td>
            <td>{{ row.topicName }}</td>
            <td>
              <span class="mdm-type-tag">{{ getTypeName(row.dataType) }}</span>
            </td>
            <td>{{ getModeName(row.distributeMode) }}</td>
            <td>{{ row.cronExpression || '-' }}</td>
            <td>
              <span class="subscription-link" @click="handleManageSubscriptions(row)">
                {{ row.subscriptionCount || 0 }}
              </span>
            </td>
            <td>
              <div class="mdm-status-badge">
                <span :class="row.status === 'active' ? 'mdm-green-dot' : 'mdm-red-dot'"></span>
                {{ row.status === 'active' ? '启用' : '停用' }}
              </div>
            </td>
            <td>{{ row.createdAt }}</td>
            <td>
              <div class="mdm-action-buttons">
                <button class="mdm-action-btn" @click="handleManageSubscriptions(row)">订阅</button>
                <button class="mdm-action-btn" @click="handleEdit(row)">编辑</button>
                <button
                  class="mdm-action-btn"
                  :class="row.status === 'active' ? 'warning' : 'success'"
                  @click="handleToggle(row)"
                >
                  {{ row.status === 'active' ? '停用' : '启用' }}
                </button>
                <button class="mdm-action-btn delete" @click="handleDelete(row)">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="tableData.length === 0 && !loading">
            <td colspan="10" class="mdm-empty-data">暂无数据</td>
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

    <!-- 新增/编辑主题弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>主题编码</div>
        <input v-model="form.topicCode" class="mdm-input-yellow" placeholder="如: MATERIAL_SAP" :disabled="!!form.id" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>主题名称</div>
        <input v-model="form.topicName" class="mdm-input-yellow" placeholder="如: 物料分发到SAP" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>数据类型</div>
        <select v-model="form.dataType" class="mdm-select">
          <option v-for="item in dataTypeOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">分发模式</div>
        <select v-model="form.distributeMode" class="mdm-select">
          <option v-for="item in distributeModeOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row" v-if="form.distributeMode === 'SCHEDULE'">
        <div class="mdm-form-label">Cron表达式</div>
        <input v-model="form.cronExpression" class="mdm-input-yellow" placeholder="如: 0 0 2 * * ? (每天凌晨2点)" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">批量大小</div>
        <input v-model="form.batchSize" type="number" class="mdm-input-yellow" min="1" max="1000" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">重试次数</div>
        <input v-model="form.retryCount" type="number" class="mdm-input-yellow" min="0" max="10" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">重试间隔(ms)</div>
        <input v-model="form.retryInterval" type="number" class="mdm-input-yellow" min="1000" max="60000" step="1000" />
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
            停用
          </label>
        </div>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">描述</div>
        <textarea v-model="form.description" class="mdm-textarea" rows="2" placeholder="请输入描述"></textarea>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSave">确定</button>
      </template>
    </MdmDialog>

    <!-- 订阅管理弹窗 -->
    <MdmDialog v-model="subscriptionVisible" :title="`订阅管理 - ${currentTopic?.topicName || ''}`" width="900px">
      <div class="subscription-header">
        <span class="subscription-tip">配置数据分发的目标系统</span>
        <button class="mdm-btn-red" @click="handleAddSubscription">+ 添加订阅</button>
      </div>

      <div class="mdm-table-wrapper" style="margin-top: 12px">
        <table class="mdm-data-table">
          <thead>
            <tr>
              <th>目标系统</th>
              <th style="width: 80px">系统类型</th>
              <th style="width: 60px">优先级</th>
              <th style="width: 70px">同步模式</th>
              <th style="width: 120px">分发操作</th>
              <th style="width: 70px">状态</th>
              <th style="width: 70px">成功</th>
              <th style="width: 70px">失败</th>
              <th style="width: 80px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in subscriptionList" :key="row.id">
              <td>{{ row.systemConfigName }}</td>
              <td>
                <span class="mdm-type-tag">{{ row.systemType }}</span>
              </td>
              <td>{{ row.priority }}</td>
              <td>{{ row.syncMode === 'SYNC' ? '同步' : '异步' }}</td>
              <td>
                <span v-if="row.enableCreate" class="op-tag">新增</span>
                <span v-if="row.enableUpdate" class="op-tag">更新</span>
                <span v-if="row.enableDelete" class="op-tag">删除</span>
              </td>
              <td>
                <div class="mdm-status-badge">
                  <span :class="row.status === 'active' ? 'mdm-green-dot' : 'mdm-red-dot'"></span>
                  {{ row.status === 'active' ? '启用' : '停用' }}
                </div>
              </td>
              <td>{{ row.totalSuccessCount || 0 }}</td>
              <td>{{ row.totalFailCount || 0 }}</td>
              <td>
                <div class="mdm-action-buttons">
                  <button class="mdm-action-btn delete" @click="handleDeleteSubscription(row)">移除</button>
                </div>
              </td>
            </tr>
            <tr v-if="subscriptionList.length === 0">
              <td colspan="9" class="mdm-empty-data">暂无订阅，请添加目标系统</td>
            </tr>
          </tbody>
        </table>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="subscriptionVisible = false">关闭</button>
      </template>
    </MdmDialog>

    <!-- 添加订阅弹窗 -->
    <MdmDialog v-model="addSubscriptionVisible" title="添加订阅" width="500px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>目标系统</div>
        <select v-model="subscriptionForm.systemConfigId" class="mdm-select">
          <option value="">请选择目标系统</option>
          <option
            v-for="item in configList.filter(c => c.status === 'active')"
            :key="item.id"
            :value="item.id"
          >
            {{ item.configName }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">优先级</div>
        <input v-model="subscriptionForm.priority" type="number" class="mdm-input-yellow" min="0" max="99" />
        <span class="form-tip">数字越小优先级越高</span>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">同步模式</div>
        <div class="mdm-radio-group">
          <label class="mdm-radio-item">
            <input type="radio" v-model="subscriptionForm.syncMode" value="SYNC" />
            同步
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="subscriptionForm.syncMode" value="ASYNC" />
            异步
          </label>
        </div>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">分发操作</div>
        <div class="mdm-checkbox-group">
          <label class="mdm-checkbox-item">
            <input type="checkbox" v-model="subscriptionForm.enableCreate" :true-value="1" :false-value="0" />
            新增
          </label>
          <label class="mdm-checkbox-item">
            <input type="checkbox" v-model="subscriptionForm.enableUpdate" :true-value="1" :false-value="0" />
            更新
          </label>
          <label class="mdm-checkbox-item">
            <input type="checkbox" v-model="subscriptionForm.enableDelete" :true-value="1" :false-value="0" />
            删除
          </label>
        </div>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="addSubscriptionVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSaveSubscription">确定</button>
      </template>
    </MdmDialog>

    <!-- 确认对话框 -->
    <MdmConfirmDialog
      v-model="confirmVisible"
      :message="confirmMessage"
      @confirm="confirmAction?.()"
    />
  </div>
</template>

<style scoped lang="scss">
@import '../../../assets/styles/mdm-common.scss';

.topic-management-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

// 类型标签
.mdm-type-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 3px;
  font-size: 12px;
}

// 订阅链接
.subscription-link {
  color: #e74c3c;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    text-decoration: underline;
  }
}

// 操作按钮
.mdm-action-btn {
  &.warning {
    color: #e6a23c;
  }
  &.success {
    color: #67c23a;
  }
}

// 订阅管理头部
.subscription-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .subscription-tip {
    color: #666;
    font-size: 13px;
  }
}

// 操作标签
.op-tag {
  display: inline-block;
  padding: 2px 6px;
  margin-right: 4px;
  background: #f0f0f0;
  border-radius: 3px;
  font-size: 12px;
  color: #666;
}

// 表单提示
.form-tip {
  margin-left: 10px;
  color: #999;
  font-size: 12px;
}

// 文本域
.mdm-textarea {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  font-size: 13px;
  resize: vertical;

  &:focus {
    outline: none;
    border-color: #e74c3c;
  }

  &::placeholder {
    color: #94a3b8;
  }
}

// 复选框组
.mdm-checkbox-group {
  display: flex;
  gap: 20px;
}

.mdm-checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 13px;

  input[type="checkbox"] {
    width: 16px;
    height: 16px;
    cursor: pointer;
  }
}
</style>
