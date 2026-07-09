<template>
  <div>
    <PageHeader title="折旧报表" description="查看固定资产月度折旧明细与汇总报表，支持按部门、分类查看折旧数据统计和净值趋势。" />

    <!-- 汇总卡片 -->
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:16px;">
      <DataCard label="本月折旧总额" :value="formatMoney(summary.monthlyDepreciationTotal)" sub="元" />
      <DataCard label="累计折旧总额" :value="formatMoney(summary.accumulatedDepreciationTotal)" sub="元" />
      <DataCard label="资产原值总计" :value="formatMoney(summary.originalValueTotal)" sub="元" />
      <DataCard label="资产净值总计" :value="formatMoney(summary.netValueTotal)" sub="元" />
    </div>
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:16px;">
      <DataCard label="计提资产数" :value="summary.depreciatingAssetCount ?? '-'" sub="件" />
      <DataCard label="资产总数" :value="summary.totalAssetCount ?? '-'" sub="件" />
      <DataCard label="原值总额" :value="formatWan(summary.originalValueTotal)" type="amount" />
      <DataCard label="净值总额" :value="formatWan(summary.netValueTotal)" type="amount" />
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

      <!-- 净值趋势 -->
      <el-tab-pane label="净值趋势" name="trend">
        <div v-if="trendData.length" style="height:400px;">
          <v-chart :option="trendChartOption" class="chart" autoresize style="height:100%;" />
        </div>
        <EmptyState v-else text="暂无趋势数据" />
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
import { getMonthlySummary, getMonthlyItems, getDepartmentStats, getCategoryStats, getDepreciationTrend } from '@/api/depreciation'
import { formatMoney, formatWan } from '@/utils/format'

use([CanvasRenderer, LineChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const activeTab = ref('monthly')
const currentMonth = ref(getDefaultMonth())
const summary = ref<any>({})
const monthlyItems = ref<any[]>([])
const monthlyLoading = ref(false)
const deptStats = ref<any[]>([])
const deptLoading = ref(false)
const catStats = ref<any[]>([])
const catLoading = ref(false)
const trendData = ref<any[]>([])

function getDefaultMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

async function fetchMonthlyData() {
  if (!currentMonth.value) return
  monthlyLoading.value = true
  try {
    const [summaryRes, itemsRes] = await Promise.all([
      getMonthlySummary(currentMonth.value),
      getMonthlyItems(currentMonth.value)
    ])
    summary.value = summaryRes.data || {}
    monthlyItems.value = itemsRes.data || []
  } finally {
    monthlyLoading.value = false
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

const trendChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross' }
  },
  legend: {
    data: ['月折旧额', '累计折旧', '资产净值'],
    top: 10
  },
  grid: { left: 60, right: 40, top: 50, bottom: 30 },
  xAxis: {
    type: 'category',
    data: trendData.value.map((d: any) => d.month),
    axisLabel: { rotate: 30 }
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: (v: number) => (v / 10000).toFixed(0) + '万'
    }
  },
  series: [
    {
      name: '月折旧额',
      type: 'bar',
      data: trendData.value.map((d: any) => d.monthlyDepreciation),
      itemStyle: { color: '#D97706' }
    },
    {
      name: '累计折旧',
      type: 'line',
      data: trendData.value.map((d: any) => d.accumulatedDepreciation),
      smooth: true,
      itemStyle: { color: '#EA580C' },
      lineStyle: { width: 2 }
    },
    {
      name: '资产净值',
      type: 'line',
      data: trendData.value.map((d: any) => d.netValue),
      smooth: true,
      itemStyle: { color: '#4F8F7B' },
      lineStyle: { width: 2 },
      areaStyle: { color: 'rgba(79,143,123,0.1)' }
    }
  ]
}))

watch(activeTab, (tab) => {
  if (tab === 'department' && deptStats.value.length === 0) fetchDeptStats()
  if (tab === 'category' && catStats.value.length === 0) fetchCatStats()
  if (tab === 'trend' && trendData.value.length === 0) fetchTrend()
})

onMounted(() => {
  fetchMonthlyData()
  fetchDeptStats()
  fetchCatStats()
  fetchTrend()
})
</script>
