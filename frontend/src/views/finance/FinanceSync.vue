<template>
  <div>
    <PageHeader title="财务同步" description="将资产折旧数据同步至财务系统（现阶段为模拟对接，暂不接入真实用友/金蝶/SAP）。" />
    <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:20px;">
      <el-alert title="当前为模拟对接状态" type="info" show-icon :closable="false" description="本模块将在第三阶段接入真实财务系统对接能力。" style="margin-bottom:16px;" />

      <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px;">
        <span style="font-size:14px;color:var(--color-text-secondary);">同步月份：</span>
        <el-date-picker
          v-model="syncMonth"
          type="month"
          placeholder="选择月份"
          format="YYYY-MM"
          value-format="YYYY-MM"
          :disabled-date="disabledDate"
        />
        <el-button type="primary" :loading="syncing" @click="handleSync">同步本月折旧</el-button>
      </div>

      <el-divider />

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="同步时间" width="180">
          <template #default="{ row }">{{ row.createdAt }}</template>
        </el-table-column>
        <el-table-column prop="syncMonth" label="同步月份" width="100" />
        <el-table-column label="总金额" width="140">
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="recordCount" label="记录数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ row.status === 'SUCCESS' ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>

      <div style="display:flex;justify-content:flex-end;margin-top:12px;">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { syncDepreciation, getSyncRecords } from '@/api/finance'

const syncMonth = ref(getDefaultMonth())
const syncing = ref(false)
const loading = ref(false)
const tableData = ref<any[]>([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

function getDefaultMonth() {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  return `${y}-${m}`
}

function disabledDate(date: Date) {
  return date.getTime() > Date.now()
}

function formatAmount(val: string | number) {
  if (val == null) return '--'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function fetchRecords() {
  loading.value = true
  try {
    const res = await getSyncRecords({ pageNum: pageNum.value, pageSize: pageSize.value })
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
    const res = await syncDepreciation(syncMonth.value)
    if (res.data?.success) {
      const msg = res.data.message || '同步成功'
      ElMessage.success(`${msg}：${res.data.syncMonth} 共 ${res.data.recordCount} 条记录，合计 ${formatAmount(res.data.totalAmount)} 元`)
      pageNum.value = 1
      await fetchRecords()
    } else {
      ElMessage.warning(res.data?.message || '同步失败')
    }
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    syncing.value = false
  }
}

onMounted(() => {
  fetchRecords()
})
</script>