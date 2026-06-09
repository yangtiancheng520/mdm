<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  distribute,
  batchDistribute,
  getConfigList,
  getLogHistory
} from '../../../api/distribution'

// 数据类型选项
const dataTypeOptions = [
  { value: 'VENDOR', label: '供应商' },
  { value: 'MATERIAL', label: '物料' },
  { value: 'CUSTOMER', label: '客户' }
]

const loading = ref(false)
const configList = ref<any[]>([])

// 表单
const form = ref({
  dataType: 'VENDOR',
  systemConfigId: null as number | null,
  dataId: null as number | null,
  dataCode: '',
  dataName: ''
})

// 分发结果
const resultVisible = ref(false)
const result = ref<any>(null)

// 历史记录
const historyVisible = ref(false)
const historyData = ref<any[]>([])

// 加载配置列表
const loadConfigList = async () => {
  try {
    const res = await getConfigList() as any
    configList.value = Array.isArray(res) ? res.filter((c: any) => c.status === 'active') : ((res.data || []).filter((c: any) => c.status === 'active')).filter((c: any) => c.status === 'active')
  } catch (e) {
    console.error(e)
  }
}

// 执行分发
const handleDistribute = async () => {
  if (!form.value.systemConfigId) {
    ElMessage.warning('请选择目标系统')
    return
  }
  if (!form.value.dataId) {
    ElMessage.warning('请输入数据ID')
    return
  }

  loading.value = true
  try {
    // 构造测试数据
    const data = {
      id: form.value.dataId,
      code: form.value.dataCode,
      name: form.value.dataName
    }

    const res = await distribute(
      form.value.dataType,
      form.value.dataId!,
      form.value.systemConfigId,
      data
    ) as any

    result.value = res.data
    resultVisible.value = true

    if (res.data?.success) {
      ElMessage.success('分发成功')
    } else {
      ElMessage.error(res.data?.errorMsg || '分发失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 查看历史
const handleViewHistory = async () => {
  if (!form.value.dataId) {
    ElMessage.warning('请输入数据ID')
    return
  }

  try {
    const res = await getLogHistory(form.value.dataType, form.value.dataId!) as any
    historyData.value = Array.isArray(res) ? res : (res.data || [])
    historyVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadConfigList()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>分发执行</h2>
    </div>

    <el-card>
      <el-form :model="form" label-width="100px" style="max-width: 600px">
        <el-form-item label="数据类型" required>
          <el-select v-model="form.dataType" style="width: 100%">
            <el-option
              v-for="item in dataTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标系统" required>
          <el-select v-model="form.systemConfigId" placeholder="请选择目标系统" style="width: 100%">
            <el-option
              v-for="item in configList"
              :key="item.id"
              :label="item.configName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-divider content-position="left">数据信息</el-divider>
        <el-form-item label="数据ID" required>
          <el-input v-model="form.dataId" placeholder="请输入数据ID" type="number" />
        </el-form-item>
        <el-form-item label="数据编码">
          <el-input v-model="form.dataCode" placeholder="请输入数据编码" />
        </el-form-item>
        <el-form-item label="数据名称">
          <el-input v-model="form.dataName" placeholder="请输入数据名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleDistribute" :loading="loading">
            执行分发
          </el-button>
          <el-button @click="handleViewHistory">查看历史</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分发结果弹窗 -->
    <el-dialog v-model="resultVisible" title="分发结果" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="状态">
          <el-tag :type="result?.success ? 'success' : 'danger'">
            {{ result?.success ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消息">{{ result?.message }}</el-descriptions-item>
        <el-descriptions-item v-if="result?.targetKey" label="目标Key">
          {{ result?.targetKey }}
        </el-descriptions-item>
        <el-descriptions-item v-if="result?.errorMsg" label="错误信息">
          <span class="text-red">{{ result?.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 历史记录弹窗 -->
    <el-dialog v-model="historyVisible" title="分发历史" width="800px">
      <el-table :data="historyData" max-height="400">
        <el-table-column prop="logCode" label="日志编码" width="150" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="80" />
        <el-table-column prop="interfaceName" label="接口" width="150" />
        <el-table-column prop="sapKey" label="目标Key" width="120" />
        <el-table-column prop="createdAt" label="时间" width="160" />
      </el-table>
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
.text-red {
  color: #f56c6c;
}
</style>
