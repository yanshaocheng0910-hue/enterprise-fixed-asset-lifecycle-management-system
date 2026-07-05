<template>
  <div class="dashboard">
    <!-- Core Stats Row -->
    <div class="stats-row">
      <DataCard label="资产总数" :value="stats.assetCount ?? '-'" sub="件" />
      <DataCard label="资产总原值" :value="formatWan(stats.totalOriginalValue)" type="amount" />
      <DataCard label="累计折旧" :value="formatWan(stats.totalAccumulatedDepreciation)" type="amount" />
      <DataCard label="资产净值" :value="formatWan(stats.totalNetValue)" type="amount" />
    </div>

    <!-- Status Stats Row -->
    <div class="stats-row" style="margin-top: 12px;">
      <DataCard label="使用中" :value="stats.inUseCount ?? 0" sub="件" color="#4F8F7B" />
      <DataCard label="闲置" :value="stats.idleCount ?? 0" sub="件" color="#8B9DC3" />
      <DataCard label="维修中" :value="stats.repairingCount ?? 0" sub="件" color="#D97706" />
      <DataCard label="待报废" :value="stats.waitingScrapCount ?? 0" sub="件" color="#EA580C" />
    </div>

    <!-- Charts Row -->
    <div class="charts-row">
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
        <h3 class="chart-title">月度折旧趋势</h3>
        <v-chart v-if="trendData.length" :option="trendChartOption" class="chart" autoresize />
        <div v-else class="chart-empty">暂无数据</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import DataCard from '@/components/DataCard.vue'
import { getDashboardStats, getCategoryDistribution, getDepartmentRanking, getDepreciationTrend } from '@/api/dashboard'
import { formatWan } from '@/utils/format'

use([CanvasRenderer, PieChart, BarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const stats = ref<any>({})
const categoryChartData = ref<any[]>([])
const deptChartData = ref<any[]>([])
const trendData = ref<any[]>([])

const categoryChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 件 ({d}%)' },
  series: [{
    type: 'pie',
    radius: ['35%', '60%'],
    center: ['50%', '55%'],
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
  series: [{
    type: 'bar',
    data: deptChartData.value.map(d => d.amount),
    itemStyle: { color: '#173B57', borderRadius: [4, 4, 0, 0] },
    barWidth: '40%'
  }]
}))

const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis', formatter: (p: any) => `${p[0].name}<br/>折旧: ${formatWan(p[0].value)}` },
  grid: { left: '3%', right: '8%', bottom: '10%', top: '8%', containLabel: true },
  xAxis: { type: 'category', data: trendData.value.map(d => d.month), axisLabel: { fontSize: 11 } },
  yAxis: { type: 'value', axisLabel: { formatter: (v: number) => (v / 10000).toFixed(0) + '万' } },
  series: [{
    type: 'line',
    data: trendData.value.map(d => d.value),
    smooth: true,
    lineStyle: { color: '#4F8F7B', width: 2 },
    itemStyle: { color: '#4F8F7B' },
    areaStyle: { color: 'rgba(79,143,123,0.12)' }
  }]
}))

async function fetchStats() {
  try {
    const r = await getDashboardStats()
    if (r.code === 200) stats.value = r.data
  } catch {}
}
async function fetchCategory() {
  try {
    const r = await getCategoryDistribution()
    if (r.code === 200) categoryChartData.value = r.data.map((d: any) => ({ name: d.name, value: d.value }))
  } catch {}
}
async function fetchDept() {
  try {
    const r = await getDepartmentRanking()
    if (r.code === 200) deptChartData.value = r.data
  } catch {}
}
async function fetchTrend() {
  try {
    const r = await getDepreciationTrend()
    if (r.code === 200) trendData.value = r.data
  } catch {}
}

onMounted(() => {
  fetchStats()
  fetchCategory()
  fetchDept()
  fetchTrend()
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
}
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.charts-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 12px;
}
.chart-card {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  border: 1px solid var(--color-border);
}
.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}
.chart {
  height: 280px;
}
.chart-empty {
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  font-size: 13px;
}
</style>
