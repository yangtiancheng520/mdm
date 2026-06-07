<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getViewDetail,
  createView,
  updateView,
  type ViewDefinition
} from '@/api/standard/viewDefinition'
import {
  getViewCategoryTree,
  type ViewCategory
} from '@/api/standard/viewCategory'

const route = useRoute()
const router = useRouter()

const viewId = computed(() => route.params.id ? Number(route.params.id) : null)
const isEdit = computed(() => !!viewId.value)

const viewData = ref<ViewDefinition>({
  viewCode: '',
  viewName: '',
  categoryId: undefined,
  description: ''
})

const loading = ref(false)
const categoryTreeData = ref<ViewCategory[]>([])

// 加载分类树
const loadCategoryTree = async () => {
  try {
    const res = await getViewCategoryTree()
    if (res.code === 200) {
      categoryTreeData.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 加载视图数据
const loadViewData = async () => {
  if (!viewId.value) return
  loading.value = true
  try {
    const res = await getViewDetail(viewId.value)
    if (res.code === 200) {
      viewData.value = {
        viewCode: res.data.viewCode,
        viewName: res.data.viewName,
        categoryId: res.data.categoryId,
        description: res.data.description
      }
    }
  } catch (error) {
    console.error('加载视图失败', error)
  } finally {
    loading.value = false
  }
}

// 保存
const handleSave = async () => {
  if (!viewData.value.viewCode) return ElMessage.warning('请输入视图编码')
  if (!viewData.value.viewName) return ElMessage.warning('请输入视图名称')
  if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(viewData.value.viewCode)) {
    return ElMessage.warning('视图编码只能包含字母、数字、下划线，且必须以字母开头')
  }

  const currentUser = localStorage.getItem('username') || 'admin'
  viewData.value.createdBy = currentUser
  viewData.value.updatedBy = currentUser

  loading.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateView(viewId.value!, viewData.value)
    } else {
      res = await createView(viewData.value)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      router.push('/standard/view')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    loading.value = false
  }
}

const handleBack = () => router.push('/standard/view')

onMounted(() => {
  loadCategoryTree()
  loadViewData()
})
</script>

<template>
  <div class="edit-page">
    <!-- 顶部栏 -->
    <div class="header">
      <button class="btn-back" @click="handleBack">← 返回</button>
      <span class="title">{{ isEdit ? '编辑视图' : '新建视图' }}</span>
      <button class="btn-save" :disabled="loading" @click="handleSave">{{ loading ? '保存中...' : '保存' }}</button>
    </div>

    <!-- 内容区 -->
    <div class="content" v-if="!loading || isEdit">
      <!-- 视图信息 -->
      <div class="section">
        <div class="section-title">基本信息</div>
        <div class="form-grid">
          <div class="form-item">
            <label><span class="req">*</span>视图编码</label>
            <input v-model="viewData.viewCode" :disabled="isEdit" placeholder="如：supplier" />
          </div>
          <div class="form-item">
            <label><span class="req">*</span>视图名称</label>
            <input v-model="viewData.viewName" placeholder="如：供应商视图" />
          </div>
          <div class="form-item">
            <label>所属分类</label>
            <select v-model="viewData.categoryId">
              <option :value="undefined">请选择分类</option>
              <option v-for="cat in categoryTreeData" :key="cat.id" :value="cat.id">
                {{ cat.categoryName }}
              </option>
            </select>
          </div>
          <div class="form-item full">
            <label>描述说明</label>
            <textarea v-model="viewData.description" placeholder="请输入描述" rows="3"></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.edit-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 60px);
  background: #f0f2f5;
}

.header {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  gap: 12px;

  .btn-back {
    padding: 6px 12px;
    background: #fff;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    font-size: 13px;
    &:hover { border-color: #ed2b33; color: #ed2b33; }
  }

  .title {
    flex: 1;
    font-size: 15px;
    font-weight: 600;
    color: #333;
  }

  .btn-save {
    padding: 6px 20px;
    background: #ed2b33;
    border: none;
    border-radius: 4px;
    color: #fff;
    cursor: pointer;
    font-size: 13px;
    &:hover:not(:disabled) { background: #c81e2c; }
    &:disabled { opacity: 0.6; cursor: not-allowed; }
  }
}

.content {
  flex: 1;
  padding: 12px;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.section {
  background: #fff;
  border-radius: 4px;
  overflow: hidden;
}

.section-title {
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 14px;
}

.form-item {
  display: flex;
  align-items: center;
  gap: 8px;

  &.full {
    grid-column: 1 / -1;
    flex-direction: column;
    align-items: stretch;
  }

  label {
    width: 80px;
    flex-shrink: 0;
    font-size: 13px;
    color: #333;

    .req { color: #f56c6c; margin-right: 2px; }
  }

  input, select, textarea {
    flex: 1;
    padding: 6px 10px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    font-size: 13px;
    &:focus { outline: none; border-color: #ed2b33; }
    &:disabled { background: #f5f5f5; color: #999; }
    &::placeholder { color: #bbb; }
  }

  select {
    background: #fff;
    cursor: pointer;
  }

  textarea {
    resize: vertical;
    min-height: 60px;
  }
}
</style>
