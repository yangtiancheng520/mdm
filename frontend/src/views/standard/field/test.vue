<script setup lang="ts">
import { ref, onMounted } from 'vue'

const message = ref('字段标准库测试页面')
const data = ref<any[]>([])

onMounted(async () => {
  console.log('测试页面已加载')
  try {
    const response = await fetch('/api/field-standard/list')
    const result = await response.json()
    console.log('直接fetch结果:', result)
    data.value = result.data?.list || []
    console.log('数据:', data.value)
  } catch (error) {
    console.error('错误:', error)
  }
})
</script>

<template>
  <div style="padding: 20px;">
    <h1>{{ message }}</h1>
    <div>数据条数: {{ data.length }}</div>
    <div v-if="data.length > 0">
      <div v-for="item in data" :key="item.id" style="margin: 10px 0; padding: 10px; border: 1px solid #ccc;">
        {{ item.fieldCode }} - {{ item.fieldName }}
      </div>
    </div>
    <div v-else style="color: red;">
      没有数据
    </div>
  </div>
</template>
