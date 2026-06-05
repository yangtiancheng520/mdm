<script setup lang="ts">
/**
 * PageContainer 页面容器组件
 *
 * 提供页面统一布局结构，包含标题、操作栏、内容区域
 *
 * @example
 * <PageContainer title="用户管理">
 *   <template #extra>
 *     <button class="btn-primary">新增</button>
 *   </template>
 *   <DataTable ... />
 * </PageContainer>
 */

interface Props {
  title?: string
  subtitle?: string
  loading?: boolean
  padding?: boolean
  shadow?: boolean
  border?: boolean
}

interface Emits {
  (e: 'refresh'): void
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  subtitle: '',
  loading: false,
  padding: true,
  shadow: false,
  border: true
})

const emit = defineEmits<Emits>()

function handleRefresh() {
  emit('refresh')
}
</script>

<template>
  <div
    class="page-container"
    :class="{
      'is-shadow': shadow,
      'is-border': border
    }"
    v-loading="loading"
  >
    <!-- 标题栏 -->
    <div v-if="title || $slots.header || $slots.extra" class="page-header">
      <div class="page-header-left">
        <slot name="header">
          <h3 class="page-title">{{ title }}</h3>
          <span v-if="subtitle" class="page-subtitle">{{ subtitle }}</span>
        </slot>
      </div>
      <div class="page-header-right">
        <slot name="extra"></slot>
      </div>
    </div>

    <!-- 工具栏 -->
    <div v-if="$slots.toolbar" class="page-toolbar">
      <slot name="toolbar"></slot>
    </div>

    <!-- 内容区域 -->
    <div class="page-content" :class="{ 'is-padding': padding }">
      <slot></slot>
    </div>

    <!-- 底部 -->
    <div v-if="$slots.footer" class="page-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  background: #fff;
  border-radius: 4px;
}

.page-container.is-border {
  border: 1px solid #eee;
}

.page-container.is-shadow {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.page-header-left {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.page-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.page-subtitle {
  font-size: 13px;
  color: #999;
}

.page-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-toolbar {
  padding: 12px 20px;
  border-bottom: 1px solid #eee;
  background: #fafafa;
}

.page-content {
  min-height: 200px;
}

.page-content.is-padding {
  padding: 20px;
}

.page-footer {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  text-align: right;
}
</style>
