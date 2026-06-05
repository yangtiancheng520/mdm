<template>
  <div class="tinper-table-wrapper">
    <div class="table-toolbar">
      <div class="toolbar-left">
        <slot name="toolbar-left"></slot>
      </div>
      <div class="toolbar-right">
        <slot name="toolbar-right"></slot>
      </div>
    </div>

    <div class="table-container">
      <table class="tinper-table">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col.key" :style="{ width: col.width }">
              {{ col.title }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in data" :key="index">
            <td v-for="col in columns" :key="col.key">
              <slot v-if="col.slot" :name="col.slot" :row="row" :index="index">
                {{ row[col.key] }}
              </slot>
              <span v-else>{{ row[col.key] }}</span>
            </td>
          </tr>
          <tr v-if="data.length === 0">
            <td :colspan="columns.length" class="empty-text">
              暂无数据
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showPagination" class="table-pagination">
      <span class="pagination-info">共 {{ total }} 条</span>
      <div class="pagination-buttons">
        <button
          class="pagination-btn"
          :disabled="currentPage === 1"
          @click="handlePageChange(currentPage - 1)"
        >
          上一页
        </button>
        <span class="pagination-current">{{ currentPage }} / {{ totalPages }}</span>
        <button
          class="pagination-btn"
          :disabled="currentPage === totalPages"
          @click="handlePageChange(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Column {
  key: string
  title: string
  width?: string
  slot?: string
}

const props = defineProps<{
  columns: Column[]
  data: any[]
  showPagination?: boolean
  total?: number
  pageSize?: number
  currentPage?: number
}>()

const emit = defineEmits(['page-change'])

const totalPages = computed(() => {
  return Math.ceil((props.total || 0) / (props.pageSize || 10))
})

function handlePageChange(page: number) {
  emit('page-change', page)
}
</script>

<style scoped>
.tinper-table-wrapper {
  background: white;
  border-radius: 4px;
  overflow: hidden;
}

.table-toolbar {
  padding: 16px;
  border-bottom: 1px solid #E4E7ED;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FAFBFC;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 8px;
}

.table-container {
  overflow-x: auto;
}

.tinper-table {
  width: 100%;
  border-collapse: collapse;
}

.tinper-table thead {
  background: #FAFBFC;
}

.tinper-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #E4E7ED;
  white-space: nowrap;
}

.tinper-table td {
  padding: 12px 16px;
  font-size: 14px;
  color: #606266;
  border-bottom: 1px solid #EBEEF5;
}

.tinper-table tbody tr:hover {
  background: #F5F7FA;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 40px 16px;
}

.table-pagination {
  padding: 16px;
  border-top: 1px solid #E4E7ED;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FAFBFC;
}

.pagination-info {
  font-size: 14px;
  color: #606266;
}

.pagination-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-btn {
  padding: 6px 12px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  background: white;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  color: #1E88E5;
  border-color: #1E88E5;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-current {
  font-size: 14px;
  color: #606266;
}
</style>
