import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export type DashboardRole =
  | 'SYSTEM_ADMIN'
  | 'ASSET_MANAGER'
  | 'DEPT_LEADER'
  | 'FINANCE'
  | 'AUDITOR'
  | 'OFFICE_STAFF'
  | 'INVENTORY_CLERK'

export interface QuickAction {
  title: string
  desc: string
  icon: string
  color: string
  to: string
  permission?: string
}

export function useRoleDashboard() {
  const authStore = useAuthStore()

  const primaryRole = computed<DashboardRole>(() => {
    const roles = authStore.userInfo?.roles || []
    if (roles.includes('ADMIN')) return 'SYSTEM_ADMIN'
    if (roles.includes('ASSET_MANAGER')) return 'ASSET_MANAGER'
    if (roles.includes('DEPT_LEADER')) return 'DEPT_LEADER'
    if (roles.includes('FINANCE')) return 'FINANCE'
    if (roles.includes('AUDITOR')) return 'AUDITOR'
    if (roles.includes('OFFICE_STAFF')) return 'OFFICE_STAFF'
    if (roles.includes('INVENTORY_CLERK')) return 'INVENTORY_CLERK'
    return 'SYSTEM_ADMIN'
  })

  const roleLabel = computed(() => {
    const map: Record<DashboardRole, string> = {
      SYSTEM_ADMIN: '系统管理员',
      ASSET_MANAGER: '资产管理员',
      DEPT_LEADER: '部门负责人',
      FINANCE: '财务人员',
      AUDITOR: '审计人员',
      OFFICE_STAFF: '普通员工',
      INVENTORY_CLERK: '盘点人员'
    }
    return map[primaryRole.value]
  })

  const quickActions = computed<QuickAction[]>(() => {
    const common: QuickAction[] = []
    const hasPerm = (p?: string) => !p || authStore.hasPermission(p)

    if (hasPerm('asset:view')) {
      common.push({ title: '资产台账', desc: '查看全部资产', icon: 'Files', color: '#1F4E79', to: '/assets', permission: 'asset:view' })
    }
    if (hasPerm('inventory:view')) {
      common.push({ title: '盘点任务', desc: '查看盘点进度', icon: 'List', color: '#18A058', to: '/inventory/tasks', permission: 'inventory:view' })
    }
    if (hasPerm('approval:todo')) {
      common.push({ title: '我的待办', desc: '处理审批事项', icon: 'Checked', color: '#F0A020', to: '/approval/todo', permission: 'approval:todo' })
    }
    if (hasPerm('depreciation:view')) {
      common.push({ title: '折旧报表', desc: '资产价值分析', icon: 'DataLine', color: '#2F6BFF', to: '/depreciation/report', permission: 'depreciation:view' })
    }
    if (hasPerm('receive:create')) {
      common.push({ title: '资产领用', desc: '提交领用申请', icon: 'RefreshRight', color: '#6B7280', to: '/lifecycle/receive', permission: 'receive:create' })
    }
    if (hasPerm('repair:create')) {
      common.push({ title: '维修申请', desc: '提交维修单', icon: 'Tools', color: '#D03050', to: '/lifecycle/repair', permission: 'repair:create' })
    }
    if (hasPerm('finance:view')) {
      common.push({ title: '财务同步', desc: '查看同步记录', icon: 'Connection', color: '#2F6BFF', to: '/finance/sync', permission: 'finance:view' })
    }
    if (hasPerm('approval:audit')) {
      common.push({ title: '审计追踪', desc: '操作日志查询', icon: 'Document', color: '#6B7280', to: '/audit/logs', permission: 'approval:audit' })
    }
    return common
  })

  const showStats = computed(() => primaryRole.value !== 'OFFICE_STAFF')
  const showStatusStats = computed(() => ['SYSTEM_ADMIN', 'ASSET_MANAGER'].includes(primaryRole.value))
  const showCharts = computed(() => ['SYSTEM_ADMIN', 'ASSET_MANAGER', 'FINANCE'].includes(primaryRole.value))
  const showApprovalSection = computed(() => ['DEPT_LEADER', 'OFFICE_STAFF'].includes(primaryRole.value))
  const showInventorySection = computed(() => ['INVENTORY_CLERK', 'ASSET_MANAGER', 'SYSTEM_ADMIN'].includes(primaryRole.value))
  const showFinanceSection = computed(() => primaryRole.value === 'FINANCE')
  const showAuditSection = computed(() => primaryRole.value === 'AUDITOR')
  const showQuickActions = computed(() => true)

  return {
    primaryRole,
    roleLabel,
    quickActions,
    showStats,
    showStatusStats,
    showCharts,
    showApprovalSection,
    showInventorySection,
    showFinanceSection,
    showAuditSection,
    showQuickActions
  }
}
