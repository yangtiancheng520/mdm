<script setup lang="ts">
/**
 * 组织树选择组件 - 基于 Element Plus Tree
 * 支持单选、多选、搜索、展开/收起等功能
 */

import { ref, computed, watch, onMounted } from 'vue'
import { ElTree } from 'element-plus'
import type { TreeNode } from 'element-plus/es/components/tree/src/tree.type'
import { getActiveOrganizationTree, type Organization } from '../../api/system/organization'

// Props
interface Props {
  modelValue?: number | number[] | null  // 支持单选和多选
  multiple?: boolean                      // 是否多选
  checkStrictly?: boolean                 // 是否严格的父子不关联模式
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  showSearch?: boolean                    // 是否显示搜索框
  showAll?: boolean                       // 是否显示"全部"选项
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: null,
  multiple: false,
  checkStrictly: false,
  placeholder: '请选择组织',
  disabled: false,
  clearable: true,
  showSearch: true,
  showAll: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: number | number[] | null]
  'change': [value: number | number[] | null, node: Organization | Organization[] | null]
}>()

// 数据
const treeData = ref<Organization[]>([])
const loading = ref(false)
const treeRef = ref<InstanceType<typeof ElTree>>()
const filterText = ref('')
const selectedNode = ref<Organization | null>(null)

// 选中的值
const selectedValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 显示的文本
const displayText = computed(() => {
  if (!selectedValue.value) return ''

  if (props.multiple && Array.isArray(selectedValue.value)) {
    // 多选：显示选中的节点名称
    const names = selectedValue.value.map(id => {
      const node = findNode(treeData.value, id)
      return node?.orgName || ''
    }).filter(Boolean)
    return names.join(', ')
  } else {
    // 单选
    const node = findNode(treeData.value, selectedValue.value as number)
    return node?.orgName || ''
  }
})

// 查找节点
function findNode(nodes: Organization[], id: number): Organization | null {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNode(node.children, id)
      if (found) return found
    }
  }
  return null
}

// 获取组织树数据
async function fetchTree() {
  loading.value = true
  try {
    // 添加时间戳参数，避免缓存
    const res = await getActiveOrganizationTree()
    treeData.value = res.data || []

    // 如果需要显示"全部"选项
    if (props.showAll) {
      treeData.value = [
        {
          id: 0,
          orgCode: 'ALL',
          orgName: '全部',
          orgType: 'all',
          parentId: null,
          level: 0,
          path: '/0',
          manager: '',
          phone: '',
          email: '',
          sort: 0,
          status: 'active',
          createdBy: '',
          createdAt: '',
          updatedBy: '',
          updatedAt: '',
          description: '',
          children: treeData.value
        }
      ]
    }
    console.log('组织树刷新成功，数据量:', treeData.value.length)
  } catch (error) {
    console.error('获取组织树失败:', error)
  } finally {
    loading.value = false
  }
}

// 过滤节点
function filterNode(value: string, data: Organization): boolean {
  if (!value) return true
  return data.orgName.includes(value) || data.orgCode.includes(value)
}

// 搜索
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 单选处理
function handleNodeClick(data: Organization) {
  if (props.multiple) return

  selectedNode.value = data
  selectedValue.value = data.id
  emit('change', data.id, data)
}

// 多选处理
function handleCheckChange() {
  if (!props.multiple) return

  const checkedNodes = treeRef.value?.getCheckedNodes() as Organization[]
  const checkedIds = checkedNodes.map(node => node.id)
  selectedValue.value = checkedIds
  emit('change', checkedIds, checkedNodes)
}

// 清空
function handleClear() {
  selectedValue.value = props.multiple ? [] : null
  selectedNode.value = null
  emit('change', null, null)
}

// 初始化
onMounted(() => {
  fetchTree()
})

// 暴露方法
defineExpose({
  refresh: fetchTree,
  getSelectedNode: () => selectedNode.value
})
</script>

<template>
  <div class="org-tree-select">
    <!-- 搜索框 -->
    <div v-if="showSearch" class="search-wrapper">
      <el-input
        v-model="filterText"
        placeholder="搜索组织名称/编码"
        clearable
        :disabled="disabled"
      >
        <template #prefix>
          <span class="search-icon">🔍</span>
        </template>
      </el-input>
    </div>

    <!-- 树形结构 -->
    <div class="tree-wrapper" v-loading="loading">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="{
          label: 'orgName',
          children: 'children'
        }"
        :node-key="'id'"
        :default-expanded-keys="modelValue ? [modelValue as number] : []"
        :highlight-current="!multiple"
        :show-checkbox="multiple"
        :check-strictly="checkStrictly"
        :filter-node-method="filterNode"
        :disabled="disabled"
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
        @check-change="handleCheckChange"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <span class="node-label">{{ data.orgName }}</span>
            <span class="node-code">({{ data.orgCode }})</span>
            <el-tag v-if="data.orgType && data.orgType !== 'all'" size="small" type="info">
              {{ data.orgType === 'company' ? '公司' : data.orgType === 'department' ? '部门' : data.orgType === 'group' ? '组' : '岗位' }}
            </el-tag>
          </div>
        </template>
      </el-tree>
    </div>
  </div>
</template>

<style scoped lang="scss">
.org-tree-select {
  width: 100%;

  .search-wrapper {
    margin-bottom: 12px;

    .search-icon {
      font-size: 14px;
    }
  }

  .tree-wrapper {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    padding: 8px;

    &:hover {
      border-color: #c0c4cc;
    }
  }

  .tree-node {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;

    .node-label {
      color: #333;
      font-weight: 500;
    }

    .node-code {
      color: #999;
      font-size: 12px;
    }
  }
}

// Element Plus Tree 样式覆盖
:deep(.el-tree) {
  background: transparent;

  .el-tree-node__content {
    height: 32px;
    border-radius: 4px;

    &:hover {
      background-color: #f5f7fa;
    }
  }

  .el-tree-node.is-current > .el-tree-node__content {
    background-color: #e8f4ff;
    color: #1890ff;
    font-weight: 500;
  }

  .el-tree-node__expand-icon {
    color: #999;
  }
}
</style>
