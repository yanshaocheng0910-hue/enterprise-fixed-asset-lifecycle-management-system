<template>
  <div>
    <PageHeader :title="'执行盘点 - ' + (task?.taskName || '')" description="逐项核对资产的存放地点和使用人，记录实盘数据。" />

    <div v-loading="loading">
      <div v-if="task" style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:16px;margin-bottom:12px;">
        <el-descriptions :column="4" size="small" border>
          <el-descriptions-item label="任务编号">{{ task.taskCode }}</el-descriptions-item>
          <el-descriptions-item label="盘点范围">{{ scopeLabel(task.scopeType) }}</el-descriptions-item>
          <el-descriptions-item label="总资产数">{{ task.totalCount }}</el-descriptions-item>
          <el-descriptions-item label="已完成">{{ task.scannedCount }}/{{ task.totalCount }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:12px;display:flex;gap:12px;">
          <el-button type="primary" @click="handleBatchScan" :disabled="task.scannedCount === task.totalCount">一键扫描全部</el-button>
          <el-button type="success" @click="handleComplete" :disabled="task.scannedCount === 0">完成盘点</el-button>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </div>

      <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;">
        <div style="margin-bottom:12px;display:flex;align-items:center;gap:12px;">
          <span style="font-size:14px;font-weight:500;">资产盘点明细</span>
          <div style="margin-left:auto;display:flex;gap:8px;align-items:center;">
            <span style="font-size:13px;color:var(--color-text-secondary);">筛选：</span>
            <el-input v-model="searchKeyword" placeholder="搜索资产编号/名称" size="small" clearable style="width:180px" @input="applyFilter" />
            <el-select v-model="filterResult" placeholder="全部" size="small" style="width:120px" @change="applyFilter">
              <el-option label="全部" value="" />
              <el-option label="待盘点" value="PENDING" />
              <el-option label="正常" value="NORMAL" />
              <el-option label="地点不符" value="LOCATION_MISMATCH" />
              <el-option label="保管人不符" value="KEEPER_MISMATCH" />
              <el-option label="缺失" value="MISSING" />
            </el-select>
          </div>
        </div>
        <el-table :data="filteredRecords" border stripe size="small" style="width:100%">
          <el-table-column prop="assetCode" label="资产编号" width="130" />
          <el-table-column prop="assetName" label="资产名称" min-width="110" />
          <el-table-column prop="expectedLocation" label="期望存放地" width="120" show-overflow-tooltip />
          <el-table-column label="实际存放地" width="120">
            <template #default="{ row }">
              <el-input v-if="row.result === 'PENDING' || row.result === 'LOCATION_MISMATCH' || row.result === 'MISMATCH'" v-model="row.actualLocation" size="small" placeholder="输入实际地点" />
              <span v-else>{{ row.actualLocation || row.expectedLocation }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="expectedKeeper" label="期望保管人" width="90" />
          <el-table-column label="实际保管人" width="100">
            <template #default="{ row }">
              <el-input v-if="row.result === 'PENDING' || row.result === 'KEEPER_MISMATCH' || row.result === 'MISMATCH'" v-model="row.actualKeeper" size="small" placeholder="输入保管人" />
              <span v-else>{{ row.actualKeeper || row.expectedKeeper }}</span>
            </template>
          </el-table-column>
          <el-table-column label="盘点结果" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.result === 'PENDING'" type="info" size="small">待盘点</el-tag>
              <el-tag v-else-if="row.result === 'NORMAL'" type="success" size="small">正常</el-tag>
              <el-tag v-else-if="row.result === 'LOCATION_MISMATCH'" type="warning" size="small">地点不符</el-tag>
              <el-tag v-else-if="row.result === 'KEEPER_MISMATCH'" type="warning" size="small">保管人不符</el-tag>
              <el-tag v-else-if="row.result === 'MISMATCH'" type="danger" size="small">多项不符</el-tag>
              <el-tag v-else-if="row.result === 'MISSING'" type="danger" size="small">缺失</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.result === 'PENDING'" type="primary" size="small" @click="handleScan(row)">确认</el-button>
              <el-button v-else-if="row.result !== 'MISSING'" link type="warning" size="small" @click="resetRecord(row)">重盘</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getInventoryTaskDetail, getInventoryRecords, scanInventoryRecord, completeInventoryTask, batchScanPending } from '@/api/inventory'

const route = useRoute()
const router = useRouter()
const taskId = computed(() => Number(route.params.id))
const loading = ref(false)
const task = ref<any>(null)
const allRecords = ref<any[]>([])
const filterResult = ref('')
const searchKeyword = ref('')

const filteredRecords = computed(() => {
  let list = allRecords.value
  if (filterResult.value) {
    list = list.filter((r: any) => r.result === filterResult.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter((r: any) => 
      (r.assetCode && r.assetCode.toLowerCase().includes(kw)) ||
      (r.assetName && r.assetName.toLowerCase().includes(kw))
    )
  }
  return list
})

function scopeLabel(s: string) {
  const map: Record<string, string> = { ALL: '全部', DEPARTMENT: '按部门', LOCATION: '按存放地点' }
  return map[s] || s
}

function goBack() {
  router.push('/inventory/tasks')
}

async function loadData() {
  loading.value = true
  try {
    const [taskRes, recordsRes] = await Promise.all([
      getInventoryTaskDetail(taskId.value),
      getInventoryRecords(taskId.value)
    ])
    if (taskRes.code === 200) task.value = taskRes.data
    if (recordsRes.code === 200) allRecords.value = recordsRes.data
  } catch {} finally {
    loading.value = false
  }
}

function applyFilter() {}

async function handleScan(row: any) {
  try {
    const data: any = { recordId: row.id }
    if (row.actualLocation) data.actualLocation = row.actualLocation
    if (row.actualKeeper) data.actualKeeper = row.actualKeeper

    const r = await scanInventoryRecord(data)
    if (r.code === 200) {
      ElMessage.success('已确认')
      await loadData()
    }
  } catch {}
}

async function resetRecord(row: any) {
  try {
    const r = await scanInventoryRecord({
      recordId: row.id,
      actualLocation: '',
      actualKeeper: '',
      result: 'PENDING'
    })
    if (r.code === 200) {
      ElMessage.success('已重置')
      await loadData()
    }
  } catch {}
}

async function handleBatchScan() {
  try {
    const r = await batchScanPending(taskId.value)
    if (r.code === 200) {
      ElMessage.success('已批量扫描所有待盘点资产')
      await loadData()
    }
  } catch {}
}

async function handleComplete() {
  try {
    const r = await completeInventoryTask(taskId.value)
    if (r.code === 200) {
      ElMessage.success('盘点已完成\uff01')
      router.push('/inventory/tasks/' + taskId.value + '/report')
    }
  } catch {}
}

onMounted(() => { loadData() })
</script>
