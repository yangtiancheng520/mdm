<template>
  <div class="auto-form-renderer">
    <el-form label-width="120px" :model="modelValue" :disabled="disabled">
      <el-row :gutter="20">
        <el-col :span="12" v-for="field in fields" :key="field.fieldCode">
          <el-form-item :label="field.fieldName" :required="field.isRequired">
            <!-- 文本输入 -->
            <el-input
              v-if="getInputType(field) === 'input'"
              v-model="modelValue[field.fieldCode]"
              :placeholder="field.placeholder || `请输入${field.fieldName}`"
              :maxlength="field.length"
              show-word-limit
            />
            <!-- 多行文本 -->
            <el-input
              v-else-if="getInputType(field) === 'textarea'"
              v-model="modelValue[field.fieldCode]"
              type="textarea"
              :rows="3"
              :placeholder="field.placeholder || `请输入${field.fieldName}`"
            />
            <!-- 数字输入 -->
            <el-input-number
              v-else-if="getInputType(field) === 'number'"
              v-model="modelValue[field.fieldCode]"
              style="width: 100%"
              controls-position="right"
              :precision="field.precisionVal"
            />
            <!-- 日期选择 -->
            <el-date-picker
              v-else-if="getInputType(field) === 'date'"
              v-model="modelValue[field.fieldCode]"
              style="width: 100%"
              type="date"
              placeholder="请选择日期"
              value-format="YYYY-MM-DD"
            />
            <!-- 日期时间选择 -->
            <el-date-picker
              v-else-if="getInputType(field) === 'datetime'"
              v-model="modelValue[field.fieldCode]"
              style="width: 100%"
              type="datetime"
              placeholder="请选择日期时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
            <!-- 下拉选择 -->
            <el-select
              v-else-if="getInputType(field) === 'select'"
              v-model="modelValue[field.fieldCode]"
              style="width: 100%"
              :placeholder="`请选择${field.fieldName}`"
              clearable
            >
              <el-option
                v-for="opt in getDomainOptions(field.domainId)"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <!-- 单选框 -->
            <el-radio-group
              v-else-if="getInputType(field) === 'radio'"
              v-model="modelValue[field.fieldCode]"
            >
              <el-radio
                v-for="opt in getDomainOptions(field.domainId)"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </el-radio>
            </el-radio-group>
            <!-- 开关 -->
            <el-switch
              v-else-if="getInputType(field) === 'switch'"
              v-model="modelValue[field.fieldCode]"
            />
            <!-- 默认文本输入 -->
            <el-input
              v-else
              v-model="modelValue[field.fieldCode]"
              :placeholder="`请输入${field.fieldName}`"
              :maxlength="field.length"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import type { ViewField } from '@/api/standard/viewDefinition'
import { getValueDomainById, type DomainOption } from '@/api/standard/valueDomain'

const props = defineProps<{
  fields: ViewField[]
  modelValue: Record<string, any>
  mode?: 'edit' | 'view'
}>()

const disabled = ref(props.mode === 'view')

// 值域数据缓存
const domainMap = ref<Map<number, DomainOption[]>>(new Map())

// 获取输入类型
function getInputType(field: ViewField): string {
  const fieldType = field.fieldType?.toLowerCase() || 'text'

  switch (fieldType) {
    case 'text':
    case 'textarea':
    case 'longtext':
      return 'textarea'
    case 'number':
    case 'decimal':
    case 'int':
    case 'float':
    case 'double':
      return 'number'
    case 'date':
      return 'date'
    case 'datetime':
    case 'timestamp':
      return 'datetime'
    case 'boolean':
    case 'bool':
      return 'switch'
    case 'select':
    case 'enum':
      return 'select'
    case 'radio':
      return 'radio'
    default:
      // 如果有值域，使用下拉选择
      if (field.domainId) {
        return 'select'
      }
      return 'input'
  }
}

// 获取值域选项
function getDomainOptions(domainId?: number): DomainOption[] {
  if (!domainId) return []
  return domainMap.value.get(domainId) || []
}

// 加载值域数据
async function loadDomainData() {
  const domainIds = new Set<number>()
  props.fields.forEach(field => {
    if (field.domainId) domainIds.add(field.domainId)
  })

  for (const domainId of domainIds) {
    if (!domainMap.value.has(domainId)) {
      try {
        const res = await getValueDomainById(domainId)
        domainMap.value.set(domainId, res.data?.options || [])
      } catch {
        // 忽略错误
      }
    }
  }
}

// 监听 fields 变化
watch(() => props.fields, () => {
  loadDomainData()
}, { immediate: true })

onMounted(() => {
  loadDomainData()
})
</script>

<style scoped>
.auto-form-renderer {
  padding: 10px 0;
}
</style>
