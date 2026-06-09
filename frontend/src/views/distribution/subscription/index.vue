<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getConfigList,
  createConfig,
  updateConfig,
  deleteConfig,
  testConnection,
  getMappingList,
  saveMappings,
  deleteMapping
} from '../../../api/distribution'
import MdmDialog from '../../../components/MdmDialog.vue'
import MdmConfirmDialog from '../../../components/MdmConfirmDialog.vue'

// 当前激活的标签页
const activeTab = ref('system')

// ==================== 系统配置管理 ====================

const systemTypeOptions = [
  { value: 'SAP', label: 'SAP RFC/BAPI' },
  { value: 'HTTP', label: 'HTTP/REST API' },
  { value: 'DATABASE', label: '数据库直连' },
  { value: 'WEBSERVICE', label: 'WebService/SOAP' },
  { value: 'MQ', label: '消息队列' }
]

const authTypeOptions = [
  { value: 'None', label: '无认证' },
  { value: 'Bearer', label: 'Bearer Token' },
  { value: 'Basic', label: 'Basic Auth' },
  { value: 'ApiKey', label: 'API Key' }
]

const systemTableData = ref<any[]>([])
const systemLoading = ref(false)
const systemDialogVisible = ref(false)
const systemDialogTitle = ref('新增配置')
const confirmVisible = ref(false)
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

const systemForm = ref<any>({
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

const sapForm = ref({
  host: '',
  systemNumber: '00',
  client: '800',
  user: '',
  password: '',
  language: 'ZH'
})

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

const testLoading = ref<number | null>(null)

const loadSystemList = async () => {
  systemLoading.value = true
  try {
    const res = await getConfigList() as any
    systemTableData.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    console.error(e)
  } finally {
    systemLoading.value = false
  }
}

const handleAddSystem = () => {
  systemDialogTitle.value = '新增配置'
  systemForm.value = {
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
  systemDialogVisible.value = true
}

const handleEditSystem = (row: any) => {
  systemDialogTitle.value = '编辑配置'
  systemForm.value = { ...row }
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
  systemDialogVisible.value = true
}

const handleSaveSystem = async () => {
  if (!systemForm.value.configName || !systemForm.value.configCode) {
    ElMessage.warning('请填写配置名称和编码')
    return
  }

  try {
    let connectionConfig = {}
    if (systemForm.value.systemType === 'SAP') {
      connectionConfig = { ...sapForm.value }
    } else if (systemForm.value.systemType === 'HTTP') {
      connectionConfig = { ...httpForm.value }
    }
    systemForm.value.connectionConfig = JSON.stringify(connectionConfig)

    if (systemForm.value.id) {
      await updateConfig(systemForm.value.id, systemForm.value)
      ElMessage.success('更新成功')
    } else {
      await createConfig(systemForm.value)
      ElMessage.success('创建成功')
    }
    systemDialogVisible.value = false
    loadSystemList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleDeleteSystem = (row: any) => {
  confirmMessage.value = `确定删除配置"${row.configName}"吗？`
  confirmAction.value = async () => {
    try {
      await deleteConfig(row.id)
      ElMessage.success('删除成功')
      loadSystemList()
    } catch (e) {
      console.error(e)
    }
  }
  confirmVisible.value = true
}

const handleTestConnection = async (row: any) => {
  testLoading.value = row.id
  const loadingMsg = ElMessage({
    message: `正在测试连接 ${row.configName}...`,
    type: 'info',
    duration: 0,
    showClose: true
  })

  try {
    const res = await Promise.race([
      testConnection(row.id),
      new Promise((_, reject) => setTimeout(() => reject(new Error('连接超时')), 30000))
    ]) as any

    loadingMsg.close()
    if (res.data?.success) {
      ElMessage.success('连接成功')
    } else {
      ElMessage.error(res.data?.message || '连接失败')
    }
    loadSystemList()
  } catch (e: any) {
    loadingMsg.close()
    ElMessage.error(e.message || '连接测试失败')
  } finally {
    testLoading.value = null
  }
}

const handleToggleSystemStatus = (row: any) => {
  const newStatus = row.status === 'active' ? 'inactive' : 'active'
  confirmMessage.value = `确定${newStatus === 'active' ? '启用' : '停用'}该配置吗？`
  confirmAction.value = async () => {
    try {
      await updateConfig(row.id, { ...row, status: newStatus })
      ElMessage.success('操作成功')
      loadSystemList()
    } catch (e) {
      console.error(e)
    }
  }
  confirmVisible.value = true
}

const getTypeName = (type: string) => {
  return systemTypeOptions.find(o => o.value === type)?.label || type
}

// ==================== 字段映射管理 ====================

const dataTypeOptions = [
  { value: 'MATERIAL', label: '物料' },
  { value: 'CUSTOMER', label: '客户' },
  { value: 'VENDOR', label: '供应商' }
]

const transformTypeOptions = [
  { value: 'DIRECT', label: '直接映射' },
  { value: 'VALUE_MAP', label: '值域映射' },
  { value: 'FIXED', label: '固定值' },
  { value: 'EXPRESSION', label: '表达式' }
]

const mappingTableData = ref<any[]>([])
const mappingLoading = ref(false)
const mappingDialogVisible = ref(false)
const mappingDialogTitle = ref('新增映射')

const mappingSearchForm = ref({
  dataType: 'MATERIAL',
  systemConfigId: null as number | null
})

const mappingForm = ref<any>({
  dataType: 'MATERIAL',
  mdmField: '',
  mdmFieldName: '',
  sapField: '',
  sapFieldName: '',
  sapStructure: '',
  fieldType: 'STRING',
  isRequired: 0,
  isKey: 0,
  transformType: 'DIRECT',
  transformRule: '',
  defaultValue: '',
  sortOrder: 0,
  status: 'active'
})

const loadMappingList = async () => {
  mappingLoading.value = true
  try {
    const res = await getMappingList({
      dataType: mappingSearchForm.value.dataType,
      systemConfigId: mappingSearchForm.value.systemConfigId || undefined
    }) as any
    mappingTableData.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    console.error(e)
  } finally {
    mappingLoading.value = false
  }
}

const handleAddMapping = () => {
  mappingDialogTitle.value = '新增映射'
  mappingForm.value = {
    dataType: mappingSearchForm.value.dataType,
    mdmField: '',
    mdmFieldName: '',
    sapField: '',
    sapFieldName: '',
    sapStructure: '',
    fieldType: 'STRING',
    isRequired: 0,
    isKey: 0,
    transformType: 'DIRECT',
    transformRule: '',
    defaultValue: '',
    sortOrder: mappingTableData.value.length + 1,
    status: 'active'
  }
  mappingDialogVisible.value = true
}

const handleEditMapping = (row: any) => {
  mappingDialogTitle.value = '编辑映射'
  mappingForm.value = { ...row }
  mappingDialogVisible.value = true
}

const handleSaveMapping = async () => {
  if (!mappingForm.value.mdmField || !mappingForm.value.sapField) {
    ElMessage.warning('请填写MDM字段和目标字段')
    return
  }

  try {
    await saveMappings([mappingForm.value])
    ElMessage.success('保存成功')
    mappingDialogVisible.value = false
    loadMappingList()
  } catch (e) {
    console.error(e)
  }
}

const handleDeleteMapping = async (row: any) => {
  try {
    await deleteMapping(row.id)
    ElMessage.success('删除成功')
    loadMappingList()
  } catch (e) {
    console.error(e)
  }
}

// 监听数据类型变化
watch(() => mappingSearchForm.value.dataType, () => {
  loadMappingList()
})

onMounted(() => {
  loadSystemList()
  loadMappingList()
})
</script>

<template>
  <div class="subscription-management-page">
    <!-- 标签页切换 -->
    <div class="tab-nav">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'system' }"
        @click="activeTab = 'system'"
      >
        目标系统配置
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'mapping' }"
        @click="activeTab = 'mapping'"
      >
        字段映射配置
      </div>
    </div>

    <!-- 系统配置标签页 -->
    <div v-show="activeTab === 'system'" class="tab-content">
      <!-- 顶部操作栏 -->
      <div class="mdm-top-bar">
        <div class="mdm-filter-row"></div>
        <div class="mdm-right-group">
          <button class="mdm-btn-red" @click="handleAddSystem">+ 新增配置</button>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="mdm-table-wrapper">
        <table class="mdm-data-table">
          <thead>
            <tr>
              <th style="width: 50px">序号</th>
              <th>配置名称</th>
              <th>配置编码</th>
              <th style="width: 140px">系统类型</th>
              <th style="width: 80px">状态</th>
              <th style="width: 80px">测试结果</th>
              <th style="width: 150px">测试时间</th>
              <th style="width: 220px">操作</th>
            </tr>
          </thead>
          <tbody v-loading="systemLoading">
            <tr v-for="(row, index) in systemTableData" :key="row.id">
              <td>{{ index + 1 }}</td>
              <td>{{ row.configName }}</td>
              <td>{{ row.configCode }}</td>
              <td>
                <span class="mdm-type-tag">{{ getTypeName(row.systemType) }}</span>
              </td>
              <td>
                <div class="mdm-status-badge">
                  <span :class="row.status === 'active' ? 'mdm-green-dot' : 'mdm-red-dot'"></span>
                  {{ row.status === 'active' ? '启用' : '停用' }}
                </div>
              </td>
              <td>
                <template v-if="row.lastTestResult">
                  <span :class="row.lastTestResult === 'success' ? 'text-success' : 'text-danger'">
                    {{ row.lastTestResult === 'success' ? '成功' : '失败' }}
                  </span>
                </template>
                <span v-else class="text-gray">未测试</span>
              </td>
              <td>{{ row.lastTestTime || '-' }}</td>
              <td>
                <div class="mdm-action-buttons">
                  <button
                    class="mdm-action-btn"
                    :disabled="testLoading === row.id"
                    @click="handleTestConnection(row)"
                  >
                    {{ testLoading === row.id ? '测试中' : '测试' }}
                  </button>
                  <button
                    class="mdm-action-btn"
                    :class="row.status === 'active' ? 'warning' : 'success'"
                    :disabled="testLoading === row.id"
                    @click="handleToggleSystemStatus(row)"
                  >
                    {{ row.status === 'active' ? '停用' : '启用' }}
                  </button>
                  <button class="mdm-action-btn" :disabled="testLoading === row.id" @click="handleEditSystem(row)">编辑</button>
                  <button class="mdm-action-btn delete" :disabled="testLoading === row.id" @click="handleDeleteSystem(row)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="systemTableData.length === 0 && !systemLoading">
              <td colspan="8" class="mdm-empty-data">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 字段映射标签页 -->
    <div v-show="activeTab === 'mapping'" class="tab-content">
      <!-- 顶部操作栏 -->
      <div class="mdm-top-bar">
        <div class="mdm-filter-row">
          <div class="mdm-filter-item">
            <select v-model="mappingSearchForm.dataType" @change="loadMappingList">
              <option v-for="item in dataTypeOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>
          <div class="mdm-filter-item">
            <select v-model="mappingSearchForm.systemConfigId" @change="loadMappingList">
              <option :value="null">通用映射</option>
              <option v-for="item in systemTableData" :key="item.id" :value="item.id">
                {{ item.configName }}
              </option>
            </select>
          </div>
        </div>
        <div class="mdm-right-group">
          <button class="mdm-btn-red" @click="handleAddMapping">+ 新增映射</button>
        </div>
      </div>

      <!-- 数据表格 -->
      <div class="mdm-table-wrapper">
        <table class="mdm-data-table">
          <thead>
            <tr>
              <th style="width: 50px">序号</th>
              <th>MDM字段</th>
              <th>MDM字段名</th>
              <th>目标字段</th>
              <th>目标字段名</th>
              <th style="width: 100px">转换类型</th>
              <th style="width: 60px">必填</th>
              <th style="width: 60px">主键</th>
              <th style="width: 80px">状态</th>
              <th style="width: 120px">操作</th>
            </tr>
          </thead>
          <tbody v-loading="mappingLoading">
            <tr v-for="(row, index) in mappingTableData" :key="row.id">
              <td>{{ row.sortOrder || index + 1 }}</td>
              <td>{{ row.mdmField }}</td>
              <td>{{ row.mdmFieldName || '-' }}</td>
              <td>{{ row.sapField }}</td>
              <td>{{ row.sapFieldName || '-' }}</td>
              <td>
                <span class="mdm-type-tag">{{ transformTypeOptions.find(o => o.value === row.transformType)?.label }}</span>
              </td>
              <td>
                <span v-if="row.isRequired" class="required-tag">是</span>
                <span v-else>-</span>
              </td>
              <td>
                <span v-if="row.isKey" class="key-tag">是</span>
                <span v-else>-</span>
              </td>
              <td>
                <div class="mdm-status-badge">
                  <span :class="row.status === 'active' ? 'mdm-green-dot' : 'mdm-red-dot'"></span>
                  {{ row.status === 'active' ? '启用' : '停用' }}
                </div>
              </td>
              <td>
                <div class="mdm-action-buttons">
                  <button class="mdm-action-btn" @click="handleEditMapping(row)">编辑</button>
                  <button class="mdm-action-btn delete" @click="handleDeleteMapping(row)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="mappingTableData.length === 0 && !mappingLoading">
              <td colspan="10" class="mdm-empty-data">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 系统配置弹窗 -->
    <MdmDialog v-model="systemDialogVisible" :title="systemDialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>配置名称</div>
        <input v-model="systemForm.configName" class="mdm-input-yellow" placeholder="请输入配置名称" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>配置编码</div>
        <input v-model="systemForm.configCode" class="mdm-input-yellow" placeholder="请输入配置编码" :disabled="!!systemForm.id" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>系统类型</div>
        <select v-model="systemForm.systemType" class="mdm-select">
          <option v-for="item in systemTypeOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">连接池大小</div>
        <input v-model="systemForm.poolSize" type="number" class="mdm-input-yellow" min="1" max="100" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">超时时间(ms)</div>
        <input v-model="systemForm.timeout" type="number" class="mdm-input-yellow" min="1000" max="300000" step="1000" />
      </div>

      <!-- SAP配置 -->
      <template v-if="systemForm.systemType === 'SAP'">
        <div class="form-divider">SAP连接参数</div>
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>服务器IP</div>
          <input v-model="sapForm.host" class="mdm-input-yellow" placeholder="如: 10.0.0.1" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>系统编号</div>
          <input v-model="sapForm.systemNumber" class="mdm-input-yellow" placeholder="如: 00" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>客户端</div>
          <input v-model="sapForm.client" class="mdm-input-yellow" placeholder="如: 800" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>用户名</div>
          <input v-model="sapForm.user" class="mdm-input-yellow" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>密码</div>
          <input v-model="sapForm.password" type="password" class="mdm-input-yellow" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">语言</div>
          <input v-model="sapForm.language" class="mdm-input-yellow" />
        </div>
      </template>

      <!-- HTTP配置 -->
      <template v-if="systemForm.systemType === 'HTTP'">
        <div class="form-divider">HTTP连接参数</div>
        <div class="mdm-form-row">
          <div class="mdm-form-label required"><em>*</em>请求地址</div>
          <input v-model="httpForm.url" class="mdm-input-yellow" placeholder="如: http://api.example.com" />
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">请求方式</div>
          <select v-model="httpForm.method" class="mdm-select">
            <option value="GET">GET</option>
            <option value="POST">POST</option>
            <option value="PUT">PUT</option>
            <option value="DELETE">DELETE</option>
          </select>
        </div>
        <div class="mdm-form-row">
          <div class="mdm-form-label">认证方式</div>
          <select v-model="httpForm.authType" class="mdm-select">
            <option v-for="item in authTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="mdm-form-row" v-if="httpForm.authType === 'Bearer'">
          <div class="mdm-form-label">Token</div>
          <input v-model="httpForm.token" type="password" class="mdm-input-yellow" />
        </div>
        <template v-if="httpForm.authType === 'Basic'">
          <div class="mdm-form-row">
            <div class="mdm-form-label">用户名</div>
            <input v-model="httpForm.username" class="mdm-input-yellow" />
          </div>
          <div class="mdm-form-row">
            <div class="mdm-form-label">密码</div>
            <input v-model="httpForm.password" type="password" class="mdm-input-yellow" />
          </div>
        </template>
      </template>

      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>状态</div>
        <div class="mdm-radio-group">
          <label class="mdm-radio-item">
            <input type="radio" v-model="systemForm.status" value="active" />
            启用
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="systemForm.status" value="inactive" />
            停用
          </label>
        </div>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="systemDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSaveSystem">确定</button>
      </template>
    </MdmDialog>

    <!-- 字段映射弹窗 -->
    <MdmDialog v-model="mappingDialogVisible" :title="mappingDialogTitle" width="550px">
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>MDM字段</div>
        <input v-model="mappingForm.mdmField" class="mdm-input-yellow" placeholder="如: MATNR" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">MDM字段名</div>
        <input v-model="mappingForm.mdmFieldName" class="mdm-input-yellow" placeholder="如: 物料号" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label required"><em>*</em>目标字段</div>
        <input v-model="mappingForm.sapField" class="mdm-input-yellow" placeholder="如: MATERIAL" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">目标字段名</div>
        <input v-model="mappingForm.sapFieldName" class="mdm-input-yellow" placeholder="如: 物料号" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">转换类型</div>
        <select v-model="mappingForm.transformType" class="mdm-select">
          <option v-for="item in transformTypeOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
      <div class="mdm-form-row" v-if="mappingForm.transformType === 'FIXED'">
        <div class="mdm-form-label">固定值</div>
        <input v-model="mappingForm.defaultValue" class="mdm-input-yellow" />
      </div>
      <div class="mdm-form-row" v-if="mappingForm.transformType === 'VALUE_MAP'">
        <div class="mdm-form-label">值域映射</div>
        <textarea v-model="mappingForm.transformRule" class="mdm-textarea" rows="3" placeholder='{"A":"10","B":"20"}'></textarea>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">排序号</div>
        <input v-model="mappingForm.sortOrder" type="number" class="mdm-input-yellow" min="0" />
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">是否必填</div>
        <div class="mdm-radio-group">
          <label class="mdm-radio-item">
            <input type="radio" v-model="mappingForm.isRequired" :value="1" />
            是
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="mappingForm.isRequired" :value="0" />
            否
          </label>
        </div>
      </div>
      <div class="mdm-form-row">
        <div class="mdm-form-label">是否主键</div>
        <div class="mdm-radio-group">
          <label class="mdm-radio-item">
            <input type="radio" v-model="mappingForm.isKey" :value="1" />
            是
          </label>
          <label class="mdm-radio-item">
            <input type="radio" v-model="mappingForm.isKey" :value="0" />
            否
          </label>
        </div>
      </div>

      <template #footer>
        <button class="mdm-btn-cancel" @click="mappingDialogVisible = false">取消</button>
        <button class="mdm-btn-primary" @click="handleSaveMapping">确定</button>
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

.subscription-management-page {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 60px);
}

// 标签页导航
.tab-nav {
  display: flex;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
  padding: 0 16px;
  border-bottom: 1px solid #e8e8e8;

  .tab-item {
    padding: 14px 24px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    margin-bottom: -1px;
    transition: all 0.3s;

    &:hover {
      color: #e74c3c;
    }

    &.active {
      color: #e74c3c;
      border-bottom-color: #e74c3c;
      font-weight: 500;
    }
  }
}

// 标签页内容
.tab-content {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
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

// 必填标签
.required-tag {
  display: inline-block;
  padding: 2px 6px;
  background: #fff1f0;
  color: #f5222d;
  border-radius: 3px;
  font-size: 12px;
}

// 主键标签
.key-tag {
  display: inline-block;
  padding: 2px 6px;
  background: #fff7e6;
  color: #fa8c16;
  border-radius: 3px;
  font-size: 12px;
}

// 文本颜色
.text-success {
  color: #52c41a;
}

.text-danger {
  color: #f5222d;
}

.text-gray {
  color: #999;
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
</style>
