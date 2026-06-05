import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export interface TabItem {
  path: string
  name: string
  title: string
  icon?: string
}

export const useTabsStore = defineStore('tabs', () => {
  const tabs = ref<TabItem[]>([
    { path: '/home', name: 'Home', title: '首页', icon: 'HomeFilled' }
  ])
  const activeTab = ref('/home')

  function addTab(tab: TabItem) {
    const exists = tabs.value.find(t => t.path === tab.path)
    if (!exists) {
      tabs.value.push(tab)
    }
    activeTab.value = tab.path
  }

  function removeTab(path: string, router: ReturnType<typeof useRouter>) {
    // 首页不可关闭
    if (path === '/home') return

    const index = tabs.value.findIndex(t => t.path === path)
    if (index > -1) {
      tabs.value.splice(index, 1)
      if (activeTab.value === path) {
        const nextTab = tabs.value[index] || tabs.value[index - 1]
        if (nextTab) {
          activeTab.value = nextTab.path
          router.push(nextTab.path)
        }
      }
    }
  }

  function setActiveTab(path: string) {
    activeTab.value = path
  }

  function clearTabs() {
    tabs.value = [{ path: '/home', name: 'Home', title: '首页', icon: 'HomeFilled' }]
    activeTab.value = '/home'
  }

  function initTabs() {
    // 刷新页面时，只保留首页
    tabs.value = [{ path: '/home', name: 'Home', title: '首页', icon: 'HomeFilled' }]
    activeTab.value = '/home'
  }

  return {
    tabs,
    activeTab,
    addTab,
    removeTab,
    setActiveTab,
    clearTabs,
    initTabs
  }
})
