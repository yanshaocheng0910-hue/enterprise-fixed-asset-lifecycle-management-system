import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/token'

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
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '首页驾驶舱' }
        },
        {
          path: 'assets',
          name: 'AssetList',
          component: () => import('@/views/asset/AssetList.vue'),
          meta: { title: '资产台账' }
        },
        {
          path: 'assets/:id',
          name: 'AssetDetail',
          component: () => import('@/views/asset/AssetDetail.vue'),
          meta: { title: '资产详情' }
        },
        {
          path: 'asset-categories',
          name: 'AssetCategory',
          component: () => import('@/views/asset/AssetCategory.vue'),
          meta: { title: '资产分类' }
        },
        {
          path: 'lifecycle/inbound',
          name: 'Inbound',
          component: () => import('@/views/lifecycle/Inbound.vue'),
          meta: { title: '资产入库' }
        },
        {
          path: 'lifecycle/receive',
          name: 'Receive',
          component: () => import('@/views/lifecycle/Receive.vue'),
          meta: { title: '资产领用' }
        },
        {
          path: 'lifecycle/transfer',
          name: 'Transfer',
          component: () => import('@/views/lifecycle/Transfer.vue'),
          meta: { title: '资产调拨' }
        },
        {
          path: 'lifecycle/repair',
          name: 'Repair',
          component: () => import('@/views/lifecycle/Repair.vue'),
          meta: { title: '维修管理' }
        },
        {
          path: 'lifecycle/scrap',
          name: 'Scrap',
          component: () => import('@/views/lifecycle/Scrap.vue'),
          meta: { title: '报废管理' }
        },
        {
          path: 'inventory/tasks',
          name: 'InventoryTask',
          component: () => import('@/views/inventory/InventoryTask.vue'),
          meta: { title: '盘点任务' }
        },
        {
          path: 'depreciation/report',
          name: 'DepreciationReport',
          component: () => import('@/views/depreciation/DepreciationReport.vue'),
          meta: { title: '折旧报表' }
        },
        {
          path: 'finance/sync',
          name: 'FinanceSync',
          component: () => import('@/views/finance/FinanceSync.vue'),
          meta: { title: '财务同步' }
        },
        {
          path: 'ai/analysis',
          name: 'AiAnalysis',
          component: () => import('@/views/ai/AiAnalysis.vue'),
          meta: { title: 'AI 智能分析' }
        },
        {
          path: 'system/users',
          name: 'UserManage',
          component: () => import('@/views/system/UserManage.vue'),
          meta: { title: '用户管理' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (to.path === '/login' && token) {
    next('/dashboard')
  } else if (to.meta.noAuth) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
