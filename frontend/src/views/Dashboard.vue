<template>
  <div class="dashboard">
    <!-- 顶部欢迎区 -->
    <div class="welcome-bar">
      <div class="welcome-left">
        <h2 class="welcome-title">{{ greeting }}，{{ userName }}</h2>
        <div class="welcome-meta">
          <el-tag size="small" type="primary" effect="dark">{{ roleLabel }}</el-tag>
          <span v-if="userInfo?.department" class="meta-item">{{ userInfo.department }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">{{ today }}</span>
        </div>
      </div>
      <div class="welcome-right">
        <div v-if="showApprovalSection" class="summary-chip summary-todo">
          <span class="summary-num">{{ approvalTodoList.length }}</span>
          <span class="summary-label">待办事项</span>
        </div>
        <div v-if="showAuditSection" class="summary-chip summary-audit">
          <span class="summary-num">{{ auditSummary.todayOperationCount || 0 }}</span>
          <span class="summary-label">今日操作</span>
        </div>
        <div v-if="showInventorySection" class="summary-chip summary-inv">
          <span class="summary-num">{{ inventoryTasks.length }}</span>
          <span class="summary-label">盘点任务</span>
        </div>
        <div v-if="showFinanceSection" class="summary-chip summary-fin">
          <span class="summary-num">{{ financeSyncList.length }}</span>
          <span class="summary-label">同步记录</span>
        </div>
      </div>
    </div>

    <!-- 核心统计卡 -->
    <div v-if="showStats" class="stats-grid">
      <DataCard label="资产总数" :value="stats.assetCount ?? '-'" unit="件" :icon="Files" accent variant="primary" sub="在册资产总量" />
      <DataCard label="资产总原值" :value="formatWan(stats.totalOriginalValue)" :icon="Money" accent sub="全部资产购置金额" />
      <DataCard label="累计折旧" :value="formatWan(stats.totalAccumulatedDepreciation)" :icon="TrendCharts" accent variant="warning" sub="已计提折旧总额" />
      <DataCard label="资产净值" :value="formatWan(stats.totalNetValue)" :icon="Wallet" accent variant="success" sub="原值减去累计折旧" />
    </div>

    <!-- 状态统计卡 -->
    <div v-if="showStatusStats" class="stats-grid stats-grid-status">
      <DataCard label="使用中" :value="stats.inUseCount ?? 0" unit="件" variant="success" sub="正常使用" />
      <DataCard label="闲置" :value="stats.idleCount ?? 0" unit="件" variant="info" sub="未投入使用" />
      <DataCard label="维修中" :value="stats.repairingCount ?? 0" unit="件" variant="warning" sub="待修复" />
      <DataCard label="待报废" :value="stats.waitingScrapCount ?? 0" unit="件" variant="danger" sub="待处置" />
    </div>

    <!-- 图表区 -->
    <div v-if="showCharts" class="charts-grid">
      <div class="chart-card">
        <div class="chart-card-header">
          <h3 class="chart-card-title">资产分类分布</h3>
          <span class="chart-card-sub">按分类统计数量</span>
        </div>
        <v-chart v-if="categoryChartData.length" :option="categoryChartOption" class="chart" autoresize />
        <div v-else class="chart-empty">暂无数据</div>
      </div>
      <div class="chart-card">
        <div class="chart-card-header">
          <h3 class="chart-card-title">部门资产金额排行</h3>
          <span class="chart-card-sub">按部门统计原值</span>
        </div>
        <v-chart v-if="deptChartData.length" :option="deptChartOption" class="chart" autoresize />
        <div v-else class="chart-empty">暂无数据</div>
      </div>
      <div class="chart-card">
        <div class="chart-card-header">
          <h3 class="chart-card-title">月度折旧趋势</h3>
          <span class="chart-card-sub">近 12 个月</span>
        </div>
        <v-chart v-if="trendData.length" :option="trendChartOption" class="chart" autoresize />
        <div v-else class="chart-empty">暂无数据</div>
      </div>
    </div>

    <!-- 角色区块 -->
    <div class="sections-grid">
      <!-- 我的待办 -->
      <RoleDashboardSection v-if="showApprovalSection" title="我的待办" desc="待处理的审批事项" to="/approval/todo">
        <el-table :data="approvalTodoList" size="small" border v-loading="approvalLoading">
          <el-table-column prop="flowName" label="事项" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.flowName || bizTypeText(row.businessType) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="businessType" label="类型" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="bizTypeTag(row.businessType)" effect="light">{{ bizTypeText(row.businessType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applicantName" label="申请人" width="80" />
          <el-table-column prop="startedAt" label="发起时间" width="110">
            <template #default="{ row }">{{ formatDate(row.startedAt) }}</template>
          </el-table-column>
          <template #empty><el-empty description="暂无待办事项" :image-size="60" /></template>
        </el-table>
      </RoleDashboardSection>

      <!-- 盘点任务 -->
      <RoleDashboardSection v-if="showInventorySection" title="盘点任务" desc="当前盘点进度" to="/inventory/tasks">
        <el-table :data="inventoryTasks" size="small" border v-loading="inventoryLoading">
          <el-table-column prop="taskName" label="任务名称" min-width="140" show-overflow-tooltip />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'COMPLETED' ? 'success' : 'warning'" effect="light">{{ row.status === 'COMPLETED' ? '已完成' : '进行中' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="90">
            <template #default="{ row }">{{ row.completedRecords }}/{{ row.totalRecords }}</template>
          </el-table-column>
          <template #empty><el-empty description="暂无盘点任务" :image-size="60" /></template>
        </el-table>
      </RoleDashboardSection>

      <!-- 财务概览 -->
      <RoleDashboardSection v-if="showFinanceSection" title="财务概览" desc="资产价值与同步状态" to="/finance/sync">
        <div class="finance-stats">
          <div class="fin-item">
            <span class="fin-label">本月折旧</span>
            <span class="fin-value">¥{{ formatMoney(financeDepAmount) }}</span>
          </div>
          <div class="fin-item">
            <span class="fin-label">资产原值</span>
            <span class="fin-value">¥{{ formatMoney(stats.totalOriginalValue) }}</span>
          </div>
          <div class="fin-item">
            <span class="fin-label">累计折旧</span>
            <span class="fin-value">¥{{ formatMoney(stats.totalAccumulatedDepreciation) }}</span>
          </div>
          <div class="fin-item">
            <span class="fin-label">资产净值</span>
            <span class="fin-value">¥{{ formatMoney(stats.totalNetValue) }}</span>
          </div>
        </div>
        <div class="fin-sync-list" v-if="financeSyncList.length">
          <div v-for="r in financeSyncList.slice(0, 5)" :key="r.id" class="fin-sync-item">
            <el-tag size="small" :type="r.status === 'SUCCESS' ? 'success' : 'danger'" effect="light">{{ r.status === 'SUCCESS' ? '成功' : '失败' }}</el-tag>
            <span class="sync-batch">{{ r.syncBatchNo }}</span>
            <span class="sync-time">{{ formatDate(r.syncTime) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无同步记录" :image-size="60" />
      </RoleDashboardSection>

      <!-- 审计概览 -->
      <RoleDashboardSection v-if="showAuditSection" title="审计概览" desc="操作日志与异常监控" to="/audit/logs">
        <div class="audit-stats">
          <div class="audit-item"><span class="audit-value">{{ auditSummary.todayOperationCount || 0 }}</span><span class="audit-label">今日操作</span></div>
          <div class="audit-item"><span class="audit-value">{{ auditSummary.assetChangeCount || 0 }}</span><span class="audit-label">资产变更</span></div>
          <div class="audit-item"><span class="audit-value">{{ auditSummary.approvalOperationCount || 0 }}</span><span class="audit-label">审批操作</span></div>
          <div class="audit-item"><span class="audit-value">{{ auditSummary.inventoryAbnormalCount || 0 }}</span><span class="audit-label">盘点异常</span></div>
        </div>
        <el-table :data="auditList" size="small" border style="margin-top: 12px" v-loading="auditLoading">
          <el-table-column prop="logTypeName" label="类型" width="70" />
          <el-table-column prop="operation" label="操作" min-width="100" show-overflow-tooltip />
          <el-table-column prop="operatorName" label="操作人" width="80" />
          <el-table-column prop="operationTime" label="时间" width="110">
            <template #default="{ row }">{{ formatDate(row.operationTime) }}</template>
          </el-table-column>
          <template #empty><el-empty description="暂无审计日志" :image-size="60" /></template>
        </el-table>
      </RoleDashboardSection>

      <!-- 快捷操作 -->
      <RoleDashboardSection v-if="showQuickActions" title="快捷操作" desc="常用功能入口">
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
      </RoleDashboardSection>
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
import RoleDashboardSection from '@/components/RoleDashboardSection.vue'
import { getDashboardStats, getCategoryDistribution, getDepartmentRanking, getDepreciationTrend } from '@/api/dashboard'
import { getApprovalTodoPage } from '@/api/approval'
import { getInventoryTaskPage } from '@/api/inventory'
import { getFinanceSyncRecords } from '@/api/finance'
import { getAuditLogPage, getAuditSummary } from '@/api/audit'
import { useAuthStore } from '@/stores/auth'
import { useRoleDashboard } from '@/composables/useRoleDashboard'
import { formatWan, formatMoney } from '@/utils/format'
import { Files, List, Checked, DataLine, RefreshRight, Connection, Document, Tools, Setting, Warning, Monitor, Money, Wallet, TrendCharts } from '@element-plus/icons-vue'

use([CanvasRenderer, PieChart, BarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const router = useRouter()
const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo)
const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '用户')
const {
  roleLabel, quickActions, showStats, showStatusStats, showCharts,
  showApprovalSection, showInventorySection, showFinanceSection, showAuditSection, showQuickActions
} = useRoleDashboard()

const iconMap: Record<string, any> = { Files, List, Checked, DataLine, RefreshRight, Connection, Document, Tools, Setting, Warning, Monitor }

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const today = computed(() => {
  const d = new Date()
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${week}`
})

// ===== 核心数据 =====
const stats = ref<any>({})
const categoryChartData = ref<any[]>([])
const deptChartData = ref<any[]>([])
const trendData = ref<any[]>([])

const categoryChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 件 ({d}%)' },
  series: [{
    type: 'pie', radius: ['38%', '62%'], center: ['50%', '55%'],
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
  series: [{ type: 'bar', data: deptChartData.value.map(d => d.amount), itemStyle: { color: '#1F4E79', borderRadius: [4, 4, 0, 0] }, barWidth: '40%' }]
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
    { name: '月折旧额', type: 'bar', data: trendData.value.map(d => d.monthlyDepreciation || d.value || 0), itemStyle: { color: '#F0A020', borderRadius: [4, 4, 0, 0] } },
    { name: '累计折旧', type: 'line', yAxisIndex: 1, data: trendData.value.map(d => d.accumulatedDepreciation || 0), smooth: true, lineStyle: { color: '#1F4E79', width: 2 }, itemStyle: { color: '#1F4E79' } }
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
.dashboard {
  max-width: 1440px;
}

/* ===== 欢迎区 ===== */
.welcome-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, #1F4E79 0%, #2A6BA6 100%);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-lg);
  box-shadow: var(--shadow-md);
}
.welcome-left {
  flex: 1;
}
.welcome-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin: 0;
  line-height: 1.4;
}
.welcome-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.welcome-meta .meta-item {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}
.welcome-meta .meta-divider {
  color: rgba(255, 255, 255, 0.4);
}
.welcome-right {
  display: flex;
  gap: 12px;
}
.summary-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-md);
  min-width: 90px;
}
.summary-num {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}
.summary-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4px;
}

/* ===== 统计卡网格 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-base);
  margin-bottom: var(--space-base);
}
.stats-grid-status {
  margin-bottom: var(--space-lg);
}

/* ===== 图表区 ===== */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-base);
  margin-bottom: var(--space-lg);
}
.chart-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-lg);
}
.chart-card-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: var(--space-base);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--color-border-light);
}
.chart-card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0;
}
.chart-card-sub {
  font-size: 12px;
  color: var(--color-text-tertiary);
}
.chart {
  height: 280px;
}
.chart-empty {
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-tertiary);
  font-size: 13px;
}

/* ===== 角色区块网格 ===== */
.sections-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-base);
}

/* ===== 财务概览 ===== */
.finance-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-md);
}
.fin-item {
  display: flex;
  flex-direction: column;
  padding: 12px var(--space-base);
  background: var(--color-bg-soft);
  border-radius: var(--radius-sm);
}
.fin-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}
.fin-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-top: 4px;
  font-variant-numeric: tabular-nums;
}
.fin-sync-list {
  margin-top: var(--space-md);
}
.fin-sync-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border-light);
}
.fin-sync-item:last-child {
  border-bottom: none;
}
.sync-batch {
  font-size: 12px;
  color: var(--color-text-secondary);
  flex: 1;
}
.sync-time {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

/* ===== 审计概览 ===== */
.audit-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-sm);
}
.audit-item {
  text-align: center;
  padding: 12px 8px;
  background: var(--color-bg-soft);
  border-radius: var(--radius-sm);
}
.audit-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
  display: block;
  font-variant-numeric: tabular-nums;
}
.audit-label {
  font-size: 11px;
  color: var(--color-text-secondary);
  display: block;
  margin-top: 4px;
}

/* ===== 快捷操作 ===== */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-md);
}

@media (max-width: 1200px) {
  .charts-grid { grid-template-columns: repeat(2, 1fr); }
  .sections-grid { grid-template-columns: 1fr; }
}
</style>
