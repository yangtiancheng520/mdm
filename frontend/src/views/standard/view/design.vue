<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getViewDetail,
  updateView,
  getVersionHistory,
  type ViewDefinition,
  type ViewEntity,
  type ViewField
} from '@/api/standard/viewDefinition'
import {
  getFieldStandardList,
  type FieldStandard
} from '@/api/standard/fieldStandard'
import {
  getActiveEncodingRules,
  type EncodingRule
} from '@/api/standard/encodingRule'
import {
  getActiveCategoryTree,
  type FieldCategory
} from '@/api/standard/fieldCategory'
import MdmConfirmDialog from '@/components/MdmConfirmDialog.vue'
import CategoryTreeSelect from '@/components/common/CategoryTreeSelect.vue'

const route = useRoute()
const router = useRouter()

const viewId = computed(() => Number(route.params.id))
const isViewMode = computed(() => route.query.mode === 'view') // 只读模式
const isRevising = computed(() => viewData.value.status === 'revising') // 修订中模式
const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)  // 发布中

// 视图数据
const viewData = ref<ViewDefinition>({})

// 一级TAB: 主表 / 子表
const activeMainTab = ref('main')

// 二级TAB: 子表列表
const activeSubTabIndex = ref(0)

// 字段标准库列表
const fieldStandardList = ref<FieldStandard[]>([])
const fieldSelectVisible = ref(false)
const selectedFieldIds = ref<number[]>([])
const searchKeyword = ref('')
const displayKeyword = ref('') // 用于显示的关键词

// 分类筛选
const categoryTreeData = ref<FieldCategory[]>([])
const selectedCategoryId = ref<number | null>(null)
const displayCategoryId = ref<number | null>(null) // 用于显示的分类

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalFields = ref(0)
const loadingFields = ref(false)

// 编码规则列表
const encodingRuleList = ref<EncodingRule[]>([])
const encodingRuleSelectVisible = ref(false)
const currentEditingField = ref<ViewField | null>(null)

// 版本历史
const versionHistory = ref<ViewDefinition[]>([])
const loadingVersions = ref(false)
const showVersionHistory = ref(false)

// 编辑子表名称
const editingSubTabIndex = ref(-1)
const editingSubTabName = ref('')

// 删除确认框
const confirmVisible = ref(false)
const confirmMessage = ref('')
const pendingDeleteIndex = ref(-1)

// 发布确认框
const publishConfirmVisible = ref(false)

// 当前实体
const currentEntity = computed(() => {
  if (activeMainTab.value === 'main') {
    return viewData.value.entities?.find(e => e.entityType === 'main')
  } else {
    const subs = viewData.value.entities?.filter(e => e.entityType === 'sub') || []
    return subs[activeSubTabIndex.value]
  }
})

// 子表列表
const subEntities = computed(() => {
  return viewData.value.entities?.filter(e => e.entityType === 'sub') || []
})

// 视图类型
const viewTypeLabel = computed(() => {
  return subEntities.value.length > 0 ? '主子表' : '单表'
})

// 过滤后的字段列表（应用分类筛选和搜索）
const filteredFieldList = computed(() => {
  let result = fieldStandardList.value

  // 分类筛选
  if (displayCategoryId.value) {
    result = result.filter(f => f.categoryId === displayCategoryId.value)
  }

  // 关键词搜索
  if (displayKeyword.value) {
    const keyword = displayKeyword.value.toLowerCase()
    result = result.filter(f =>
      f.fieldCode.toLowerCase().includes(keyword) ||
      f.fieldName.toLowerCase().includes(keyword)
    )
  }

  console.log(`筛选结果: ${result.length}条 (总共${fieldStandardList.value.length}条)`)
  return result
})

// 分页后的字段列表
const paginatedFieldList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  const result = filteredFieldList.value.slice(start, end)
  console.log(`分页计算: 第${currentPage.value}页, 显示${start+1}-${Math.min(end, filteredFieldList.value.length)}条, 共${filteredFieldList.value.length}条`)
  return result
})

// 总页数
const totalPages = computed(() => {
  const pages = Math.ceil(filteredFieldList.value.length / pageSize.value)
  console.log(`总页数: ${pages}, 总条数: ${filteredFieldList.value.length}, 每页: ${pageSize.value}条`)
  return pages || 1 // 至少1页
})

// 加载数据
const loadData = async () => {
  if (!viewId.value) return
  loading.value = true
  try {
    const res = await getViewDetail(viewId.value)
    if (res.code === 200) {
      viewData.value = res.data
      // 确保 entities 是数组
      if (!viewData.value.entities) {
        viewData.value.entities = []
      }
      // 确保有主表实体
      const mainEntity = viewData.value.entities.find(e => e.entityType === 'main')
      if (!mainEntity) {
        viewData.value.entities.unshift({
          entityCode: 'main',
          entityName: '主表',
          entityType: 'main',
          sort: 0,
          fields: []
        })
      }
      console.log('视图数据加载完成:', viewData.value)
      // 加载版本历史
      loadVersionHistory()
    }
  } catch (error) {
    console.error('加载视图失败', error)
  } finally {
    loading.value = false
  }
}

const loadFieldStandardList = async () => {
  try {
    loadingFields.value = true
    console.log('开始加载字段标准库...')

    // 先请求第一页获取总数和每页条数
    const firstRes = await getFieldStandardList({
      page: 1
    })

    console.log('第一页数据:', firstRes)

    if (firstRes && firstRes.data) {
      const total = firstRes.data.total || 0
      const pageSize = firstRes.data.pageSize || 10
      console.log(`总数据量: ${total}, 每页: ${pageSize}`)

      const allData: FieldStandard[] = [...(firstRes.data.list || [])]

      // 如果总数大于每页条数，需要请求所有页
      if (total > pageSize) {
        const totalPages = Math.ceil(total / pageSize)
        console.log(`需要请求 ${totalPages} 页`)

        // 循环请求剩余页
        for (let page = 2; page <= totalPages; page++) {
          console.log(`请求第 ${page} 页...`)
          const res = await getFieldStandardList({
            page: page
          })
          if (res && res.data && res.data.list) {
            allData.push(...res.data.list)
          }
        }
      }

      fieldStandardList.value = allData
      console.log('最终加载的字段数量:', fieldStandardList.value.length)
    }
  } catch (error) {
    console.error('加载字段标准库失败', error)
    ElMessage.error('加载字段标准库失败')
  } finally {
    loadingFields.value = false
  }
}

const loadEncodingRuleList = async () => {
  try {
    const res = await getActiveEncodingRules()
    console.log('编码规则返回:', res)
    if (res && res.data) {
      encodingRuleList.value = res.data || []
      console.log('编码规则列表:', encodingRuleList.value)
    }
  } catch (error) {
    console.error('加载编码规则失败', error)
  }
}

const loadCategoryTree = async () => {
  try {
    const res = await getActiveCategoryTree()
    console.log('分类树返回:', res)
    if (res && res.data) {
      categoryTreeData.value = res.data || []
      console.log('分类树数据:', categoryTreeData.value)
    }
  } catch (error) {
    console.error('加载分类树失败', error)
  }
}

// 切换一级TAB
const handleMainTabChange = (tab: string) => {
  activeMainTab.value = tab
  if (tab === 'sub' && subEntities.value.length > 0) {
    activeSubTabIndex.value = 0
  }
}

// 添加子表
const addSubEntity = () => {
  if (!viewData.value.entities) viewData.value.entities = []
  const subCount = subEntities.value.length + 1
  viewData.value.entities.push({
    id: Date.now(),
    entityCode: `sub_${subCount}`,
    entityName: `子表${subCount}`,
    entityType: 'sub',
    sort: viewData.value.entities.length,
    fields: []
  })
  activeSubTabIndex.value = subEntities.value.length - 1
}

// 删除子表
const deleteSubEntity = (index: number) => {
  pendingDeleteIndex.value = index
  confirmMessage.value = '确定删除该子表？'
  confirmVisible.value = true
}

// 确认删除子表
const confirmDeleteSubEntity = () => {
  const index = pendingDeleteIndex.value
  if (index < 0) return

  const entity = subEntities.value[index]
  const idx = viewData.value.entities!.findIndex(e => e.id === entity.id)
  if (idx > -1) viewData.value.entities!.splice(idx, 1)
  if (activeSubTabIndex.value >= subEntities.value.length) {
    activeSubTabIndex.value = Math.max(0, subEntities.value.length - 1)
  }
  pendingDeleteIndex.value = -1
}

// 编辑子表名称
const startEditSubTabName = (index: number) => {
  editingSubTabIndex.value = index
  editingSubTabName.value = subEntities.value[index].entityName || ''
}

const finishEditSubTabName = () => {
  if (editingSubTabIndex.value > -1 && editingSubTabName.value.trim()) {
    const entity = subEntities.value[editingSubTabIndex.value]
    if (entity) entity.entityName = editingSubTabName.value.trim()
  }
  editingSubTabIndex.value = -1
  editingSubTabName.value = ''
}

// 分组管理
// 字段选择
const openFieldSelectDialog = async () => {
  selectedFieldIds.value = []
  searchKeyword.value = ''
  displayKeyword.value = ''
  selectedCategoryId.value = null
  displayCategoryId.value = null
  currentPage.value = 1

  // 如果字段列表为空，重新加载
  if (fieldStandardList.value.length === 0) {
    console.log('字段列表为空，重新加载...')
    await loadFieldStandardList()
  }

  // 如果分类树为空，重新加载
  if (categoryTreeData.value.length === 0) {
    console.log('分类树为空，重新加载...')
    await loadCategoryTree()
  }

  fieldSelectVisible.value = true
}

// 查询字段
const handleFieldSearch = () => {
  console.log('=== 执行查询 ===')
  console.log('搜索关键词:', searchKeyword.value)
  console.log('分类ID:', selectedCategoryId.value)

  displayKeyword.value = searchKeyword.value
  displayCategoryId.value = selectedCategoryId.value
  currentPage.value = 1

  ElMessage.success(`查询完成，共${filteredFieldList.value.length}条数据`)
}

// 重置查询
const handleFieldReset = () => {
  console.log('=== 重置查询 ===')

  searchKeyword.value = ''
  displayKeyword.value = ''
  selectedCategoryId.value = null
  displayCategoryId.value = null
  currentPage.value = 1

  ElMessage.info('已重置筛选条件')
}

const toggleSelectAll = () => {
  if (selectedFieldIds.value.length === paginatedFieldList.value.length) {
    selectedFieldIds.value = []
  } else {
    selectedFieldIds.value = paginatedFieldList.value.map(f => f.id)
  }
}

const toggleFieldSelect = (id: number) => {
  const index = selectedFieldIds.value.indexOf(id)
  if (index > -1) {
    selectedFieldIds.value.splice(index, 1)
  } else {
    selectedFieldIds.value.push(id)
  }
}

// 监听 currentPage 变化
watch(currentPage, (newVal) => {
  console.log('页码变化:', newVal, '总页数:', totalPages.value)
})

// 分页处理函数
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  console.log('每页条数变化:', size)
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  console.log('页码变化:', page)
}

const confirmSelectFields = () => {
  if (!currentEntity.value) return
  if (!currentEntity.value.fields) currentEntity.value.fields = []

  const existingIds = currentEntity.value.fields.map(f => f.fieldStandardId)
  const newIds = selectedFieldIds.value.filter(id => !existingIds.includes(id))

  newIds.forEach(id => {
    const standard = fieldStandardList.value.find(f => f.id === id)
    if (standard) {
      currentEntity.value.fields!.push({
        id: Date.now() + Math.random() * 1000,
        fieldStandardId: standard.id,
        fieldCode: standard.fieldCode,
        fieldName: standard.fieldName,
        fieldType: standard.fieldType,
        length: standard.length,
        precisionVal: standard.precision,  // 小数位
        domainId: standard.domainId,        // 值域ID
        domainCode: standard.domainCode,    // 值域编码
        domainName: standard.domainName,    // 值域名称
        isRequired: standard.required || false,
        sort: currentEntity.value.fields!.length
      })
    }
  })
  fieldSelectVisible.value = false
}

// 编码规则选择
const openEncodingRuleSelect = (field: ViewField) => {
  currentEditingField.value = field
  encodingRuleSelectVisible.value = true
}

const selectEncodingRule = (rule: EncodingRule) => {
  if (currentEditingField.value) {
    // 计算编码规则生成的编码长度
    const ruleLength = calculateRuleLength(rule)

    // 检查编码长度是否超过字段长度
    const fieldLength = currentEditingField.value.length || 0
    if (fieldLength > 0 && ruleLength > fieldLength) {
      ElMessage.warning(`编码规则生成的编码长度(${ruleLength})超过字段长度限制(${fieldLength})，请选择其他规则或调整字段长度`)
      return
    }

    currentEditingField.value.encodingRuleId = rule.id
    currentEditingField.value.encodingRuleName = rule.ruleName
    ElMessage.success(`已选择编码规则：${rule.ruleName}，预计编码长度：${ruleLength}`)
  }
  encodingRuleSelectVisible.value = false
}

const clearEncodingRule = (field: ViewField) => {
  field.encodingRuleId = undefined
  field.encodingRuleName = undefined
  field.autoNumber = false
}

// 计算编码规则生成的编码长度
const calculateRuleLength = (rule: EncodingRule): number => {
  if (!rule.ruleDefinition?.segments) return 0

  let totalLength = 0
  rule.ruleDefinition.segments.forEach(segment => {
    switch (segment.type) {
      case 'fixed':
        // 固定值：取值的长度
        totalLength += (segment.config.value || '').length
        break
      case 'date':
        // 日期：根据格式计算长度
        const format = segment.config.format || 'yyyyMMdd'
        totalLength += format.length
        break
      case 'sequence':
        // 序列号：取配置的长度
        totalLength += segment.config.length || 4
        break
      case 'field':
        // 字段引用：取配置的长度或默认值
        totalLength += segment.config.length || 10
        break
      case 'conditional':
        // 条件值：取最大分支长度
        const branches = segment.config.branches || []
        const maxLength = Math.max(...branches.map((b: any) => (b.value || '').length), 0)
        totalLength += maxLength
        break
      case 'category':
        // 分类编码：取最大映射长度
        const mappings = segment.config.mappings || []
        const maxMappingLength = Math.max(...mappings.map((m: any) => (m.code || '').length), 0)
        totalLength += maxMappingLength
        break
      case 'checkDigit':
        // 校验位：通常1-2位
        totalLength += 1
        break
      case 'random':
        // 随机数：取配置的长度
        totalLength += segment.config.length || 4
        break
      default:
        totalLength += 4 // 默认长度
    }
  })

  return totalLength
}

// 删除字段
const pendingDeleteField = ref<ViewField | null>(null)
const deleteFieldVisible = ref(false)

// 拖拽排序
const dragIndex = ref(-1)
const dropIndex = ref(-1)

const handleDragStart = (e: DragEvent, index: number) => {
  dragIndex.value = index
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
  }
}

const handleDragOver = (e: DragEvent, index: number) => {
  dropIndex.value = index
}

const handleDrop = (e: DragEvent, index: number) => {
  if (dragIndex.value !== -1 && dragIndex.value !== index && currentEntity.value?.fields) {
    const fields = currentEntity.value.fields
    const dragItem = fields[dragIndex.value]
    fields.splice(dragIndex.value, 1)
    fields.splice(index, 0, dragItem)
    // 更新sort
    fields.forEach((f, i) => {
      f.sort = i
    })
  }
  dropIndex.value = -1
}

const handleDragEnd = () => {
  dragIndex.value = -1
  dropIndex.value = -1
}

const deleteField = (field: ViewField) => {
  pendingDeleteField.value = field
  deleteFieldVisible.value = true
}

const confirmDeleteField = () => {
  if (!pendingDeleteField.value || !currentEntity.value?.fields) return
  const index = currentEntity.value.fields.findIndex(f => f.id === pendingDeleteField.value!.id)
  if (index > -1) currentEntity.value.fields.splice(index, 1)
  pendingDeleteField.value = null
}

const getFieldTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    string: '字符串', number: '数字', date: '日期', datetime: '日期时间',
    boolean: '布尔', text: '长文本', select: '单选', multi_select: '多选'
  }
  return labels[type] || type
}

const handleSave = async () => {
  saving.value = true
  try {
    const currentUser = localStorage.getItem('username') || 'admin'

    // 临时解决方案：移除实体的ID和字段ID，让后端重新创建
    // 这是因为后端在更新时有bug，会把已有记录当作新记录插入
    const dataToSave = {
      ...viewData.value,
      updatedBy: currentUser,
      entities: viewData.value.entities?.map(entity => ({
        entityCode: entity.entityCode,
        entityName: entity.entityName,
        entityType: entity.entityType,
        sort: entity.sort,
        minRows: entity.minRows,
        maxRows: entity.maxRows,
        enableAdd: entity.enableAdd,
        enableDelete: entity.enableDelete,
        enableCopy: entity.enableCopy,
        enableSort: entity.enableSort,
        isInherited: entity.isInherited,  // 是否继承实体
        status: entity.status,
        description: entity.description,
        fields: entity.fields?.map(field => ({
          fieldStandardId: field.fieldStandardId,
          fieldCode: field.fieldCode,
          fieldName: field.fieldName,
          fieldType: field.fieldType,
          length: field.length,
          precisionVal: field.precisionVal,
          domainId: field.domainId,
          domainCode: field.domainCode,
          domainName: field.domainName,
          groupId: field.groupId,
          groupName: field.groupName,
          sort: field.sort,
          columnSpan: field.columnSpan,
          isRequired: field.isRequired,
          isReadonly: field.isReadonly,
          isHidden: field.isHidden,
          isUnique: field.isUnique,
          isQuery: field.isQuery,
          isQueryResult: field.isQueryResult,
          isSortable: field.isSortable,
          isInherited: field.isInherited,  // 是否继承字段
          defaultValue: field.defaultValue,
          defaultValueType: field.defaultValueType,
          autoNumber: field.autoNumber,
          encodingRuleId: field.encodingRuleId,
          encodingRuleName: field.encodingRuleName
        }))
      }))
    }

    console.log('保存数据（不包含ID）:', dataToSave)

    const res = await updateView(viewId.value, dataToSave)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      // 重新加载数据，确保ID正确
      await loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 发布修订版本
const handlePublish = () => {
  publishConfirmVisible.value = true
}

// 确认发布
const confirmPublish = async () => {
  publishing.value = true
  try {
    const res = await publishView(viewId.value)
    if (res.code === 200) {
      ElMessage.success('发布成功！新版本V' + res.data.version + '已成为主干版本')
      // 跳转到列表页
      router.push('/standard/view')
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const handleBack = () => router.push('/standard/view')

// 加载版本历史
const loadVersionHistory = async () => {
  if (!viewData.value.viewCode) {
    console.log('viewCode 为空，无法加载版本历史')
    return
  }
  loadingVersions.value = true
  try {
    console.log('加载版本历史，viewCode:', viewData.value.viewCode)
    const res = await getVersionHistory(viewData.value.viewCode)
    console.log('版本历史接口返回:', res)
    if (res.code === 200) {
      versionHistory.value = res.data || []
      console.log('版本历史数据:', versionHistory.value)
      console.log('版本数量:', versionHistory.value.length)
    } else {
      console.error('版本历史接口返回错误:', res.message)
    }
  } catch (error) {
    console.error('加载版本历史失败', error)
  } finally {
    loadingVersions.value = false
  }
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取字段数量
const getFieldCount = (entities?: ViewEntity[]) => {
  if (!entities) return 0
  return entities.reduce((sum, e) => sum + (e.fields?.length || 0), 0)
}

// 获取状态标签
const getStatusLabel = (status?: string) => {
  const labels: Record<string, string> = {
    draft: '草稿',
    revising: '修订中',
    published: '已发布',
    disabled: '已停用',
    history: '历史版本'
  }
  return labels[status || ''] || status || '未知'
}

// 判断字段是否为新增（在修订模式下）
const isNewField = (field: ViewField) => {
  // 如果不是修订模式，所有字段都可编辑
  if (!isRevising.value) return true

  // 如果后端返回了 isInherited 字段，直接使用
  if (field.isInherited !== undefined) {
    return !field.isInherited  // isInherited=true 表示继承字段，返回 false
  }

  // 兼容处理：如果 isInherited 为空，根据ID判断
  if (!field.id) return true

  // 如果ID是数字类型，且大于某个阈值（如1000000000000），说明是前端临时生成的
  if (typeof field.id === 'number' && field.id > 1000000000000) return true

  // 如果ID是字符串类型，且包含'temp'或'new'
  if (typeof field.id === 'string' && (field.id.includes('temp') || field.id.includes('new'))) return true

  // 其他情况认为是数据库中已存在的字段（从主干继承）
  return false
}

// 判断子表是否为新增（在修订模式下）
const isNewEntity = (entity: ViewEntity) => {
  // 如果不是修订模式，所有子表都可删除
  if (!isRevising.value) return true

  // 如果后端返回了 isInherited 字段，直接使用
  if (entity.isInherited !== undefined) {
    return !entity.isInherited  // isInherited=true 表示继承实体，返回 false
  }

  // 兼容处理：如果 isInherited 为空，根据ID判断
  if (!entity.id) return true

  // 如果ID是数字类型，且大于某个阈值（如1000000000000），说明是前端临时生成的
  if (typeof entity.id === 'number' && entity.id > 1000000000000) return true

  // 如果ID是字符串类型，且包含'temp'或'new'
  if (typeof entity.id === 'string' && (entity.id.includes('temp') || entity.id.includes('new'))) return true

  // 其他情况认为是数据库中已存在的子表（从主干继承）
  return false
}

onMounted(() => {
  loadData()
  loadFieldStandardList()
  loadEncodingRuleList()
  loadCategoryTree()
})
</script>

<template>
  <div class="design-page">
    <!-- 顶部栏 -->
    <div class="header">
      <button class="btn-back" @click="handleBack">← 返回</button>
      <span class="title">{{ isViewMode ? '查看视图：' : '设计视图：' }}{{ viewData.viewName }}</span>
      <button v-if="!isViewMode" class="btn-save" :disabled="saving" @click="handleSave">{{ saving ? '保存中...' : '保存' }}</button>
    </div>

    <!-- 内容区 -->
    <div class="content" v-if="!loading">
      <!-- 视图基本信息 -->
      <div class="view-info">
        <div class="info-item">
          <label>视图编码：</label>
          <span class="info-value">{{ viewData.viewCode || '-' }}</span>
        </div>
        <div class="info-item">
          <label>视图名称：</label>
          <span class="info-value">{{ viewData.viewName || '-' }}</span>
        </div>
        <div class="info-item">
          <label>分类：</label>
          <span class="info-value">{{ viewData.categoryName || '-' }}</span>
        </div>
        <div class="info-item">
          <label>状态：</label>
          <span class="info-value" :class="viewData.status === 'published' ? 'status-active' : 'status-inactive'">
            {{ viewData.status === 'published' ? '已发布' : viewData.status === 'draft' ? '草稿' : viewData.status === 'revising' ? '修订中' : '已停用' }}
          </span>
        </div>
        <div class="info-item full-width" v-if="viewData.description">
          <label>描述：</label>
          <span class="info-value">{{ viewData.description }}</span>
        </div>
      </div>

      <!-- 一级TAB -->
      <div class="tabs-primary">
        <div class="tab-item" :class="{ active: activeMainTab === 'main' }" @click="handleMainTabChange('main')">主表</div>
        <div class="tab-item" :class="{ active: activeMainTab === 'sub' }" @click="handleMainTabChange('sub')">子表</div>
      </div>

      <!-- 主表内容 -->
      <div class="panel" v-if="activeMainTab === 'main' && currentEntity">
        <!-- 主表编码和名称 -->
        <div class="entity-info">
          <div class="info-item">
            <label>主表编码：</label>
            <input v-model="currentEntity.entityCode" class="input-info" placeholder="请输入主表编码" :disabled="isViewMode" />
          </div>
          <div class="info-item">
            <label>主表名称：</label>
            <input v-model="currentEntity.entityName" class="input-info" placeholder="请输入主表名称" :disabled="isViewMode" />
          </div>
        </div>

        <table class="data-table">
          <thead>
            <tr>
              <th style="width: 40px">序号</th>
              <th>字段编码</th>
              <th>字段名称</th>
              <th>字段类型</th>
              <th>长度</th>
              <th>小数位</th>
              <th>值域</th>
              <th>必填</th>
              <th>只读</th>
              <th>隐藏</th>
              <th>唯一</th>
              <th>可查询</th>
              <th>自动编号</th>
              <th>编码规则</th>
              <th>默认值</th>
              <th v-if="!isViewMode">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(f, index) in currentEntity?.fields" :key="f.id"
                :draggable="!isViewMode"
                @dragstart="!isViewMode && handleDragStart($event, index)"
                @dragover.prevent="!isViewMode && handleDragOver($event, index)"
                @drop="!isViewMode && handleDrop($event, index)"
                @dragend="!isViewMode && handleDragEnd"
                :class="{ 'dragging': !isViewMode && dragIndex === index, 'drag-over': !isViewMode && dropIndex === index }">
              <td class="text-center drag-handle">{{ index + 1 }}</td>
              <td>{{ f.fieldCode }}</td>
              <td>{{ f.fieldName }}</td>
              <td>{{ getFieldTypeLabel(f.fieldType) }}</td>
              <td class="text-center">{{ f.length || '-' }}</td>
              <td class="text-center">{{ f.precisionVal || '-' }}</td>
              <td class="text-center">{{ f.domainName || '-' }}</td>
              <td><input type="checkbox" v-model="f.isRequired" :disabled="isViewMode" /></td>
              <td><input type="checkbox" v-model="f.isReadonly" :disabled="isViewMode" /></td>
              <td><input type="checkbox" v-model="f.isHidden" :disabled="isViewMode" /></td>
              <td><input type="checkbox" v-model="f.isUnique" :disabled="isViewMode" /></td>
              <td><input type="checkbox" v-model="f.isQuery" :disabled="isViewMode" /></td>
              <td><input type="checkbox" v-model="f.autoNumber" :disabled="isViewMode || f.fieldType !== 'string' || !isNewField(f)" /></td>
              <td>
                <div v-if="f.autoNumber" class="rule-cell">
                  <span v-if="f.encodingRuleName" class="rule-name">
                    {{ f.encodingRuleName }}
                  </span>
                  <span v-else-if="isNewField(f)" class="link-btn" @click="openEncodingRuleSelect(f)">选择规则</span>
                  <span v-else class="text-muted">未选择</span>
                </div>
                <span v-else class="text-muted">-</span>
              </td>
              <td>
                <span v-if="f.autoNumber" class="text-muted">自动生成</span>
                <input v-else v-model="f.defaultValue" class="input-cell" placeholder="默认值" :disabled="isViewMode" />
              </td>
              <td v-if="!isViewMode">
                <span v-if="isNewField(f)" class="link-btn" @click="deleteField(f)">删除</span>
                <span v-else class="text-muted">-</span>
              </td>
            </tr>
            <tr class="add-row" v-if="!isViewMode"><td colspan="16"><span class="add-btn" @click="openFieldSelectDialog">+ 选择字段</span></td></tr>
          </tbody>
        </table>
      </div>

      <!-- 子表内容 -->
      <div class="panel" v-else>
        <!-- 二级TAB -->
        <div class="tabs-secondary">
          <div v-for="(sub, i) in subEntities" :key="sub.id"
               class="tab-item" :class="{ active: activeSubTabIndex === i }"
               @click="activeSubTabIndex = i">
            <span v-if="editingSubTabIndex === i">
              <input v-model="editingSubTabName" @blur="finishEditSubTabName" @keyup.enter="finishEditSubTabName"
                     class="input-edit" autoFocus />
            </span>
            <span v-else @dblclick="!isViewMode && startEditSubTabName(i)">{{ sub.entityName }}</span>
            <span v-if="!isViewMode && isNewEntity(sub)" class="close-icon" @click.stop="deleteSubEntity(i)">×</span>
          </div>
          <div v-if="!isViewMode" class="tab-item add" @click="addSubEntity">+</div>
        </div>

        <!-- 子表编码和名称 -->
        <div class="entity-info" v-if="currentEntity">
          <div class="info-item">
            <label>子表编码：</label>
            <input v-model="currentEntity.entityCode" class="input-info" placeholder="请输入子表编码" :disabled="isViewMode" />
          </div>
          <div class="info-item">
            <label>子表名称：</label>
            <input v-model="currentEntity.entityName" class="input-info" placeholder="请输入子表名称" :disabled="isViewMode" />
          </div>
        </div>

        <!-- 子表字段 -->
        <table class="data-table" v-if="currentEntity">
          <thead>
            <tr>
              <th style="width: 40px">序号</th>
              <th>字段编码</th>
              <th>字段名称</th>
              <th>字段类型</th>
              <th>长度</th>
              <th>小数位</th>
              <th>值域</th>
              <th>必填</th>
              <th>只读</th>
              <th>隐藏</th>
              <th>唯一</th>
              <th>默认值</th>
              <th v-if="!isViewMode">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(f, index) in currentEntity.fields" :key="f.id"
                :draggable="!isViewMode"
                @dragstart="!isViewMode && handleDragStart($event, index)"
                @dragover.prevent="!isViewMode && handleDragOver($event, index)"
                @drop="!isViewMode && handleDrop($event, index)"
                @dragend="!isViewMode && handleDragEnd"
                :class="{ 'dragging': !isViewMode && dragIndex === index, 'drag-over': !isViewMode && dropIndex === index }">
              <td class="text-center drag-handle">{{ index + 1 }}</td>
              <td>{{ f.fieldCode }}</td>
              <td>{{ f.fieldName }}</td>
              <td>{{ getFieldTypeLabel(f.fieldType) }}</td>
              <td class="text-center">{{ f.length || '-' }}</td>
              <td class="text-center">{{ f.precisionVal || '-' }}</td>
              <td class="text-center">{{ f.domainName || '-' }}</td>
              <td><input type="checkbox" v-model="f.isRequired" :disabled="isViewMode" /></td>
              <td><input type="checkbox" v-model="f.isReadonly" :disabled="isViewMode" /></td>
              <td><input type="checkbox" v-model="f.isHidden" :disabled="isViewMode" /></td>
              <td><input type="checkbox" v-model="f.isUnique" :disabled="isViewMode" /></td>
              <td>
                <input v-model="f.defaultValue" class="input-cell" placeholder="默认值" :disabled="isViewMode" />
              </td>
              <td v-if="!isViewMode">
                <span v-if="isNewField(f)" class="link-btn" @click="deleteField(f)">删除</span>
                <span v-else class="text-muted">-</span>
              </td>
            </tr>
            <tr class="add-row" v-if="!isViewMode"><td colspan="13"><span class="add-btn" @click="openFieldSelectDialog">+ 选择字段</span></td></tr>
          </tbody>
        </table>
        <div class="empty" v-else>点击 + 添加子表</div>
      </div>
    </div>

    <!-- 字段选择弹窗 -->
    <div class="modal field-select-modal" v-if="fieldSelectVisible">
      <div class="mask" @click="fieldSelectVisible = false"></div>
      <div class="modal-box modal-box-lg">
        <div class="modal-hd">选择字段 <span class="close" @click="fieldSelectVisible = false">×</span></div>
        <div class="modal-bd">
          <!-- 筛选条件 -->
          <div class="filter-row">
            <input v-model="searchKeyword" placeholder="搜索字段编码或名称" class="search-input" @keyup.enter="handleFieldSearch" />
            <div class="category-select-wrapper">
              <CategoryTreeSelect
                v-model="selectedCategoryId"
                placeholder="全部分类"
                :show-all="true"
                :show-search="true"
                class="category-select"
              />
            </div>
            <button class="btn-search" @click="handleFieldSearch">查询</button>
            <button class="btn-reset" @click="handleFieldReset">重置</button>
          </div>

          <!-- 字段表格 -->
          <div class="field-table-wrapper">
            <table class="field-select-table">
              <thead>
                <tr>
                  <th style="width: 40px">
                    <input type="checkbox"
                      :checked="selectedFieldIds.length === paginatedFieldList.length && paginatedFieldList.length > 0"
                      @change="toggleSelectAll" />
                  </th>
                  <th>字段编码</th>
                  <th>字段名称</th>
                  <th>字段类型</th>
                  <th>长度</th>
                  <th>小数位</th>
                  <th>值域</th>
                  <th>分类</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="f in paginatedFieldList" :key="f.id" @click="toggleFieldSelect(f.id)">
                  <td>
                    <input type="checkbox" :value="f.id" v-model="selectedFieldIds" @click.stop />
                  </td>
                  <td>{{ f.fieldCode }}</td>
                  <td>{{ f.fieldName }}</td>
                  <td>{{ getFieldTypeLabel(f.fieldType) }}</td>
                  <td class="text-center">{{ f.length || '-' }}</td>
                  <td class="text-center">{{ f.precision || '-' }}</td>
                  <td>{{ f.domainName || '-' }}</td>
                  <td>{{ f.categoryName || '-' }}</td>
                </tr>
                <tr v-if="paginatedFieldList.length === 0">
                  <td colspan="8" class="empty-tip">暂无数据</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="filteredFieldList.length > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="filteredFieldList.length"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
        <div class="modal-ft">
          <span>已选 {{ selectedFieldIds.length }} 项</span>
          <div>
            <button class="btn-cancel" @click="fieldSelectVisible = false">取消</button>
            <button class="btn-ok" @click="confirmSelectFields">确定</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 删除确认框 -->
    <MdmConfirmDialog
      v-model="confirmVisible"
      :message="confirmMessage"
      @confirm="confirmDeleteSubEntity"
    />

    <!-- 删除字段确认框 -->
    <MdmConfirmDialog
      v-model="deleteFieldVisible"
      message="确定删除该字段？"
      @confirm="confirmDeleteField"
    />

    <!-- 编码规则选择弹窗 -->
    <div class="modal" v-if="encodingRuleSelectVisible">
      <div class="mask" @click="encodingRuleSelectVisible = false"></div>
      <div class="modal-box">
        <div class="modal-hd">选择编码规则 <span class="close" @click="encodingRuleSelectVisible = false">×</span></div>
        <div class="modal-bd">
          <div class="rule-list">
            <div
              class="rule-item"
              v-for="rule in encodingRuleList"
              :key="rule.id"
              @click="selectEncodingRule(rule)"
            >
              <div class="rule-info">
                <div class="rule-name">{{ rule.ruleName }}</div>
                <div class="rule-code">{{ rule.ruleCode }}</div>
              </div>
              <div class="rule-example" v-if="rule.example">
                示例: {{ rule.example }}
              </div>
            </div>
            <div class="empty-tip" v-if="encodingRuleList.length === 0">
              暂无可用编码规则
            </div>
          </div>
        </div>
        <div class="modal-ft">
          <span>共 {{ encodingRuleList.length }} 条规则</span>
          <button class="btn-cancel" @click="encodingRuleSelectVisible = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 版本历史 -->
    <div class="version-history" v-if="versionHistory.length > 0">
      <div class="history-header" @click="showVersionHistory = !showVersionHistory">
        <span class="title">版本历史 ({{ versionHistory.length }})</span>
        <span class="toggle">{{ showVersionHistory ? '收起' : '展开' }}</span>
      </div>
      <div class="history-timeline" v-if="showVersionHistory">
        <div v-for="(item, index) in versionHistory" :key="item.id" class="timeline-item">
          <div class="timeline-line">
            <div class="timeline-dot" :class="item.status"></div>
            <div v-if="index < versionHistory.length - 1" class="timeline-connector"></div>
          </div>
          <div class="timeline-content" :class="{ current: item.id === viewData.id }">
            <div class="timeline-header">
              <span class="version-tag">V{{ item.version }}</span>
              <span class="status-tag" :class="item.status">{{ getStatusLabel(item.status) }}</span>
              <span v-if="item.isLatest" class="latest-badge">最新</span>
              <span v-if="item.id === viewData.id" class="current-badge">当前</span>
            </div>
            <div class="timeline-body">
              <div class="info-row">
                <span class="label">发布时间：</span>
                <span class="value">{{ formatDate(item.publishTime) }}</span>
              </div>
              <div class="info-row">
                <span class="label">更新人：</span>
                <span class="value">{{ item.updatedBy || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">字段数：</span>
                <span class="value">{{ getFieldCount(item.entities) }} 个</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布确认对话框 -->
    <MdmConfirmDialog
      v-model="publishConfirmVisible"
      message="确定发布该修订版本吗？发布后将成为新的主干版本，原版本将变为历史版本。"
      @confirm="confirmPublish"
    />
  </div>
</template>

<style scoped lang="scss">
.design-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background: #f0f2f5;
}

// 顶部栏
.header {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  gap: 12px;

  .btn-back {
    padding: 6px 12px;
    background: #fff;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
    &:hover { border-color: #ed2b33; color: #ed2b33; }
  }

  .title {
    flex: 1;
    font-size: 15px;
    font-weight: 600;
    color: #333;
  }

  .btn-save {
    padding: 6px 20px;
    background: #ed2b33;
    border: none;
    border-radius: 4px;
    color: #fff;
    cursor: pointer;
    font-size: 13px;
    &:hover:not(:disabled) { background: #c81e2c; }
    &:disabled { opacity: 0.6; cursor: not-allowed; }
  }

  .btn-publish {
    padding: 6px 20px;
    background: #52c41a;
    border: none;
    border-radius: 4px;
    color: #fff;
    cursor: pointer;
    font-size: 13px;
    &:hover:not(:disabled) { background: #389e0d; }
    &:disabled { opacity: 0.6; cursor: not-allowed; }
  }
}

// 内容区
.content {
  flex: 1;
  padding: 12px;
  overflow: auto;
}

// 视图基本信息
.view-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
  background: #fff;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 12px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;

    &.full-width {
      width: 100%;
    }

    label {
      font-size: 13px;
      color: #999;
      white-space: nowrap;
    }

    .info-value {
      font-size: 13px;
      color: #333;

      &.status-active {
        color: #52c41a;
        font-weight: 500;
      }

      &.status-inactive {
        color: #ff4d4f;
        font-weight: 500;
      }
    }
  }
}

// 一级TAB
.tabs-primary {
  display: flex;
  background: #fff;
  border-radius: 4px 4px 0 0;
  border-bottom: 1px solid #e8e8e8;

  .tab-item {
    padding: 10px 24px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    border-bottom: 2px solid transparent;
    margin-bottom: -1px;
    transition: all 0.2s;

    &:hover { color: #ed2b33; }
    &.active {
      color: #ed2b33;
      border-bottom-color: #ed2b33;
      font-weight: 500;
    }
  }
}

// 面板
.panel {
  background: #fff;
  padding: 12px;
  border-radius: 0 0 4px 4px;
  overflow-x: auto;
}

// 实体信息（编码、名称）
.entity-info {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #fafafa;
  border-radius: 4px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;

    label {
      font-size: 13px;
      color: #666;
      white-space: nowrap;
    }

    .input-info {
      padding: 5px 10px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      font-size: 13px;
      width: 160px;

      &:focus { outline: none; border-color: #ed2b33; }
      &:disabled {
        background: #f5f5f5;
        color: #666;
        cursor: not-allowed;
      }
    }
  }
}

// 二级TAB
.tabs-secondary {
  display: flex;
  gap: 2px;
  margin-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 8px;

  .tab-item {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 5px 14px;
    font-size: 13px;
    color: #666;
    cursor: pointer;
    border-radius: 3px;
    transition: all 0.2s;

    &:hover { color: #ed2b33; }
    &.active { background: #fef0f0; color: #ed2b33; }
    &.add { color: #999; &:hover { color: #ed2b33; } }

    .close-icon {
      font-size: 14px;
      color: #bbb;
      &:hover { color: #f56c6c; }
    }

    .input-edit {
      width: 60px;
      padding: 2px 4px;
      border: 1px solid #ed2b33;
      border-radius: 2px;
      font-size: 13px;
    }
  }
}

// 数据表格
.data-table {
  width: 100%;
  min-width: 950px;
  border-collapse: collapse;
  font-size: 13px;
  table-layout: fixed;

  th, td {
    padding: 6px 4px;
    border: 1px solid #e8e8e8;
    text-align: left;
  }

  th {
    background: #fafafa;
    font-weight: 600;
    color: #333;
    font-size: 12px;
  }

  // 拖拽样式
  tbody tr {
    transition: all 0.2s;

    &.dragging {
      opacity: 0.5;
      background: #f0f0f0;
    }

    &.drag-over {
      border-top: 2px solid #ed2b33;
      background: #fff9f9;
    }

    &:hover .drag-handle {
      cursor: move;
      color: #ed2b33;
    }
  }

  .drag-handle {
    color: #999;
    font-weight: 500;
  }

  // 列宽设置（16列）
  th:nth-child(1), td:nth-child(1) { width: 40px; }   // 序号
  th:nth-child(2), td:nth-child(2) { width: 100px; }  // 字段编码
  th:nth-child(3), td:nth-child(3) { width: 100px; }  // 字段名称
  th:nth-child(4), td:nth-child(4) { width: 60px; }   // 字段类型
  th:nth-child(5), td:nth-child(5) { width: 50px; text-align: center; }   // 长度
  th:nth-child(6), td:nth-child(6) { width: 40px; text-align: center; }   // 小数位
  th:nth-child(7), td:nth-child(7) { width: 80px; text-align: center; }   // 值域
  th:nth-child(8), td:nth-child(8) { width: 40px; text-align: center; }   // 必填
  th:nth-child(9), td:nth-child(9) { width: 40px; text-align: center; }   // 只读
  th:nth-child(10), td:nth-child(10) { width: 40px; text-align: center; } // 隐藏
  th:nth-child(11), td:nth-child(11) { width: 40px; text-align: center; } // 唯一
  th:nth-child(12), td:nth-child(12) { width: 50px; text-align: center; } // 可查询
  th:nth-child(13), td:nth-child(13) { width: 60px; text-align: center; } // 自动编号
  th:nth-child(14), td:nth-child(14) { width: 100px; }  // 编码规则
  th:nth-child(15), td:nth-child(15) { width: 80px; }   // 默认值
  th:nth-child(16), td:nth-child(16) { width: 45px; text-align: center; } // 操作

  .text-center { text-align: center; }

  .input-cell {
    width: 100%;
    padding: 3px 5px;
    border: 1px solid transparent;
    border-radius: 2px;
    font-size: 12px;
    &:hover { border-color: #d9d9d9; }
    &:focus { outline: none; border-color: #ed2b33; }
    &:disabled {
      background: transparent;
      color: #666;
      cursor: not-allowed;
      border-color: transparent;
    }
  }

  .select-cell {
    width: 100%;
    padding: 3px 5px;
    border: 1px solid transparent;
    border-radius: 2px;
    font-size: 12px;
    background: transparent;
    &:hover { border-color: #d9d9d9; }
    &:focus { outline: none; border-color: #ed2b33; }
  }

  .link-btn {
    color: #1890ff;
    cursor: pointer;
    font-size: 12px;
    &:hover { color: #40a9ff; }
  }

  .add-row {
    td {
      text-align: center;
      background: #fafafa;
      padding: 6px;
    }
    .add-btn {
      color: #ed2b33;
      cursor: pointer;
      font-size: 13px;
      &:hover { font-weight: 500; }
    }
  }
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 13px;
}

// 弹窗
.modal {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;

  .mask {
    position: absolute;
    inset: 0;
    background: rgba(0,0,0,0.45);
  }

  .modal-box {
    position: relative;
    width: 500px;
    background: #fff;
    border-radius: 6px;
    overflow: hidden;
  }

  .modal-hd {
    padding: 12px 16px;
    font-size: 15px;
    font-weight: 600;
    border-bottom: 1px solid #e8e8e8;
    background: #fafafa;

    .close {
      float: right;
      font-size: 18px;
      color: #999;
      cursor: pointer;
      &:hover { color: #333; }
    }
  }

  .modal-bd {
    padding: 12px 16px;
    max-height: 400px;
    overflow-y: auto;

    .form-item {
      display: flex;
      align-items: center;
      gap: 10px;

      label {
        width: 70px;
        font-size: 13px;
        color: #666;
        flex-shrink: 0;
      }

      input {
        flex: 1;
        padding: 6px 10px;
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        font-size: 13px;
        &:focus { outline: none; border-color: #ed2b33; }
      }
    }

    .search-input {
      width: 100%;
      padding: 6px 10px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      font-size: 13px;
      margin-bottom: 10px;
      &:focus { outline: none; border-color: #ed2b33; }
    }

    .field-list {
      border: 1px solid #e8e8e8;
      border-radius: 4px;
      max-height: 280px;
      overflow-y: auto;

      .field-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 10px;
        font-size: 13px;
        cursor: pointer;

        &:not(:last-child) { border-bottom: 1px solid #f0f0f0; }
        &:hover { background: #fafafa; }
        &.hd { background: #f5f5f5; font-weight: 500; }

        input[type="checkbox"] { width: 14px; height: 14px; }
        .code { width: 110px; font-weight: 500; }
        .name { flex: 1; color: #666; }
        .type { width: 70px; color: #999; font-size: 12px; }
      }
    }
  }

  .modal-ft {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 16px;
    border-top: 1px solid #e8e8e8;
    font-size: 13px;
    color: #666;

    .btn-cancel {
      padding: 6px 16px;
      background: #fff;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      cursor: pointer;
      font-size: 13px;
      &:hover { border-color: #ed2b33; color: #ed2b33; }
    }

    .btn-ok {
      padding: 6px 16px;
      background: #ed2b33;
      border: none;
      border-radius: 4px;
      color: #fff;
      cursor: pointer;
      font-size: 13px;
      margin-left: 8px;
      &:hover { background: #c81e2c; }
    }
  }
}

// 编码规则单元格样式
.rule-cell {
  display: flex;
  align-items: center;
  gap: 4px;

  .rule-name {
    color: #1890ff;
    cursor: pointer;
    font-size: 12px;
    &:hover { color: #40a9ff; }
  }

  .clear-icon {
    color: #999;
    cursor: pointer;
    font-size: 14px;
    &:hover { color: #f56c6c; }
  }
}

.text-muted {
  color: #bbb;
  font-size: 12px;
}

// 编码规则列表样式
.rule-list {
  .rule-item {
    padding: 12px;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #ed2b33;
      background: #fafafa;
    }

    .rule-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;

      .rule-name {
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }

      .rule-code {
        font-size: 12px;
        color: #999;
      }
    }

    .rule-example {
      font-size: 12px;
      color: #666;
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 3px;
    }
  }

  .empty-tip {
    text-align: center;
    padding: 20px;
    color: #999;
    font-size: 13px;
  }
}

// 字段选择弹窗样式
.field-select-modal {
  .modal-box-lg {
    width: 900px;
    max-width: 95vw;
  }

  .filter-row {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
    align-items: flex-start; // 顶部对齐

    .search-input {
      flex: 1;
      height: 36px; // 固定高度
      padding: 0 10px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      font-size: 13px;
      line-height: 36px; // 与高度一致，文字垂直居中
      box-sizing: border-box;
      &:focus { outline: none; border-color: #ed2b33; }
    }

    .category-select-wrapper {
      width: 200px;
      height: 36px; // 固定高度

      .category-select {
        width: 100%;
        height: 100%;

        // 强制覆盖组件内部样式
        :deep(.category-tree-select) {
          height: 100%;
        }

        :deep(.select-trigger) {
          height: 36px !important;
          min-height: 36px !important;
        }
      }
    }

    .btn-search, .btn-reset {
      height: 36px; // 固定高度
      padding: 0 16px;
      border-radius: 4px;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;
      box-sizing: border-box;
      white-space: nowrap;
      line-height: 36px; // 与高度一致，文字垂直居中
    }

    .btn-search {
      background: #ed2b33;
      border: none;
      color: #fff;
      &:hover { background: #c81e2c; }
    }

    .btn-reset {
      background: #fff;
      border: 1px solid #d9d9d9;
      color: #666;
      &:hover { border-color: #ed2b33; color: #ed2b33; }
    }
  }

  .field-table-wrapper {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
  }

  .field-select-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;

    th, td {
      padding: 8px 10px;
      border: 1px solid #e8e8e8;
      text-align: left;
    }

    th {
      background: #fafafa;
      font-weight: 600;
      color: #333;
      font-size: 12px;
      position: sticky;
      top: 0;
      z-index: 1;
    }

    tbody tr {
      cursor: pointer;
      transition: background 0.2s;

      &:hover {
        background: #f5f5f5;
      }

      &.selected {
        background: #e6f7ff;
      }
    }

    td {
      color: #666;

      &:nth-child(2) { // 字段编码
        font-weight: 500;
        color: #333;
      }
    }

    .text-center {
      text-align: center;
    }

    .empty-tip {
      text-align: center;
      padding: 40px 20px;
      color: #999;
    }
  }

  .pagination-wrapper {
    margin-top: 12px;
    padding: 8px 0;
    border-top: 1px solid #e8e8e8;
    display: flex;
    justify-content: center;

    :deep(.el-pagination) {
      .el-pagination__total,
      .el-pagination__sizes,
      .el-pagination__jump {
        margin-right: 10px;
      }
    }
  }
}

// 版本历史
.version-history {
  background: #fff;
  border-radius: 4px;
  margin-top: 12px;
  overflow: hidden;

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 16px;
    cursor: pointer;
    transition: background 0.2s;
    border-bottom: 1px solid #e8e8e8;

    &:hover {
      background: #fafafa;
    }

    .title {
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }

    .toggle {
      font-size: 13px;
      color: #1890ff;
    }
  }
}

// 时间轴
.history-timeline {
  max-height: 500px;
  overflow-y: auto;
  padding: 16px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  margin-bottom: 0;
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 20px;
  flex-shrink: 0;
}

.timeline-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #d9d9d9;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #d9d9d9;
  flex-shrink: 0;

  &.draft {
    background: #909399;
    box-shadow: 0 0 0 2px #909399;
  }

  &.published {
    background: #67c23a;
    box-shadow: 0 0 0 2px #67c23a;
  }

  &.disabled {
    background: #ff4d4f;
    box-shadow: 0 0 0 2px #ff4d4f;
  }
}

.timeline-connector {
  width: 2px;
  flex: 1;
  background: #e8e8e8;
  min-height: 20px;
}

.timeline-content {
  flex: 1;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 14px;
  margin-bottom: 16px;

  &:hover {
    border-color: #ed2b33;
  }

  &.current {
    background: #e6f7ff;
    border-color: #91d5ff;
  }
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;

  .version-tag {
    display: inline-block;
    padding: 2px 10px;
    background: #f0f0f0;
    border-radius: 10px;
    font-size: 13px;
    font-weight: 600;
    color: #333;
  }

  .status-tag {
    padding: 2px 8px;
    border-radius: 2px;
    font-size: 12px;

    &.published {
      background: #f6ffed;
      color: #52c41a;
    }

    &.draft {
      background: #fffbe6;
      color: #faad14;
    }

    &.revising {
      background: #f9f0ff;
      color: #722ed1;
    }

    &.disabled {
      background: #fff1f0;
      color: #ff4d4f;
    }

    &.history {
      background: #f4f4f5;
      color: #909399;
    }
  }

  .latest-badge {
    padding: 2px 8px;
    background: #52c41a;
    color: #fff;
    border-radius: 2px;
    font-size: 12px;
  }

  .current-badge {
    padding: 2px 8px;
    background: #1890ff;
    color: #fff;
    border-radius: 2px;
    font-size: 12px;
  }
}

.timeline-body {
  .info-row {
    display: flex;
    margin-bottom: 6px;
    font-size: 13px;

    .label {
      width: 70px;
      color: #999;
      flex-shrink: 0;
    }

    .value {
      color: #333;
    }
  }
}
</style>
