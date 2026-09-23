<template>
  <div class="audit-log">
    <PageHeader title="审计追踪" description="全局操作日志与异常追踪">
      <template #actions>
        <el-button type="success" :loading="exporting" @click="handleExport">
          <el-icon><Download /></el-icon>导出日志
        </el-button>
      </template>
    </PageHeader>

    <!-- 顶部统计卡片 -->
    <div class="stat-grid stat-grid-5">
      <DataCard label="今日操作" :value="summary.todayOperationCount ?? 0" unit="条" variant="primary" accent />
      <DataCard label="资产变更" :value="summary.assetChangeCount ?? 0" unit="条" variant="info" accent />
      <DataCard label="审批操作" :value="summary.approvalOperationCount ?? 0" unit="条" variant="success" accent />
      <DataCard label="盘点异常" :value="summary.inventoryAbnormalCount ?? 0" unit="条" variant="warning" accent />
      <DataCard label="财务同步" :value="summary.financeSyncCount ?? 0" unit="条" variant="default" accent />
    </div>

    <!-- 筛选 + 列表 -->
    <div class="panel">
      <div class="filter-bar">
        <div class="filter-item">
          <span class="filter-label">日志类型</span>
          <el-select v-model="filterForm.logType" placeholder="全部类型" clearable style="width:160px;" @change="handleFilter">
            <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </div>
        <div class="filter-item">
          <span class="filter-label">资产编号</span>
          <el-input v-model="filterForm.assetCode" placeholder="请输入" clearable style="width:150px;" @keyup.enter="handleFilter" />
        </div>
        <div class="filter-item">
          <span class="filter-label">资产名称</span>
          <el-input v-model="filterForm.assetName" placeholder="请输入" clearable style="width:150px;" @keyup.enter="handleFilter" />
        </div>
        <div class="filter-item">
          <span class="filter-label">操作人</span>
          <el-input v-model="filterForm.operatorName" placeholder="请输入" clearable style="width:130px;" @keyup.enter="handleFilter" />
        </div>
        <div class="filter-item">
          <span class="filter-label">日期范围</span>
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
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="fetchSummary">刷新统计</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" max-height="600">
        <el-table-column label="日志类型" width="120">
          <template #default="{ row }">
            <el-tag :type="logTypeTagType(row.logType)" size="small" effect="dark">{{ row.logTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assetCode" label="资产编号" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.assetCode ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="assetName" label="资产名称" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.assetName ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="businessType" label="业务类型" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.businessType" :type="businessTypeTagType(row.businessType)" size="small" effect="light">{{ businessTypeLabel(row.businessType) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.operation ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120">
          <template #default="{ row }">{{ row.operatorName ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="170">
          <template #default="{ row }">
            <span v-if="row.operationTime">{{ formatDateTime(row.operationTime) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源模块" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.source" type="info" size="small" effect="plain">{{ row.source }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
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

      <div class="pagination">
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

function businessTypeLabel(type: string): string {
  const map: Record<string, string> = {
    INBOUND: '入库',
    RECEIVE: '领用',
    TRANSFER: '调拨',
    REPAIR: '维修',
    SCRAP: '报废',
    INVENTORY: '盘点',
    FINANCE_SYNC: '财务同步'
  }
  return map[type] || type
}

function businessTypeTagType(type: string): 'info' | 'success' | 'warning' | 'danger' | '' {
  const map: Record<string, 'info' | 'success' | 'warning' | 'danger' | ''> = {
    INBOUND: 'info',
    RECEIVE: 'success',
    TRANSFER: 'warning',
    REPAIR: 'danger',
    SCRAP: '',
    INVENTORY: 'warning',
    FINANCE_SYNC: 'success'
  }
  return map[type] || 'info'
}

function formatDateTime(dt: string): string {
  if (!dt) return '-'
  // 已是 YYYY-MM-DD HH:mm:ss 格式则截断到分钟
  if (dt.length >= 16) return dt.substring(0, 16)
  return dt
}

onMounted(() => {
  fetchSummary()
  fetchItems()
})
</script>

<style scoped>
.audit-log {
  display: flex;
  flex-direction: column;
}

/* 统计卡片网格 */
.stat-grid {
  display: grid;
  gap: var(--space-base);
  margin-bottom: var(--space-lg);
}
.stat-grid-5 {
  grid-template-columns: repeat(5, 1fr);
}

/* 筛选 + 表格面板 */
.panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-lg);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  flex-wrap: wrap;
  margin-bottom: var(--space-base);
}
.filter-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
.filter-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}
.filter-actions {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-left: auto;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-base);
}
</style>
