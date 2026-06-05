<script setup lang="ts">
/**
 * DataTable 数据表格组件
 *
 * 用于列表页面的数据展示，支持分页、排序、多选等功能
 *
 * @example
 * <DataTable
 *   :columns="[
 *     { key: 'name', title: '名称', width: 150 },
 *     { key: 'status', title: '状态', width: 100, render: (val) => val === 'active' ? '启用' : '禁用' }
 *   ]"
 *   :data="tableData"
 *   :loading="loading"
 *   :pagination="{ page: 1, pageSize: 10, total: 100 }"
 *   @page-change="handlePageChange"
 *   @selection-change="handleSelectionChange"
 * />
 */

import { ref, computed } from 'vue'

export interface TableColumn {
  key: string
  title: string
  width?: number | string
  minWidth?: number
  align?: 'left' | 'center' | 'right'
  fixed?: 'left' | 'right'
  sortable?: boolean
  render?: (value: any, row: any, index: number) => string | any
  slot?: string // 自定义插槽名称
  children?: TableColumn[] // 表头分组
}

export interface Pagination {
  page: number
  pageSize: number
  total: number
  pageSizes?: number[]
}

interface Props {
  columns: TableColumn[]
  data: any[]
  loading?: boolean
  pagination?: Pagination | false
  selection?: boolean
  rowKey?: string
  emptyText?: string
  stripe?: boolean
  border?: boolean
  height?: number | string
  maxHeight?: number | string
}

interface Emits {
  (e: 'page-change', page: number): void
  (e: 'size-change', size: number): void
  (e: 'selection-change', rows: any[]): void
  (e: 'sort-change', sort: { key: string; order: 'asc' | 'desc' }): void
  (e: 'row-click', row: any, index: number): void
  (e: 'row-dblclick', row: any, index: number): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  selection: false,
  rowKey: 'id',
  emptyText: '暂无数据',
  stripe: true,
  border: false,
  pagination: () => ({
    page: 1,
    pageSize: 10,
    total: 0,
    pageSizes: [10, 20, 50, 100]
  })
})

const emit = defineEmits<Emits>()

// 选中的行
const selectedRows = ref<any[]>([])

// 全选状态
const isAllSelected = computed(() => {
  return props.data.length > 0 && selectedRows.value.length === props.data.length
})

// 是否半选
const isIndeterminate = computed(() => {
  return selectedRows.value.length > 0 && selectedRows.value.length < props.data.length
})

// 切换全选
function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedRows.value = []
  } else {
    selectedRows.value = [...props.data]
  }
  emit('selection-change', selectedRows.value)
}

// 切换单行选择
function toggleRowSelect(row: any) {
  const index = selectedRows.value.findIndex(r => r[props.rowKey] === row[props.rowKey])
  if (index > -1) {
    selectedRows.value.splice(index, 1)
  } else {
    selectedRows.value.push(row)
  }
  emit('selection-change', selectedRows.value)
}

// 判断行是否选中
function isRowSelected(row: any): boolean {
  return selectedRows.value.some(r => r[props.rowKey] === row[props.rowKey])
}

// 分页变化
function handlePageChange(page: number) {
  emit('page-change', page)
}

// 每页条数变化
function handleSizeChange(event: Event) {
  const size = parseInt((event.target as HTMLSelectElement).value)
  emit('size-change', size)
}

// 计算总页数
const totalPages = computed(() => {
  if (!props.pagination) return 0
  return Math.ceil(props.pagination.total / props.pagination.pageSize)
})

// 渲染单元格内容
function renderCell(column: TableColumn, row: any, index: number): any {
  const value = row[column.key]
  if (column.render) {
    return column.render(value, row, index)
  }
  return value ?? '-'
}

// 获取列样式
function getColumnStyle(column: TableColumn): Record<string, any> {
  const style: Record<string, any> = {}
  if (column.width) {
    style.width = typeof column.width === 'number' ? `${column.width}px` : column.width
    style.minWidth = style.width
  }
  if (column.minWidth) {
    style.minWidth = `${column.minWidth}px`
  }
  if (column.align) {
    style.textAlign = column.align
  }
  return style
}

// 暴露方法
defineExpose({
  clearSelection: () => {
    selectedRows.value = []
  },
  getSelectedRows: () => selectedRows.value
})
</script>

<template>
  <div class="data-table-wrapper">
    <!-- 表格 -->
    <div class="data-table-container" :class="{ 'is-loading': loading }">
      <table class="data-table" :class="{ 'is-striped': stripe, 'is-bordered': border }">
        <!-- 表头 -->
        <thead>
          <tr>
            <!-- 选择列 -->
            <th v-if="selection" class="data-table-checkbox" style="width: 40px">
              <label class="checkbox-wrapper">
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  :indeterminate="isIndeterminate"
                  @change="toggleSelectAll"
                />
                <span class="checkbox-indicator"></span>
              </label>
            </th>

            <!-- 数据列 -->
            <th
              v-for="col in columns"
              :key="col.key"
              :style="getColumnStyle(col)"
              :class="{ 'is-sortable': col.sortable }"
            >
              <slot :name="`header-${col.key}`" :column="col">
                {{ col.title }}
              </slot>
            </th>

            <!-- 操作列 -->
            <th v-if="$slots.action" class="data-table-action" style="width: 120px">
              <slot name="header-action">操作</slot>
            </th>
          </tr>
        </thead>

        <!-- 表体 -->
        <tbody v-loading="loading">
          <tr
            v-for="(row, index) in data"
            :key="rowKey ? row[rowKey] : index"
            :class="{ 'is-selected': isRowSelected(row) }"
            @click="emit('row-click', row, index)"
            @dblclick="emit('row-dblclick', row, index)"
          >
            <!-- 选择列 -->
            <td v-if="selection" class="data-table-checkbox">
              <label class="checkbox-wrapper">
                <input
                  type="checkbox"
                  :checked="isRowSelected(row)"
                  @change="toggleRowSelect(row)"
                />
                <span class="checkbox-indicator"></span>
              </label>
            </td>

            <!-- 数据列 -->
            <td
              v-for="col in columns"
              :key="col.key"
              :style="getColumnStyle(col)"
            >
              <slot
                v-if="col.slot"
                :name="col.slot"
                :row="row"
                :value="row[col.key]"
                :index="index"
              >
                {{ row[col.key] }}
              </slot>
              <template v-else>
                {{ renderCell(col, row, index) }}
              </template>
            </td>

            <!-- 操作列 -->
            <td v-if="$slots.action" class="data-table-action">
              <slot name="action" :row="row" :index="index"></slot>
            </td>
          </tr>

          <!-- 空数据 -->
          <tr v-if="data.length === 0 && !loading">
            <td :colspan="(selection ? 1 : 0) + columns.length + ($slots.action ? 1 : 0)" class="data-table-empty">
              <slot name="empty">
                <div class="empty-content">
                  <span class="empty-icon">📭</span>
                  <span class="empty-text">{{ emptyText }}</span>
                </div>
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div v-if="pagination && pagination.total > 0" class="data-table-pagination">
      <span class="pagination-total">共 {{ pagination.total }} 条</span>

      <div class="pagination-pages">
        <button
          class="page-btn"
          :disabled="pagination.page <= 1"
          @click="handlePageChange(1)"
          title="首页"
        >
          ««
        </button>
        <button
          class="page-btn"
          :disabled="pagination.page <= 1"
          @click="handlePageChange(pagination.page - 1)"
          title="上一页"
        >
          «
        </button>

        <!-- 页码 -->
        <template v-for="p in totalPages" :key="p">
          <button
            v-if="Math.abs(p - pagination.page) <= 2 || p === 1 || p === totalPages"
            class="page-btn"
            :class="{ 'is-active': p === pagination.page }"
            @click="handlePageChange(p)"
          >
            {{ p }}
          </button>
          <span v-else-if="p === 2 || p === totalPages - 1" class="page-ellipsis">...</span>
        </template>

        <button
          class="page-btn"
          :disabled="pagination.page >= totalPages"
          @click="handlePageChange(pagination.page + 1)"
          title="下一页"
        >
          »
        </button>
        <button
          class="page-btn"
          :disabled="pagination.page >= totalPages"
          @click="handlePageChange(totalPages)"
          title="末页"
        >
          »»
        </button>
      </div>

      <select
        class="page-size-select"
        :value="pagination.pageSize"
        @change="handleSizeChange"
      >
        <option v-for="size in (pagination.pageSizes || [10, 20, 50, 100])" :key="size" :value="size">
          {{ size }}条/页
        </option>
      </select>
    </div>
  </div>
</template>

<style scoped>
.data-table-wrapper {
  background: #fff;
  border-radius: 4px;
}

.data-table-container {
  overflow-x: auto;
}

.data-table-container.is-loading {
  opacity: 0.7;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table.is-striped tbody tr:nth-child(even) {
  background: #fafafa;
}

.data-table.is-bordered {
  border: 1px solid #eee;
}

.data-table.is-bordered th,
.data-table.is-bordered td {
  border: 1px solid #eee;
}

.data-table th {
  background: #f5f7fa;
  padding: 12px 16px;
  text-align: left;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  border-bottom: 1px solid #eee;
}

.data-table th.is-sortable {
  cursor: pointer;
  user-select: none;
}

.data-table th.is-sortable:hover {
  background: #eef;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  color: #666;
}

.data-table tbody tr:hover {
  background: #f5f7fa;
}

.data-table tbody tr.is-selected {
  background: #fff7f7;
}

.data-table-checkbox {
  text-align: center;
}

.data-table-action {
  text-align: center;
}

.data-table-empty {
  padding: 40px 16px;
  text-align: center;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
}

.empty-text {
  color: #999;
  font-size: 14px;
}

/* 复选框样式 */
.checkbox-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.checkbox-wrapper input {
  display: none;
}

.checkbox-indicator {
  width: 16px;
  height: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 2px;
  position: relative;
  transition: all 0.2s;
}

.checkbox-wrapper input:checked + .checkbox-indicator {
  background: #c41a1a;
  border-color: #c41a1a;
}

.checkbox-wrapper input:checked + .checkbox-indicator::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 4px;
  height: 8px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-wrapper input:indeterminate + .checkbox-indicator {
  background: #c41a1a;
  border-color: #c41a1a;
}

.checkbox-wrapper input:indeterminate + .checkbox-indicator::after {
  content: '';
  position: absolute;
  left: 3px;
  top: 6px;
  width: 8px;
  height: 2px;
  background: #fff;
}

/* 分页样式 */
.data-table-pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #eee;
}

.pagination-total {
  color: #666;
  font-size: 14px;
}

.pagination-pages {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #c41a1a;
  color: #c41a1a;
}

.page-btn:disabled {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.page-btn.is-active {
  background: #c41a1a;
  border-color: #c41a1a;
  color: #fff;
}

.page-ellipsis {
  color: #999;
  padding: 0 4px;
}

.page-size-select {
  height: 32px;
  padding: 0 24px 0 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}
</style>
