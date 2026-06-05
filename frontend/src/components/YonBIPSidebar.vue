<template>
  <aside class="yonbip-sidebar">
    <!-- 搜索框 -->
    <div class="search-wrapper">
      <div class="search-box">
        <svg viewBox="0 0 24 24" width="16" height="16">
          <path fill="currentColor" d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <input
          type="text"
          placeholder="搜索菜单"
          v-model="searchKeyword"
        />
      </div>
    </div>

    <!-- 菜单容器 -->
    <div class="menu-container">
      <ul class="menu-list">
        <!-- 菜单项 -->
        <li
          v-for="menu in filteredMenus"
          :key="menu.path"
          class="menu-item"
          :class="{ 'has-children': menu.children }"
        >
          <!-- 有子菜单的父级 -->
          <div
            v-if="menu.children"
            class="menu-parent"
            :class="{ active: activeMenu === menu.path }"
            @click="toggleSubmenu(menu)"
          >
            <div class="parent-left">
              <i class="menu-icon">
                <svg viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" :d="getIconPath(menu.icon)"/>
                </svg>
              </i>
              <span>{{ menu.title }}</span>
            </div>
            <svg
              class="arrow-icon"
              :class="{ rotated: !menu.collapsed }"
              viewBox="0 0 24 24"
              width="12"
              height="12"
            >
              <path fill="currentColor" d="M7 10l5 5 5-5z"/>
            </svg>
          </div>

          <!-- 子菜单列表 -->
          <ul
            v-if="menu.children"
            class="submenu"
            :class="{ collapsed: menu.collapsed }"
          >
            <li
              v-for="child in menu.children"
              :key="child.path"
              class="sub-item"
              :class="{ active: activeMenu === child.path }"
              @click="selectMenu(child)"
            >
              <div class="sub-item-left">
                <span class="sub-dot"></span>
                <span>{{ child.title }}</span>
              </div>
            </li>
          </ul>

          <!-- 无子菜单的叶子节点 -->
          <div
            v-if="!menu.children"
            class="menu-leaf"
            :class="{ active: activeMenu === menu.path }"
            @click="selectMenu(menu)"
          >
            <div class="leaf-left">
              <i class="menu-icon">
                <svg viewBox="0 0 24 24" width="20" height="20">
                  <path fill="currentColor" :d="getIconPath(menu.icon)"/>
                </svg>
              </i>
              <span>{{ menu.title }}</span>
            </div>
          </div>
        </li>
      </ul>
    </div>

    <!-- 底部信息 -->
    <div class="sidebar-footer">
      <svg viewBox="0 0 24 24" width="12" height="12">
        <path fill="currentColor" d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/>
      </svg>
      <span>MDM 平台 · v1.0</span>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface MenuItem {
  path: string
  title: string
  icon: string
  children?: MenuItem[]
  collapsed?: boolean
}

const props = defineProps<{
  menus: MenuItem[]
  activeMenu: string
}>()

const emit = defineEmits(['select'])

const searchKeyword = ref('')

// 过滤菜单
const filteredMenus = computed(() => {
  if (!searchKeyword.value) return props.menus

  return props.menus.filter(menu => {
    const matchTitle = menu.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
    const matchChildren = menu.children?.some(child =>
      child.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
    return matchTitle || matchChildren
  })
})

// 获取图标路径
function getIconPath(icon: string) {
  const iconMap: Record<string, string> = {
    'home': 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z',
    'user': 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
    'role': 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.98 4-3.08 6-3.08 1.99 0 5.97 1.1 6 3.08-1.29 1.94-3.5 3.22-6 3.22z',
    'permission': 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z',
    'database': 'M12 3C7.58 3 4 4.79 4 7v10c0 2.21 3.58 4 8 4s8-1.79 8-4V7c0-2.21-3.58-4-8-4zm0 2c3.87 0 6 1.5 6 2s-2.13 2-6 2-6-1.5-6-2 2.13-2 6-2zm6 12c0 .5-2.13 2-6 2s-6-1.5-6-2v-2.23c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23V17zm0-5c0 .5-2.13 2-6 2s-6-1.5-6-2V9.77c1.61.78 3.72 1.23 6 1.23s4.39-.45 6-1.23V12z',
    'star': 'M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z',
    'building': 'M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z'
  }
  return iconMap[icon] || iconMap['home']
})

// 切换子菜单
function toggleSubmenu(menu: MenuItem) {
  menu.collapsed = !menu.collapsed
}

// 选择菜单
function selectMenu(menu: MenuItem) {
  emit('select', menu)
}
</script>

<style scoped>
.yonbip-sidebar {
  width: 280px;
  background-color: #1c2940;
  color: #e0e6f0;
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
  background: #2a3a55;
}

.yonbip-sidebar::-webkit-scrollbar-thumb {
  background: #4c6a8f;
  border-radius: 6px;
}

/* 搜索框区域 */
.search-wrapper {
  padding: 20px 16px 16px 20px;
  border-bottom: 1px solid #2c3a55;
  margin-bottom: 12px;
}

.search-box {
  background: #25334f;
  border-radius: 28px;
  padding: 10px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s;
  border: 1px solid #3a4868;
  color: #90a0c2;
}

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: #f0f4fa;
  font-size: 14px;
  width: 100%;
  letter-spacing: 0.3px;
}

.search-box input::placeholder {
  color: #7a89aa;
  font-weight: 400;
}

.search-box:focus-within {
  border-color: #5d7bb2;
  background: #2a3857;
}

/* 菜单容器 */
.menu-container {
  flex: 1;
  padding: 8px 12px 20px 12px;
}

.menu-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.menu-item {
  border-radius: 12px;
  margin-bottom: 2px;
}

/* 一级菜单（父级） */
.menu-parent,
.menu-leaf {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 12px;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.2s;
  color: #e2e8f2;
  font-weight: 500;
  font-size: 15px;
}

.menu-parent:hover,
.menu-leaf:hover {
  background: #2a3a58;
}

.parent-left,
.leaf-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-icon {
  width: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9aaeca;
}

.arrow-icon {
  color: #8c9fc0;
  transition: transform 0.2s;
}

.arrow-icon.rotated {
  transform: rotate(180deg);
}

/* 子菜单列表 */
.submenu {
  list-style: none;
  padding-left: 44px;
  overflow: hidden;
  transition: max-height 0.3s ease-out;
  max-height: 500px;
  margin: 0;
}

.submenu.collapsed {
  max-height: 0;
  display: none;
}

/* 子菜单项 */
.sub-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin: 2px 0;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: #cbd5e6;
  transition: all 0.2s;
}

.sub-item-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sub-dot {
  width: 6px;
  height: 6px;
  background-color: #6f84af;
  border-radius: 50%;
  display: inline-block;
}

.sub-item:hover {
  background: #2e3f5c;
  color: #ffffff;
}

/* 激活状态 */
.menu-parent.active,
.menu-leaf.active,
.sub-item.active {
  background: #2f4170;
  color: white;
}

.sub-item.active .sub-dot {
  background-color: #60a5fa;
}

/* 底部信息 */
.sidebar-footer {
  padding: 20px 16px 24px;
  font-size: 11px;
  color: #7f8daa;
  border-top: 1px solid #2c3a55;
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
