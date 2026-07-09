<template>
  <div>
    <PageHeader title="折旧报表" description="查看固定资产折旧总览、部门/分类价值统计、月度趋势及风险资产分析。" />

    <!-- 折旧总览指标卡 -->
    <div style="display:grid;grid-template-columns:repeat(7,1fr);gap:12px;margin-bottom:16px;">
      <DataCard label="资产总数" :value="summary.assetCount ?? '-'" sub="件" />
      <DataCard label="原值总额" :value="formatMoney(summary.totalOriginalValue)" sub="元" />
      <DataCard label="净值总额" :value="formatMoney(summary.totalNetValue)" sub="元" />
      <DataCard label="累计折旧" :value="formatMoney(summary.totalAccumulatedDepreciation)" sub="元" />
      <DataCard label="本月折旧额" :value="formatMoney(summary.monthlyDepreciation)" sub="元" />
      <DataCard label="平均折旧率" :value="summary.averageDepreciationRate != null ? Number(summary.averageDepreciationRate).toFixed(2) + '%' : '-'" />
      <DataCard label="风险资产" :value="(summary.lowValueAssetCount ?? 0) + (summary.nearEndAssetCount ?? 0)" sub="件" />
    </div>

    <!-- 图表区域：部门价值 + 分类价值 -->
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">
      <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;">
        <h3 style="font-size:14px;margin:0 0 8px;color:var(--color-text);">部门资产价值统计</h3>
        <div v-if="deptStats.length" style="height:320px;">
          <v-chart :option="deptChartOption" class="chart" autoresize style="height:100%;" />
        </div>
        <EmptyState v-else text="暂无部门数据" />
      </div>
      <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;">
        <h3 style="font-size:14px;margin:0 0 8px;color:var(--color-text);">分类资产价值统计</h3>
        <div v-if="catStats.length" style="height:320px;">
          <v-chart :option="catChartOption" class="chart" autoresize style="height:100%;" />
        </div>
        <EmptyState v-else text="暂无分类数据" />
      </div>
    </div>

    <!-- 月度折旧趋势 -->
    <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;margin-bottom:16px;">
      <h3 style="font-size:14px;margin:0 0 8px;color:var(--color-text);">月度折旧趋势（近12个月）</h3>
      <div v-if="trendData.length" style="height:360px;">
        <v-chart :option="trendChartOption" class="chart" autoresize style="height:100%;" />
      </div>
      <EmptyState v-else text="暂无趋势数据" />
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;">
      <!-- 月度折旧明细 -->
      <el-tab-pane label="月度折旧明细" name="monthly">
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px;">
          <span style="font-size:14px;color:var(--color-text-secondary);">报表月份：</span>
          <el-date-picker
            v-model="currentMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            @change="fetchMonthlyData"
          />
          <el-button type="success" :loading="exporting" @click="handleExport">
            <el-icon><Download /></el-icon>导出报表
          </el-button>
        </div>
        <el-table :data="monthlyItems" border stripe v-loading="monthlyLoading" max-height="500">
          <el-table-column prop="assetCode" label="资产编号" width="140" fixed />
          <el-table-column prop="assetName" label="资产名称" width="150" fixed />
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column prop="department" label="部门" width="120" />
          <el-table-column prop="keeper" label="保管人" width="80" />
          <el-table-column prop="purchaseDate" label="购置日期" width="110" />
          <el-table-column label="原值" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.originalValue) }}</template>
          </el-table-column>
          <el-table-column label="残值率" width="80" align="right">
            <template #default="{ row }">{{ row.residualRate != null ? (Number(row.residualRate) * 100).toFixed(2) + '%' : '-' }}</template>
          </el-table-column>
          <el-table-column label="使用年限" width="80" align="center">
            <template #default="{ row }">{{ row.usefulLife ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="月折旧额" width="120" align="right">
            <template #default="{ row }">
              <span style="color:#D97706;font-weight:600;">{{ formatMoney(row.monthlyDepreciation) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="累计折旧" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.accumulatedDepreciation) }}</template>
          </el-table-column>
          <el-table-column label="当前净值" width="120" align="right">
            <template #default="{ row }">
              <span style="color:#4F8F7B;font-weight:600;">{{ formatMoney(row.netValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <AssetStatusTag :status="row.status" />
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 部门统计 -->
      <el-tab-pane label="部门统计" name="department">
        <el-table :data="deptStats" border stripe v-loading="deptLoading">
          <el-table-column prop="department" label="部门" width="160" />
          <el-table-column prop="assetCount" label="资产数量" width="100" align="center" />
          <el-table-column label="原值合计" align="right">
            <template #default="{ row }">{{ formatMoney(row.originalValueTotal) }}</template>
          </el-table-column>
          <el-table-column label="累计折旧" align="right">
            <template #default="{ row }">{{ formatMoney(row.accumulatedDepreciationTotal) }}</template>
          </el-table-column>
          <el-table-column label="净值合计" align="right">
            <template #default="{ row }">
              <span style="color:#4F8F7B;font-weight:600;">{{ formatMoney(row.netValueTotal) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="月折旧额" align="right">
            <template #default="{ row }">
              <span style="color:#D97706;font-weight:600;">{{ formatMoney(row.monthlyDepreciation) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="折旧率" width="100" align="right">
            <template #default="{ row }">
              {{ row.originalValueTotal > 0 ? ((Number(row.accumulatedDepreciationTotal) / Number(row.originalValueTotal)) * 100).toFixed(1) + '%' : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 分类统计 -->
      <el-tab-pane label="分类统计" name="category">
        <el-table :data="catStats" border stripe v-loading="catLoading">
          <el-table-column prop="categoryName" label="资产分类" width="160" />
          <el-table-column prop="assetCount" label="资产数量" width="100" align="center" />
          <el-table-column label="原值合计" align="right">
            <template #default="{ row }">{{ formatMoney(row.originalValueTotal) }}</template>
          </el-table-column>
          <el-table-column label="累计折旧" align="right">
            <template #default="{ row }">{{ formatMoney(row.accumulatedDepreciationTotal) }}</template>
          </el-table-column>
          <el-table-column label="净值合计" align="right">
            <template #default="{ row }">
              <span style="color:#4F8F7B;font-weight:600;">{{ formatMoney(row.netValueTotal) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="月折旧额" align="right">
            <template #default="{ row }">
              <span style="color:#D97706;font-weight:600;">{{ formatMoney(row.monthlyDepreciation) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="折旧率" width="100" align="right">
            <template #default="{ row }">
              {{ row.originalValueTotal > 0 ? ((Number(row.accumulatedDepreciationTotal) / Number(row.originalValueTotal)) * 100).toFixed(1) + '%' : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 低净值资产 -->
      <el-tab-pane :label="`低净值资产(${lowValueAssets.length})`" name="lowValue">
        <el-table :data="lowValueAssets" border stripe v-loading="lowValueLoading">
          <el-table-column prop="assetCode" label="资产编号" width="150" />
          <el-table-column prop="assetName" label="资产名称" min-width="140" />
          <el-table-column prop="department" label="部门" width="120" />
          <el-table-column label="原值" width="130" align="right">
            <template #default="{ row }">{{ formatMoney(row.originalValue) }}</template>
          </el-table-column>
          <el-table-column label="净值" width="130" align="right">
            <template #default="{ row }">
              <span style="color:#DC2626;font-weight:600;">{{ formatMoney(row.netValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="净值率" width="100" align="right">
            <template #default="{ row }">
              <el-tag type="danger" size="small">{{ (Number(row.netValueRate) * 100).toFixed(1) }}%</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <AssetStatusTag :status="row.status" />
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无低净值资产" />
          </template>
        </el-table>
      </el-tab-pane>

      <!-- 接近使用年限资产 -->
      <el-tab-pane :label="`接近报废资产(${nearEndAssets.length})`" name="nearEnd">
        <el-table :data="nearEndAssets" border stripe v-loading="nearEndLoading">
          <el-table-column prop="assetCode" label="资产编号" width="150" />
          <el-table-column prop="assetName" label="资产名称" min-width="140" />
          <el-table-column prop="purchaseDate" label="购置日期" width="120" />
          <el-table-column label="使用年限" width="90" align="center">
            <template #default="{ row }">{{ row.usefulLife }} 年</template>
          </el-table-column>
          <el-table-column label="已使用" width="90" align="center">
            <template #default="{ row }">{{ row.usedMonths }} 月</template>
          </el-table-column>
          <el-table-column label="剩余" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.remainingMonths <= 6 ? 'danger' : 'warning'" size="small">{{ row.remainingMonths }} 月</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <AssetStatusTag :status="row.status" />
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无接近报废资产" />
          </template>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import PageHeader from '@/components/PageHeader.vue'
import DataCard from '@/components/DataCard.vue'
import AssetStatusTag from '@/components/AssetStatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import {
  getMonthlyItems,
  getDepartmentStats,
  getCategoryStats,
  getDepreciationTrend,
  getDepreciationSummary,
  getLowValueAssets,
  getNearEndAssets
} from '@/api/depreciation'
import { formatMoney } from '@/utils/format'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { exportDepreciationReport } from '@/api/export'

use([CanvasRenderer, LineChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const activeTab = ref('monthly')
const currentMonth = ref(getDefaultMonth())
const exporting = ref(false)

// 折旧总览
const summary = ref<any>({})

// 月度明细
const monthlyItems = ref<any[]>([])
const monthlyLoading = ref(false)

// 部门统计
const deptStats = ref<any[]>([])
const deptLoading = ref(false)

// 分类统计
const catStats = ref<any[]>([])
const catLoading = ref(false)

// 趋势
const trendData = ref<any[]>([])

// 低净值资产
const lowValueAssets = ref<any[]>([])
const lowValueLoading = ref(false)

// 接近报废资产
const nearEndAssets = ref<any[]>([])
const nearEndLoading = ref(false)

function getDefaultMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

async function fetchSummary() {
  try {
    const res = await getDepreciationSummary()
    summary.value = res.data || {}
  } catch { /* ignore */ }
}

async function fetchMonthlyData() {
  if (!currentMonth.value) return
  monthlyLoading.value = true
  try {
    const itemsRes = await getMonthlyItems(currentMonth.value)
    monthlyItems.value = itemsRes.data || []
  } finally {
    monthlyLoading.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    await exportDepreciationReport(currentMonth.value)
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

async function fetchDeptStats() {
  deptLoading.value = true
  try {
    const res = await getDepartmentStats()
    deptStats.value = res.data || []
  } finally {
    deptLoading.value = false
  }
}

async function fetchCatStats() {
  catLoading.value = true
  try {
    const res = await getCategoryStats()
    catStats.value = res.data || []
  } finally {
    catLoading.value = false
  }
}

async function fetchTrend() {
  try {
    const res = await getDepreciationTrend()
    trendData.value = res.data || []
  } catch { /* ignore */ }
}

async function fetchLowValueAssets() {
  lowValueLoading.value = true
  try {
    const res = await getLowValueAssets()
    lowValueAssets.value = res.data || []
  } finally {
    lowValueLoading.value = false
  }
}

async function fetchNearEndAssets() {
  nearEndLoading.value = true
  try {
    const res = await getNearEndAssets()
    nearEndAssets.value = res.data || []
  } finally {
    nearEndLoading.value = false
  }
}

// 部门价值柱状图
const deptChartOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['原值', '净值'], top: 0 },
  grid: { left: 60, right: 20, top: 40, bottom: 40 },
  xAxis: {
    type: 'category',
    data: deptStats.value.map((d: any) => d.department),
    axisLabel: { rotate: 20, fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    axisLabel: { formatter: (v: number) => (v / 10000).toFixed(0) + '万' }
  },
  series: [
    { name: '原值', type: 'bar', data: deptStats.value.map((d: any) => d.originalValueTotal), itemStyle: { color: '#6366F1' } },
    { name: '净值', type: 'bar', data: deptStats.value.map((d: any) => d.netValueTotal), itemStyle: { color: '#4F8F7B' } }
  ]
}))

// 分类价值柱状图
const catChartOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['原值', '净值'], top: 0 },
  grid: { left: 60, right: 20, top: 40, bottom: 40 },
  xAxis: {
    type: 'category',
    data: catStats.value.map((d: any) => d.categoryName),
    axisLabel: { rotate: 20, fontSize: 11 }
  },
  yAxis: {
    type: 'value',
    axisLabel: { formatter: (v: number) => (v / 10000).toFixed(0) + '万' }
  },
  series: [
    { name: '原值', type: 'bar', data: catStats.value.map((d: any) => d.originalValueTotal), itemStyle: { color: '#6366F1' } },
    { name: '净值', type: 'bar', data: catStats.value.map((d: any) => d.netValueTotal), itemStyle: { color: '#4F8F7B' } }
  ]
}))

// 月度趋势图
const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
  legend: { data: ['月折旧额', '累计折旧', '资产净值'], top: 10 },
  grid: { left: 60, right: 40, top: 50, bottom: 30 },
  xAxis: {
    type: 'category',
    data: trendData.value.map((d: any) => d.month),
    axisLabel: { rotate: 30 }
  },
  yAxis: {
    type: 'value',
    axisLabel: { formatter: (v: number) => (v / 10000).toFixed(0) + '万' }
  },
  series: [
    { name: '月折旧额', type: 'bar', data: trendData.value.map((d: any) => d.monthlyDepreciation), itemStyle: { color: '#D97706' } },
    { name: '累计折旧', type: 'line', data: trendData.value.map((d: any) => d.accumulatedDepreciation), smooth: true, itemStyle: { color: '#EA580C' }, lineStyle: { width: 2 } },
    { name: '资产净值', type: 'line', data: trendData.value.map((d: any) => d.netValue), smooth: true, itemStyle: { color: '#4F8F7B' }, lineStyle: { width: 2 }, areaStyle: { color: 'rgba(79,143,123,0.1)' } }
  ]
}))

watch(activeTab, (tab) => {
  if (tab === 'lowValue' && lowValueAssets.value.length === 0) fetchLowValueAssets()
  if (tab === 'nearEnd' && nearEndAssets.value.length === 0) fetchNearEndAssets()
})

onMounted(() => {
  fetchSummary()
  fetchMonthlyData()
  fetchDeptStats()
  fetchCatStats()
  fetchTrend()
  fetchLowValueAssets()
  fetchNearEndAssets()
})
</script>
