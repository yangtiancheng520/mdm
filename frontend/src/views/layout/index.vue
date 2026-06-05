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

const isCollapse = ref(false)

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
          <div class="logo-icon">M</div>
          <span class="logo-text">MDM 管理系统</span>
        </div>
      </div>

      <div class="header-right">
        <div class="header-actions">
          <!-- 通知图标 -->
          <div class="action-item">
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path fill="currentColor" d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
            </svg>
            <span class="badge">3</span>
          </div>

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
      <!-- 左侧菜单 -->
      <aside class="tinper-sidebar" :class="{ collapse: isCollapse }">
        <div class="sidebar-toggle" @click="isCollapse = !isCollapse">
          <svg viewBox="0 0 24 24" width="16" height="16">
            <path v-if="!isCollapse" fill="currentColor" d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
            <path v-else fill="currentColor" d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
          </svg>
        </div>

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
            <span v-if="!isCollapse" class="menu-title">{{ menu.title }}</span>
          </div>
        </nav>
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

/* 顶部导航栏 */
.tinper-header {
  height: 56px;
  background: linear-gradient(135deg, #1E88E5 0%, #1565C0 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  color: white;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  backdrop-filter: blur(10px);
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
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

.action-item {
  position: relative;
  color: white;
  cursor: pointer;
  opacity: 0.9;
  transition: opacity 0.3s;
}

.action-item:hover {
  opacity: 1;
}

.badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  background: #F5222D;
  border-radius: 9px;
  font-size: 11px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-dropdown:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.3);
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
  font-size: 14px;
  font-weight: 500;
}

.user-role {
  font-size: 12px;
  opacity: 0.8;
}

.dropdown-icon {
  opacity: 0.7;
}

/* 主体区域 */
.tinper-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧菜单 */
.tinper-sidebar {
  width: 220px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  position: relative;
}

.tinper-sidebar.collapse {
  width: 64px;
}

.sidebar-toggle {
  position: absolute;
  top: 12px;
  right: -12px;
  width: 24px;
  height: 24px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  color: #606266;
  transition: all 0.3s;
}

.sidebar-toggle:hover {
  background: #1E88E5;
  color: white;
}

.sidebar-menu {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  margin: 4px 0;
}

.menu-item:hover {
  background: rgba(30, 136, 229, 0.05);
  color: #1E88E5;
}

.menu-item.active {
  background: rgba(30, 136, 229, 0.1);
  color: #1E88E5;
  border-left-color: #1E88E5;
  font-weight: 500;
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
}

.menu-title {
  font-size: 14px;
  white-space: nowrap;
}

.tinper-sidebar.collapse .menu-item {
  padding: 12px;
  justify-content: center;
}

/* 内容区域 */
.tinper-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 标签页导航 */
.tabs-wrapper {
  background: white;
  border-bottom: 1px solid #E4E7ED;
  padding: 8px 16px 0;
}

.tabs-container {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #F5F7FA;
  border-radius: 4px 4px 0 0;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  border: 1px solid #E4E7ED;
  border-bottom: none;
}

.tab-item:hover {
  background: white;
  color: #1E88E5;
}

.tab-item.active {
  background: white;
  color: #1E88E5;
  font-weight: 500;
}

.tab-close {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  opacity: 0.6;
  transition: all 0.3s;
}

.tab-close:hover {
  background: #F5222D;
  color: white;
  opacity: 1;
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
