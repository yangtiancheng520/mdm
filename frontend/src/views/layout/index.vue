<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../store/user'
import { useTabsStore } from '../../store/tabs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const tabsStore = useTabsStore()

// 菜单列表
const menuList = computed(() => {
  const allMenus = [
    { path: '/home', name: 'Home', title: '首页', icon: 'HomeFilled' },
    { path: '/user', name: 'User', title: '用户管理', icon: 'User', permission: 'user:view' },
    { path: '/role', name: 'Role', title: '角色管理', icon: 'UserFilled', permission: 'role:view' },
    { path: '/permission', name: 'Permission', title: '权限管理', icon: 'Lock', permission: 'permission:view' }
  ]

  // 根据权限过滤菜单
  return allMenus.filter(menu => {
    if (!menu.permission) return true
    return userStore.hasPermission(menu.permission)
  })
})

// 点击菜单
function handleMenuSelect(path: string) {
  const menu = menuList.value.find(m => m.path === path)
  if (menu) {
    tabsStore.addTab({
      path: menu.path,
      name: menu.name,
      title: menu.title,
      icon: menu.icon
    })
    router.push(path)
  }
}

// 点击 Tab
function handleTabClick(tab: any) {
  tabsStore.setActiveTab(tab.props.name)
  router.push(tab.props.name)
}

// 关闭 Tab
function handleTabRemove(path: string) {
  tabsStore.removeTab(path, router)
}

// 退出登录
function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning'
  }).then(() => {
    tabsStore.clearTabs()
    userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  tabsStore.initTabs()
  // 如果当前不在首页，跳转到首页
  if (route.path !== '/home' && route.path !== '/') {
    router.replace('/home')
  }
})
</script>

<template>
  <div class="tinper-layout">
    <!-- 顶部导航栏 -->
    <header class="tinper-header">
      <div class="header-left">
        <div class="logo">
          <div class="logo-icon">
            <!-- MDM 品牌图标 - 数据节点与M形架构 -->
            <svg viewBox="0 0 60 60" width="36" height="36" fill="none">
              <!-- 数据节点 -->
              <circle cx="18" cy="20" r="4" fill="white"/>
              <circle cx="42" cy="20" r="4" fill="white"/>
              <circle cx="30" cy="42" r="5" fill="white"/>
              <!-- 连接线形成M形架构 -->
              <path d="M18 20 L30 42 L42 20" stroke="white" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
              <!-- 中心数据汇聚横线 -->
              <line x1="24" y1="32" x2="36" y2="32" stroke="white" stroke-width="2" stroke-dasharray="3 2"/>
              <!-- 外扩数据节点 -->
              <circle cx="12" cy="34" r="2.5" fill="white" fill-opacity="0.8"/>
              <circle cx="48" cy="34" r="2.5" fill="white" fill-opacity="0.8"/>
            </svg>
          </div>
          <div class="logo-text">
            <div class="brand-name"><span>MDM</span> 主数据管理平台</div>
          </div>
        </div>
      </div>

      <div class="header-right">
        <div class="header-actions">
          <!-- 用户信息 -->
          <div class="user-dropdown" @click="handleLogout">
            <div class="user-avatar">
              <img :src="userStore.userInfo?.avatar" alt="avatar" />
            </div>
            <div class="user-info">
              <span class="user-name">{{ userStore.userInfo?.name }}</span>
              <span class="user-role">{{ userStore.userInfo?.roles?.[0] || '用户' }}</span>
            </div>
            <svg viewBox="0 0 24 24" width="16" height="16" class="dropdown-icon">
              <path fill="currentColor" d="M7 10l5 5 5-5z"/>
            </svg>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体区域 -->
    <div class="tinper-main">
      <!-- 左侧菜单 - YonBIP深色风格 -->
      <aside class="yonbip-sidebar">
        <!-- 菜单容器 -->
        <nav class="sidebar-menu">
          <div
            v-for="menu in menuList"
            :key="menu.path"
            class="menu-item"
            :class="{ active: tabsStore.activeTab === menu.path }"
            @click="handleMenuSelect(menu.path)"
          >
            <div class="menu-icon">
              <svg v-if="menu.icon === 'HomeFilled'" viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
              </svg>
              <svg v-else-if="menu.icon === 'User'" viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
              <svg v-else-if="menu.icon === 'UserFilled'" viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.98 4-3.08 6-3.08 1.99 0 5.97 1.1 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
              </svg>
              <svg v-else-if="menu.icon === 'Lock'" viewBox="0 0 24 24" width="20" height="20">
                <path fill="currentColor" d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
              </svg>
            </div>
            <span class="menu-title">{{ menu.title }}</span>
          </div>
        </nav>

        <!-- 底部信息 -->
        <div class="sidebar-footer">
          <svg viewBox="0 0 24 24" width="12" height="12">
            <path fill="currentColor" d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/>
          </svg>
          <span>MDM 平台 · v1.0</span>
        </div>
      </aside>

      <!-- 内容区域 -->
      <main class="tinper-content">
        <!-- 标签页导航 -->
        <div class="tabs-wrapper">
          <div class="tabs-container">
            <div
              v-for="tab in tabsStore.tabs"
              :key="tab.path"
              class="tab-item"
              :class="{ active: tabsStore.activeTab === tab.path }"
              @click="handleTabClick({ props: { name: tab.path } })"
            >
              <span class="tab-title">{{ tab.title }}</span>
              <span
                v-if="tab.path !== '/home'"
                class="tab-close"
                @click.stop="handleTabRemove(tab.path)"
              >
                ×
              </span>
            </div>
          </div>
        </div>

        <!-- 页面内容 -->
        <div class="page-content">
          <RouterView v-slot="{ Component }">
            <transition name="slide-fade" mode="out-in">
              <keep-alive>
                <component :is="Component" :key="route.path" />
              </keep-alive>
            </transition>
          </RouterView>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.tinper-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F5F7FA;
}

/* 顶部导航栏 - 浅蓝色背景红色主题 */
.tinper-header {
  height: 50px;
  background: #f0f5fc;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #e2e9f5;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 34px;
  height: 34px;
  background: linear-gradient(145deg, #e31b23, #b01018);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(227, 27, 35, 0.25);
  transition: all 0.3s;
}

.logo-icon:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(227, 27, 35, 0.35);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

.brand-name span {
  color: #e31b23;
}


.header-right {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #4a5b6e;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 40px;
  transition: all 0.2s;
}

.user-dropdown:hover {
  background: #f0f4f9;
  color: #dd2222;
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  overflow: hidden;
  background: #ffe0d9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c52a2a;
  font-weight: bold;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #2c3e66;
}

.user-role {
  font-size: 11px;
  color: #8192aa;
}

.dropdown-icon {
  color: #8192aa;
}

/* 主体区域 */
.tinper-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧菜单 - 深色风格 */
.yonbip-sidebar {
  width: 180px;
  background-color: #162439;
  color: #e0e0e0;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 12px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  transition: all 0.2s;
}

/* 自定义滚动条 */
.yonbip-sidebar::-webkit-scrollbar {
  width: 5px;
}

.yonbip-sidebar::-webkit-scrollbar-track {
  background: #1d2d47;
}

.yonbip-sidebar::-webkit-scrollbar-thumb {
  background: #3a5070;
  border-radius: 6px;
}

/* 菜单容器 */
.sidebar-menu {
  flex: 1;
  padding: 16px 10px 16px 10px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 10px;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 10px;
  margin: 2px 0;
  font-weight: 500;
  font-size: 12px;
}

.menu-item:hover {
  background: #1d2d47;
  color: #fff;
}

.menu-item.active {
  background: #2a3d5c;
  color: white;
}

.menu-item.active .menu-icon {
  color: white;
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  color: #fff;
}

.menu-icon svg {
  width: 18px;
  height: 18px;
}

.menu-title {
  font-size: 12px;
  white-space: nowrap;
}

/* 底部信息 */
.sidebar-footer {
  padding: 16px 12px 20px;
  font-size: 11px;
  color: #6a7a95;
  border-top: 1px solid #1d2d47;
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 内容区域 */
.tinper-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 标签页导航 - 小圆角红色主题 */
.tabs-wrapper {
  background: white;
  border-bottom: 1px solid #e9edf2;
  padding: 0 16px;
}

.tabs-container {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  height: 34px;
  align-items: center;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  height: 28px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: #5b6e8c;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  background: #f8fafc;
}

.tab-item:hover {
  background: #eef2f8;
  color: #dd2222;
}

.tab-item.active {
  background: #ffece5;
  color: #dd2222;
}

.tab-close {
  margin-left: 4px;
  font-size: 14px;
  cursor: pointer;
  opacity: 0.6;
  color: #99aac6;
}

.tab-close:hover {
  opacity: 1;
  color: #dd2222;
}

/* 页面内容 */
.page-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #F5F7FA;
}

/* 过渡动画 */
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from {
  transform: translateX(10px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(-10px);
  opacity: 0;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #F5F7FA;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #DCDFE6;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #C0C4CC;
}
</style>
