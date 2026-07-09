<template>
  <div>
    <PageHeader :title="'盘点报告 - ' + (report?.task?.taskName || '')" description="盘点结果汇总与明细。" />

    <div v-loading="loading">
      <div v-if="report" style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:16px;margin-bottom:12px;">
        <el-descriptions :column="4" size="small" border>
          <el-descriptions-item label="任务编号">{{ report.task.taskCode }}</el-descriptions-item>
          <el-descriptions-item label="盘点范围">{{ scopeLabel(report.task.scopeType) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ report.task.endTime || '--' }}</el-descriptions-item>
          <el-descriptions-item label="资产总数">{{ report.totalCount }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top:16px;">
          <div style="font-size:14px;font-weight:500;margin-bottom:12px;">盘点结果统计</div>
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="stat-card" style="border-left:3px solid #67C23A;">
                <div class="stat-num">{{ report.normalCount }}</div>
                <div class="stat-label">正常</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="border-left:3px solid #E6A23C;">
                <div class="stat-num">{{ report.locationMismatchCount }}</div>
                <div class="stat-label">存放地点不符</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="border-left:3px solid #E6A23C;">
                <div class="stat-num">{{ report.keeperMismatchCount }}</div>
                <div class="stat-label">保管人不符</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" style="border-left:3px solid #F56C6C;">
                <div class="stat-num">{{ report.missingCount }}</div>
                <div class="stat-label">缺失</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;">
        <div style="margin-bottom:12px;display:flex;align-items:center;gap:12px;">
          <span style="font-size:14px;font-weight:500;">盘点明细</span>
          <div style="margin-left:auto;display:flex;gap:8px;align-items:center;">
            <span style="font-size:13px;color:var(--color-text-secondary);">筛选异常：</span>
            <el-select v-model="filterResult" placeholder="全部" size="small" style="width:120px" @change="applyFilter">
              <el-option label="全部" value="" />
              <el-option label="所有异常" value="ABNORMAL" />
              <el-option label="地点不符" value="LOCATION_MISMATCH" />
              <el-option label="保管人不符" value="KEEPER_MISMATCH" />
              <el-option label="缺失" value="MISSING" />
            </el-select>
          </div>
        </div>
        <el-table :data="filteredDetails" border stripe size="small" style="width:100%">
          <el-table-column prop="assetCode" label="资产编号" width="130" />
          <el-table-column prop="assetName" label="资产名称" min-width="110" />
          <el-table-column prop="expectedLocation" label="期望存放地" width="120" show-overflow-tooltip />
          <el-table-column prop="actualLocation" label="实际存放地" width="120" show-overflow-tooltip />
          <el-table-column prop="expectedKeeper" label="期望保管人" width="90" />
          <el-table-column prop="actualKeeper" label="实际保管人" width="90" />
          <el-table-column label="盘点结果" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.result === 'NORMAL'" type="success" size="small">正常</el-tag>
              <el-tag v-else-if="row.result === 'LOCATION_MISMATCH'" type="warning" size="small">地点不符</el-tag>
              <el-tag v-else-if="row.result === 'KEEPER_MISMATCH'" type="warning" size="small">保管人不符</el-tag>
              <el-tag v-else-if="row.result === 'MISMATCH'" type="danger" size="small">多项不符</el-tag>
              <el-tag v-else-if="row.result === 'MISSING'" type="danger" size="small">缺失</el-tag>
              <el-tag v-else type="info" size="small">{{ row.result }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />
        </el-table>
      </div>

      <div style="margin-top:12px;">
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { getInventoryReport } from '@/api/inventory'

const route = useRoute()
const router = useRouter()
const taskId = computed(() => Number(route.params.id))
const loading = ref(false)
const report = ref<any>(null)
const filterResult = ref('')

const filteredDetails = computed(() => {
  if (!filterResult.value) return report.value?.details || []
  if (filterResult.value === 'ABNORMAL') {
    return (report.value?.details || []).filter((r: any) => r.result !== 'NORMAL')
  }
  return (report.value?.details || []).filter((r: any) => r.result === filterResult.value)
})

function scopeLabel(s: string) {
  const map: Record<string, string> = { ALL: '全部', DEPARTMENT: '按部门', LOCATION: '按存放地点' }
  return map[s] || s
}

function goBack() {
  router.push('/inventory/tasks')
}

function applyFilter() {}

async function loadData() {
  loading.value = true
  try {
    const r = await getInventoryReport(taskId.value)
    if (r.code === 200) report.value = r.data
  } catch {} finally {
    loading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.stat-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}
.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
}
.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}
</style>
