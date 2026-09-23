<template>
  <el-tag :type="tagType" :effect="effect" size="small" class="status-tag">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  value: string
  category?: 'asset' | 'approval' | 'inventory' | 'finance' | 'warning' | 'lifecycle'
  effect?: 'light' | 'dark' | 'plain'
}>()

// 资产状态映射
const assetMap: Record<string, { label: string; type: string }> = {
  IDLE: { label: '闲置', type: 'info' },
  IN_USE: { label: '使用中', type: 'success' },
  TRANSFERRING: { label: '调拨中', type: 'primary' },
  REPAIRING: { label: '维修中', type: 'warning' },
  WAITING_SCRAP: { label: '待报废', type: 'warning' },
  SCRAPPED: { label: '已报废', type: 'danger' },
  INVENTORY_ABNORMAL: { label: '盘点异常', type: 'danger' }
}

// 审批状态
const approvalMap: Record<string, { label: string; type: string }> = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' },
  PROCESSING: { label: '审批中', type: 'primary' }
}

// 盘点任务状态
const inventoryMap: Record<string, { label: string; type: string }> = {
  PENDING: { label: '待开始', type: 'info' },
  IN_PROGRESS: { label: '进行中', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  ABNORMAL: { label: '有异常', type: 'danger' }
}

// 财务同步状态
const financeMap: Record<string, { label: string; type: string }> = {
  SUCCESS: { label: '成功', type: 'success' },
  FAILED: { label: '失败', type: 'danger' },
  SYNCING: { label: '同步中', type: 'primary' }
}

// 预警等级
const warningMap: Record<string, { label: string; type: string }> = {
  HIGH: { label: '高风险', type: 'danger' },
  MEDIUM: { label: '中风险', type: 'warning' },
  LOW: { label: '低风险', type: 'info' }
}

// 生命周期业务类型
const lifecycleMap: Record<string, { label: string; type: string }> = {
  INBOUND: { label: '入库', type: 'info' },
  RECEIVE: { label: '领用', type: 'success' },
  TRANSFER: { label: '调拨', type: 'warning' },
  REPAIR: { label: '维修', type: 'danger' },
  SCRAP: { label: '报废', type: 'danger' }
}

const config = computed(() => {
  const maps: Record<string, Record<string, { label: string; type: string }>> = {
    asset: assetMap,
    approval: approvalMap,
    inventory: inventoryMap,
    finance: financeMap,
    warning: warningMap,
    lifecycle: lifecycleMap
  }
  const map = maps[props.category || 'asset'] || assetMap
  return map[props.value] || { label: props.value, type: 'info' }
})

const label = computed(() => config.value.label)
const tagType = computed(() => config.value.type as any)
</script>

<style scoped>
.status-tag {
  font-weight: 500;
}
</style>
