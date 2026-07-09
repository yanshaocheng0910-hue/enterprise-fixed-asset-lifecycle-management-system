<template>
  <div class="approval-progress" v-loading="loading">
    <div class="approval-header">
      <span class="approval-title">审批进度</span>
      <el-tag v-if="status" :type="statusTagType" size="small">{{ statusLabel }}</el-tag>
    </div>
    <el-empty v-if="!loading && records.length === 0" description="暂无审批记录" :image-size="60" />
    <el-timeline v-else>
      <el-timeline-item
        v-for="(item, index) in records"
        :key="index"
        :timestamp="formatTime(item.approvedAt)"
        :type="getTimelineType(item.action)"
        placement="top"
      >
        <div class="record-item">
          <div class="record-header">
            <el-tag :type="getTagType(item.action)" size="small">{{ actionLabel(item.action) }}</el-tag>
            <span class="record-approver">{{ item.approverName || '系统' }}</span>
          </div>
          <div class="record-node" v-if="item.nodeName">节点：{{ item.nodeName }}</div>
          <div class="record-comment">审批意见：{{ item.comment || '-' }}</div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { getApprovalRecords, type ApprovalRecordItem } from '@/api/approval'

const props = defineProps<{
  businessType: string
  businessId: number
  status?: string
}>()

const loading = ref(false)
const records = ref<ApprovalRecordItem[]>([])

const statusLabelMap: Record<string, string> = {
  DRAFT: '草稿',
  PENDING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const statusLabel = ref('')
const statusTagType = ref<'info' | 'warning' | 'success' | 'danger' | 'primary'>('info')

function updateStatusDisplay() {
  if (props.status) {
    statusLabel.value = statusLabelMap[props.status] || props.status
    const typeMap: Record<string, 'info' | 'warning' | 'success' | 'danger' | 'primary'> = {
      DRAFT: 'info',
      PENDING: 'warning',
      APPROVED: 'success',
      REJECTED: 'danger',
      COMPLETED: 'success',
      CANCELLED: 'info'
    }
    statusTagType.value = typeMap[props.status] || 'info'
  }
}

function actionLabel(action: string): string {
  const map: Record<string, string> = {
    SUBMIT: '提交申请',
    APPROVED: '审批通过',
    REJECTED: '审批驳回'
  }
  return map[action] || '审批记录'
}

function getTagType(action: string): 'info' | 'success' | 'danger' | 'warning' {
  const map: Record<string, 'info' | 'success' | 'danger' | 'warning'> = {
    SUBMIT: 'info',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[action] || 'info'
}

function getTimelineType(action: string): 'primary' | 'success' | 'danger' | 'info' {
  const map: Record<string, 'primary' | 'success' | 'danger' | 'info'> = {
    SUBMIT: 'primary',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[action] || 'info'
}

function formatTime(time: string | null): string {
  if (!time) return '-'
  return time
}

async function fetchRecords() {
  if (!props.businessId) return
  loading.value = true
  try {
    const r = await getApprovalRecords({ businessType: props.businessType, businessId: props.businessId })
    if (r.code === 200) {
      records.value = r.data || []
    } else {
      records.value = []
    }
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

watch(() => props.businessId, () => {
  if (props.businessId) fetchRecords()
})

watch(() => props.status, () => {
  updateStatusDisplay()
})

onMounted(() => {
  updateStatusDisplay()
  fetchRecords()
})
</script>

<style scoped>
.approval-progress {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border, #ebeef5);
}
.approval-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.approval-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.record-item {
  line-height: 1.6;
}
.record-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.record-approver {
  font-size: 13px;
  color: #606266;
}
.record-node {
  font-size: 12px;
  color: #909399;
}
.record-comment {
  font-size: 13px;
  color: #606266;
}
</style>
