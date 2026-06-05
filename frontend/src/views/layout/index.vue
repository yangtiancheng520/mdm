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
  <div class="layout-container">
    <!-- 左侧菜单 -->
    <div class="sidebar" :class="{ collapse: isCollapse }">
      <div class="logo">
        <span v-if="!isCollapse">后台管理系统</span>
        <span v-else>Admin</span>
      </div>
      <el-menu
        :default-active="tabsStore.activeTab"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleMenuSelect"
      >
        <el-menu-item v-for="menu in menuList" :key="menu.path" :index="menu.path">
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.title }}</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧内容区 -->
    <div class="main-container">
      <!-- 头部 -->
      <div class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleLogout">
            <span class="user-info">
              <el-avatar :size="30" :src="userStore.userInfo?.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.name }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- Tab 栏 -->
      <div class="tabs-container">
        <el-tabs
          v-model="tabsStore.activeTab"
          type="card"
          @tab-click="handleTabClick"
          @tab-remove="handleTabRemove"
        >
          <el-tab-pane
            v-for="tab in tabsStore.tabs"
            :key="tab.path"
            :label="tab.title"
            :name="tab.path"
            :closable="tab.path !== '/home'"
          >
            <template #label>
              <span>
                <el-icon style="margin-right: 5px"><component :is="tab.icon" /></el-icon>
                {{ tab.title }}
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 内容区 -->
      <div class="content">
        <RouterView v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" :key="route.path" />
            </keep-alive>
          </transition>
        </RouterView>
      </div>
    </div>
  </div>
</template>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
}

.sidebar {
  width: 210px;
  background-color: #304156;
  transition: width 0.3s;
}

.sidebar.collapse {
  width: 64px;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.el-menu {
  border-right: none;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-name {
  color: #333;
}

.tabs-container {
  padding: 5px 10px 0;
  background: #fff;
}

.tabs-container .el-tabs {
  --el-tabs-header-height: 35px;
}

.content {
  flex: 1;
  padding: 20px;
  background: #f0f2f5;
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>