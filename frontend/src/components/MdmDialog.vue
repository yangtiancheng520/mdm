<script setup lang="ts">
import { ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  modelValue: boolean
  title: string
  width?: string
  showFooter?: boolean
  confirmText?: string
  cancelText?: string
}>(), {
  width: '600px',
  showFooter: true,
  confirmText: '确定',
  cancelText: '取消'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'cancel'): void
  (e: 'confirm'): void
}>()

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function close() {
  visible.value = false
  emit('cancel')
}

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  close()
}
</script>

<template>
  <div v-if="visible" class="mdm-dialog-overlay">
    <div class="mdm-dialog-wrap" :style="{ width: width }">
      <!-- 弹窗标题栏 -->
      <div class="mdm-dialog-head">
        <div class="mdm-dialog-title">{{ title }}</div>
        <div class="mdm-dialog-close" @click="close">×</div>
      </div>
      <!-- 表单内容 -->
      <div class="mdm-dialog-body">
        <slot></slot>
      </div>
      <!-- 底部按钮 -->
      <div class="mdm-dialog-footer" v-if="showFooter && !$slots.footer">
        <el-button @click="handleCancel">{{ cancelText }}</el-button>
        <el-button type="primary" @click="handleConfirm">{{ confirmText }}</el-button>
      </div>
      <!-- 自定义底部 -->
      <div class="mdm-dialog-footer" v-if="$slots.footer">
        <slot name="footer"></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mdm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.mdm-dialog-wrap {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.mdm-dialog-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #e5e8ee;
}

.mdm-dialog-title {
  font-size: 16px;
  font-weight: bold;
  color: #111111;
}

.mdm-dialog-close {
  font-size: 20px;
  color: #666;
  cursor: pointer;
  line-height: 1;
}

.mdm-dialog-close:hover {
  color: #ed2b33;
}

.mdm-dialog-body {
  padding: 20px 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.mdm-dialog-footer {
  padding: 12px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #e5e8ee;
}
</style>
