import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/token'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    noAuth?: boolean
    permission?: string
    title?: string
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录', noAuth: true }
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/system/Forbidden.vue'),
      meta: { noAuth: true }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '首页驾驶舱', permission: 'dashboard:view' }
        },
        {
          path: 'assets',
          name: 'AssetList',
          component: () => import('@/views/asset/AssetList.vue'),
          meta: { title: '资产台账', permission: 'asset:view' }
        },
        {
          path: 'assets/:id',
          name: 'AssetDetail',
          component: () => import('@/views/asset/AssetDetail.vue'),
          meta: { title: '资产详情', permission: 'asset:view' }
        },
        {
          path: 'asset-categories',
          name: 'AssetCategory',
          component: () => import('@/views/asset/AssetCategory.vue'),
          meta: { title: '资产分类', permission: 'category:view' }
        },
        {
          path: 'lifecycle/inbound',
          name: 'Inbound',
          component: () => import('@/views/lifecycle/Inbound.vue'),
          meta: { title: '资产入库', permission: 'inbound:create' }
        },
        {
          path: 'lifecycle/receive',
          name: 'Receive',
          component: () => import('@/views/lifecycle/Receive.vue'),
          meta: { title: '资产领用', permission: 'receive:create' }
        },
        {
          path: 'lifecycle/transfer',
          name: 'Transfer',
          component: () => import('@/views/lifecycle/Transfer.vue'),
          meta: { title: '资产调拨', permission: 'transfer:create' }
        },
        {
          path: 'lifecycle/repair',
          name: 'Repair',
          component: () => import('@/views/lifecycle/Repair.vue'),
          meta: { title: '维修管理', permission: 'repair:create' }
        },
        {
          path: 'lifecycle/scrap',
          name: 'Scrap',
          component: () => import('@/views/lifecycle/Scrap.vue'),
          meta: { title: '报废管理', permission: 'scrap:create' }
        },
        {
          path: 'inventory/tasks',
          name: 'InventoryTask',
          component: () => import('@/views/inventory/InventoryTask.vue'),
          meta: { title: '盘点任务', permission: 'inventory:view' }
        },
        {
          path: 'depreciation/report',
          name: 'DepreciationReport',
          component: () => import('@/views/depreciation/DepreciationReport.vue'),
          meta: { title: '折旧报表', permission: 'depreciation:view' }
        },
        {
          path: 'finance/sync',
          name: 'FinanceSync',
          component: () => import('@/views/finance/FinanceSync.vue'),
          meta: { title: '财务同步', permission: 'finance:view' }
        },
        {
          path: 'ai/analysis',
          name: 'AiAnalysis',
          component: () => import('@/views/ai/AiAnalysis.vue'),
          meta: { title: 'AI 智能分析', permission: 'ai:view' }
        },
        {
          path: 'system/users',
          name: 'UserManage',
          component: () => import('@/views/system/UserManage.vue'),
          meta: { title: '用户管理', permission: 'user:view' }
        },
        {
          path: 'system/roles',
          name: 'RoleManage',
          component: () => import('@/views/system/RoleManage.vue'),
          meta: { title: '角色管理', permission: 'role:view' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const token = getToken()
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }
  if (to.meta.noAuth) {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }

  const authStore = useAuthStore()
  if (authStore.permissions.length === 0) {
    await authStore.fetchCurrentUser()
    if (!authStore.isLoggedIn) {
      next('/login')
      return
    }
  }

  if (to.meta.permission) {
    if (!authStore.hasPermission(to.meta.permission as string)) {
      next('/403')
      return
    }
  }
  next()
})

export default router
