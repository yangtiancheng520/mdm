<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getConfigList,
  createConfig,
  updateConfig,
  deleteConfig,
  testConnection
} from '../../../api/distribution'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

// 系统类型选项
const systemTypeOptions = [
  { value: 'SAP', label: 'SAP RFC/BAPI' },
  { value: 'HTTP', label: 'HTTP/REST API' },
  { value: 'DATABASE', label: '数据库直连' },
  { value: 'WEBSERVICE', label: 'WebService/SOAP' },
  { value: 'MQ', label: '消息队列' }
]

const tableData = ref<any[]>([])
const loading = ref(false)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增配置')

// 确认对话框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 表单
const form = ref<any>({
  configName: '',
  configCode: '',
  systemType: 'SAP',
  connectionConfig: '{}',
  poolSize: 10,
  timeout: 30000,
  status: 'inactive',
  isDefault: 0,
  remark: ''
})

// SAP配置表单
const sapForm = ref({
  host: '',
  systemNumber: '00',
  client: '800',
  user: '',
  password: '',
  language: 'ZH'
})

// HTTP配置表单
const httpForm = ref({
  url: '',
  method: 'POST',
  authType: 'None',
  token: '',
  username: '',
  password: '',
  apiKey: '',
  apiKeyHeader: 'X-API-Key',
  timeout: 30000
})

// 认证方式选项
const authTypeOptions = [
  { value: 'None', label: '无认证' },
  { value: 'Bearer', label: 'Bearer Token' },
  { value: 'Basic', label: 'Basic Auth' },
  { value: 'ApiKey', label: 'API Key' }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getConfigList() as any
    // 兼容两种返回格式：直接返回数组 或 {data: [...]}
    tableData.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增配置'
  form.value = {
    configName: '',
    configCode: '',
    systemType: 'SAP',
    connectionConfig: '{}',
    poolSize: 10,
    timeout: 30000,
    status: 'inactive',
    isDefault: 0,
    remark: ''
  }
  sapForm.value = { host: '', systemNumber: '00', client: '800', user: '', password: '', language: 'ZH' }
  httpForm.value = { url: '', method: 'POST', authType: 'None', token: '', username: '', password: '', apiKey: '', apiKeyHeader: 'X-API-Key', timeout: 30000 }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑配置'
  form.value = { ...row }

  // 解析连接配置
  try {
    const config = JSON.parse(row.connectionConfig)
    if (row.systemType === 'SAP') {
      sapForm.value = { ...sapForm.value, ...config }
    } else if (row.systemType === 'HTTP') {
      httpForm.value = { ...httpForm.value, ...config }
    }
  } catch (e) {
    console.error(e)
  }

  dialogVisible.value = true
}

// 保存
const handleSave = async () => {
  try {
    // 构建连接配置
    let connectionConfig = {}
    if (form.value.systemType === 'SAP') {
      connectionConfig = { ...sapForm.value }
    } else if (form.value.systemType === 'HTTP') {
      connectionConfig = { ...httpForm.value }
    }
    form.value.connectionConfig = JSON.stringify(connectionConfig)

    if (form.value.id) {
      await updateConfig(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createConfig(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

// 删除
const handleDelete = (row: any) => {
  confirmMessage.value = `确定删除配置"${row.configName}"吗？`
  confirmAction.value = async () => {
    try {
      await deleteConfig(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (e) {
      console.error(e)
    }
  }
  confirmVisible.value = true
}

// 测试连接
const testLoading = ref<number | null>(null) // 正在测试的行ID

const handleTest = async (row: any) => {
  testLoading.value = row.id
  const loadingMsg = ElMessage({
    message: `正在测试连接 ${row.configName}，请稍候...`,
    type: 'info',
    duration: 0, // 不自动关闭
    showClose: true
  })

  try {
    // 设置30秒超时
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('连接超时，请检查网络或SAP服务状态')), 30000)
    })

    const res = await Promise.race([
      testConnection(row.id),
      timeoutPromise
    ]) as any

    loadingMsg.close()

    if (res.data?.success) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.error(res.data?.message || '连接失败')
    }
    loadData()
  } catch (e: any) {
    loadingMsg.close()
    ElMessage.error(e.message || '连接测试失败')
    console.error(e)
  } finally {
    testLoading.value = null
  }
}

// 切换状态
const handleToggleStatus = (row: any) => {
  const newStatus = row.status === 'active' ? 'inactive' : 'active'
  confirmMessage.value = `确定${newStatus === 'active' ? '启用' : '停用'}该配置吗？`
  confirmAction.value = async () => {
    try {
      await updateConfig(row.id, { ...row, status: newStatus })
      ElMessage.success('操作成功')
      loadData()
    } catch (e) {
      console.error(e)
    }
  }
  confirmVisible.value = true
}

// 获取类型名称
const getTypeName = (type: string) => {
  const opt = systemTypeOptions.find(o => o.value === type)
  return opt ? opt.label : type
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>连接配置</h2>
      <el-button type="primary" @click="handleAdd">新增配置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="configName" label="配置名称" min-width="150" />
      <el-table-column prop="configCode" label="配置编码" width="120" />
      <el-table-column label="系统类型" width="140">
        <template #default="{ row }">
          <el-tag>{{ getTypeName(row.systemType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'">
            {{ row.status === 'active' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="测试结果" width="100">
        <template #default="{ row }">
          <template v-if="row.lastTestResult">
            <el-tag :type="row.lastTestResult === 'success' ? 'success' : 'danger'" size="small">
              {{ row.lastTestResult === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
          <span v-else class="text-gray">未测试</span>
        </template>
      </el-table-column>
      <el-table-column prop="lastTestTime" label="测试时间" width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleTest(row)" :loading="testLoading === row.id">
            {{ testLoading === row.id ? '测试中...' : '测试' }}
          </el-button>
          <el-button link type="primary" @click="handleToggleStatus(row)" :disabled="testLoading === row.id">
            {{ row.status === 'active' ? '停用' : '启用' }}
          </el-button>
          <el-button link type="primary" @click="handleEdit(row)" :disabled="testLoading === row.id">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)" :disabled="testLoading === row.id">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <MdmDialog v-model="dialogVisible" :title="dialogTitle" width="600px" @confirm="handleSave">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置名称" required>
          <el-input v-model="form.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置编码" required>
          <el-input v-model="form.configCode" placeholder="请输入配置编码" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="系统类型" required>
          <el-select v-model="form.systemType" style="width: 100%">
            <el-option
              v-for="item in systemTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="连接池大小">
          <el-input-number v-model="form.poolSize" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="超时时间">
          <el-input-number v-model="form.timeout" :min="1000" :max="300000" :step="1000" />
          <span class="ml-2 text-gray">毫秒</span>
        </el-form-item>
        <el-form-item label="是否默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>

        <!-- SAP配置 -->
        <template v-if="form.systemType === 'SAP'">
          <el-divider content-position="left">SAP连接参数</el-divider>
          <el-form-item label="服务器IP" required>
            <el-input v-model="sapForm.host" placeholder="如: 10.0.0.1" />
          </el-form-item>
          <el-form-item label="系统编号" required>
            <el-input v-model="sapForm.systemNumber" placeholder="如: 00" />
          </el-form-item>
          <el-form-item label="客户端" required>
            <el-input v-model="sapForm.client" placeholder="如: 800" />
          </el-form-item>
          <el-form-item label="用户名" required>
            <el-input v-model="sapForm.user" />
          </el-form-item>
          <el-form-item label="密码" required>
            <el-input v-model="sapForm.password" type="password" show-password />
          </el-form-item>
          <el-form-item label="语言">
            <el-input v-model="sapForm.language" />
          </el-form-item>
        </template>

        <!-- HTTP配置 -->
        <template v-if="form.systemType === 'HTTP'">
          <el-divider content-position="left">HTTP连接参数</el-divider>
          <el-form-item label="请求地址" required>
            <el-input v-model="httpForm.url" placeholder="如: http://api.example.com" />
          </el-form-item>
          <el-form-item label="请求方式">
            <el-select v-model="httpForm.method" style="width: 100%">
              <el-option label="GET" value="GET" />
              <el-option label="POST" value="POST" />
              <el-option label="PUT" value="PUT" />
              <el-option label="DELETE" value="DELETE" />
            </el-select>
          </el-form-item>
          <el-form-item label="认证方式">
            <el-select v-model="httpForm.authType" style="width: 100%">
              <el-option
                v-for="item in authTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item v-if="httpForm.authType === 'Bearer'" label="Token">
            <el-input v-model="httpForm.token" type="password" show-password />
          </el-form-item>
          <template v-if="httpForm.authType === 'Basic'">
            <el-form-item label="用户名">
              <el-input v-model="httpForm.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="httpForm.password" type="password" show-password />
            </el-form-item>
          </template>
          <template v-if="httpForm.authType === 'ApiKey'">
            <el-form-item label="Header名称">
              <el-input v-model="httpForm.apiKeyHeader" />
            </el-form-item>
            <el-form-item label="API Key">
              <el-input v-model="httpForm.apiKey" type="password" show-password />
            </el-form-item>
          </template>
        </template>
      </el-form>
    </MdmDialog>

    <!-- 确认对话框 -->
    <MdmConfirmDialog
      v-model="confirmVisible"
      :message="confirmMessage"
      @confirm="confirmAction?.()"
    />
  </div>
</template>

<style scoped>
.page-container {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
}
.text-gray {
  color: #999;
}
.ml-2 {
  margin-left: 8px;
}
</style>
