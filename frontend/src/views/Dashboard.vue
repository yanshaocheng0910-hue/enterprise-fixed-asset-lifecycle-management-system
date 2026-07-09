<template>
  <div class="dashboard">
    <!-- 角色标题 -->
    <div class="role-header">
      <h2 class="role-title">{{ roleLabel }}工作台</h2>
      <span class="role-sub">{{ userInfo?.department || '' }} · {{ userInfo?.realName || userInfo?.username || '' }}</span>
    </div>

    <!-- 核心统计卡片 -->
    <div v-if="showStats" class="stats-row">
      <DataCard label="资产总数" :value="stats.assetCount ?? '-'" sub="件" />
      <DataCard label="资产总原值" :value="formatWan(stats.totalOriginalValue)" type="amount" />
      <DataCard label="累计折旧" :value="formatWan(stats.totalAccumulatedDepreciation)" type="amount" />
      <DataCard label="资产净值" :value="formatWan(stats.totalNetValue)" type="amount" />
    </div>

    <!-- 状态统计（管理员/资产管理员） -->
    <div v-if="showStatusStats" class="stats-row" style="margin-top: 12px;">
      <DataCard label="使用中" :value="stats.inUseCount ?? 0" sub="件" color="#4F8F7B" />
      <DataCard label="闲置" :value="stats.idleCount ?? 0" sub="件" color="#8B9DC3" />
      <DataCard label="维修中" :value="stats.repairingCount ?? 0" sub="件" color="#D97706" />
      <DataCard label="待报废" :value="stats.waitingScrapCount ?? 0" sub="件" color="#EA580C" />
    </div>

    <!-- 图表区（管理员/资产管理员/财务） -->
    <div v-if="showCharts" class="charts-row">
      <div class="chart-card">
        <h3 class="chart-title">资产分类分布</h3>
        <v-chart v-if="categoryChartData.length" :option="categoryChartOption" class="chart" autoresize />
        <div v-else class="chart-empty">暂无数据</div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">部门资产金额排行</h3>
        <v-chart v-if="deptChartData.length" :option="deptChartOption" class="chart" autoresize />
        <div v-else class="chart-empty">暂无数据</div>
      </div>
      <div class="chart-card">
        <h3 class="chart-title">月度折旧趋势（近12个月）</h3>
        <v-chart v-if="trendData.length" :option="trendChartOption" class="chart" autoresize />
        <div v-else class="chart-empty">暂无数据</div>
      </div>
    </div>

    <!-- 两列布局区 -->
    <div class="dual-row">
      <!-- 我的待办（部门负责人/普通员工） -->
      <div v-if="showApprovalSection" class="section-card">
        <div class="section-header">
          <h3 class="section-title">我的待办</h3>
          <el-button text type="primary" @click="$router.push('/approval/todo')">查看全部</el-button>
        </div>
        <el-table :data="approvalTodoList" size="small" border style="width: 100%" v-loading="approvalLoading">
          <el-table-column prop="flowName" label="事项" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.flowName || bizTypeText(row.businessType) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="businessType" label="类型" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="bizTypeTag(row.businessType)">{{ bizTypeText(row.businessType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applicantName" label="申请人" width="80" />
          <el-table-column prop="startedAt" label="发起时间" width="120">
            <template #default="{ row }">{{ formatDate(row.startedAt) }}</template>
          </el-table-column>
        </el-table>
        <div v-if="!approvalLoading && approvalTodoList.length === 0" class="empty-hint">暂无待办事项</div>
      </div>

      <!-- 盘点任务（盘点人员/资产管理员） -->
      <div v-if="showInventorySection" class="section-card">
        <div class="section-header">
          <h3 class="section-title">盘点任务</h3>
          <el-button text type="primary" @click="$router.push('/inventory/tasks')">查看全部</el-button>
        </div>
        <el-table :data="inventoryTasks" size="small" border style="width: 100%" v-loading="inventoryLoading">
          <el-table-column prop="taskName" label="任务名称" min-width="140" show-overflow-tooltip />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'COMPLETED' ? 'success' : 'warning'">{{ row.status === 'COMPLETED' ? '已完成' : '进行中' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="80">
            <template #default="{ row }">{{ row.completedRecords }}/{{ row.totalRecords }}</template>
          </el-table-column>
        </el-table>
        <div v-if="!inventoryLoading && inventoryTasks.length === 0" class="empty-hint">暂无盘点任务</div>
      </div>

      <!-- 财务摘要（财务人员） -->
      <div v-if="showFinanceSection" class="section-card">
        <div class="section-header">
          <h3 class="section-title">财务概览</h3>
          <el-button text type="primary" @click="$router.push('/finance/sync')">查看同步记录</el-button>
        </div>
        <div class="finance-stats">
          <div class="fin-item"><span class="fin-label">本月折旧</span><span class="fin-value">¥{{ formatMoney(financeDepAmount) }}</span></div>
          <div class="fin-item"><span class="fin-label">资产原值</span><span class="fin-value">¥{{ formatMoney(stats.totalOriginalValue) }}</span></div>
          <div class="fin-item"><span class="fin-label">累计折旧</span><span class="fin-value">¥{{ formatMoney(stats.totalAccumulatedDepreciation) }}</span></div>
          <div class="fin-item"><span class="fin-label">资产净值</span><span class="fin-value">¥{{ formatMoney(stats.totalNetValue) }}</span></div>
        </div>
        <div class="fin-sync-list">
          <div v-for="r in financeSyncList.slice(0, 5)" :key="r.id" class="fin-sync-item">
            <el-tag size="small" :type="r.status === 'SUCCESS' ? 'success' : 'danger'">{{ r.status === 'SUCCESS' ? '成功' : '失败' }}</el-tag>
            <span class="sync-batch">{{ r.syncBatchNo }}</span>
            <span class="sync-time">{{ formatDate(r.syncTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 审计概览（审计人员） -->
      <div v-if="showAuditSection" class="section-card">
        <div class="section-header">
          <h3 class="section-title">审计概览</h3>
          <el-button text type="primary" @click="$router.push('/audit/logs')">查看全部</el-button>
        </div>
        <div class="audit-stats">
          <div class="audit-item"><span class="audit-label">今日操作</span><span class="audit-value">{{ auditSummary.todayOperationCount || 0 }}</span></div>
          <div class="audit-item"><span class="audit-label">资产变更</span><span class="audit-value">{{ auditSummary.assetChangeCount || 0 }}</span></div>
          <div class="audit-item"><span class="audit-label">审批操作</span><span class="audit-value">{{ auditSummary.approvalOperationCount || 0 }}</span></div>
          <div class="audit-item"><span class="audit-label">盘点异常</span><span class="audit-value">{{ auditSummary.inventoryAbnormalCount || 0 }}</span></div>
        </div>
        <el-table :data="auditList" size="small" border style="width: 100%; margin-top: 10px" v-loading="auditLoading">
          <el-table-column prop="logTypeName" label="类型" width="70" />
          <el-table-column prop="operation" label="操作" min-width="100" show-overflow-tooltip />
          <el-table-column prop="operatorName" label="操作人" width="80" />
          <el-table-column prop="operationTime" label="时间" width="120">
            <template #default="{ row }">{{ formatDate(row.operationTime) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 快捷操作 -->
      <div v-if="showQuickActions" class="section-card">
        <div class="section-header">
          <h3 class="section-title">快捷操作</h3>
        </div>
        <div class="quick-actions-grid">
          <QuickActionCard
            v-for="action in quickActions"
            :key="action.to"
            :title="action.title"
            :desc="action.desc"
            :icon="iconMap[action.icon] || iconMap.Files"
            :color="action.color"
            :to="action.to"
            @navigate="handleNavigate"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import DataCard from '@/components/DataCard.vue'
import QuickActionCard from '@/components/QuickActionCard.vue'
import { getDashboardStats, getCategoryDistribution, getDepartmentRanking, getDepreciationTrend } from '@/api/dashboard'
import { getApprovalTodoPage } from '@/api/approval'
import { getInventoryTaskPage } from '@/api/inventory'
import { getFinanceSyncRecords } from '@/api/finance'
import { getAuditLogPage, getAuditSummary } from '@/api/audit'
import { useAuthStore } from '@/stores/auth'
import { useRoleDashboard } from '@/composables/useRoleDashboard'
import { formatWan, formatMoney } from '@/utils/format'
import { Files, List, Checked, DataLine, RefreshRight, Connection, Document, Tools, Setting, Warning, Monitor } from '@element-plus/icons-vue'

use([CanvasRenderer, PieChart, BarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const router = useRouter()
const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo)
const {
  roleLabel, quickActions, showStats, showStatusStats, showCharts,
  showApprovalSection, showInventorySection, showFinanceSection, showAuditSection, showQuickActions
} = useRoleDashboard()

const iconMap: Record<string, any> = { Files, List, Checked, DataLine, RefreshRight, Connection, Document, Tools, Setting, Warning, Monitor }

// ===== 核心数据 =====
const stats = ref<any>({})
const categoryChartData = ref<any[]>([])
const deptChartData = ref<any[]>([])
const trendData = ref<any[]>([])

const categoryChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 件 ({d}%)' },
  series: [{
    type: 'pie', radius: ['35%', '60%'], center: ['50%', '55%'],
    data: categoryChartData.value,
    label: { show: true, formatter: '{b}\n{d}%', fontSize: 11 },
    itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 }
  }]
}))
const deptChartOption = computed(() => ({
  tooltip: { trigger: 'axis', formatter: (p: any) => `${p[0].name}<br/>金额: ${formatWan(p[0].value)}` },
  grid: { left: '3%', right: '8%', bottom: '10%', top: '8%', containLabel: true },
  xAxis: { type: 'category', data: deptChartData.value.map(d => d.department), axisLabel: { fontSize: 11 } },
  yAxis: { type: 'value', axisLabel: { formatter: (v: number) => (v / 10000).toFixed(0) + '万' } },
  series: [{ type: 'bar', data: deptChartData.value.map(d => d.amount), itemStyle: { color: '#173B57', borderRadius: [4, 4, 0, 0] }, barWidth: '40%' }]
}))
const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['月折旧额', '累计折旧'], bottom: 0, fontSize: 11 },
  grid: { left: '3%', right: '8%', bottom: '15%', top: '8%', containLabel: true },
  xAxis: { type: 'category', data: trendData.value.map(d => d.month), axisLabel: { fontSize: 11 } },
  yAxis: [
    { type: 'value', name: '月折旧额', axisLabel: { formatter: (v: number) => (v / 10000).toFixed(0) + '万' } },
    { type: 'value', name: '累计折旧', axisLabel: { formatter: (v: number) => (v / 10000).toFixed(0) + '万' } }
  ],
  series: [
    { name: '月折旧额', type: 'bar', data: trendData.value.map(d => d.monthlyDepreciation || d.value || 0), itemStyle: { color: '#D97706', borderRadius: [4, 4, 0, 0] } },
    { name: '累计折旧', type: 'line', yAxisIndex: 1, data: trendData.value.map(d => d.accumulatedDepreciation || 0), smooth: true, lineStyle: { color: '#EA580C', width: 2 }, itemStyle: { color: '#EA580C' } }
  ]
}))

// ===== 待办数据 =====
const approvalTodoList = ref<any[]>([])
const approvalLoading = ref(false)

// ===== 盘点数据 =====
const inventoryTasks = ref<any[]>([])
const inventoryLoading = ref(false)

// ===== 财务数据 =====
const financeDepAmount = ref(0)
const financeSyncList = ref<any[]>([])

// ===== 审计数据 =====
const auditSummary = ref<any>({})
const auditList = ref<any[]>([])
const auditLoading = ref(false)

function formatDate(dt: string): string {
  if (!dt) return '-'
  return dt.substring(0, 10)
}
function formatNumber(n: number): string {
  return (n || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function bizTypeText(t: string): string {
  const map: Record<string, string> = { INBOUND: '入库', RECEIVE: '领用', TRANSFER: '调拨', REPAIR: '维修', SCRAP: '报废' }
  return map[t] || t || '-'
}
function bizTypeTag(t: string): string {
  const map: Record<string, string> = { INBOUND: '', RECEIVE: 'success', TRANSFER: 'warning', REPAIR: 'danger', SCRAP: 'info' }
  return map[t] || ''
}
function handleNavigate(to: string) {
  router.push(to)
}

async function fetchStats() { try { const r = await getDashboardStats(); if (r.code === 200) stats.value = r.data } catch {} }
async function fetchCategory() { try { const r = await getCategoryDistribution(); if (r.code === 200) categoryChartData.value = r.data.map((d: any) => ({ name: d.name, value: d.value })) } catch {} }
async function fetchDept() { try { const r = await getDepartmentRanking(); if (r.code === 200) deptChartData.value = r.data } catch {} }
async function fetchTrend() {
  try {
    const r = await getDepreciationTrend()
    if (r.code === 200) {
      trendData.value = r.data
      if (r.data.length > 0) financeDepAmount.value = r.data[r.data.length - 1].monthlyDepreciation || r.data[r.data.length - 1].value || 0
    }
  } catch {}
}
async function fetchApprovalTodo() {
  approvalLoading.value = true
  try { const r = await getApprovalTodoPage({ pageNum: 1, pageSize: 5 }); if (r.code === 200) approvalTodoList.value = r.data.records || [] } catch {} finally { approvalLoading.value = false }
}
async function fetchInventory() {
  inventoryLoading.value = true
  try { const r = await getInventoryTaskPage({ pageNum: 1, pageSize: 5 }); if (r.code === 200) inventoryTasks.value = r.data.records || [] } catch {} finally { inventoryLoading.value = false }
}
async function fetchFinance() {
  try { const r = await getFinanceSyncRecords({ pageNum: 1, pageSize: 5 }); if (r.code === 200) financeSyncList.value = r.data.records || [] } catch {}
}
async function fetchAudit() {
  auditLoading.value = true
  try {
    const [sumRes, listRes] = await Promise.all([getAuditSummary(), getAuditLogPage({ pageNum: 1, pageSize: 5 })])
    if (sumRes.code === 200) auditSummary.value = sumRes.data
    if (listRes.code === 200) auditList.value = listRes.data.records || []
  } catch {} finally { auditLoading.value = false }
}

onMounted(() => {
  if (showStats.value) fetchStats()
  if (showCharts.value) { fetchCategory(); fetchDept(); fetchTrend() }
  else if (showFinanceSection.value) fetchTrend()
  if (showApprovalSection.value) fetchApprovalTodo()
  if (showInventorySection.value) fetchInventory()
  if (showFinanceSection.value) fetchFinance()
  if (showAuditSection.value) fetchAudit()
})
</script>

<style scoped>
.dashboard { max-width: 1400px; }
.role-header { margin-bottom: 16px; }
.role-title { font-size: 20px; font-weight: 700; color: var(--color-text, #303133); margin: 0; }
.role-sub { font-size: 13px; color: var(--color-text-secondary, #909399); margin-top: 4px; display: block; }
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.charts-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 12px; }
.chart-card { background: #fff; border-radius: 6px; padding: 16px; border: 1px solid var(--color-border, #e4e7ed); }
.chart-title { font-size: 14px; font-weight: 600; color: var(--color-text, #303133); margin-bottom: 12px; }
.chart { height: 280px; }
.chart-empty { height: 280px; display: flex; align-items: center; justify-content: center; color: var(--color-text-secondary, #909399); font-size: 13px; }
.dual-row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-top: 12px; }
.section-card { background: #fff; border-radius: 6px; padding: 16px; border: 1px solid var(--color-border, #e4e7ed); }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-title { font-size: 14px; font-weight: 600; color: var(--color-text, #303133); margin: 0; }
.empty-hint { text-align: center; color: var(--color-text-secondary, #909399); font-size: 13px; padding: 20px 0; }
.quick-actions-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.finance-stats { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.fin-item { display: flex; flex-direction: column; padding: 8px 12px; background: #f8fafc; border-radius: 4px; }
.fin-label { font-size: 12px; color: #909399; }
.fin-value { font-size: 16px; font-weight: 600; color: #303133; margin-top: 4px; }
.fin-sync-list { margin-top: 10px; }
.fin-sync-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; border-bottom: 1px solid #f0f0f0; }
.sync-batch { font-size: 12px; color: #606266; flex: 1; }
.sync-time { font-size: 12px; color: #909399; }
.audit-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.audit-item { text-align: center; padding: 8px; background: #f8fafc; border-radius: 4px; }
.audit-label { font-size: 11px; color: #909399; display: block; }
.audit-value { font-size: 20px; font-weight: 700; color: #173B57; }
</style>
