<template>
  <div>
    <PageHeader title="预警中心" description="集中展示资产价值、使用年限、闲置、维修、盘点和财务同步风险，便于及时发现异常资产。" />

    <!-- 顶部统计卡片：总预警、高风险、中风险、低风险 -->
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:16px;">
      <DataCard label="总预警数" :value="summary.totalWarningCount ?? 0" sub="条" />
      <DataCard label="高风险" :value="summary.highWarningCount ?? 0" sub="条" color="#D93025" />
      <DataCard label="中风险" :value="summary.mediumWarningCount ?? 0" sub="条" color="#E3742E" />
      <DataCard label="低风险" :value="summary.lowWarningCount ?? 0" sub="条" color="#1A73E8" />
    </div>

    <!-- 预警类型数量卡片 -->
    <div style="display:grid;grid-template-columns:repeat(6,1fr);gap:12px;margin-bottom:16px;">
      <DataCard label="低净值资产" :value="summary.lowValueCount ?? 0" sub="条" />
      <DataCard label="接近使用年限" :value="summary.nearEndCount ?? 0" sub="条" />
      <DataCard label="长期闲置" :value="summary.idleLongTimeCount ?? 0" sub="条" />
      <DataCard label="维修超期" :value="summary.repairOverdueCount ?? 0" sub="条" />
      <DataCard label="盘点异常" :value="summary.inventoryAbnormalCount ?? 0" sub="条" />
      <DataCard label="财务同步异常" :value="summary.financeSyncAbnormalCount ?? 0" sub="条" />
    </div>

    <!-- 筛选区域 + 预警列表 -->
    <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;">
      <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px;flex-wrap:wrap;">
        <span style="font-size:14px;color:var(--color-text-secondary);">预警类型：</span>
        <el-select v-model="filterType" placeholder="全部类型" clearable style="width:180px;" @change="handleFilter">
          <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <span style="font-size:14px;color:var(--color-text-secondary);margin-left:12px;">预警等级：</span>
        <el-select v-model="filterLevel" placeholder="全部等级" clearable style="width:160px;" @change="handleFilter">
          <el-option label="高风险" value="HIGH" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="低风险" value="LOW" />
        </el-select>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" @click="fetchData">刷新</el-button>
        <el-button type="success" :loading="exporting" @click="handleExport">
          <el-icon><Download /></el-icon>导出预警
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" max-height="600">
        <el-table-column label="预警类型" width="140">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.warningType)" size="small">{{ row.warningTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="90">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.warningLevel)" size="small">{{ levelText(row.warningLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="280" show-overflow-tooltip />
        <el-table-column prop="assetCode" label="资产编号" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.assetCode ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="assetName" label="资产名称" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.assetName ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="生成时间" width="170" />
        <el-table-column prop="suggestion" label="处置建议" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              link
              :disabled="!row.assetId"
              @click="handleViewAsset(row)"
            >查看资产</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无预警数据" />
        </template>
      </el-table>

      <div style="display:flex;justify-content:flex-end;margin-top:12px;">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchItems"
          @current-change="fetchItems"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import DataCard from '@/components/DataCard.vue'
import {
  getWarningSummary,
  getWarningItems,
  type WarningSummary,
  type WarningItem
} from '@/api/warning'
import { Download } from '@element-plus/icons-vue'
import { exportWarnings } from '@/api/export'

const router = useRouter()

const summary = ref<Partial<WarningSummary>>({})
const tableData = ref<WarningItem[]>([])
const loading = ref(false)
const exporting = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filterType = ref<string | undefined>(undefined)
const filterLevel = ref<string | undefined>(undefined)

const typeOptions = [
  { label: '低净值资产', value: 'LOW_VALUE' },
  { label: '接近使用年限', value: 'NEAR_END' },
  { label: '长期闲置', value: 'IDLE_LONG_TIME' },
  { label: '维修超期', value: 'REPAIR_OVERDUE' },
  { label: '盘点异常', value: 'INVENTORY_ABNORMAL' },
  { label: '财务同步异常', value: 'FINANCE_SYNC_ABNORMAL' }
]

async function fetchSummary() {
  try {
    const res = await getWarningSummary()
    summary.value = res.data || {}
  } catch {
    // 错误已在拦截器处理
  }
}

async function fetchItems() {
  loading.value = true
  try {
    const res = await getWarningItems({
      type: filterType.value,
      level: filterLevel.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function fetchData() {
  pageNum.value = 1
  fetchSummary()
  fetchItems()
}

function handleFilter() {
  pageNum.value = 1
  fetchItems()
}

function handleReset() {
  filterType.value = undefined
  filterLevel.value = undefined
  pageNum.value = 1
  fetchItems()
}

async function handleExport() {
  exporting.value = true
  try {
    await exportWarnings({ type: filterType.value, level: filterLevel.value })
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

function handleViewAsset(row: WarningItem) {
  if (!row.assetId) return
  router.push(`/assets/${row.assetId}`)
}

function typeTagType(type: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    LOW_VALUE: 'info',
    NEAR_END: 'primary',
    IDLE_LONG_TIME: 'warning',
    REPAIR_OVERDUE: 'danger',
    INVENTORY_ABNORMAL: 'danger',
    FINANCE_SYNC_ABNORMAL: 'danger'
  }
  return map[type] || 'info'
}

function levelTagType(level: string): 'danger' | 'warning' | 'info' {
  const map: Record<string, 'danger' | 'warning' | 'info'> = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info'
  }
  return map[level] || 'info'
}

function levelText(level: string): string {
  const map: Record<string, string> = {
    HIGH: '高风险',
    MEDIUM: '中风险',
    LOW: '低风险'
  }
  return map[level] || level
}

onMounted(() => {
  fetchSummary()
  fetchItems()
})
</script>
