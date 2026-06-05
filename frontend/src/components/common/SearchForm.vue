<script setup lang="ts">
/**
 * SearchForm 搜索表单组件
 *
 * 用于列表页面的搜索筛选功能
 *
 * @example
 * <SearchForm
 *   :fields="[
 *     { key: 'name', label: '名称', type: 'input', placeholder: '请输入名称' },
 *     { key: 'status', label: '状态', type: 'select', options: [{ label: '启用', value: 'active' }] }
 *   ]"
 *   @search="handleSearch"
 *   @reset="handleReset"
 * />
 */

export interface SearchField {
  key: string
  label: string
  type: 'input' | 'select' | 'date' | 'dateRange'
  placeholder?: string
  options?: Array<{ label: string; value: string | number }>
  defaultValue?: any
  span?: number // 栅格宽度，默认 6
}

interface Props {
  fields: SearchField[]
  modelValue?: Record<string, any>
  loading?: boolean
  collapsed?: boolean // 是否折叠
  showCollapse?: boolean // 是否显示折叠按钮
}

interface Emits {
  (e: 'update:modelValue', value: Record<string, any>): void
  (e: 'search'): void
  (e: 'reset'): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({}),
  loading: false,
  collapsed: false,
  showCollapse: true
})

const emit = defineEmits<Emits>()

// 内部表单数据
const formData = ref<Record<string, any>>({})

// 是否展开
const isCollapsed = ref(props.collapsed)

// 初始化表单数据
watch(() => props.modelValue, (val) => {
  formData.value = { ...val }
}, { immediate: true, deep: true })

// 初始化默认值
onMounted(() => {
  props.fields.forEach(field => {
    if (field.defaultValue !== undefined && formData.value[field.key] === undefined) {
      formData.value[field.key] = field.defaultValue
    }
  })
})

// 输入变化
function handleInput(key: string, value: any) {
  formData.value[key] = value
  emit('update:modelValue', { ...formData.value })
}

// 搜索
function handleSearch() {
  emit('search')
}

// 重置
function handleReset() {
  props.fields.forEach(field => {
    formData.value[field.key] = field.defaultValue ?? ''
  })
  emit('update:modelValue', { ...formData.value })
  emit('reset')
}

// 切换折叠
function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

// 显示的字段（折叠时只显示前3个）
const visibleFields = computed(() => {
  if (!isCollapsed.value) return props.fields
  return props.fields.slice(0, 3)
})

// 是否有更多字段
const hasMoreFields = computed(() => props.fields.length > 3)

// 获取栅格宽度
function getSpan(field: SearchField): number {
  return field.span ?? (field.type === 'dateRange' ? 8 : 6)
}
</script>

<template>
  <div class="search-form">
    <div class="search-form-content">
      <div class="search-form-fields">
        <template v-for="field in visibleFields" :key="field.key">
          <div class="search-field" :style="{ width: `calc(${getSpan(field) / 24 * 100}% - 12px)` }">
            <label class="search-field-label">{{ field.label }}</label>

            <!-- 输入框 -->
            <input
              v-if="field.type === 'input'"
              v-model="formData[field.key]"
              type="text"
              class="search-input"
              :placeholder="field.placeholder || `请输入${field.label}`"
              @keyup.enter="handleSearch"
            />

            <!-- 下拉选择 -->
            <select
              v-else-if="field.type === 'select'"
              v-model="formData[field.key]"
              class="search-select"
              @change="handleSearch"
            >
              <option value="">{{ field.placeholder || '全部' }}</option>
              <option v-for="opt in field.options" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>

            <!-- 日期选择 -->
            <input
              v-else-if="field.type === 'date'"
              v-model="formData[field.key]"
              type="date"
              class="search-input"
              :placeholder="field.placeholder"
            />

            <!-- 日期范围 -->
            <div v-else-if="field.type === 'dateRange'" class="search-date-range">
              <input
                v-model="formData[field.key + 'Start']"
                type="date"
                class="search-input"
              />
              <span class="date-separator">至</span>
              <input
                v-model="formData[field.key + 'End']"
                type="date"
                class="search-input"
              />
            </div>
          </div>
        </template>
      </div>

      <div class="search-form-actions">
        <button class="search-btn search-btn-primary" :disabled="loading" @click="handleSearch">
          <span v-if="loading" class="loading-icon">⏳</span>
          <span v-else>查询</span>
        </button>
        <button class="search-btn search-btn-default" @click="handleReset">
          重置
        </button>
        <button
          v-if="showCollapse && hasMoreFields"
          class="search-btn search-btn-link"
          @click="toggleCollapse"
        >
          {{ isCollapsed ? '展开 ↓' : '收起 ↑' }}
        </button>
      </div>
    </div>

    <!-- 操作按钮插槽 -->
    <div class="search-form-extra" v-if="$slots.extra">
      <slot name="extra"></slot>
    </div>
  </div>
</template>

<style scoped>
.search-form {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
}

.search-form-content {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 16px;
}

.search-form-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  flex: 1;
}

.search-field {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 200px;
}

.search-field-label {
  color: #333;
  font-size: 14px;
  white-space: nowrap;
  min-width: 60px;
}

.search-input {
  flex: 1;
  height: 32px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #c41a1a;
}

.search-select {
  flex: 1;
  height: 32px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  background: #fff;
  cursor: pointer;
}

.search-select:focus {
  border-color: #c41a1a;
}

.search-date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.date-separator {
  color: #999;
  font-size: 14px;
}

.search-form-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.search-btn-primary {
  background: #c41a1a;
  color: #fff;
}

.search-btn-primary:hover {
  background: #a01515;
}

.search-btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.search-btn-default {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #333;
}

.search-btn-default:hover {
  border-color: #c41a1a;
  color: #c41a1a;
}

.search-btn-link {
  background: transparent;
  color: #666;
  padding: 0 8px;
}

.search-btn-link:hover {
  color: #c41a1a;
}

.loading-icon {
  display: inline-block;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.search-form-extra {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}
</style>
