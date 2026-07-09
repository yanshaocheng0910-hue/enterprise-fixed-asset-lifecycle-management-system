<template>
  <el-dialog :model-value="visible" :title="title" width="600px" :close-on-click-modal="false" @update:model-value="$emit('update:visible', $event)">
    <template v-if="data">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="单据编号" :span="2">{{ data.orderCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ data.assetCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ data.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="data.inboundType !== undefined" label="入库类型">{{ data.inboundType }}</el-descriptions-item>
        <el-descriptions-item v-if="data.supplier !== undefined" label="供应商">{{ data.supplier || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="data.handler !== undefined" label="经办人">{{ data.handler || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="data.receiver" label="领用人">{{ data.receiver }}</el-descriptions-item>
        <el-descriptions-item v-if="data.receiver" label="领用部门">{{ data.receiverDepartment || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="data.fromDepartment" label="调出部门">{{ data.fromDepartment }}</el-descriptions-item>
        <el-descriptions-item v-if="data.fromDepartment" label="调入部门">{{ data.toDepartment }}</el-descriptions-item>
        <el-descriptions-item v-if="data.faultDescription" label="故障描述" :span="2">{{ data.faultDescription }}</el-descriptions-item>
        <el-descriptions-item v-if="data.repairVendor !== undefined" label="维修商">{{ data.repairVendor || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="data.repairCost !== undefined" label="维修费用">{{ data.repairCost != null ? data.repairCost : '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="data.scrapReason" label="报废原因" :span="2">{{ data.scrapReason }}</el-descriptions-item>
        <el-descriptions-item v-if="data.scrapDate" label="报废日期">{{ data.scrapDate }}</el-descriptions-item>
        <el-descriptions-item label="变更前状态">{{ statusLabel(data.beforeStatus) }}</el-descriptions-item>
        <el-descriptions-item label="变更后状态">{{ statusLabel(data.afterStatus) }}</el-descriptions-item>
        <el-descriptions-item label="单据状态">{{ orderStatusLabel(data.status) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ data.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ data.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <ApprovalProgress
        v-if="businessType && data && data.id"
        :business-type="businessType"
        :business-id="data.id"
        :status="data.status"
      />
    </template>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { STATUS_MAP } from '@/utils/dict'
import ApprovalProgress from '@/components/approval/ApprovalProgress.vue'

defineProps<{
  visible: boolean
  data: any
  title: string
  businessType?: string
}>()

defineEmits<{
  'update:visible': [value: boolean]
}>()

function statusLabel(s: string) {
  if (!s) return '-'
  return STATUS_MAP[s] || s
}

function orderStatusLabel(s: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[s] || s || '-'
}
</script>
