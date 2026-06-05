<template>
  <div class="tinper-card">
    <div v-if="title" class="card-header">
      <h3 class="card-title">{{ title }}</h3>
      <div class="card-extra">
        <slot name="extra"></slot>
      </div>
    </div>
    <div class="card-body">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
}>()
</script>

<style scoped>
.tinper-card {
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  padding: 16px 24px;
  border-bottom: 1px solid #E4E7ED;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.card-extra {
  display: flex;
  gap: 8px;
}

.card-body {
  padding: 24px;
}
</style>
