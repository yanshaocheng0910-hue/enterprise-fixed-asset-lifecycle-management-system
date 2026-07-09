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
        <el-menu-item index="/dashboard" v-if="authStore.hasPermission('dashboard:view')">
          <el-icon><Monitor /></el-icon>
          <span>首页驾驶舱</span>
        </el-menu-item>
        <el-sub-menu index="asset" v-if="authStore.hasPermission('asset:view') || authStore.hasPermission('category:view')">
          <template #title>
            <el-icon><Files /></el-icon>
            <span>资产管理</span>
          </template>
          <el-menu-item index="/assets" v-if="authStore.hasPermission('asset:view')">资产台账</el-menu-item>
          <el-menu-item index="/asset-categories" v-if="authStore.hasPermission('category:view')">资产分类</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="lifecycle" v-if="authStore.hasPermission('inbound:create') || authStore.hasPermission('receive:create') || authStore.hasPermission('transfer:create') || authStore.hasPermission('repair:create') || authStore.hasPermission('scrap:create')">
          <template #title>
            <el-icon><RefreshRight /></el-icon>
            <span>生命周期</span>
          </template>
          <el-menu-item index="/lifecycle/inbound" v-if="authStore.hasPermission('inbound:create')">资产入库</el-menu-item>
          <el-menu-item index="/lifecycle/receive" v-if="authStore.hasPermission('receive:create')">资产领用</el-menu-item>
          <el-menu-item index="/lifecycle/transfer" v-if="authStore.hasPermission('transfer:create')">资产调拨</el-menu-item>
          <el-menu-item index="/lifecycle/repair" v-if="authStore.hasPermission('repair:create')">维修管理</el-menu-item>
          <el-menu-item index="/lifecycle/scrap" v-if="authStore.hasPermission('scrap:create')">报废管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/inventory/tasks" v-if="authStore.hasPermission('inventory:view')">
          <el-icon><List /></el-icon>
          <span>盘点管理</span>
        </el-menu-item>
        <el-menu-item index="/depreciation/report" v-if="authStore.hasPermission('depreciation:view')">
          <el-icon><DataLine /></el-icon>
          <span>折旧报表</span>
        </el-menu-item>
        <el-menu-item index="/finance/sync" v-if="authStore.hasPermission('finance:view')">
          <el-icon><Connection /></el-icon>
          <span>财务数据</span>
        </el-menu-item>
        <el-menu-item index="/ai/analysis" v-if="authStore.hasPermission('ai:view')">
          <el-icon><Cpu /></el-icon>
          <span>AI 辅助分析</span>
        </el-menu-item>
        <el-sub-menu index="system" v-if="authStore.hasPermission('user:view') || authStore.hasPermission('role:view')">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/system/users" v-if="authStore.hasPermission('user:view')">用户管理</el-menu-item>
          <el-menu-item index="/system/roles" v-if="authStore.hasPermission('role:view')">角色管理</el-menu-item>
        </el-sub-menu>
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
