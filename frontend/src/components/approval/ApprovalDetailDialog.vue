<template>
  <el-dialog :model-value="visible" title="审批详情" width="640px" :close-on-click-modal="false" @update:model-value="$emit('update:visible', $event)">
    <div v-loading="loading">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="流程名称" :span="2">{{ detail.flowName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ businessTypeLabel(detail.businessType) }}</el-descriptions-item>
          <el-descriptions-item label="单据ID">{{ detail.businessId }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前节点">{{ detail.currentNodeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)" size="small">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ detail.startedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间" :span="2">{{ detail.completedAt || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:16px;">
          <div style="font-size:14px;font-weight:600;margin-bottom:12px;">审批记录</div>
          <el-empty v-if="!detail.records || detail.records.length === 0" description="暂无审批记录" :image-size="60" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="(item, index) in detail.records"
              :key="index"
              :timestamp="item.approvedAt || '-'"
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
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getApprovalDetail, type ApprovalDetail } from '@/api/approval'

const props = defineProps<{
  visible: boolean
  instanceId: number | null
}>()

defineEmits<{
  'update:visible': [value: boolean]
}>()

const loading = ref(false)
const detail = ref<ApprovalDetail | null>(null)

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

async function fetchDetail() {
  if (!props.instanceId) return
  loading.value = true
  try {
    const r = await getApprovalDetail(props.instanceId)
    if (r.code === 200) {
      detail.value = r.data
    } else {
      detail.value = null
    }
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val && props.instanceId) {
    fetchDetail()
  } else if (!val) {
    detail.value = null
  }
})

watch(() => props.instanceId, (val) => {
  if (val && props.visible) {
    fetchDetail()
  }
})
</script>

<style scoped>
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
