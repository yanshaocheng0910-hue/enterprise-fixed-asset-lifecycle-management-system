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

      <!-- 快速盘点：输入资产编号回车即可盘点 -->
      <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:16px;margin-bottom:12px;">
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:12px;">
          <el-icon style="font-size:18px;color:var(--color-primary,#1F4E79);"><Search /></el-icon>
          <span style="font-size:15px;font-weight:600;">快速盘点</span>
          <span style="font-size:13px;color:var(--color-text-secondary);">输入资产编号后回车，自动匹配并完成盘点</span>
        </div>
        <div style="display:flex;gap:8px;align-items:center;">
          <el-input
            ref="quickInputRef"
            v-model="quickInput"
            placeholder="请输入资产编号，如 FA2024040011"
            size="large"
            clearable
            style="width:340px"
            @keyup.enter="handleQuickScan"
          />
          <el-button type="primary" size="large" @click="handleQuickScan">匹配</el-button>
          <el-button type="success" size="large" @click="openScanner">
            <el-icon style="margin-right:4px"><Aim /></el-icon>扫码盘点
          </el-button>
        </div>
        <!-- 匹配到的资产信息卡 -->
        <div v-if="matchedRecord" style="margin-top:12px;padding:12px;border:1px solid var(--color-border);border-radius:6px;background:#fafbfc;">
          <div style="display:flex;align-items:center;gap:12px;margin-bottom:10px;">
            <el-tag type="success" size="small" effect="dark">已匹配</el-tag>
            <span style="font-weight:600;font-size:14px;">{{ matchedRecord.assetCode }}</span>
            <span style="color:var(--color-text-secondary);font-size:13px;">{{ matchedRecord.assetName }}</span>
            <el-tag v-if="matchedRecord.result !== 'PENDING'" size="small" :type="matchedRecord.result === 'NORMAL' ? 'success' : 'warning'" style="margin-left:auto;">已盘点：{{ resultLabel(matchedRecord.result) }}</el-tag>
          </div>
          <el-descriptions :column="2" size="small" border style="margin-bottom:10px;">
            <el-descriptions-item label="期望存放地">{{ matchedRecord.expectedLocation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="期望保管人">{{ matchedRecord.expectedKeeper || '-' }}</el-descriptions-item>
          </el-descriptions>
          <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;">
            <span style="font-size:13px;color:var(--color-text-secondary);">实际地点：</span>
            <el-input v-model="quickActualLocation" size="small" placeholder="实际存放地" style="width:160px" />
            <span style="font-size:13px;color:var(--color-text-secondary);">实际保管人：</span>
            <el-input v-model="quickActualKeeper" size="small" placeholder="实际保管人" style="width:120px" />
            <el-button type="success" size="small" @click="handleQuickConfirm">确认正常</el-button>
            <el-button type="danger" size="small" plain @click="handleQuickMissing">标记缺失</el-button>
            <el-button size="small" text @click="resetQuickScan">取消</el-button>
          </div>
        </div>
        <!-- 未匹配提示 -->
        <div v-if="quickNotFound" style="margin-top:10px;color:var(--color-danger,#D03050);font-size:13px;">
          未找到编号为「{{ quickNotFound }}」的盘点记录，请检查编号是否正确
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

    <!-- 扫码弹窗 -->
    <el-dialog v-model="scannerVisible" title="扫码盘点（连续扫码）" width="520px" :close-on-click-modal="false" @close="closeScanner">
      <div v-if="scannerError" style="text-align:center;padding:16px;">
        <el-icon style="font-size:36px;color:var(--color-danger,#D03050);"><Aim /></el-icon>
        <div style="margin-top:8px;font-size:14px;color:var(--color-danger,#D03050);font-weight:600;">摄像头无法访问</div>
        <div style="margin-top:6px;font-size:13px;color:var(--color-text-secondary);">{{ scannerError }}</div>
        <div style="margin-top:12px;">
          <el-button type="primary" size="small" @click="retryScanner">重试摄像头</el-button>
        </div>
      </div>
      <div v-else style="text-align:center;">
        <video ref="scannerVideoRef" style="width:100%;max-width:400px;border:1px solid var(--color-border);border-radius:6px;background:#000;" />
        <div style="margin-top:10px;color:var(--color-text-secondary);font-size:13px;">
          将资产二维码对准摄像头，识别后自动盘点，可连续扫描多个
        </div>
      </div>
      <div v-if="scannedList.length > 0" style="margin-top:12px;max-height:200px;overflow-y:auto;border:1px solid var(--color-border);border-radius:6px;padding:8px;">
        <div style="font-size:13px;font-weight:600;margin-bottom:6px;color:var(--color-text-primary);">已扫描 {{ scannedList.length }} 个</div>
        <div v-for="(item, idx) in scannedList" :key="idx" style="display:flex;justify-content:space-between;align-items:center;padding:4px 0;border-bottom:1px solid #f0f0f0;font-size:13px;">
          <span>{{ item.code }}</span>
          <el-tag :type="item.success ? 'success' : 'danger'" size="small">{{ item.success ? '成功' : '失败' }}</el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeScanner">完成扫码（{{ scannedList.length }}）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { Aim } from '@element-plus/icons-vue'
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

// 快速盘点
const quickInput = ref('')
const quickInputRef = ref<any>(null)
const matchedRecord = ref<any>(null)
const quickActualLocation = ref('')
const quickActualKeeper = ref('')
const quickNotFound = ref('')

// 扫码盘点
const scannerVisible = ref(false)
const scannerVideoRef = ref<HTMLVideoElement | null>(null)
const scannedList = ref<{ code: string; success: boolean }[]>([])
const scannerError = ref('')
let zxingReader: any = null
let zxingControls: any = null
let lastScanCode = ''
let lastScanTime = 0

// 扫码盘点（连续模式）
async function startCameraScan() {
  scannerError.value = ''
  await nextTick()
  try {
    // 先检测摄像头权限
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true })
      stream.getTracks().forEach((t: MediaStreamTrack) => t.stop())
    } catch (e: any) {
      if (e.name === 'NotAllowedError') {
        scannerError.value = '浏览器拒绝了摄像头权限。请点击地址栏锁图标，允许摄像头访问后重试。'
      } else if (e.name === 'NotFoundError') {
        scannerError.value = '未检测到摄像头设备，请检查设备连接后重试。'
      } else {
        scannerError.value = '摄像头不可用：' + e.message
      }
      return
    }
    const { BrowserMultiFormatReader } = await import('@zxing/library')
    if (zxingReader) { try { zxingReader.reset() } catch {} zxingReader = null }
    zxingReader = new BrowserMultiFormatReader()
    zxingReader.decodeFromVideoDevice(undefined, scannerVideoRef.value, async (result: any) => {
      if (result) {
        const text = result.getText()
        let code = ''
        try { const obj = JSON.parse(text); code = obj.code || '' } catch { code = text.trim() }
        if (!code) return
        const now = Date.now()
        if (code === lastScanCode && now - lastScanTime < 3000) return
        lastScanCode = code
        lastScanTime = now
        quickInput.value = code
        const matched = handleQuickScan()
        if (matched) {
          try {
            await handleQuickConfirm()
            scannedList.value.push({ code, success: true })
            ElMessage.success(`已盘点：${code}`)
          } catch {
            scannedList.value.push({ code, success: false })
            ElMessage.warning(`盘点失败：${code}`)
          }
        } else {
          scannedList.value.push({ code, success: false })
          ElMessage.warning(`未找到资产：${code}`)
        }
      }
    })
  } catch (err: any) {
    scannerError.value = '摄像头初始化失败：' + (err?.message || '未知错误')
  }
}

async function openScanner() {
  scannedList.value = []
  lastScanCode = ''
  lastScanTime = 0
  scannerError.value = ''
  scannerVisible.value = true
  await startCameraScan()
}

function retryScanner() {
  scannerError.value = ''
  startCameraScan()
}

function closeScanner() {
  if (zxingReader) {
    try { zxingReader.reset() } catch {}
    zxingReader = null
  }
  scannerVisible.value = false
  // 关闭后刷新数据
  if (scannedList.value.length > 0) {
    loadData()
  }
}

function resultLabel(s: string): string {
  const map: Record<string, string> = { PENDING: '待盘点', NORMAL: '正常', LOCATION_MISMATCH: '地点不符', KEEPER_MISMATCH: '保管人不符', MISMATCH: '多项不符', MISSING: '缺失' }
  return map[s] || s
}

function handleQuickScan(): boolean {
  const code = quickInput.value.trim()
  if (!code) return false
  quickNotFound.value = ''
  matchedRecord.value = null
  // 在盘点记录中匹配资产编号（支持模糊匹配）
  const found = allRecords.value.find((r: any) => r.assetCode && r.assetCode.toLowerCase() === code.toLowerCase())
  if (!found) {
    // 尝试模糊匹配
    const fuzzy = allRecords.value.find((r: any) => r.assetCode && r.assetCode.toLowerCase().includes(code.toLowerCase()))
    if (!fuzzy) {
      quickNotFound.value = code
      return false
    }
    matchedRecord.value = fuzzy
  } else {
    matchedRecord.value = found
  }
  // 默认填入期望值，方便快速确认
  quickActualLocation.value = matchedRecord.value.expectedLocation || ''
  quickActualKeeper.value = matchedRecord.value.expectedKeeper || ''
  if (matchedRecord.value.result !== 'PENDING') {
    ElMessage.info(`该资产已盘点：${resultLabel(matchedRecord.value.result)}，可重新盘点`)
  }
  return true
}

async function handleQuickConfirm() {
  if (!matchedRecord.value) return
  try {
    const r = await scanInventoryRecord({
      recordId: matchedRecord.value.id,
      actualLocation: quickActualLocation.value,
      actualKeeper: quickActualKeeper.value
    })
    if (r.code === 200) {
      // 根据实际输入判断结果类型，给出提示
      const locOk = !quickActualLocation.value || quickActualLocation.value.trim() === (matchedRecord.value.expectedLocation || '').trim()
      const keeperOk = !quickActualKeeper.value || quickActualKeeper.value.trim() === (matchedRecord.value.expectedKeeper || '').trim()
      if (locOk && keeperOk) {
        ElMessage.success(`${matchedRecord.value.assetCode} 盘点正常`)
      } else {
        ElMessage.warning(`${matchedRecord.value.assetCode} 盘点异常：${!locOk ? '地点不符 ' : ''}${!keeperOk ? '保管人不符' : ''}`)
      }
      resetQuickScan()
      await loadData()
    }
  } catch {}
}

async function handleQuickMissing() {
  if (!matchedRecord.value) return
  try {
    const r = await scanInventoryRecord({
      recordId: matchedRecord.value.id,
      result: 'MISSING'
    })
    if (r.code === 200) {
      ElMessage.success(`${matchedRecord.value.assetCode} 已标记为缺失`)
      resetQuickScan()
      await loadData()
    }
  } catch {}
}

function resetQuickScan() {
  quickInput.value = ''
  matchedRecord.value = null
  quickActualLocation.value = ''
  quickActualKeeper.value = ''
  quickNotFound.value = ''
  nextTick(() => { quickInputRef.value?.focus() })
}

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
  } catch (err: any) {
    ElMessage.error('数据加载失败：' + (err?.message || '未知错误'))
  } finally {
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
      ElMessage.success('已批量扫描所有待盘点资产，全部盘点成功')
      await loadData()
    } else {
      ElMessage.warning(r.message || '批量扫描失败')
    }
  } catch (err: any) {
    ElMessage.error('批量扫描失败：' + (err?.message || '未知错误'))
  }
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
