import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const pageTitle = ref('首页驾驶舱')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setPageTitle(title: string) {
    pageTitle.value = title
  }

  return { sidebarCollapsed, pageTitle, toggleSidebar, setPageTitle }
})
