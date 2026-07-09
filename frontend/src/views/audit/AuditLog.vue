<template>
  <div>
    <PageHeader title="审计日志" description="集中展示资产操作、审批操作、盘点异常、财务同步等关键行为，支持多维筛选与导出，便于审计追溯。" />

    <!-- 顶部统计卡片 -->
    <div style="display:grid;grid-template-columns:repeat(5,1fr);gap:12px;margin-bottom:16px;">
      <DataCard label="今日操作" :value="summary.todayOperationCount ?? 0" sub="条" color="#1A73E8" />
      <DataCard label="资产变更" :value="summary.assetChangeCount ?? 0" sub="条" />
      <DataCard label="审批操作" :value="summary.approvalOperationCount ?? 0" sub="条" color="#E3742E" />
      <DataCard label="盘点异常" :value="summary.inventoryAbnormalCount ?? 0" sub="条" color="#D93025" />
      <DataCard label="财务同步" :value="summary.financeSyncCount ?? 0" sub="条" color="#188038" />
    </div>

    <!-- 筛选 + 列表 -->
    <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;">
      <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px;flex-wrap:wrap;">
        <span style="font-size:14px;color:var(--color-text-secondary);">日志类型：</span>
        <el-select v-model="filterForm.logType" placeholder="全部类型" clearable style="width:160px;" @change="handleFilter">
          <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-input v-model="filterForm.assetCode" placeholder="资产编号" clearable style="width:150px;" @keyup.enter="handleFilter" />
        <el-input v-model="filterForm.assetName" placeholder="资产名称" clearable style="width:150px;" @keyup.enter="handleFilter" />
        <el-input v-model="filterForm.operatorName" placeholder="操作人" clearable style="width:130px;" @keyup.enter="handleFilter" />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width:240px;"
          @change="handleFilter"
        />
        <el-button type="primary" @click="handleFilter">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="fetchSummary">刷新统计</el-button>
        <el-button type="success" :loading="exporting" @click="handleExport">
          <el-icon><Download /></el-icon>导出Excel
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" max-height="600">
        <el-table-column label="日志类型" width="110">
          <template #default="{ row }">
            <el-tag :type="logTypeTagType(row.logType)" size="small">{{ row.logTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assetCode" label="资产编号" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.assetCode ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="assetName" label="资产名称" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.assetName ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="110">
          <template #default="{ row }">{{ row.businessType ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.operation ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120">
          <template #default="{ row }">{{ row.operatorName ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="170">
          <template #default="{ row }">{{ row.operationTime ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              link
              :disabled="!row.assetId"
              @click="handleViewAsset(row)"
            >查看资产</el-button>
            <el-button
              size="small"
              type="info"
              link
              @click="handleViewDetail(row)"
            >查看详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无审计日志" />
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

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="detailVisible" title="审计日志详情" width="640px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="日志ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="日志类型">{{ detailData.logTypeName }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ detailData.assetCode ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ detailData.assetName ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detailData.businessType ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务单据ID">{{ detailData.businessId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ detailData.operation ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ detailData.source }}</el-descriptions-item>
        <el-descriptions-item label="变更前状态">{{ detailData.beforeStatus ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="变更后状态">{{ detailData.afterStatus ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailData.operatorName ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.operationTime ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark ?? '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import DataCard from '@/components/DataCard.vue'
import {
  getAuditSummary,
  getAuditLogPage,
  getAuditLogDetail,
  exportAuditLogs,
  type AuditSummary,
  type AuditLog
} from '@/api/audit'

const router = useRouter()

const summary = ref<Partial<AuditSummary>>({})
const tableData = ref<AuditLog[]>([])
const loading = ref(false)
const exporting = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filterForm = reactive({
  logType: '' as string,
  assetCode: '' as string,
  assetName: '' as string,
  operatorName: '' as string
})
const dateRange = ref<[string, string] | null>(null)

const detailVisible = ref(false)
const detailData = ref<AuditLog | null>(null)

const typeOptions = [
  { label: '资产操作', value: 'ASSET_OPERATION' },
  { label: '审批操作', value: 'APPROVAL' },
  { label: '盘点异常', value: 'INVENTORY_ABNORMAL' },
  { label: '财务同步', value: 'FINANCE_SYNC' }
]

async function fetchSummary() {
  try {
    const res = await getAuditSummary()
    summary.value = res.data || {}
  } catch {
    // 错误已在拦截器处理
  }
}

async function fetchItems() {
  loading.value = true
  try {
    const res = await getAuditLogPage({
      logType: filterForm.logType || undefined,
      assetCode: filterForm.assetCode || undefined,
      assetName: filterForm.assetName || undefined,
      operatorName: filterForm.operatorName || undefined,
      startDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined,
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

function handleFilter() {
  pageNum.value = 1
  fetchItems()
}

function handleReset() {
  filterForm.logType = ''
  filterForm.assetCode = ''
  filterForm.assetName = ''
  filterForm.operatorName = ''
  dateRange.value = null
  pageNum.value = 1
  fetchItems()
}

async function handleViewDetail(row: AuditLog) {
  try {
    const res = await getAuditLogDetail(row.id)
    detailData.value = res.data || null
    detailVisible.value = true
  } catch {
    // 错误已在拦截器处理
  }
}

function handleViewAsset(row: AuditLog) {
  if (!row.assetId) return
  router.push(`/assets/${row.assetId}`)
}

async function handleExport() {
  exporting.value = true
  try {
    await exportAuditLogs({
      logType: filterForm.logType || undefined,
      assetCode: filterForm.assetCode || undefined,
      assetName: filterForm.assetName || undefined,
      operatorName: filterForm.operatorName || undefined,
      startDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined
    })
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

function logTypeTagType(type: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    ASSET_OPERATION: 'primary',
    APPROVAL: 'warning',
    INVENTORY_ABNORMAL: 'danger',
    FINANCE_SYNC: 'success'
  }
  return map[type] || 'info'
}

onMounted(() => {
  fetchSummary()
  fetchItems()
})
</script>
