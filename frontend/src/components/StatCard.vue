<template>
  <div class="tinper-stat-card" :style="{ background: bgColor }">
    <div class="stat-icon" :style="{ background: iconBgColor }">
      <slot name="icon">
        <svg viewBox="0 0 24 24" width="24" height="24">
          <path fill="currentColor" :d="iconPath"/>
        </svg>
      </slot>
    </div>
    <div class="stat-content">
      <div class="stat-value">{{ value }}</div>
      <div class="stat-label">{{ label }}</div>
    </div>
    <div v-if="trend" class="stat-trend" :class="trend > 0 ? 'up' : 'down'">
      <svg viewBox="0 0 24 24" width="16" height="16">
        <path v-if="trend > 0" fill="currentColor" d="M7 14l5-5 5 5z"/>
        <path v-else fill="currentColor" d="M7 10l5 5 5-5z"/>
      </svg>
      <span>{{ Math.abs(trend) }}%</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  value: string | number
  label: string
  iconPath?: string
  bgColor?: string
  iconBgColor?: string
  trend?: number
}>()
</script>

<style scoped>
.tinper-stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.tinper-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-trend {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
}

.stat-trend.up {
  color: #52C41A;
}

.stat-trend.down {
  color: #F5222D;
}
</style>
