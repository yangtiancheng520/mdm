<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getLogList,
  getLog,
  retry,
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
  { value: 'VENDOR', label: '供应商' },
  { value: 'MATERIAL', label: '物料' },
  { value: 'CUSTOMER', label: '客户' }
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

// 详情弹窗
const detailVisible = ref(false)
const detail = ref<any>(null)

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

// 查看详情
const handleDetail = async (row: any) => {
  try {
    const res = await getLog(row.id) as any
    detail.value = res.data
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

// 分页变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

onMounted(() => {
  loadConfigList()
  loadData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>分发日志</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form inline>
        <el-form-item label="数据类型">
          <el-select v-model="searchForm.dataType" style="width: 120px">
            <el-option
              v-for="item in dataTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" style="width: 100px">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标系统">
          <el-select v-model="searchForm.systemConfigId" clearable placeholder="全部" style="width: 150px">
            <el-option
              v-for="item in configList"
              :key="item.id"
              :label="item.configName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="logCode" label="日志编码" width="180" />
      <el-table-column prop="dataType" label="数据类型" width="80" />
      <el-table-column prop="dataCode" label="数据编码" width="100" />
      <el-table-column prop="dataName" label="数据名称" min-width="150" />
      <el-table-column prop="systemConfigName" label="目标系统" width="120" />
      <el-table-column prop="interfaceName" label="接口" width="150" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'info'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="durationMs" label="耗时(ms)" width="80" />
      <el-table-column prop="createdAt" label="时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'FAILED'" link type="warning" @click="handleRetry(row)">重试</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志编码">{{ detail?.logCode }}</el-descriptions-item>
        <el-descriptions-item label="数据类型">{{ detail?.dataType }}</el-descriptions-item>
        <el-descriptions-item label="数据编码">{{ detail?.dataCode }}</el-descriptions-item>
        <el-descriptions-item label="数据名称">{{ detail?.dataName }}</el-descriptions-item>
        <el-descriptions-item label="目标系统">{{ detail?.systemConfigName }}</el-descriptions-item>
        <el-descriptions-item label="接口">{{ detail?.interfaceName }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ detail?.operation }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail?.status === 'SUCCESS' ? 'success' : detail?.status === 'FAILED' ? 'danger' : 'info'">
            {{ detail?.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标Key">{{ detail?.sapKey }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail?.durationMs }} ms</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail?.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail?.endTime }}</el-descriptions-item>
      </el-descriptions>

      <template v-if="detail?.errorMsg">
        <el-divider content-position="left">错误信息</el-divider>
        <el-alert type="error" :closable="false">
          <pre>{{ detail?.errorMsg }}</pre>
        </el-alert>
      </template>

      <template v-if="detail?.requestData">
        <el-divider content-position="left">请求数据</el-divider>
        <pre class="code-block">{{ JSON.stringify(JSON.parse(detail?.requestData || '{}'), null, 2) }}</pre>
      </template>

      <template v-if="detail?.responseData">
        <el-divider content-position="left">响应数据</el-divider>
        <pre class="code-block">{{ JSON.stringify(JSON.parse(detail?.responseData || '{}'), null, 2) }}</pre>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
}
.search-bar {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.code-block {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
  max-height: 200px;
  font-size: 12px;
}
</style>
