<template>
  <div class="form-renderer">
    <el-form label-width="120px" :model="modelValue">
      <el-row :gutter="20">
        <el-col :span="getSpan(comp)" v-for="comp in components" :key="comp.id">
          <el-form-item :label="comp.fieldName" :required="comp.isRequired">
            <!-- 文本输入 -->
            <el-input
              v-if="getComponentType(comp) === 'input'"
              v-model="modelValue[comp.fieldCode]"
              :placeholder="comp.placeholder || '请输入'"
              :disabled="disabled"
            />
            <!-- 多行文本 -->
            <el-input
              v-else-if="getComponentType(comp) === 'textarea'"
              v-model="modelValue[comp.fieldCode]"
              type="textarea"
              :rows="2"
              :placeholder="comp.placeholder || '请输入'"
              :disabled="disabled"
            />
            <!-- 密码输入 -->
            <el-input
              v-else-if="getComponentType(comp) === 'password'"
              v-model="modelValue[comp.fieldCode]"
              type="password"
              show-password
              :placeholder="comp.placeholder || '请输入'"
              :disabled="disabled"
            />
            <!-- 数字输入 -->
            <el-input-number
              v-else-if="getComponentType(comp) === 'inputNumber'"
              v-model="modelValue[comp.fieldCode]"
              style="width: 100%"
              controls-position="right"
              :disabled="disabled"
            />
            <!-- 日期选择 -->
            <el-date-picker
              v-else-if="getComponentType(comp) === 'datePicker'"
              v-model="modelValue[comp.fieldCode]"
              style="width: 100%"
              placeholder="请选择日期"
              :disabled="disabled"
            />
            <!-- 日期时间选择 -->
            <el-date-picker
              v-else-if="getComponentType(comp) === 'dateTimePicker'"
              v-model="modelValue[comp.fieldCode]"
              style="width: 100%"
              type="datetime"
              placeholder="请选择日期时间"
              :disabled="disabled"
            />
            <!-- 时间选择 -->
            <el-time-picker
              v-else-if="getComponentType(comp) === 'timePicker'"
              v-model="modelValue[comp.fieldCode]"
              style="width: 100%"
              placeholder="请选择时间"
              :disabled="disabled"
            />
            <!-- 开关 -->
            <el-switch
              v-else-if="getComponentType(comp) === 'switch'"
              v-model="modelValue[comp.fieldCode]"
              :disabled="disabled"
            />
            <!-- 下拉选择 -->
            <el-select
              v-else-if="getComponentType(comp) === 'select'"
              v-model="modelValue[comp.fieldCode]"
              style="width: 100%"
              placeholder="请选择"
              :disabled="disabled"
            >
              <el-option
                v-for="opt in getDomainOptions(comp.domainId)"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <!-- 单选框 -->
            <el-radio-group
              v-else-if="getComponentType(comp) === 'radio'"
              v-model="modelValue[comp.fieldCode]"
              :disabled="disabled"
            >
              <el-radio
                v-for="opt in getDomainOptions(comp.domainId)"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </el-radio>
            </el-radio-group>
            <!-- 复选框 -->
            <el-checkbox-group
              v-else-if="getComponentType(comp) === 'checkbox'"
              v-model="modelValue[comp.fieldCode]"
              :disabled="disabled"
            >
              <el-checkbox
                v-for="opt in getDomainOptions(comp.domainId)"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </el-checkbox>
            </el-checkbox-group>
            <!-- 默认文本输入 -->
            <el-input
              v-else
              v-model="modelValue[comp.fieldCode]"
              :placeholder="comp.placeholder || '请输入'"
              :disabled="disabled"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { FormDesignRequest, FormComponentDto } from '@/api/form'
import { getValueDomainById, type DomainOption } from '@/api/standard/valueDomain'

const props = defineProps<{
  design: FormDesignRequest
  modelValue: Record<string, any>
  mode?: 'edit' | 'view'
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Record<string, any>): void
}>()

const disabled = computed(() => props.mode === 'view')

// 组件列表
const components = computed(() => props.design.components || [])

// 值域数据缓存
const domainMap = ref<Map<number, DomainOption[]>>(new Map())

// 获取控件类型
function getComponentType(comp: FormComponentDto): string {
  return comp.componentType || 'input'
}

// 获取占用列数
function getSpan(comp: FormComponentDto): number {
  const colSpan = comp.colSpan || 1
  return colSpan * 6 // 24栅格，每列6格
}

// 获取值域选项
function getDomainOptions(domainId?: number): DomainOption[] {
  if (!domainId) return []
  return domainMap.value.get(domainId) || []
}

// 加载值域数据
async function loadDomainData() {
  const domainIds = new Set<number>()
  components.value.forEach(comp => {
    if (comp.domainId) domainIds.add(comp.domainId)
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

// 监听 design 变化
watch(() => props.design, () => {
  loadDomainData()
}, { immediate: true })

onMounted(() => {
  loadDomainData()
})
</script>

<style scoped>
.form-renderer {
  padding: 10px 0;
}
</style>
