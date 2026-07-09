<template>
  <div>
    <PageHeader title="我的已办" description="查看已处理的审批记录。" />

    <div class="table-wrapper">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template #default="{ row }">{{ businessTypeLabel(row.businessType) }}</template>
        </el-table-column>
        <el-table-column prop="orderCode" label="单据编号" width="160" show-overflow-tooltip />
        <el-table-column prop="assetName" label="资产名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="action" label="审批动作" width="100">
          <template #default="{ row }">
            <el-tag :type="actionTagType(row.action)" size="small">{{ actionLabel(row.action) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="审批意见" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.comment || '-' }}</template>
        </el-table-column>
        <el-table-column prop="approverName" label="审批人" width="100" />
        <el-table-column prop="approvedAt" label="审批时间" width="160" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </div>

    <ApprovalDetailDialog v-model:visible="detailVisible" :instance-id="detailInstanceId" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import ApprovalDetailDialog from '@/components/approval/ApprovalDetailDialog.vue'
import { getApprovalDonePage, type ApprovalDoneItem } from '@/api/approval'

const loading = ref(false)
const tableData = ref<ApprovalDoneItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const detailInstanceId = ref<number | null>(null)

function businessTypeLabel(type: string): string {
  const map: Record<string, string> = {
    RECEIVE: '领用',
    TRANSFER: '调拨',
    REPAIR: '维修',
    SCRAP: '报废',
    INBOUND: '入库'
  }
  return map[type] || type
}

function actionLabel(action: string): string {
  const map: Record<string, string> = {
    SUBMIT: '提交申请',
    APPROVED: '审批通过',
    REJECTED: '审批驳回'
  }
  return map[action] || action
}

function actionTagType(action: string): 'info' | 'success' | 'danger' {
  const map: Record<string, 'info' | 'success' | 'danger'> = {
    SUBMIT: 'info',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[action] || 'info'
}

function statusLabel(s: string): string {
  const map: Record<string, string> = {
    PENDING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    COMPLETED: '已完成'
  }
  return map[s] || s
}

function statusTagType(s: string): 'info' | 'warning' | 'success' | 'danger' {
  const map: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    COMPLETED: 'success'
  }
  return map[s] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    const r = await getApprovalDonePage(params)
    if (r.code === 200) {
      tableData.value = r.data.records
      total.value = r.data.total
    }
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function openDetail(row: ApprovalDoneItem) {
  detailInstanceId.value = row.instanceId
  detailVisible.value = true
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.table-wrapper {
  background: #fff;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
}
</style>
