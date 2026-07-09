<template>
  <div>
    <PageHeader title="我的待办" description="待处理的审批事项">
      <template #actions>
        <el-button type="success" :loading="exporting" @click="handleExport">
          <el-icon><Download /></el-icon>导出审批记录
        </el-button>
      </template>
    </PageHeader>

    <div class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template #default="{ row }">
            <el-tag :type="businessTypeTagType(row.businessType)" size="small" effect="light">{{ businessTypeLabel(row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessId" label="单据ID" width="80" />
        <el-table-column prop="flowName" label="流程名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nodeName" label="当前节点" width="120" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="startedAt" label="提交时间" width="160">
          <template #default="{ row }">{{ row.startedAt || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">查看详情</el-button>
            <el-button link type="success" size="small" @click="openApprove(row)">通过</el-button>
            <el-button link type="danger" size="small" @click="openReject(row)">驳回</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无待办事项" :image-size="80" />
        </template>
      </el-table>
      <div class="pagination-wrapper">
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
    </div>

    <ApprovalDetailDialog v-model:visible="detailVisible" :instance-id="detailInstanceId" />

    <el-dialog v-model="actionVisible" :title="actionTitle" width="480px" :close-on-click-modal="false">
      <el-form :model="actionForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="actionForm.comment" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import ApprovalDetailDialog from '@/components/approval/ApprovalDetailDialog.vue'
import { getApprovalTodoPage, approveApproval, rejectApproval, type ApprovalTodoItem } from '@/api/approval'
import { Download } from '@element-plus/icons-vue'
import { exportApprovalRecords } from '@/api/export'

const loading = ref(false)
const exporting = ref(false)
const tableData = ref<ApprovalTodoItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const detailInstanceId = ref<number | null>(null)

const actionVisible = ref(false)
const actionLoading = ref(false)
const actionTitle = ref('')
const actionType = ref<'APPROVED' | 'REJECTED'>('APPROVED')
const currentInstanceId = ref<number | null>(null)
const actionForm = reactive({ comment: '' })

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

function businessTypeTagType(type: string): 'info' | 'success' | 'warning' | 'danger' | '' {
  const map: Record<string, 'info' | 'success' | 'warning' | 'danger' | ''> = {
    INBOUND: 'info',
    RECEIVE: 'success',
    TRANSFER: 'warning',
    REPAIR: 'danger',
    SCRAP: ''
  }
  return map[type] || 'info'
}

function statusLabel(s: string): string {
  const map: Record<string, string> = {
    PENDING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }
  return map[s] || s
}

function statusTagType(s: string): 'info' | 'warning' | 'success' | 'danger' {
  const map: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[s] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    const r = await getApprovalTodoPage(params)
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

async function handleExport() {
  exporting.value = true
  try {
    await exportApprovalRecords()
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

function openDetail(row: ApprovalTodoItem) {
  detailInstanceId.value = row.instanceId
  detailVisible.value = true
}

function openApprove(row: ApprovalTodoItem) {
  currentInstanceId.value = row.instanceId
  actionType.value = 'APPROVED'
  actionTitle.value = '审批通过'
  actionForm.comment = ''
  actionVisible.value = true
}

function openReject(row: ApprovalTodoItem) {
  currentInstanceId.value = row.instanceId
  actionType.value = 'REJECTED'
  actionTitle.value = '审批驳回'
  actionForm.comment = ''
  actionVisible.value = true
}

async function submitAction() {
  if (!currentInstanceId.value) return
  actionLoading.value = true
  try {
    const data = { action: actionType.value, comment: actionForm.comment || undefined }
    if (actionType.value === 'APPROVED') {
      await approveApproval(currentInstanceId.value, data)
    } else {
      await rejectApproval(currentInstanceId.value, data)
    }
    ElMessage.success(actionType.value === 'APPROVED' ? '审批通过成功' : '审批驳回成功')
    actionVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.table-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-lg);
}
.pagination-wrapper {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: flex-end;
}
</style>
