<template>
  <div class="finance-sync">
    <PageHeader title="财务数据同步" description="资产折旧数据与财务系统同步记录">
      <template #actions>
        <el-button type="primary" :loading="syncing" @click="handleSync">
          <el-icon><Refresh /></el-icon>手动同步
        </el-button>
        <el-button type="success" :loading="exporting" @click="handleExport">
          <el-icon><Download /></el-icon>导出
        </el-button>
      </template>
    </PageHeader>

    <!-- 总览卡片 -->
    <div class="metric-grid">
      <DataCard label="同步记录总数" :value="total" sub="条" variant="info" accent />
      <DataCard label="最近同步月份" :value="latestRecord?.syncMonth ?? '-'" variant="primary" accent />
      <DataCard label="累计同步折旧" :value="formatMoney(totalSyncedDepreciation)" unit="元" variant="warning" accent />
      <div class="status-card">
        <div class="status-label">最近同步状态</div>
        <div class="status-value">
          <el-tag
            v-if="latestRecord?.status"
            :type="latestRecord.status === 'SUCCESS' ? 'success' : 'danger'"
            effect="dark"
            size="large"
          >
            <el-icon class="status-icon">
              <CircleCheckFilled v-if="latestRecord.status === 'SUCCESS'" />
              <CircleCloseFilled v-else />
            </el-icon>
            {{ latestRecord.status === 'SUCCESS' ? '同步成功' : '同步失败' }}
          </el-tag>
          <span v-else>-</span>
        </div>
      </div>
    </div>

    <!-- 操作区域 -->
    <div class="panel">
      <el-alert
        title="同步说明"
        type="info"
        show-icon
        :closable="false"
        description="本模块为网页端模拟同步记录，用于演示资产折旧数据流转，不会调用外部财务系统。"
      />
      <div class="sync-row">
        <span class="sync-label">同步月份：</span>
        <el-date-picker
          v-model="syncMonth"
          type="month"
          placeholder="选择月份"
          format="YYYY-MM"
          value-format="YYYY-MM"
          :disabled-date="disabledDate"
        />
      </div>
    </div>

    <!-- 同步记录表格 -->
    <div class="panel">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="syncBatchNo" label="批次号" width="180" show-overflow-tooltip />
        <el-table-column prop="syncMonth" label="同步月份" width="110">
          <template #default="{ row }">
            <span class="month-cell">{{ row.syncMonth }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="assetCount" label="资产数量" width="90" align="center" />
        <el-table-column label="原值总额" width="130" align="right" class-name="gx-amount">
          <template #default="{ row }">{{ formatMoney(row.totalOriginalValue) }}</template>
        </el-table-column>
        <el-table-column label="净值总额" width="130" align="right" class-name="gx-amount">
          <template #default="{ row }">{{ formatMoney(row.totalNetValue) }}</template>
        </el-table-column>
        <el-table-column label="累计折旧" width="130" align="right" class-name="gx-amount">
          <template #default="{ row }">{{ formatMoney(row.totalAccumulatedDepreciation) }}</template>
        </el-table-column>
        <el-table-column label="本月折旧额" width="130" align="right" class-name="gx-amount">
          <template #default="{ row }">
            <span class="num-warning">{{ formatMoney(row.monthlyDepreciation) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" effect="dark" size="small">
              <el-icon class="status-icon">
                <CircleCheckFilled v-if="row.status === 'SUCCESS'" />
                <CircleCloseFilled v-else />
              </el-icon>
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="syncTime" label="同步时间" width="170" />
        <el-table-column prop="operatorName" label="操作人" width="90" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无同步记录" />
        </template>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchRecords"
          @current-change="fetchRecords"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="同步记录详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="批次号">{{ detailData.syncBatchNo ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="同步月份">{{ detailData.syncMonth }}</el-descriptions-item>
        <el-descriptions-item label="资产数量">{{ detailData.assetCount }} 件</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ detailData.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="原值总额">{{ formatMoney(detailData.totalOriginalValue) }} 元</el-descriptions-item>
        <el-descriptions-item label="净值总额">{{ formatMoney(detailData.totalNetValue) }} 元</el-descriptions-item>
        <el-descriptions-item label="累计折旧">{{ formatMoney(detailData.totalAccumulatedDepreciation) }} 元</el-descriptions-item>
        <el-descriptions-item label="本月折旧额">{{ formatMoney(detailData.monthlyDepreciation) }} 元</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailData.operatorName ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="同步时间">{{ detailData.syncTime ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark ?? '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import DataCard from '@/components/DataCard.vue'
import {
  syncDepreciationData,
  getFinanceSyncRecords,
  getFinanceSyncDetail,
  type FinanceSyncRecordItem
} from '@/api/finance'
import { Download, CircleCheckFilled, CircleCloseFilled, Refresh } from '@element-plus/icons-vue'
import { exportFinanceSyncRecords } from '@/api/export'

const syncMonth = ref(getDefaultMonth())
const syncing = ref(false)
const exporting = ref(false)
const loading = ref(false)
const tableData = ref<FinanceSyncRecordItem[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref<FinanceSyncRecordItem | null>(null)

// 总览计算
const latestRecord = computed(() => tableData.value[0] ?? null)
const totalSyncedDepreciation = computed(() =>
  tableData.value.reduce((sum, r) => sum + Number(r.monthlyDepreciation || 0), 0)
)

function getDefaultMonth() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  return `${y}-${m}`
}

function disabledDate(date: Date) {
  return date.getTime() > Date.now()
}

function formatMoney(val: any) {
  if (val == null) return '--'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getFinanceSyncRecords({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function handleSync() {
  if (!syncMonth.value) {
    ElMessage.warning('请选择同步月份')
    return
  }
  syncing.value = true
  try {
    const res = await syncDepreciationData(syncMonth.value)
    ElMessage.success(`模拟同步成功：${res.data.syncMonth}，资产 ${res.data.assetCount} 件，本月折旧 ${formatMoney(res.data.monthlyDepreciation)} 元`)
    pageNum.value = 1
    await fetchRecords()
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    syncing.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    await exportFinanceSyncRecords()
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

async function openDetail(id: number) {
  try {
    const res = await getFinanceSyncDetail(id)
    detailData.value = res.data
    detailVisible.value = true
  } catch {
    // 错误已在 request 拦截器中处理
  }
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-base);
  margin-bottom: var(--space-lg);
}
.panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-base);
  margin-bottom: var(--space-lg);
}
.status-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-lg);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-sm);
}
.status-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
}
.status-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
}
.status-icon {
  margin-right: 4px;
  vertical-align: -2px;
}
.sync-row {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-top: var(--space-base);
}
.sync-label {
  font-size: 14px;
  color: var(--color-text-secondary);
}
.month-cell {
  font-weight: 700;
  color: var(--color-text);
}
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-base);
}
.num-warning {
  color: var(--color-warning);
  font-weight: 600;
}
:deep(.gx-amount .cell) {
  font-variant-numeric: tabular-nums;
  font-feature-settings: 'tnum';
  color: var(--color-text);
}
</style>
