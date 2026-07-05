<template>
  <el-tag :type="tagType" :color="bgColor" effect="dark" size="small">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { STATUS_MAP } from '@/utils/dict'

const props = defineProps<{
  status: string
}>()

const statusColorMap: Record<string, string> = {
  IDLE: '#8B9DC3',
  IN_USE: '#4F8F7B',
  TRANSFERRING: '#2563EB',
  REPAIRING: '#D97706',
  WAITING_SCRAP: '#EA580C',
  SCRAPPED: '#B42318',
  INVENTORY_ABNORMAL: '#B42318'
}

const label = computed(() => STATUS_MAP[props.status] || props.status)
const bgColor = computed(() => statusColorMap[props.status] || '#909399')

const tagType = computed(() => {
  switch (props.status) {
    case 'IN_USE': return 'success'
    case 'IDLE': return 'info'
    case 'REPAIRING': case 'WAITING_SCRAP': return 'warning'
    case 'SCRAPPED': case 'INVENTORY_ABNORMAL': return 'danger'
    case 'TRANSFERRING': return 'primary'
    default: return 'info'
  }
})
</script>
