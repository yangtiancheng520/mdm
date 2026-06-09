<script setup lang="ts">
/**
 * 分类树形选择组件 - 基于 Element Plus Tree
 * 使用弹窗方式选择
 * 支持传入自定义树数据或从API获取
 */

import { ref, computed, watch, onMounted } from 'vue'
import { ElTree, ElDialog } from 'element-plus'
import { getActiveCategoryTree, type FieldCategory } from '../../api/standard/fieldCategory'

// 通用树节点类型
interface TreeNode {
  id?: number
  categoryCode?: string
  categoryName?: string
  parentId?: number | null
  children?: TreeNode[]
  [key: string]: any
}

// Props
interface Props {
  modelValue?: number | null
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  showAll?: boolean
  treeData?: TreeNode[]  // 可选：传入自定义树数据
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: null,
  placeholder: '请选择分类',
  disabled: false,
  clearable: true,
  showAll: false,
  treeData: undefined
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: number | null]
  'change': [value: number | null, node: TreeNode | null]
}>()

// 数据
const apiTreeData = ref<TreeNode[]>([])
const loading = ref(false)
const treeRef = ref<InstanceType<typeof ElTree>>()
const filterText = ref('')
const dialogVisible = ref(false)

// 实际使用的树数据：优先使用传入的数据，否则使用API获取的数据
const treeData = computed(() => {
  if (props.treeData && props.treeData.length > 0) {
    return props.treeData
  }
  return apiTreeData.value
})

// 选中的值
const selectedValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 显示的文本
const displayText = computed(() => {
  if (!selectedValue.value) return props.placeholder
  const node = findNode(treeData.value, selectedValue.value as number)
  return node?.categoryName || props.placeholder
})

// 查找节点
function findNode(nodes: TreeNode[], id: number): TreeNode | null {
  if (!nodes || nodes.length === 0) return null
  for (const node of nodes) {
    if (node.id !== undefined && node.id === id) return node
    if (node.children && node.children.length > 0) {
      const found = findNode(node.children, id)
      if (found) return found
    }
  }
  return null
}

// 获取分类树数据
async function fetchTree() {
  // 如果传入了自定义树数据，则不从API获取
  if (props.treeData && props.treeData.length > 0) {
    return
  }

  loading.value = true
  try {
    const res = await getActiveCategoryTree()
    apiTreeData.value = res.data || []

    // 如果需要显示"全部"选项
    if (props.showAll) {
      apiTreeData.value = [
        {
          id: 0,
          categoryCode: 'ALL',
          categoryName: '全部',
          parentId: null,
          sort: -1,
          status: 'active',
          createdAt: '',
          updatedAt: '',
          children: apiTreeData.value
        }
      ]
    }
  } catch (error) {
    console.error('获取分类树失败', error)
  } finally {
    loading.value = false
  }
}

// 过滤节点
function filterNode(value: string, data: TreeNode): boolean {
  if (!value) return true
  return (data.categoryName?.includes(value) || false) || (data.categoryCode?.includes(value) || false)
}

// 监听搜索文本
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 节点点击
function handleNodeClick(data: TreeNode) {
  if (data.id === 0 && props.showAll) {
    selectedValue.value = null
    emit('change', null, null)
  } else if (data.id) {
    selectedValue.value = data.id
    emit('change', data.id, data)
  }
  dialogVisible.value = false
}

// 清空
function handleClear() {
  selectedValue.value = null
  emit('change', null, null)
}

// 打开选择器
function openDialog() {
  if (!props.disabled) {
    dialogVisible.value = true
  }
}

// 初始化
onMounted(() => {
  fetchTree()
})

// 暴露方法
defineExpose({
  refresh: fetchTree
})
</script>

<template>
  <div class="category-tree-select">
    <!-- 显示框 -->
    <div class="select-trigger" :class="{ disabled: disabled }" @click="openDialog">
      <span class="select-text">{{ displayText }}</span>
      <span v-if="clearable && selectedValue" class="clear-btn" @click.stop="handleClear">×</span>
      <span v-else class="arrow">▼</span>
    </div>

    <!-- 弹窗选择器 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择分类"
      width="500px"
      :close-on-click-modal="true"
      append-to-body
    >
      <!-- 搜索框 -->
      <div class="search-wrapper">
        <el-input
          v-model="filterText"
          placeholder="搜索分类名称/编码"
          clearable
        >
          <template #prefix>
            <span>🔍</span>
          </template>
        </el-input>
      </div>

      <!-- 树形结构 -->
      <div v-loading="loading" class="tree-wrapper">
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="{
            label: 'categoryName',
            children: 'children'
          }"
          :node-key="'id'"
          :default-expand-all="true"
          :highlight-current="true"
          :filter-node-method="filterNode"
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <span class="node-label">{{ data.categoryName }}</span>
              <span class="node-code">({{ data.categoryCode }})</span>
            </div>
          </template>
        </el-tree>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="clearable && selectedValue" type="danger" @click="handleClear">
          清空选择
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.category-tree-select {
  width: 100%;

  .select-trigger {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 36px;
    padding: 0 12px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #fff;
    cursor: pointer;
    transition: all 0.3s;

    &:hover:not(.disabled) {
      border-color: #c41a1a;
    }

    &.disabled {
      background: #f5f7fa;
      cursor: not-allowed;
      opacity: 0.7;
    }

    .select-text {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: #606266;
    }

    .clear-btn {
      font-size: 18px;
      color: #c0c4cc;

      &:hover {
        color: #909399;
      }
    }

    .arrow {
      font-size: 12px;
      color: #c0c4cc;
    }
  }
}

.search-wrapper {
  margin-bottom: 12px;
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

  .tree-node {
    display: flex;
    align-items: center;
    gap: 8px;

    .node-label {
      font-size: 14px;
      font-weight: 500;
    }

    .node-code {
      font-size: 12px;
      color: #909399;
    }
  }
}

// Element Plus Tree 样式覆盖
:deep(.el-tree) {
  background: transparent;

  .el-tree-node__content {
    height: 36px;

    &:hover {
      background-color: #f5f7fa;
    }
  }

  .el-tree-node.is-current > .el-tree-node__content {
    background-color: #fff5f5;
    color: #c41a1a;
    font-weight: 500;
  }
}
</style>
