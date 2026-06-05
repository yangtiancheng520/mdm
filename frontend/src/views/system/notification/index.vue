<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ title }}</h2>
    </div>
    <div class="page-content">
      <el-empty description="功能开发中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
const title = '消息通知'
</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  min-height: calc(100vh - 140px);
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e8e8e8;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.page-content {
  padding: 40px 0;
}
</style>
