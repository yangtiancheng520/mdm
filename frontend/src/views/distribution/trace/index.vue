<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getFieldLineageFlatList,
  getConfigList
} from '../../../api/distribution'

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
  dateRange: [] as string[]
})

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

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

    const res = await getFieldLineageFlatList(params) as any
    tableData.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (e) {
    console.error('加载失败:', e)
    ElMessage.error('加载数据失败')
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
    dateRange: []
  }
  currentPage.value = 1
  loadData()
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

// 获取操作名称
const getOperationName = (operation: string) => {
  switch (operation) {
    case 'CREATE': return '新增'
    case 'UPDATE': return '更新'
    case 'DELETE': return '删除'
    default: return operation
  }
}

// 获取转换类型名称
const getTransformTypeName = (type: string) => {
  switch (type) {
    case 'DIRECT': return '直接映射'
    case 'VALUE_MAP': return '值域映射'
    case 'FIXED': return '固定值'
    case 'EXPRESSION': return '表达式计算'
    default: return type
  }
}

// 获取状态样式
const getStatusType = (status: string) => {
  switch (status) {
    case 'SUCCESS': return 'success'
    case 'FAILED': return 'danger'
    case 'RUNNING': return 'warning'
    default: return 'info'
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
        <el-select v-model="searchForm.dataType" placeholder="数据类型" clearable @change="handleSearch" style="width: 120px">
          <el-option v-for="item in dataTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="状态" clearable @change="handleSearch" style="width: 120px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="searchForm.systemConfigId" placeholder="目标系统" clearable @change="handleSearch" style="width: 140px">
          <el-option v-for="item in configList" :key="item.id" :label="item.configName" :value="item.id" />
        </el-select>
        <el-date-picker
          v-model="searchForm.dateRange[0]"
          type="date"
          placeholder="开始时间"
          value-format="YYYY-MM-DD"
          style="width: 140px"
        />
        <el-date-picker
          v-model="searchForm.dateRange[1]"
          type="date"
          placeholder="结束时间"
          value-format="YYYY-MM-DD"
          style="width: 140px"
        />
      </div>
      <div class="mdm-right-group">
        <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        height="100%"
        style="width: 100%"
      >
        <el-table-column prop="dataId" label="数据ID" width="80" align="center" />
        <el-table-column prop="formName" label="表单名" width="100" />
        <el-table-column prop="dataCode" label="数据编码" width="120" show-overflow-tooltip />
        <el-table-column prop="dataName" label="数据名称" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.dataName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="70" align="center">
          <template #default="{ row }">
            {{ getOperationName(row.operation) }}
          </template>
        </el-table-column>
        <el-table-column prop="systemConfigName" label="目标系统" width="120" show-overflow-tooltip />
        <el-table-column label="发送方字段" width="160">
          <template #default="{ row }">
            <div class="field-cell">
              <div class="field-name">{{ row.mdmFieldName }}</div>
              <div class="field-code">{{ row.mdmField }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="接收方字段" width="160">
          <template #default="{ row }">
            <div class="field-cell">
              <div class="field-name">{{ row.sapFieldName }}</div>
              <div class="field-code">{{ row.sapField }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sourceValue" label="源值" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.sourceValue || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="targetValue" label="目标值" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.targetValue || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="transformType" label="转换类型" width="100">
          <template #default="{ row }">
            {{ getTransformTypeName(row.transformType) }}
          </template>
        </el-table-column>
        <el-table-column prop="fieldStatus" label="字段状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.fieldStatus)" size="small">
              {{ row.fieldStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sendTime" label="发送时间" width="160" />
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.trace-management-page {
  padding: 20px;
  background: #f5f5f5;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

// 顶部操作栏
.mdm-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
}

.mdm-filter-row {
  display: flex;
  gap: 12px;
  flex: 1;
}

.mdm-right-group {
  display: flex;
  gap: 8px;
}

// 表格容器
.table-container {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}

// 字段单元格
.field-cell {
  .field-name {
    color: #303133;
    font-size: 12px;
    line-height: 1.4;
  }
  .field-code {
    color: #909399;
    font-size: 11px;
    line-height: 1.4;
  }
}

// 分页容器
.pagination-container {
  background: #fff;
  padding: 16px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #ebeef5;
}
</style>
