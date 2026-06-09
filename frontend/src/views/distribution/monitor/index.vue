<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getLogStats,
  getConfigList,
  distribute,
  batchDistribute,
  getLogList,
  getLog,
  getDistributableDataList,
  getDataDetail
} from '../../../api/distribution'
import { getTopicList } from '../../../api/topic'
import MdmDialog from '../../../components/MdmDialog.vue'

// ==================== 统计看板 ====================

const loading = ref(false)
const configList = ref<any[]>([])

const stats = ref<any>({
  total: 0,
  byStatus: {}
})

const dateRange = ref<string[]>([])

const loadStats = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (dateRange.value?.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getLogStats(params) as any
    stats.value = res.data || { total: 0, byStatus: {} }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const successRate = () => {
  const total = stats.value.total || 0
  if (total === 0) return 0
  const success = stats.value.byStatus?.SUCCESS || 0
  return Math.round((success / total) * 100)
}

// ==================== 手动分发 ====================

const dataTypeOptions = [
  { value: 'MATERIAL', label: '物料' },
  { value: 'CUSTOMER', label: '客户' },
  { value: 'VENDOR', label: '供应商' }
]

const distributeLoading = ref(false)
const distributeForm = ref({
  dataType: 'MATERIAL',
  topicId: null as number | null,
  systemConfigId: null as number | null,
  dataId: null as number | null,
  dataCode: '',
  dataName: ''
})

const resultVisible = ref(false)
const result = ref<any>(null)

// ==================== 数据选择 ====================

const selectDataVisible = ref(false)
const selectDataLoading = ref(false)
const selectDataTable = ref<any[]>([])
const selectDataTotal = ref(0)
const selectDataPage = ref(1)
const selectDataKeyword = ref('')
const selectDataSelected = ref<any[]>([])
const selectDataAllChecked = ref(false)

// 打开数据选择弹窗
const openSelectDataDialog = () => {
  selectDataVisible.value = true
  selectDataPage.value = 1
  selectDataKeyword.value = ''
  selectDataSelected.value = []
  selectDataAllChecked.value = false
  loadSelectDataList()
}

// 加载可选数据列表
const loadSelectDataList = async () => {
  selectDataLoading.value = true
  try {
    const res = await getDistributableDataList({
      dataType: distributeForm.value.dataType,
      keyword: selectDataKeyword.value,
      page: selectDataPage.value,
      size: 10
    }) as any

    selectDataTable.value = res.data?.content || []
    selectDataTotal.value = res.data?.totalElements || 0
  } catch (e) {
    console.error(e)
    selectDataTable.value = []
    selectDataTotal.value = 0
  } finally {
    selectDataLoading.value = false
  }
}

// 搜索数据
const handleSearchData = () => {
  selectDataPage.value = 1
  loadSelectDataList()
}

// 分页变化
const handleSelectDataPageChange = (page: number) => {
  selectDataPage.value = page
  loadSelectDataList()
}

// 切换单行选择
const toggleSelectRow = (row: any) => {
  const idx = selectDataSelected.value.findIndex(r => r.id === row.id)
  if (idx > -1) {
    selectDataSelected.value.splice(idx, 1)
  } else {
    selectDataSelected.value.push(row)
  }
  updateAllCheckedStatus()
}

// 判断是否选中
const isRowSelected = (row: any) => {
  return selectDataSelected.value.some(r => r.id === row.id)
}

// 全选/取消全选（当前页）
const toggleSelectAll = () => {
  if (selectDataAllChecked.value) {
    // 取消全选
    selectDataSelected.value = []
  } else {
    // 全选当前页
    selectDataSelected.value = [...selectDataTable.value]
  }
  selectDataAllChecked.value = !selectDataAllChecked.value
}

// 更新全选状态
const updateAllCheckedStatus = () => {
  selectDataAllChecked.value = selectDataTable.value.length > 0 &&
    selectDataTable.value.every(row => isRowSelected(row))
}

// 确认选择（单条）
const confirmSelectSingle = (row: any) => {
  distributeForm.value.dataId = row.id
  distributeForm.value.dataCode = row.code || ''
  distributeForm.value.dataName = row.name || ''
  selectDataVisible.value = false
}

// 确认选择（多条）
const confirmSelectMultiple = () => {
  if (selectDataSelected.value.length === 0) {
    ElMessage.warning('请至少选择一条数据')
    return
  }
  selectDataVisible.value = false
}

// 清空选择
const clearSelection = () => {
  selectDataSelected.value = []
  selectDataAllChecked.value = false
}

// ==================== 分发执行 ====================

// 单条分发
const handleDistribute = async () => {
  if (!distributeForm.value.systemConfigId) {
    ElMessage.warning('请选择目标系统')
    return
  }
  if (!distributeForm.value.dataId) {
    ElMessage.warning('请选择要分发的数据')
    return
  }

  distributeLoading.value = true
  try {
    // 先获取完整数据
    const detailRes = await getDataDetail(
      distributeForm.value.dataType,
      distributeForm.value.dataId!
    ) as any

    const data = detailRes.data || {
      id: distributeForm.value.dataId,
      code: distributeForm.value.dataCode,
      name: distributeForm.value.dataName
    }

    const res = await distribute(
      distributeForm.value.dataType,
      distributeForm.value.dataId!,
      distributeForm.value.systemConfigId,
      data
    ) as any

    result.value = res.data
    resultVisible.value = true

    if (res.data?.success) {
      ElMessage.success('分发成功')
      loadStats()
      loadRecentLogs()
    } else {
      ElMessage.error(res.data?.errorMsg || '分发失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('分发失败')
  } finally {
    distributeLoading.value = false
  }
}

// 批量分发选中的数据
const handleBatchDistribute = async () => {
  if (!distributeForm.value.systemConfigId) {
    ElMessage.warning('请选择目标系统')
    return
  }
  if (selectDataSelected.value.length === 0) {
    ElMessage.warning('请选择要分发的数据')
    return
  }

  distributeLoading.value = true
  try {
    // 获取所有选中数据的详情
    const dataList = []
    for (const item of selectDataSelected.value) {
      try {
        const detailRes = await getDataDetail(distributeForm.value.dataType, item.id) as any
        if (detailRes.data) {
          dataList.push(detailRes.data)
        }
      } catch (e) {
        console.error('获取数据详情失败:', item.id, e)
      }
    }

    if (dataList.length === 0) {
      ElMessage.error('无法获取数据详情')
      return
    }

    const res = await batchDistribute(
      distributeForm.value.dataType,
      distributeForm.value.systemConfigId,
      dataList
    ) as any

    const results = res.data || []
    const successCount = results.filter((r: any) => r.success).length

    ElMessage.success(`批量分发完成：成功 ${successCount}/${results.length} 条`)

    selectDataVisible.value = false
    selectDataSelected.value = []
    loadStats()
    loadRecentLogs()
  } catch (e) {
    console.error(e)
    ElMessage.error('批量分发失败')
  } finally {
    distributeLoading.value = false
  }
}

// ==================== 最近分发记录 ====================

const recentLogs = ref<any[]>([])
const recentLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadRecentLogs = async () => {
  recentLoading.value = true
  try {
    const res = await getLogList({ page: currentPage.value - 1, size: pageSize.value }) as any
    recentLogs.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (e) {
    console.error(e)
  } finally {
    recentLoading.value = false
  }
}

// 查看详情
const detailVisible = ref(false)
const detail = ref<any>(null)

const handleViewDetail = async (row: any) => {
  try {
    const res = await getLog(row.id) as any
    detail.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

// 获取类型名称
const getTypeName = (type: string) => {
  return dataTypeOptions.find(o => o.value === type)?.label || type
}

// 分页
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadRecentLogs()
}

onMounted(() => {
  loadStats()
  loadRecentLogs()
  getConfigList().then((res: any) => {
    configList.value = Array.isArray(res) ? res.filter((c: any) => c.status === 'active') : ((res.data || []).filter((c: any) => c.status === 'active'))
  })
})
</script>

<template>
  <div class="monitor-management-page">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon total"></div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">分发总数</div>
        </div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon success"></div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.byStatus?.SUCCESS || 0 }}</div>
          <div class="stat-label">成功数</div>
        </div>
      </div>
      <div class="stat-card danger">
        <div class="stat-icon danger"></div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.byStatus?.FAILED || 0 }}</div>
          <div class="stat-label">失败数</div>
        </div>
      </div>
      <div class="stat-card primary">
        <div class="stat-icon primary"></div>
        <div class="stat-content">
          <div class="stat-value">{{ successRate() }}%</div>
          <div class="stat-label">成功率</div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧手动分发 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3>手动分发</h3>
        </div>
        <div class="panel-content">
          <div class="mdm-form-row">
            <div class="mdm-form-label">数据类型</div>
            <select v-model="distributeForm.dataType" class="mdm-select" @change="distributeForm.dataId = null; distributeForm.dataCode = ''; distributeForm.dataName = ''">
              <option v-for="item in dataTypeOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="mdm-form-row">
            <div class="mdm-form-label required"><em>*</em>目标系统</div>
            <select v-model="distributeForm.systemConfigId" class="mdm-select">
              <option :value="null">请选择</option>
              <option v-for="item in configList" :key="item.id" :value="item.id">
                {{ item.configName }}
              </option>
            </select>
          </div>

          <div class="form-divider">数据信息</div>

          <div class="mdm-form-row">
            <div class="mdm-form-label required"><em>*</em>选择数据</div>
            <div class="data-select-wrapper">
              <input
                :value="distributeForm.dataId ? `${distributeForm.dataCode || distributeForm.dataId}` : ''"
                class="mdm-input-yellow"
                placeholder="点击选择数据"
                readonly
                @click="openSelectDataDialog"
              />
              <button class="select-btn" @click="openSelectDataDialog">选择</button>
            </div>
          </div>

          <div class="mdm-form-row" v-if="distributeForm.dataName">
            <div class="mdm-form-label">数据名称</div>
            <input :value="distributeForm.dataName" class="mdm-input-normal" readonly />
          </div>

          <div class="form-actions">
            <button class="mdm-btn-primary" @click="handleDistribute" :disabled="distributeLoading">
              {{ distributeLoading ? '分发中...' : '执行分发' }}
            </button>
          </div>
        </div>

        <!-- 状态分布 -->
        <div class="panel-header" style="margin-top: 16px">
          <h3>状态分布</h3>
          <div class="header-actions">
            <input v-model="dateRange" type="date" class="mdm-input-yellow" style="width: 130px" @change="loadStats" />
          </div>
        </div>
        <div class="panel-content">
          <div class="status-list">
            <div v-for="(count, status) in stats.byStatus" :key="status" class="status-item">
              <span class="status-name">
                <span :class="['status-dot', status === 'SUCCESS' ? 'success' : status === 'FAILED' ? 'danger' : 'warning']"></span>
                {{ status === 'SUCCESS' ? '成功' : status === 'FAILED' ? '失败' : status === 'RUNNING' ? '执行中' : status }}
              </span>
              <div class="status-bar">
                <div
                  class="status-bar-inner"
                  :class="status === 'SUCCESS' ? 'success' : status === 'FAILED' ? 'danger' : 'warning'"
                  :style="{ width: (stats.total > 0 ? Math.round((count / stats.total) * 100) : 0) + '%' }"
                ></div>
              </div>
              <span class="status-count">{{ count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧分发记录 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>最近分发记录</h3>
          <div class="header-actions">
            <button class="mdm-btn-outline" @click="loadRecentLogs">刷新</button>
          </div>
        </div>

        <div class="mdm-table-wrapper">
          <table class="mdm-data-table">
            <thead>
              <tr>
                <th>日志编码</th>
                <th style="width: 70px">类型</th>
                <th>数据编码</th>
                <th>数据名称</th>
                <th>目标系统</th>
                <th style="width: 70px">状态</th>
                <th style="width: 70px">耗时</th>
                <th style="width: 140px">时间</th>
                <th style="width: 60px">操作</th>
              </tr>
            </thead>
            <tbody v-loading="recentLoading">
              <tr v-for="row in recentLogs" :key="row.id">
                <td>{{ row.logCode }}</td>
                <td>
                  <span class="mdm-type-tag">{{ getTypeName(row.dataType) }}</span>
                </td>
                <td>{{ row.dataCode || '-' }}</td>
                <td>{{ row.dataName || '-' }}</td>
                <td>{{ row.systemConfigName }}</td>
                <td>
                  <div class="mdm-status-badge">
                    <span :class="row.status === 'SUCCESS' ? 'mdm-green-dot' : row.status === 'FAILED' ? 'mdm-red-dot' : 'mdm-yellow-dot'"></span>
                    {{ row.status === 'SUCCESS' ? '成功' : row.status === 'FAILED' ? '失败' : row.status }}
                  </div>
                </td>
                <td>{{ row.durationMs ? row.durationMs + 'ms' : '-' }}</td>
                <td>{{ row.createdAt }}</td>
                <td>
                  <div class="mdm-action-buttons">
                    <button class="mdm-action-btn" @click="handleViewDetail(row)">详情</button>
                  </div>
                </td>
              </tr>
              <tr v-if="recentLogs.length === 0 && !recentLoading">
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
        </div>
      </div>
    </div>

    <!-- 数据选择弹窗 -->
    <MdmDialog v-model="selectDataVisible" :title="`选择${dataTypeOptions.find(o => o.value === distributeForm.dataType)?.label || '数据'}`" width="800px">
      <!-- 搜索栏 -->
      <div class="select-search-bar">
        <input v-model="selectDataKeyword" class="mdm-input-yellow" placeholder="输入编码或名称搜索" @keyup.enter="handleSearchData" />
        <button class="mdm-btn-outline" @click="handleSearchData">搜索</button>
        <span class="select-count">已选: {{ selectDataSelected.length }} 条</span>
        <button v-if="selectDataSelected.length > 0" class="mdm-btn-outline" @click="clearSelection">清空选择</button>
      </div>

      <!-- 数据表格 -->
      <div class="mdm-table-wrapper" style="margin-top: 12px">
        <table class="mdm-data-table">
          <thead>
            <tr>
              <th style="width: 40px">
                <input type="checkbox" :checked="selectDataAllChecked" @change="toggleSelectAll" />
              </th>
              <th>编码</th>
              <th>名称</th>
              <th style="width: 100px">状态</th>
              <th style="width: 150px">创建时间</th>
              <th style="width: 80px">操作</th>
            </tr>
          </thead>
          <tbody v-loading="selectDataLoading">
            <tr v-for="row in selectDataTable" :key="row.id" :class="{ 'row-selected': isRowSelected(row) }">
              <td>
                <input type="checkbox" :checked="isRowSelected(row)" @change="toggleSelectRow(row)" />
              </td>
              <td>{{ row.code || '-' }}</td>
              <td>{{ row.name || '-' }}</td>
              <td>
                <span class="mdm-type-tag">{{ row.status }}</span>
              </td>
              <td>{{ row.createdAt }}</td>
              <td>
                <button class="mdm-action-btn" @click="confirmSelectSingle(row)">选择</button>
              </td>
            </tr>
            <tr v-if="selectDataTable.length === 0 && !selectDataLoading">
              <td colspan="6" class="mdm-empty-data">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="mdm-pagination">
        <span class="mdm-pagination-total">共 {{ selectDataTotal }} 条</span>
        <button class="mdm-page-btn" @click="handleSelectDataPageChange(1)">◀◀</button>
        <button class="mdm-page-btn" @click="handleSelectDataPageChange(selectDataPage - 1)">‹</button>
        <button class="mdm-page-btn active">{{ selectDataPage }}</button>
        <button class="mdm-page-btn" @click="handleSelectDataPageChange(selectDataPage + 1)">›</button>
        <button class="mdm-page-btn" @click="handleSelectDataPageChange(Math.ceil(selectDataTotal / 10))">▶▶</button>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="selectDataVisible = false">取消</button>
        <button
          class="mdm-btn-primary"
          @click="handleBatchDistribute"
          :disabled="selectDataSelected.length === 0 || distributeLoading"
        >
          {{ distributeLoading ? '分发中...' : `批量分发 (${selectDataSelected.length})` }}
        </button>
      </template>
    </MdmDialog>

    <!-- 分发结果弹窗 -->
    <MdmDialog v-model="resultVisible" title="分发结果" width="450px">
      <div class="result-item">
        <span class="result-label">状态</span>
        <span :class="['result-value', result?.success ? 'text-success' : 'text-danger']">
          {{ result?.success ? '成功' : '失败' }}
        </span>
      </div>
      <div class="result-item">
        <span class="result-label">消息</span>
        <span class="result-value">{{ result?.message }}</span>
      </div>
      <div class="result-item" v-if="result?.targetKey">
        <span class="result-label">目标Key</span>
        <span class="result-value">{{ result?.targetKey }}</span>
      </div>
      <div class="result-item" v-if="result?.errorMsg">
        <span class="result-label">错误信息</span>
        <span class="result-value text-danger">{{ result?.errorMsg }}</span>
      </div>

      <template #footer>
        <button class="mdm-btn-primary" @click="resultVisible = false">确定</button>
      </template>
    </MdmDialog>

    <!-- 详情弹窗 -->
    <MdmDialog v-model="detailVisible" title="日志详情" width="600px">
      <div class="detail-grid">
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
          <span class="detail-label">耗时</span>
          <span class="detail-value">{{ detail?.durationMs }} ms</span>
        </div>
      </div>

      <template v-if="detail?.sapMessage">
        <div class="detail-section">
          <span class="detail-section-title">SAP返回消息</span>
          <div class="detail-section-content">{{ detail?.sapMessage }}</div>
        </div>
      </template>

      <template v-if="detail?.errorMsg">
        <div class="detail-section error">
          <span class="detail-section-title">错误信息</span>
          <div class="detail-section-content">{{ detail?.errorMsg }}</div>
        </div>
      </template>

      <template #footer>
        <button class="mdm-btn-cancel" @click="detailVisible = false">关闭</button>
      </template>
    </MdmDialog>
  </div>
</template>

<style scoped lang="scss">
@import '../../../assets/styles/mdm-common.scss';

.monitor-management-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

// 统计卡片
.stat-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  border-left: 4px solid #409eff;

  &.success { border-left-color: #67c23a; }
  &.danger { border-left-color: #f56c6c; }
  &.primary { border-left-color: #409eff; }

  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    margin-right: 16px;
    background: #e6f7ff;

    &.success { background: #f0f9eb; }
    &.danger { background: #fef0f0; }
    &.primary { background: #e6f7ff; }
  }

  .stat-content {
    flex: 1;
  }

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
  }

  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-top: 4px;
  }
}

// 主内容区
.main-content {
  display: flex;
  gap: 16px;
}

// 左侧面板
.left-panel {
  width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 4px;
}

// 右侧面板
.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

// 面板头部
.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

// 面板内容
.panel-content {
  padding: 16px;
}

// 表单分隔线
.form-divider {
  margin: 16px 0;
  padding: 8px 12px;
  background: #fafafa;
  border-left: 3px solid #e74c3c;
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

// 表单操作
.form-actions {
  margin-top: 20px;

  .mdm-btn-primary {
    width: 100%;
  }
}

// 数据选择包装器
.data-select-wrapper {
  flex: 1;
  display: flex;
  gap: 8px;

  .mdm-input-yellow {
    flex: 1;
    cursor: pointer;
  }

  .select-btn {
    padding: 0 12px;
    background: #ed2b33;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;

    &:hover {
      background: #c81e2c;
    }
  }
}

// 状态列表
.status-list {
  .status-item {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .status-name {
      min-width: 60px;
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
    }

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;

      &.success { background: #67c23a; }
      &.danger { background: #f56c6c; }
      &.warning { background: #e6a23c; }
    }

    .status-bar {
      flex: 1;
      height: 12px;
      background: #f0f0f0;
      border-radius: 6px;
      overflow: hidden;

      .status-bar-inner {
        height: 100%;
        border-radius: 6px;
        transition: width 0.3s;

        &.success { background: #67c23a; }
        &.danger { background: #f56c6c; }
        &.warning { background: #e6a23c; }
      }
    }

    .status-count {
      min-width: 40px;
      text-align: right;
      font-size: 13px;
      font-weight: 500;
    }
  }
}

// 表格wrapper
.mdm-table-wrapper {
  flex: 1;
  padding: 0 16px;
  overflow: auto;
}

// 分页
.mdm-pagination {
  padding: 12px 16px;
  border-top: 1px solid #e8e8e8;
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

// 文本颜色
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }

// 结果项
.result-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child { border-bottom: none; }

  .result-label {
    width: 80px;
    color: #666;
    font-size: 13px;
  }

  .result-value {
    flex: 1;
    font-size: 13px;
    word-break: break-all;
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
      word-break: break-all;
    }
  }
}

// 详情区块
.detail-section {
  margin-top: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;

  &.error {
    background: #fef0f0;
  }

  .detail-section-title {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
  }

  .detail-section-content {
    font-size: 13px;
    color: #666;
    line-height: 1.6;
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

// 数据选择弹窗样式
.select-search-bar {
  display: flex;
  align-items: center;
  gap: 10px;

  .mdm-input-yellow {
    width: 200px;
  }

  .select-count {
    margin-left: auto;
    font-size: 13px;
    color: #666;
  }
}

// 选中行样式
.row-selected {
  background: #fff7e6 !important;
}
</style>
