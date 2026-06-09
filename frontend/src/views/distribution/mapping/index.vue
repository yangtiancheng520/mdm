<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMappingList,
  saveMappings,
  deleteMapping,
  getConfigList
} from '../../../api/distribution'

// 数据类型选项
const dataTypeOptions = [
  { value: 'VENDOR', label: '供应商' },
  { value: 'MATERIAL', label: '物料' },
  { value: 'CUSTOMER', label: '客户' }
]

// 转换类型选项
const transformTypeOptions = [
  { value: 'DIRECT', label: '直接映射' },
  { value: 'VALUE_MAP', label: '值域映射' },
  { value: 'FIXED', label: '固定值' },
  { value: 'EXPRESSION', label: '表达式' }
]

const tableData = ref<any[]>([])
const loading = ref(false)
const configList = ref<any[]>([])

// 搜索
const searchForm = ref({
  dataType: 'VENDOR',
  systemConfigId: null as number | null
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增映射')

// 表单
const form = ref<any>({
  dataType: 'VENDOR',
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
    const res = await getMappingList({
      dataType: searchForm.value.dataType,
      systemConfigId: searchForm.value.systemConfigId || undefined
    }) as any
    tableData.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增映射'
  form.value = {
    dataType: searchForm.value.dataType,
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
    sortOrder: tableData.value.length + 1,
    status: 'active'
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑映射'
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存
const handleSave = async () => {
  try {
    await saveMappings([form.value])
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await deleteMapping(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

// 监听数据类型变化
watch(() => searchForm.value.dataType, () => {
  loadData()
})

onMounted(() => {
  loadConfigList()
  loadData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>字段映射</h2>
      <el-button type="primary" @click="handleAdd">新增映射</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form inline>
        <el-form-item label="数据类型">
          <el-select v-model="searchForm.dataType" style="width: 150px">
            <el-option
              v-for="item in dataTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标系统">
          <el-select v-model="searchForm.systemConfigId" clearable placeholder="全部" style="width: 200px">
            <el-option
              v-for="item in configList"
              :key="item.id"
              :label="item.configName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="sortOrder" label="序号" width="60" />
      <el-table-column prop="mdmField" label="MDM字段" width="120" />
      <el-table-column prop="mdmFieldName" label="MDM字段名" width="140" />
      <el-table-column prop="sapField" label="目标字段" width="120" />
      <el-table-column prop="sapFieldName" label="目标字段名" width="140" />
      <el-table-column prop="sapStructure" label="结构名" width="120" />
      <el-table-column label="转换类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ transformTypeOptions.find(o => o.value === row.transformType)?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="必填" width="60">
        <template #default="{ row }">
          <el-tag v-if="row.isRequired" type="danger" size="small">是</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="主键" width="60">
        <template #default="{ row }">
          <el-tag v-if="row.isKey" type="warning" size="small">是</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
            {{ row.status === 'active' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="MDM字段" required>
          <el-input v-model="form.mdmField" placeholder="如: vendor_code" />
        </el-form-item>
        <el-form-item label="MDM字段名">
          <el-input v-model="form.mdmFieldName" placeholder="如: 供应商编码" />
        </el-form-item>
        <el-form-item label="目标字段" required>
          <el-input v-model="form.sapField" placeholder="如: LIFNR" />
        </el-form-item>
        <el-form-item label="目标字段名">
          <el-input v-model="form.sapFieldName" placeholder="如: 供应商编号" />
        </el-form-item>
        <el-form-item label="结构名">
          <el-input v-model="form.sapStructure" placeholder="如: VENDOR_DATA" />
        </el-form-item>
        <el-form-item label="字段类型">
          <el-select v-model="form.fieldType" style="width: 100%">
            <el-option label="字符串" value="STRING" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="日期" value="DATE" />
            <el-option label="布尔" value="BOOLEAN" />
          </el-select>
        </el-form-item>
        <el-form-item label="转换类型">
          <el-select v-model="form.transformType" style="width: 100%">
            <el-option
              v-for="item in transformTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.transformType === 'FIXED'" label="固定值">
          <el-input v-model="form.defaultValue" />
        </el-form-item>
        <el-form-item v-if="form.transformType === 'VALUE_MAP'" label="值域映射">
          <el-input v-model="form.transformRule" type="textarea" :rows="3" placeholder='{"A":"10","B":"20"}' />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="是否必填">
          <el-switch v-model="form.isRequired" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="是否主键">
          <el-switch v-model="form.isKey" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
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
.search-bar {
  margin-bottom: 20px;
}
</style>
