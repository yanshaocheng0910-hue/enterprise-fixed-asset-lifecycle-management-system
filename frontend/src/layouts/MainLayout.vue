<template>
  <div class="app-container">
    <div class="sidebar">
      <div class="sidebar-logo">
        <h1>资产管理系统</h1>
      </div>
      <el-menu
        :default-active="currentRoute"
        :router="true"
        :collapse="false"
        text-color="#DDE7EF"
        active-text-color="#fff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>首页驾驶舱</span>
        </el-menu-item>
        <el-sub-menu index="asset">
          <template #title>
            <el-icon><Files /></el-icon>
            <span>资产管理</span>
          </template>
          <el-menu-item index="/assets">资产台账</el-menu-item>
          <el-menu-item index="/asset-categories">资产分类</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="lifecycle">
          <template #title>
            <el-icon><RefreshRight /></el-icon>
            <span>生命周期</span>
          </template>
          <el-menu-item index="/lifecycle/inbound">资产入库</el-menu-item>
          <el-menu-item index="/lifecycle/receive">资产领用</el-menu-item>
          <el-menu-item index="/lifecycle/transfer">资产调拨</el-menu-item>
          <el-menu-item index="/lifecycle/repair">维修管理</el-menu-item>
          <el-menu-item index="/lifecycle/scrap">报废管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/inventory/tasks">
          <el-icon><List /></el-icon>
          <span>盘点管理</span>
        </el-menu-item>
        <el-menu-item index="/depreciation/report">
          <el-icon><DataLine /></el-icon>
          <span>折旧报表</span>
        </el-menu-item>
        <el-menu-item index="/finance/sync">
          <el-icon><Connection /></el-icon>
          <span>财务对接</span>
        </el-menu-item>
        <el-menu-item index="/ai/analysis">
          <el-icon><Cpu /></el-icon>
          <span>AI 智能分析</span>
        </el-menu-item>
        <el-menu-item index="/system/users">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="main-area">
      <div class="top-bar">
        <div class="top-bar-left">
          <span class="breadcrumb">{{ pageTitle }}</span>
        </div>
        <div class="top-bar-right">
          <span class="user-name">{{ userInfo?.realName || userInfo?.username || '用户' }}</span>
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </div>
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const currentRoute = computed(() => route.path)
const pageTitle = computed(() => appStore.pageTitle)
const userInfo = computed(() => authStore.userInfo)

watch(() => route.meta.title, (val) => {
  if (val) appStore.setPageTitle(val as string)
}, { immediate: true })

onMounted(() => {
  if (!authStore.userInfo && authStore.isLoggedIn) {
    authStore.fetchCurrentUser()
  }
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
