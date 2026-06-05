<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../store/user'
import { useTabsStore } from '../../store/tabs'
import MdmConfirmDialog from '../../components/MdmConfirmDialog.vue'
import type { Menu } from '../../api/menu'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const tabsStore = useTabsStore()

// 确认对话框
const confirmVisible = ref(false)

// 展开的菜单ID列表
const expandedMenus = ref<Set<number>>(new Set())

// 菜单列表 - 从后端动态获取
const menuList = computed(() => {
  const menus = userStore.menus || []

  // 只显示一级菜单（parentId 为 null 或 0）
  return menus.filter(menu => !menu.parentId || menu.parentId === 0)
})

// 获取菜单图标组件
function getMenuIcon(icon: string) {
  return icon || 'Document'
}

// 检查菜单是否有子菜单
function hasChildren(menu: Menu): boolean {
  return menu.children && menu.children.length > 0
}

// 检查菜单是否展开
function isExpanded(menu: Menu): boolean {
  return expandedMenus.value.has(menu.id)
}

// 切换菜单展开状态
function toggleMenu(menu: Menu) {
  if (hasChildren(menu)) {
    if (expandedMenus.value.has(menu.id)) {
      expandedMenus.value.delete(menu.id)
    } else {
      expandedMenus.value.add(menu.id)
    }
  } else {
    // 如果没有子菜单，直接跳转
    handleMenuSelect(menu)
  }
}

// 点击子菜单
function handleSubmenuClick(submenu: Menu, event?: Event) {
  // 阻止事件冒泡
  if (event) {
    event.stopPropagation()
  }

  // 添加Tab
  tabsStore.addTab({
    path: submenu.path,
    name: submenu.code,
    title: submenu.name,
    icon: submenu.icon
  })

  // 跳转到页面
  router.push(submenu.path)
}

// 点击菜单（跳转到页面）
function handleMenuSelect(menu: Menu) {
  // 添加Tab
  tabsStore.addTab({
    path: menu.path,
    name: menu.code,
    title: menu.name,
    icon: menu.icon
  })
  router.push(menu.path)
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
  confirmVisible.value = true
}

// 确认退出
function handleConfirmLogout() {
  tabsStore.clearTabs()
  userStore.logout()
  ElMessage.success('退出成功')
  router.push('/login')
}

// 初始化
onMounted(async () => {
  tabsStore.initTabs()

  // 如果菜单为空，从后端获取
  if (userStore.menus.length === 0 && userStore.isLogin) {
    await userStore.fetchMenus()
  }

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
            :key="menu.id"
            class="menu-wrapper"
          >
            <!-- 一级菜单 -->
            <div
              class="menu-item"
              :class="{
                active: tabsStore.activeTab === menu.path,
                expanded: isExpanded(menu),
                'has-children': hasChildren(menu)
              }"
              @click="toggleMenu(menu)"
            >
              <div class="menu-icon">
                <!-- 动态图标渲染 -->
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
                <svg v-else-if="menu.icon === 'OfficeBuilding'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z"/>
                </svg>
                <svg v-else-if="menu.icon === 'Document'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
                </svg>
                <svg v-else-if="menu.icon === 'Edit'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
                <svg v-else-if="menu.icon === 'Connection'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"/>
                </svg>
                <svg v-else-if="menu.icon === 'DataAnalysis'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
                </svg>
                <svg v-else-if="menu.icon === 'CircleCheck'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
                <svg v-else-if="menu.icon === 'Share'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92s2.92-1.31 2.92-2.92-1.31-2.92-2.92-2.92z"/>
                </svg>
                <svg v-else-if="menu.icon === 'Clock'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
                </svg>
                <svg v-else-if="menu.icon === 'Setting'" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/>
                </svg>
                <!-- 默认图标 -->
                <svg v-else viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
                </svg>
              </div>
              <span class="menu-title" :title="menu.name">{{ menu.name }}</span>
              <!-- 展开/折叠图标 -->
              <svg
                v-if="hasChildren(menu)"
                class="expand-icon"
                :class="{ rotated: isExpanded(menu) }"
                viewBox="0 0 24 24"
                width="16"
                height="16"
              >
                <path fill="currentColor" d="M7 10l5 5 5-5z"/>
              </svg>
            </div>

            <!-- 二级菜单 -->
            <div
              v-if="hasChildren(menu) && isExpanded(menu)"
              class="submenu"
            >
              <div
                v-for="submenu in menu.children"
                :key="submenu.id"
                class="submenu-item"
                :class="{ active: tabsStore.activeTab === submenu.path }"
                @click.stop="handleSubmenuClick(submenu, $event)"
              >
                <span class="submenu-dot"></span>
                <span class="submenu-title" :title="submenu.name">{{ submenu.name }}</span>
              </div>
            </div>
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

  <!-- 确认退出对话框 -->
  <MdmConfirmDialog
    v-model="confirmVisible"
    message="确定要退出登录吗？"
    @confirm="handleConfirmLogout"
  />
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
  width: 220px;
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
  font-weight: 400;
  font-size: 13px;
  letter-spacing: 0.3px;
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
  min-width: 19px;
  color: #fff;
  flex-shrink: 0;
}

.menu-icon svg {
  width: 19px;
  height: 19px;
}

.menu-title {
  font-size: 13px;
  font-weight: 400;
  white-space: nowrap;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

/* 展开/折叠图标 */
.expand-icon {
  transition: transform 0.3s ease;
  margin-left: auto;
  flex-shrink: 0;
  min-width: 16px;
  color: #9fb3c9;
  opacity: 0.8;
}

.menu-item:hover .expand-icon {
  opacity: 1;
  color: #fff;
}

.menu-item.expanded .expand-icon {
  color: #fff;
  opacity: 1;
}

.expand-icon.rotated {
  transform: rotate(180deg);
}

/* 菜单容器 */
.menu-wrapper {
  margin-bottom: 2px;
}

/* 二级菜单 */
.submenu {
  margin-left: 20px;
  padding-left: 10px;
  border-left: 2px solid #2a3d5c;
  margin-top: 4px;
  margin-bottom: 4px;
}

.submenu-item {
  padding: 8px 12px;
  color: #9fb3c9;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 6px;
  margin: 2px 0;
  font-size: 12px;
  font-weight: 400;
  display: flex;
  align-items: center;
  letter-spacing: 0.2px;
}

.submenu-item:hover {
  background: #1d2d47;
  color: #d4e1ed;
}

.submenu-item.active {
  background: #2a3d5c;
  color: #fff;
  font-weight: 500;
}

.submenu-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

/* 子菜单小圆点 */
.submenu-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: currentColor;
  margin-right: 8px;
  flex-shrink: 0;
  opacity: 0.6;
}

.submenu-item:hover .submenu-dot {
  opacity: 1;
}

.submenu-item.active .submenu-dot {
  opacity: 1;
  background: #fff;
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
  gap: 10px;
  padding: 0 12px;
  height: 28px;
  border-radius: 0;
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
  margin-left: 0;
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
  padding: 8px;
  overflow-y: auto;
  background: #becee7;
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
