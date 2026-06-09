<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getLogList,
  getLog,
  retry,
  getConfigList,
  getLogHistory
} from '../../../api/distribution'
import MdmDialog from '../../../components/MdmDialog.vue'

// 状态选项
const statusOptions = [
  { value: '', label: '全部' },
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAILED', label: '失败' },
  { value: 'RUNNING', label: '执行中' },
  { value: 'PENDING', label: '待执行' }
]

// 数据类型选项
const dataTypeOptions = [
  { value: '', label: '全部' },
  { value: 'MATERIAL', label: '物料' },
  { value: 'CUSTOMER', label: '客户' },
  { value: 'VENDOR', label: '供应商' }
]

const tableData = ref<any[]>([])
const loading = ref(false)
const configList = ref<any[]>([])

// 搜索
const searchForm = ref({
  dataType: '',
  status: '',
  systemConfigId: null as number | null,
  dataCode: '',
  dateRange: [] as string[]
})

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 详情弹窗
const detailVisible = ref(false)
const detail = ref<any>(null)
const detailTab = ref('basic')

// 血缘查看
const lineageVisible = ref(false)
const lineageData = ref<any[]>([])
const lineageLoading = ref(false)

// 展开行
const expandedRows = ref<number[]>([])

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
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value,
      dataType: searchForm.value.dataType || undefined,
      status: searchForm.value.status || undefined,
      systemConfigId: searchForm.value.systemConfigId || undefined
    }
    if (searchForm.value.dateRange?.length === 2) {
      params.startTime = searchForm.value.dateRange[0]
      params.endTime = searchForm.value.dateRange[1]
    }

    const res = await getLogList(params) as any
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

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    dataType: '',
    status: '',
    systemConfigId: null,
    dataCode: '',
    dateRange: []
  }
  currentPage.value = 1
  loadData()
}

// 查看详情
const handleDetail = async (row: any) => {
  try {
    const res = await getLog(row.id) as any
    detail.value = res.data
    detailTab.value = 'basic'
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// 重试
const handleRetry = async (row: any) => {
  try {
    const res = await retry(row.id) as any
    if (res.data?.success) {
      ElMessage.success('重试成功')
    } else {
      ElMessage.error(res.data?.errorMsg || '重试失败')
    }
    loadData()
  } catch (e) {
    console.error(e)
  }
}

// 查看数据血缘
const handleViewLineage = async (row: any) => {
  lineageLoading.value = true
  lineageVisible.value = true
  try {
    const res = await getLogHistory(row.dataType, row.dataId) as any
    lineageData.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    console.error(e)
  } finally {
    lineageLoading.value = false
  }
}

// 切换展开
const handleToggleExpand = (row: any) => {
  const idx = expandedRows.value.indexOf(row.id)
  if (idx > -1) {
    expandedRows.value.splice(idx, 1)
  } else {
    expandedRows.value.push(row.id)
  }
}

// 判断是否展开
const isExpanded = (row: any) => {
  return expandedRows.value.includes(row.id)
}

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

// 获取类型名称
const getTypeName = (type: string) => {
  return dataTypeOptions.find(o => o.value === type)?.label || type
}

// 格式化耗时
const formatDuration = (ms: number) => {
  if (!ms) return '-'
  if (ms < 1000) return `${ms}ms`
  return `${(ms / 1000).toFixed(2)}s`
}

// 格式化JSON
const formatJson = (jsonStr: string) => {
  if (!jsonStr) return '无'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

onMounted(() => {
  loadConfigList()
  loadData()
})
</script>

<template>
  <div class="trace-management-page">
    <!-- 顶部操作栏 -->
    <div class="mdm-top-bar">
      <div class="mdm-filter-row">
        <div class="mdm-filter-item">
          <select v-model="searchForm.dataType" @change="handleSearch">
            <option v-for="item in dataTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.status" @change="handleSearch">
            <option v-for="item in statusOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <select v-model="searchForm.systemConfigId" @change="handleSearch">
            <option :value="null">全部系统</option>
            <option v-for="item in configList" :key="item.id" :value="item.id">
              {{ item.configName }}
            </option>
          </select>
        </div>
        <div class="mdm-filter-item">
          <input v-model="searchForm.dataCode" type="text" placeholder="数据编码" @keyup.enter="handleSearch" />
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
            <th style="width: 40px"></th>
            <th style="width: 70px">数据类型</th>
            <th>数据编码</th>
            <th>数据名称</th>
            <th>目标系统</th>
            <th style="width: 70px">状态</th>
            <th style="width: 70px">耗时</th>
            <th style="width: 150px">开始时间</th>
            <th style="width: 180px">操作</th>
          </tr>
        </thead>
        <tbody v-loading="loading">
          <template v-for="row in tableData" :key="row.id">
            <tr>
              <td>
                <span class="expand-btn" @click="handleToggleExpand(row)">
                  {{ isExpanded(row) ? '▼' : '▶' }}
                </span>
              </td>
              <td>
                <span class="mdm-type-tag">{{ getTypeName(row.dataType) }}</span>
              </td>
              <td>{{ row.dataCode }}</td>
              <td>{{ row.dataName || '-' }}</td>
              <td>{{ row.systemConfigName }}</td>
              <td>
                <div class="mdm-status-badge">
                  <span :class="row.status === 'SUCCESS' ? 'mdm-green-dot' : row.status === 'FAILED' ? 'mdm-red-dot' : 'mdm-yellow-dot'"></span>
                  {{ row.status === 'SUCCESS' ? '成功' : row.status === 'FAILED' ? '失败' : row.status === 'RUNNING' ? '执行中' : '待执行' }}
                </div>
              </td>
              <td>{{ formatDuration(row.durationMs) }}</td>
              <td>{{ row.startTime }}</td>
              <td>
                <div class="mdm-action-buttons">
                  <button class="mdm-action-btn" @click="handleDetail(row)">详情</button>
                  <button class="mdm-action-btn" @click="handleViewLineage(row)">血缘</button>
                  <button v-if="row.status === 'FAILED'" class="mdm-action-btn warning" @click="handleRetry(row)">重试</button>
                </div>
              </td>
            </tr>
            <!-- 展开行 -->
            <tr v-if="isExpanded(row)" class="expand-row">
              <td colspan="9">
                <div class="expand-content">
                  <div class="expand-grid">
                    <div class="expand-item">
                      <span class="expand-label">日志编码:</span>
                      <span class="expand-value">{{ row.logCode }}</span>
                    </div>
                    <div class="expand-item">
                      <span class="expand-label">数据ID:</span>
                      <span class="expand-value">{{ row.dataId }}</span>
                    </div>
                    <div class="expand-item">
                      <span class="expand-label">操作类型:</span>
                      <span class="expand-value">{{ row.operation }}</span>
                    </div>
                    <div class="expand-item">
                      <span class="expand-label">目标Key:</span>
                      <span class="expand-value">{{ row.sapKey || '-' }}</span>
                    </div>
                    <div class="expand-item">
                      <span class="expand-label">接口名称:</span>
                      <span class="expand-value">{{ row.interfaceName }}</span>
                    </div>
                    <div class="expand-item">
                      <span class="expand-label">重试次数:</span>
                      <span class="expand-value">{{ row.retryCount || 0 }}</span>
                    </div>
                  </div>
                  <div v-if="row.errorMsg" class="error-box">
                    <span class="error-label">错误信息:</span>
                    <pre>{{ row.errorMsg }}</pre>
                  </div>
                </div>
              </td>
            </tr>
          </template>
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
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
        <option :value="100">100条/页</option>
      </select>
    </div>

    <!-- 详情弹窗 -->
    <MdmDialog v-model="detailVisible" title="分发详情" width="700px">
      <!-- 标签页导航 -->
      <div class="tab-nav">
        <div
          class="tab-item"
          :class="{ active: detailTab === 'basic' }"
          @click="detailTab = 'basic'"
        >
          基本信息
        </div>
        <div
          class="tab-item"
          :class="{ active: detailTab === 'sap' }"
          @click="detailTab = 'sap'"
        >
          SAP返回
        </div>
        <div
          class="tab-item"
          :class="{ active: detailTab === 'request' }"
          @click="detailTab = 'request'"
        >
          请求数据
        </div>
        <div
          class="tab-item"
          :class="{ active: detailTab === 'mapped' }"
          @click="detailTab = 'mapped'"
        >
          映射数据
        </div>
        <div
          class="tab-item"
          :class="{ active: detailTab === 'response' }"
          @click="detailTab = 'response'"
        >
          响应数据
        </div>
        <div
          v-if="detail?.errorMsg"
          class="tab-item"
          :class="{ active: detailTab === 'error' }"
          @click="detailTab = 'error'"
        >
          错误信息
        </div>
      </div>

      <!-- 基本信息 -->
      <div v-show="detailTab === 'basic'" class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">日志编码</span>
          <span class="detail-value">{{ detail?.logCode }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">数据类型</span>
          <span class="detail-value">{{ detail?.dataType }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">数据编码</span>
          <span class="detail-value">{{ detail?.dataCode }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">数据名称</span>
          <span class="detail-value">{{ detail?.dataName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">目标系统</span>
          <span class="detail-value">{{ detail?.systemConfigName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">接口</span>
          <span class="detail-value">{{ detail?.interfaceName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作</span>
          <span class="detail-value">{{ detail?.operation }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态</span>
          <span class="detail-value">
            <span :class="['status-tag', detail?.status === 'SUCCESS' ? 'success' : detail?.status === 'FAILED' ? 'danger' : 'warning']">
              {{ detail?.status }}
            </span>
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">目标Key</span>
          <span class="detail-value">{{ detail?.sapKey || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">耗时</span>
          <span class="detail-value">{{ detail?.durationMs }} ms</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">开始时间</span>
          <span class="detail-value">{{ detail?.startTime }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">结束时间</span>
          <span class="detail-value">{{ detail?.endTime }}</span>
        </div>
      </div>

      <!-- SAP返回 -->
      <div v-show="detailTab === 'sap'" class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">返回码</span>
          <span class="detail-value">{{ detail?.sapReturnCode || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">消息类型</span>
          <span class="detail-value">{{ detail?.sapMessageType || '-' }}</span>
        </div>
        <div class="detail-item full">
          <span class="detail-label">消息</span>
          <span class="detail-value">{{ detail?.sapMessage || '-' }}</span>
        </div>
      </div>

      <!-- 请求数据 -->
      <div v-show="detailTab === 'request'" class="code-section">
        <pre class="code-block">{{ formatJson(detail?.requestData) }}</pre>
      </div>

      <!-- 映射数据 -->
      <div v-show="detailTab === 'mapped'" class="code-section">
        <pre class="code-block">{{ formatJson(detail?.mappedData) }}</pre>
      </div>

      <!-- 响应数据 -->
      <div v-show="detailTab === 'response'" class="code-section">
        <pre class="code-block">{{ formatJson(detail?.responseData) }}</pre>
      </div>

      <!-- 错误信息 -->
      <div v-show="detailTab === 'error'" class="error-section">
        <pre class="error-block">{{ detail?.errorMsg }}</pre>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="detailVisible = false">关闭</button>
      </template>
    </MdmDialog>

    <!-- 血缘弹窗 -->
    <MdmDialog v-model="lineageVisible" title="数据血缘" width="900px">
      <div class="lineage-tip">该数据的所有分发记录（按时间倒序）</div>

      <div class="mdm-table-wrapper" style="margin-top: 12px">
        <table class="mdm-data-table">
          <thead>
            <tr>
              <th style="width: 50px">#</th>
              <th>日志编码</th>
              <th>目标系统</th>
              <th style="width: 70px">操作</th>
              <th style="width: 70px">状态</th>
              <th>目标Key</th>
              <th style="width: 70px">耗时</th>
              <th style="width: 150px">时间</th>
            </tr>
          </thead>
          <tbody v-loading="lineageLoading">
            <tr v-for="(row, index) in lineageData" :key="row.id">
              <td>{{ index + 1 }}</td>
              <td>{{ row.logCode }}</td>
              <td>{{ row.systemConfigName }}</td>
              <td>{{ row.operation }}</td>
              <td>
                <div class="mdm-status-badge">
                  <span :class="row.status === 'SUCCESS' ? 'mdm-green-dot' : row.status === 'FAILED' ? 'mdm-red-dot' : 'mdm-yellow-dot'"></span>
                  {{ row.status === 'SUCCESS' ? '成功' : row.status === 'FAILED' ? '失败' : row.status }}
                </div>
              </td>
              <td>{{ row.sapKey || '-' }}</td>
              <td>{{ row.durationMs ? row.durationMs + 'ms' : '-' }}</td>
              <td>{{ row.createdAt }}</td>
            </tr>
            <tr v-if="lineageData.length === 0 && !lineageLoading">
              <td colspan="8" class="mdm-empty-data">暂无分发记录</td>
            </tr>
          </tbody>
        </table>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="lineageVisible = false">关闭</button>
      </template>
    </MdmDialog>
  </div>
</template>

<style scoped lang="scss">
@import '../../../assets/styles/mdm-common.scss';

.trace-management-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

// 展开按钮
.expand-btn {
  cursor: pointer;
  color: #409eff;
  font-size: 12px;
}

// 展开行
.expand-row {
  background: #fafafa;

  .expand-content {
    padding: 12px 20px;
  }

  .expand-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .expand-item {
    display: flex;
    font-size: 13px;

    .expand-label {
      width: 70px;
      color: #909399;
      flex-shrink: 0;
    }

    .expand-value {
      flex: 1;
      color: #303133;
    }
  }

  .error-box {
    margin-top: 12px;
    padding: 10px 12px;
    background: #fef0f0;
    border-radius: 4px;

    .error-label {
      font-size: 13px;
      font-weight: 500;
      color: #f56c6c;
    }

    pre {
      margin: 6px 0 0;
      color: #f56c6c;
      font-size: 12px;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
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

// 操作按钮
.mdm-action-btn {
  &.warning {
    color: #e6a23c;
  }
}

// 标签页导航
.tab-nav {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  padding: 4px;
  background: #f5f5f5;
  border-radius: 4px;

  .tab-item {
    padding: 8px 16px;
    font-size: 13px;
    color: #666;
    cursor: pointer;
    border-radius: 3px;
    transition: all 0.2s;

    &:hover {
      color: #333;
    }

    &.active {
      background: #fff;
      color: #e74c3c;
      font-weight: 500;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }
  }
}

// 详情网格
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: hidden;

  .detail-item {
    display: flex;
    border-bottom: 1px solid #e8e8e8;

    &.full {
      grid-column: 1 / -1;
    }

    &:nth-last-child(-n+2) { border-bottom: none; }
    &:nth-child(odd) { border-right: 1px solid #e8e8e8; }

    .detail-label {
      width: 80px;
      padding: 10px 12px;
      background: #fafafa;
      font-size: 13px;
      color: #666;
      flex-shrink: 0;
    }

    .detail-value {
      flex: 1;
      padding: 10px 12px;
      font-size: 13px;
      color: #333;
      word-break: break-all;
    }
  }
}

// 状态标签
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;

  &.success { background: #f0f9eb; color: #67c23a; }
  &.danger { background: #fef0f0; color: #f56c6c; }
  &.warning { background: #fdf6ec; color: #e6a23c; }
}

// 代码区块
.code-section {
  .code-block {
    background: #f5f5f5;
    padding: 15px;
    border-radius: 4px;
    overflow: auto;
    max-height: 300px;
    font-size: 12px;
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

// 错误区块
.error-section {
  .error-block {
    background: #fef0f0;
    padding: 15px;
    border-radius: 4px;
    overflow: auto;
    max-height: 300px;
    font-size: 12px;
    margin: 0;
    color: #f56c6c;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

// 血缘提示
.lineage-tip {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}
</style>
