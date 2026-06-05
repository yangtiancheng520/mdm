<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: boolean
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function handleConfirm() {
  visible.value = false
  emit('confirm')
}

function handleCancel() {
  visible.value = false
  emit('cancel')
}
</script>

<template>
  <div v-if="visible" class="mdm-confirm-overlay">
    <div class="mdm-confirm-dialog">
      <!-- 头部 -->
      <div class="mdm-confirm-head">
        <div class="mdm-confirm-title">{{ title || '提示' }}</div>
        <div class="mdm-confirm-close" @click="handleCancel">×</div>
      </div>
      <!-- 内容 -->
      <div class="mdm-confirm-body">
        <div class="mdm-confirm-icon">
          <svg viewBox="0 0 24 24" width="48" height="48">
            <path fill="#faad14" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
          </svg>
        </div>
        <div class="mdm-confirm-message">{{ message }}</div>
      </div>
      <!-- 底部按钮 -->
      <div class="mdm-confirm-footer">
        <button class="mdm-btn-cancel" @click="handleCancel">{{ cancelText || '取消' }}</button>
        <button class="mdm-btn-primary" @click="handleConfirm">{{ confirmText || '确定' }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mdm-confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

.mdm-confirm-dialog {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  min-width: 420px;
  max-width: 500px;
}

.mdm-confirm-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e5e8ee;
}

.mdm-confirm-title {
  font-size: 16px;
  font-weight: bold;
  color: #111111;
}

.mdm-confirm-close {
  font-size: 20px;
  color: #666;
  cursor: pointer;
  line-height: 1;
}

.mdm-confirm-close:hover {
  color: #ed2b33;
}

.mdm-confirm-body {
  padding: 24px 30px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.mdm-confirm-icon {
  flex-shrink: 0;
}

.mdm-confirm-message {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  flex: 1;
}

.mdm-confirm-footer {
  padding: 12px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #e5e8ee;
}

/* 按钮样式 */
.mdm-confirm-footer .mdm-btn-cancel {
  padding: 0 18px;
  border: 1px solid #d4d7dc;
  background: #f5f6f8;
  border-radius: 2px;
  cursor: pointer;
  font-size: 13px;
  color: #333;
  height: 28px;
  min-width: 60px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
}

.mdm-confirm-footer .mdm-btn-cancel:hover {
  opacity: 0.9;
  background: #ebedf0;
}

.mdm-confirm-footer .mdm-btn-primary {
  padding: 0 18px;
  background: #ed2b33;
  color: #fff;
  border: none;
  border-radius: 2px;
  cursor: pointer;
  font-size: 13px;
  height: 28px;
  min-width: 60px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s;
}

.mdm-confirm-footer .mdm-btn-primary:hover {
  opacity: 0.9;
  background: #c81e2c;
}
</style>
