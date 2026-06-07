<template>
  <div class="tree-node-wrapper">
    <!-- 节点行 -->
    <div
      class="tree-node"
      :class="{ active: isSelected }"
      :style="{ paddingLeft: indent + 'px' }"
    >
      <!-- 展开图标 -->
      <span
        class="expand-icon"
        :class="{ expanded: isExpanded }"
        @click.stop="$emit('toggle', node.id)"
      >
        <span v-if="hasChildren">▶</span>
        <span v-else class="empty-icon"></span>
      </span>
      <!-- 节点图标 -->
      <span class="node-icon" @click="$emit('select', node)">📂</span>
      <!-- 节点名称 -->
      <span class="node-label" :title="node.categoryName" @click="$emit('select', node)">{{ node.categoryName }}</span>
      <!-- 计数 -->
      <span class="node-count" v-if="node.viewCount">{{ node.viewCount }}</span>
      <!-- 操作按钮 -->
      <div class="node-actions">
        <button class="node-btn" title="新增子分类" @click.stop="$emit('add', node)">+</button>
        <button class="node-btn" title="编辑" @click.stop="$emit('edit', node)">✎</button>
        <button class="node-btn delete" title="删除" @click.stop="$emit('delete', node)">×</button>
      </div>
    </div>

    <!-- 子节点 -->
    <div v-if="hasChildren && isExpanded" class="tree-children">
      <TreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :selected-id="selectedId"
        :expanded-ids="expandedIds"
        :level="level + 1"
        @select="$emit('select', $event)"
        @add="$emit('add', $event)"
        @edit="$emit('edit', $event)"
        @delete="$emit('delete', $event)"
        @toggle="$emit('toggle', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ViewCategory } from '@/api/standard/viewCategory'

const props = defineProps<{
  node: ViewCategory
  selectedId: number | null
  expandedIds: number[]
  level: number
}>()

defineEmits<{
  select: [node: ViewCategory]
  add: [node: ViewCategory]
  edit: [node: ViewCategory]
  delete: [node: ViewCategory]
  toggle: [id: number]
}>()

const hasChildren = computed(() => props.node.children && props.node.children.length > 0)
const isExpanded = computed(() => props.expandedIds.includes(props.node.id!))
const isSelected = computed(() => props.selectedId === props.node.id)
const indent = computed(() => props.level * 16 + 10)
</script>

<style scoped lang="scss">
.tree-node-wrapper {
  // 递归组件包装器
}

.tree-node {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 2px;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: #fef9f5;

    .node-actions {
      display: flex;
    }
  }

  &.active {
    background: #fef9f5;
    border-left: 3px solid #ed2b33;

    .node-label {
      color: #ed2b33;
      font-weight: 500;
    }
  }

  // 展开图标
  .expand-icon {
    width: 16px;
    height: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #999;
    margin-right: 4px;
    cursor: pointer;
    transition: transform 0.2s;
    flex-shrink: 0;

    &.expanded {
      transform: rotate(90deg);
    }

    .empty-icon {
      width: 16px;
      height: 16px;
    }
  }

  // 节点图标
  .node-icon {
    font-size: 14px;
    margin-right: 6px;
  }

  .node-label {
    font-size: 13px;
    flex: 1;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    min-width: 0;
  }

  .node-count {
    font-size: 11px;
    color: #fff;
    background: #ed2b33;
    padding: 1px 6px;
    border-radius: 10px;
    margin-right: 6px;
    min-width: 18px;
    text-align: center;
  }

  .node-actions {
    display: none;
    gap: 2px;

    .node-btn {
      width: 18px;
      height: 18px;
      font-size: 10px;
      background: #fff;
      border: 1px solid #d4d7dc;
      border-radius: 3px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #666;
      transition: all 0.2s;

      &:hover {
        border-color: #ed2b33;
        color: #ed2b33;
      }

      &.delete:hover {
        border-color: #ed2b33;
        background: #ed2b33;
        color: #fff;
      }
    }
  }
}

// 子节点容器
.tree-children {
  // 递归子节点容器
}
</style>
