<script setup lang="ts">
/**
 * TreeSelect 树形选择组件
 *
 * 用于选择树形结构数据，如组织、菜单等
 *
 * @example
 * <TreeSelect
 *   v-model="selectedId"
 *   :data="treeData"
 *   placeholder="请选择组织"
 *   @change="handleChange"
 * />
 */

import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

export interface TreeNode {
  id: number | string
  label: string
  children?: TreeNode[]
  disabled?: boolean
  [key: string]: any
}

interface Props {
  modelValue?: number | string | number[] | string[] | null
  data: TreeNode[]
  placeholder?: string
  disabled?: boolean
  multiple?: boolean
  checkStrictly?: boolean // 父子不关联
  expandAll?: boolean
  defaultExpandLevel?: number
  clearable?: boolean
  filterable?: boolean
  props?: {
    label?: string
    children?: string
    disabled?: string
  }
}

interface Emits {
  (e: 'update:modelValue', value: any): void
  (e: 'change', value: any, node: TreeNode | TreeNode[]): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择',
  disabled: false,
  multiple: false,
  checkStrictly: false,
  expandAll: false,
  defaultExpandLevel: 2,
  clearable: true,
  filterable: false,
  props: () => ({
    label: 'label',
    children: 'children',
    disabled: 'disabled'
  })
})

const emit = defineEmits<Emits>()

// 字段映射
const labelKey = computed(() => props.props?.label || 'label')
const childrenKey = computed(() => props.props?.children || 'children')
const disabledKey = computed(() => props.props?.disabled || 'disabled')

// 组件状态
const isFocused = ref(false)
const isExpanded = ref(false)
const filterText = ref('')
const expandedKeys = ref<Set<number | string>>(new Set())

// 内部选中的值
const selectedValue = ref<any>(props.modelValue)

// 选中的节点
const selectedNodes = ref<TreeNode[]>([])

// 下拉框引用
const dropdownRef = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLElement | null>(null)

// 显示的文本
const displayText = computed(() => {
  if (selectedNodes.value.length === 0) return ''
  if (props.multiple) {
    return selectedNodes.value.map(n => n[labelKey.value]).join(', ')
  }
  return selectedNodes.value[0]?.[labelKey.value] || ''
})

// 过滤后的树数据
const filteredData = computed(() => {
  if (!props.filterable || !filterText.value) {
    return props.data
  }
  return filterTree(props.data, filterText.value.toLowerCase())
})

// 过滤树
function filterTree(nodes: TreeNode[], keyword: string): TreeNode[] {
  const result: TreeNode[] = []
  for (const node of nodes) {
    const label = String(node[labelKey.value]).toLowerCase()
    const children = node[childrenKey.value] as TreeNode[] | undefined

    if (label.includes(keyword)) {
      result.push(node)
    } else if (children && children.length > 0) {
      const filteredChildren = filterTree(children, keyword)
      if (filteredChildren.length > 0) {
        result.push({
          ...node,
          [childrenKey.value]: filteredChildren
        })
      }
    }
  }
  return result
}

// 初始化展开节点
function initExpandedKeys(nodes: TreeNode[], level: number = 0) {
  if (props.expandAll || level < props.defaultExpandLevel) {
    nodes.forEach(node => {
      expandedKeys.value.add(node.id)
      const children = node[childrenKey.value] as TreeNode[] | undefined
      if (children && children.length > 0) {
        initExpandedKeys(children, level + 1)
      }
    })
  }
}

// 查找节点
function findNode(nodes: TreeNode[], id: number | string): TreeNode | null {
  for (const node of nodes) {
    if (node.id === id) return node
    const children = node[childrenKey.value] as TreeNode[] | undefined
    if (children && children.length > 0) {
      const found = findNode(children, id)
      if (found) return found
    }
  }
  return null
}

// 切换节点展开
function toggleExpand(node: TreeNode) {
  if (expandedKeys.value.has(node.id)) {
    expandedKeys.value.delete(node.id)
  } else {
    expandedKeys.value.add(node.id)
  }
}

// 判断节点是否展开
function isNodeExpanded(node: TreeNode): boolean {
  return expandedKeys.value.has(node.id)
}

// 判断节点是否选中
function isSelected(node: TreeNode): boolean {
  if (props.multiple && Array.isArray(selectedValue.value)) {
    return selectedValue.value.includes(node.id)
  }
  return selectedValue.value === node.id
}

// 选择节点
function selectNode(node: TreeNode) {
  if (node[disabledKey.value]) return

  if (props.multiple) {
    const values = Array.isArray(selectedValue.value) ? [...selectedValue.value] : []
    const index = values.indexOf(node.id)
    if (index > -1) {
      values.splice(index, 1)
      selectedNodes.value = selectedNodes.value.filter(n => n.id !== node.id)
    } else {
      values.push(node.id)
      selectedNodes.value.push(node)
    }
    selectedValue.value = values
    emit('update:modelValue', values)
    emit('change', values, selectedNodes.value)
  } else {
    selectedValue.value = node.id
    selectedNodes.value = [node]
    emit('update:modelValue', node.id)
    emit('change', node.id, node)
    isExpanded.value = false
    isFocused.value = false
  }
}

// 清空选择
function clearSelection() {
  selectedValue.value = props.multiple ? [] : null
  selectedNodes.value = []
  emit('update:modelValue', selectedValue.value)
  emit('change', selectedValue.value, props.multiple ? [] : null)
}

// 切换下拉
function toggleDropdown() {
  if (props.disabled) return
  isExpanded.value = !isExpanded.value
  isFocused.value = isExpanded.value
}

// 点击外部关闭
function handleClickOutside(event: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    isExpanded.value = false
    isFocused.value = false
  }
}

// 监听值变化
watch(() => props.modelValue, (val) => {
  selectedValue.value = val
  // 更新选中的节点
  if (props.multiple && Array.isArray(val)) {
    selectedNodes.value = val.map(id => findNode(props.data, id)).filter(Boolean) as TreeNode[]
  } else if (val !== null && val !== undefined) {
    const node = findNode(props.data, val)
    selectedNodes.value = node ? [node] : []
  } else {
    selectedNodes.value = []
  }
}, { immediate: true })

// 监听数据变化
watch(() => props.data, () => {
  initExpandedKeys(props.data)
}, { immediate: true })

// 挂载和卸载
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div
    ref="dropdownRef"
    class="tree-select"
    :class="{
      'is-disabled': disabled,
      'is-focused': isFocused,
      'is-expanded': isExpanded
    }"
  >
    <!-- 输入框 -->
    <div class="tree-select-input" @click="toggleDropdown" ref="inputRef">
      <div class="tree-select-value">
        <template v-if="displayText">
          {{ displayText }}
        </template>
        <span v-else class="tree-select-placeholder">{{ placeholder }}</span>
      </div>
      <span class="tree-select-suffix">
        <span v-if="clearable && displayText" class="tree-select-clear" @click.stop="clearSelection">
          ×
        </span>
        <span class="tree-select-arrow" :class="{ 'is-reverse': isExpanded }">
          ▼
        </span>
      </span>
    </div>

    <!-- 下拉树 -->
    <transition name="slide-down">
      <div v-show="isExpanded" class="tree-select-dropdown">
        <!-- 搜索框 -->
        <div v-if="filterable" class="tree-select-filter">
          <input
            v-model="filterText"
            type="text"
            placeholder="搜索..."
            class="tree-select-filter-input"
          />
        </div>

        <!-- 树节点 -->
        <div class="tree-select-tree">
          <template v-if="filteredData.length > 0">
            <div
              v-for="node in filteredData"
              :key="node.id"
              class="tree-node"
            >
              <div
                class="tree-node-content"
                :class="{
                  'is-selected': isSelected(node),
                  'is-disabled': node[disabledKey]
                }"
                @click="selectNode(node)"
              >
                <!-- 展开/收起图标 -->
                <span
                  v-if="node[childrenKey] && node[childrenKey].length > 0"
                  class="tree-node-expand"
                  @click.stop="toggleExpand(node)"
                >
                  {{ isNodeExpanded(node) ? '▼' : '▶' }}
                </span>
                <span v-else class="tree-node-expand-placeholder"></span>

                <!-- 复选框 (多选模式) -->
                <label v-if="multiple" class="tree-node-checkbox">
                  <input type="checkbox" :checked="isSelected(node)" :disabled="node[disabledKey]" />
                  <span class="checkbox-indicator"></span>
                </label>

                <!-- 文本 -->
                <span class="tree-node-label">{{ node[labelKey] }}</span>
              </div>

              <!-- 子节点 -->
              <div v-if="node[childrenKey] && node[childrenKey].length > 0 && isNodeExpanded(node)" class="tree-node-children">
                <template v-for="child in node[childrenKey]" :key="child.id">
                  <!-- 递归渲染子节点 -->
                  <div
                    class="tree-node-content"
                    :style="{ paddingLeft: '24px' }"
                    :class="{
                      'is-selected': isSelected(child),
                      'is-disabled': child[disabledKey]
                    }"
                    @click="selectNode(child)"
                  >
                    <span
                      v-if="child[childrenKey] && child[childrenKey].length > 0"
                      class="tree-node-expand"
                      @click.stop="toggleExpand(child)"
                    >
                      {{ isNodeExpanded(child) ? '▼' : '▶' }}
                    </span>
                    <span v-else class="tree-node-expand-placeholder"></span>

                    <label v-if="multiple" class="tree-node-checkbox">
                      <input type="checkbox" :checked="isSelected(child)" :disabled="child[disabledKey]" />
                      <span class="checkbox-indicator"></span>
                    </label>

                    <span class="tree-node-label">{{ child[labelKey] }}</span>
                  </div>
                </template>
              </div>
            </div>
          </template>
          <div v-else class="tree-select-empty">
            暂无数据
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.tree-select {
  position: relative;
  width: 100%;
  font-size: 14px;
}

.tree-select-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 32px;
  padding: 0 8px 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s;
}

.tree-select:hover .tree-select-input {
  border-color: #c0c4cc;
}

.tree-select.is-focused .tree-select-input {
  border-color: #c41a1a;
}

.tree-select.is-disabled .tree-select-input {
  background: #f5f5f5;
  cursor: not-allowed;
}

.tree-select-value {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-select-placeholder {
  color: #a0a0a0;
}

.tree-select-suffix {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #c0c4cc;
}

.tree-select-clear {
  font-size: 18px;
  color: #c0c4cc;
}

.tree-select-clear:hover {
  color: #c41a1a;
}

.tree-select-arrow {
  font-size: 10px;
  transition: transform 0.2s;
}

.tree-select-arrow.is-reverse {
  transform: rotate(180deg);
}

.tree-select-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 300px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.tree-select-filter {
  padding: 8px;
  border-bottom: 1px solid #eee;
}

.tree-select-filter-input {
  width: 100%;
  height: 32px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}

.tree-select-filter-input:focus {
  border-color: #c41a1a;
}

.tree-select-tree {
  padding: 8px 0;
}

.tree-select-empty {
  padding: 16px;
  text-align: center;
  color: #999;
}

.tree-node-content {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0 8px 0 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.tree-node-content:hover {
  background: #f5f7fa;
}

.tree-node-content.is-selected {
  background: #fff7f7;
  color: #c41a1a;
}

.tree-node-content.is-disabled {
  cursor: not-allowed;
  color: #ccc;
}

.tree-node-expand {
  width: 16px;
  font-size: 10px;
  color: #999;
  text-align: center;
}

.tree-node-expand-placeholder {
  width: 16px;
}

.tree-node-children {
  margin-left: 12px;
}

.tree-node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 复选框 */
.tree-node-checkbox {
  display: flex;
  align-items: center;
}

.tree-node-checkbox input {
  display: none;
}

.checkbox-indicator {
  width: 14px;
  height: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 2px;
  position: relative;
}

.tree-node-checkbox input:checked + .checkbox-indicator {
  background: #c41a1a;
  border-color: #c41a1a;
}

.tree-node-checkbox input:checked + .checkbox-indicator::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 4px;
  height: 8px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

/* 动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
