<template>
  <div style="background:#fff;border-radius:6px;border:1px solid var(--color-border);padding:24px;margin-top:16px;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
      <h3 style="margin:0;font-size:16px;color:#303133;">生命周期时间线</h3>
      <el-select v-model="eventType" placeholder="全部" size="small" style="width:140px;" @change="fetchTimeline">
        <el-option label="全部" value="" />
        <el-option label="入库" value="INBOUND" />
        <el-option label="领用" value="RECEIVE" />
        <el-option label="调拨" value="TRANSFER" />
        <el-option label="维修" value="REPAIR" />
        <el-option label="报废" value="SCRAP" />
        <el-option label="审批" value="APPROVAL" />
        <el-option label="操作日志" value="OPERATION_LOG" />
      </el-select>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && events.length === 0" description="暂无生命周期记录" />
      <el-timeline v-else>
        <el-timeline-item
          v-for="event in events"
          :key="event.id"
          :type="getTimelineType(event.eventType)"
          :timestamp="event.eventTime"
          placement="top"
        >
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px;">
            <span style="font-weight:600;color:#303133;">{{ event.title }}</span>
            <el-tag size="small" :type="getTagType(event.eventType)">{{ event.eventTypeName }}</el-tag>
          </div>
          <div style="color:#606266;font-size:13px;margin-bottom:4px;">
            操作人：{{ event.operatorName || '系统' }}
          </div>
          <div v-if="event.description" style="color:#606266;font-size:13px;margin-bottom:4px;">
            {{ event.description }}
          </div>
          <div v-if="event.beforeStatus || event.afterStatus" style="color:#909399;font-size:12px;margin-bottom:4px;">
            状态变化：{{ event.beforeStatus || '-' }} → {{ event.afterStatus || '-' }}
          </div>
          <div v-if="event.orderCode" style="color:#909399;font-size:12px;margin-bottom:4px;">
            单据编号：{{ event.orderCode }}
          </div>
          <div v-if="event.remark" style="color:#909399;font-size:12px;">
            备注：{{ event.remark }}
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getAssetTimeline, type AssetTimelineEvent } from '@/api/asset'

const props = defineProps<{
  assetId: number
}>()

const loading = ref(false)
const events = ref<AssetTimelineEvent[]>([])
const eventType = ref('')

async function fetchTimeline() {
  loading.value = true
  try {
    const params = eventType.value ? { eventType: eventType.value } : undefined
    const r = await getAssetTimeline(props.assetId, params)
    if (r.code === 200) {
      events.value = r.data || []
    } else {
      events.value = []
    }
  } catch {
    events.value = []
  } finally {
    loading.value = false
  }
}

function getTimelineType(eventType: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (eventType) {
    case 'INBOUND': return 'primary'
    case 'RECEIVE': return 'success'
    case 'TRANSFER': return 'warning'
    case 'REPAIR': return 'warning'
    case 'SCRAP': return 'danger'
    case 'APPROVAL': return 'info'
    case 'OPERATION_LOG': return 'info'
    default: return 'info'
  }
}

function getTagType(eventType: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  switch (eventType) {
    case 'INBOUND': return ''
    case 'RECEIVE': return 'success'
    case 'TRANSFER': return 'warning'
    case 'REPAIR': return 'warning'
    case 'SCRAP': return 'danger'
    case 'APPROVAL': return 'info'
    case 'OPERATION_LOG': return 'info'
    default: return 'info'
  }
}

watch(() => props.assetId, () => {
  if (props.assetId) fetchTimeline()
})

onMounted(() => {
  if (props.assetId) fetchTimeline()
})
</script>
