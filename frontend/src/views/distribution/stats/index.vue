<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getLogStats, getConfigList } from '../../../api/distribution'

const loading = ref(false)
const configList = ref<any[]>([])

// 统计数据
const stats = ref<any>({
  total: 0,
  byStatus: {}
})

// 时间范围
const dateRange = ref<string[]>([])

// 加载统计
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

// 计算成功率
const successRate = () => {
  const total = stats.value.total || 0
  if (total === 0) return 0
  const success = stats.value.byStatus?.SUCCESS || 0
  return Math.round((success / total) * 100)
}

onMounted(() => {
  loadStats()
  getConfigList().then((res: any) => {
    configList.value = Array.isArray(res) ? res : (res.data || [])
  })
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>分发统计</h2>
    </div>

    <!-- 时间筛选 -->
    <el-card style="margin-bottom: 20px">
      <el-form inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStats">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">分发总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card success">
            <div class="stat-value">{{ stats.byStatus?.SUCCESS || 0 }}</div>
            <div class="stat-label">成功数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card danger">
            <div class="stat-value">{{ stats.byStatus?.FAILED || 0 }}</div>
            <div class="stat-label">失败数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card primary">
            <div class="stat-value">{{ successRate() }}%</div>
            <div class="stat-label">成功率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 状态分布 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>状态分布</span>
          </template>
          <div class="status-list">
            <div v-for="(count, status) in stats.byStatus" :key="status" class="status-item">
              <span class="status-name">
                <el-tag
                  :type="status === 'SUCCESS' ? 'success' : status === 'FAILED' ? 'danger' : 'info'"
                  size="small"
                >
                  {{ status }}
                </el-tag>
              </span>
              <el-progress
                :percentage="Math.round((count / stats.total) * 100)"
                :stroke-width="20"
                :status="status === 'SUCCESS' ? 'success' : status === 'FAILED' ? 'exception' : ''"
              />
              <span class="status-count">{{ count }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统配置</span>
          </template>
          <el-table :data="configList" max-height="300">
            <el-table-column prop="configName" label="配置名称" />
            <el-table-column prop="systemType" label="类型" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
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
.stat-card {
  text-align: center;
  padding: 20px 0;
}
.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
.stat-card.success .stat-value {
  color: #67c23a;
}
.stat-card.danger .stat-value {
  color: #f56c6c;
}
.stat-card.primary .stat-value {
  color: #409eff;
}
.status-list {
  padding: 0 20px;
}
.status-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.status-name {
  min-width: 80px;
}
.status-count {
  min-width: 50px;
  text-align: right;
  color: #606266;
}
</style>
