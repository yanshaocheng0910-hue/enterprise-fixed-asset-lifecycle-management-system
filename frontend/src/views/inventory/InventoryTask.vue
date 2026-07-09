<template>
  <div>
    <PageHeader title="盘点任务" description="创建和管理资产盘点任务，支持按部门、地点、全部资产进行盘点核对。" />

    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 160px" @change="loadTasks">
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已完成" value="COMPLETED" />
      </el-select>
      <el-button type="primary" @click="openCreateDialog">新建盘点任务</el-button>
      <el-button type="success" :loading="exporting" @click="handleExportTasks">
        <el-icon><Download /></el-icon>导出任务列表
      </el-button>
    </div>

    <!-- 任务列表 -->
    <el-table :data="taskList" v-loading="loading" border style="width: 100%">
      <el-table-column prop="taskCode" label="任务编号" width="150" />
      <el-table-column prop="taskName" label="任务名称" min-width="150" />
      <el-table-column label="范围类型" width="120">
        <template #default="{ row }">{{ scopeTypeText(row.scopeType) }}</template>
      </el-table-column>
      <el-table-column label="范围描述" min-width="140">
        <template #default="{ row }">{{ scopeDescription(row) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="进度" width="110">
        <template #default="{ row }">{{ row.completedRecords }} / {{ row.totalRecords }}</template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170">
        <template #default="{ row }">{{ row.endTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openRecordsDialog(row)">查看明细</el-button>
          <el-button
            size="small"
            type="success"
            v-if="row.status === 'IN_PROGRESS'"
            @click="handleComplete(row)"
          >完成任务</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无盘点任务" />
      </template>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadTasks"
        @current-change="loadTasks"
      />
    </div>

    <!-- 新建任务弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建盘点任务" width="520px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="createForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="范围类型" prop="scopeType">
          <el-radio-group v-model="createForm.scopeType">
            <el-radio label="ALL">全部资产</el-radio>
            <el-radio label="DEPARTMENT">按部门</el-radio>
            <el-radio label="LOCATION">按地点</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="部门"
          prop="department"
          v-if="createForm.scopeType === 'DEPARTMENT'"
        >
          <el-input v-model="createForm.department" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item
          label="地点"
          prop="location"
          v-if="createForm.scopeType === 'LOCATION'"
        >
          <el-input v-model="createForm.location" placeholder="请输入地点" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 明细弹窗 -->
    <el-dialog v-model="recordsDialogVisible" title="盘点明细" width="95%" top="3vh">
      <el-descriptions :column="3" border v-if="currentTask">
        <el-descriptions-item label="任务编号">{{ currentTask.taskCode }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(currentTask.status) }}</el-descriptions-item>
        <el-descriptions-item label="进度">{{ currentTask.completedRecords }} / {{ currentTask.totalRecords }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentTask.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentTask.endTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="recordList" v-loading="recordsLoading" border style="margin-top: 16px">
        <el-table-column prop="assetCode" label="资产编号" width="150" />
        <el-table-column prop="assetName" label="资产名称" min-width="140" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="expectedLocation" label="应在地点" min-width="130" />
        <el-table-column label="实际地点" min-width="130">
          <template #default="{ row }">{{ row.actualLocation || '-' }}</template>
        </el-table-column>
        <el-table-column prop="expectedKeeper" label="应在保管人" width="110" />
        <el-table-column label="实际保管人" width="110">
          <template #default="{ row }">{{ row.actualKeeper || '-' }}</template>
        </el-table-column>
        <el-table-column label="盘点结果" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.result" :type="resultTagType(row.result)">{{ resultText(row.result) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="盘点时间" width="170">
          <template #default="{ row }">{{ row.scannedAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="120">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              @click="openEditRecordDialog(row)"
              :disabled="currentTask?.status === 'COMPLETED'"
            >编辑</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无盘点明细" />
        </template>
      </el-table>

      <template #footer>
        <el-button size="small" type="success" :loading="exporting" @click="handleExportRecords">
          <el-icon><Download /></el-icon>导出明细
        </el-button>
        <el-button @click="recordsDialogVisible = false">关闭</el-button>
        <el-button
          type="success"
          v-if="currentTask?.status === 'IN_PROGRESS'"
          @click="handleComplete(currentTask)"
        >完成任务</el-button>
      </template>
    </el-dialog>

    <!-- 编辑明细弹窗 -->
    <el-dialog v-model="editRecordDialogVisible" title="录入盘点结果" width="520px">
      <el-form :model="recordForm" :rules="recordRules" ref="recordFormRef" label-width="100px">
        <el-form-item label="资产编号">
          <span>{{ currentRecord?.assetCode }}</span>
        </el-form-item>
        <el-form-item label="资产名称">
          <span>{{ currentRecord?.assetName }}</span>
        </el-form-item>
        <el-form-item label="应在地点">
          <span>{{ currentRecord?.expectedLocation || '-' }}</span>
        </el-form-item>
        <el-form-item label="应在保管人">
          <span>{{ currentRecord?.expectedKeeper || '-' }}</span>
        </el-form-item>
        <el-form-item label="实际地点" prop="actualLocation">
          <el-input v-model="recordForm.actualLocation" placeholder="请输入实际地点" />
        </el-form-item>
        <el-form-item label="实际保管人" prop="actualKeeper">
          <el-input v-model="recordForm.actualKeeper" placeholder="请输入实际保管人" />
        </el-form-item>
        <el-form-item label="盘点结果" prop="result">
          <el-select v-model="recordForm.result" placeholder="请选择盘点结果" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="地点不符" value="LOCATION_MISMATCH" />
            <el-option label="保管人不符" value="KEEPER_MISMATCH" />
            <el-option label="丢失" value="LOST" />
            <el-option label="账外资产" value="EXTRA" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.remark" type="textarea" :rows="3" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editRecordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="handleUpdateRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import {
  getInventoryTaskPage,
  createInventoryTask,
  getInventoryRecords,
  updateInventoryRecord,
  completeInventoryTask,
  type InventoryTaskItem,
  type InventoryRecordItem
} from '@/api/inventory'
import { Download } from '@element-plus/icons-vue'
import { exportInventoryTasks, exportInventoryTaskRecords } from '@/api/export'

// ===== 任务列表 =====
const loading = ref(false)
const exporting = ref(false)
const taskList = ref<InventoryTaskItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filterStatus = ref('')

async function loadTasks() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await getInventoryTaskPage(params)
    taskList.value = res.data.records
    total.value = res.data.total
  } catch (e: any) {
    ElMessage.error(e?.message || '加载任务列表失败')
  } finally {
    loading.value = false
  }
}

async function handleExportTasks() {
  exporting.value = true
  try {
    await exportInventoryTasks()
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

async function handleExportRecords() {
  if (!currentTask.value) return
  exporting.value = true
  try {
    await exportInventoryTaskRecords(currentTask.value.id)
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

// ===== 新建任务 =====
const createDialogVisible = ref(false)
const creating = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  taskName: '',
  scopeType: 'ALL',
  department: '',
  location: ''
})
const createRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  scopeType: [{ required: true, message: '请选择范围类型', trigger: 'change' }],
  department: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }]
}

function openCreateDialog() {
  createForm.taskName = ''
  createForm.scopeType = 'ALL'
  createForm.department = ''
  createForm.location = ''
  createDialogVisible.value = true
}

async function handleCreate() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    creating.value = true
    try {
      const payload: any = {
        taskName: createForm.taskName,
        scopeType: createForm.scopeType
      }
      if (createForm.scopeType === 'DEPARTMENT') payload.department = createForm.department
      if (createForm.scopeType === 'LOCATION') payload.location = createForm.location
      await createInventoryTask(payload)
      ElMessage.success('盘点任务创建成功')
      createDialogVisible.value = false
      loadTasks()
    } catch (e: any) {
      ElMessage.error(e?.message || '创建任务失败')
    } finally {
      creating.value = false
    }
  })
}

// ===== 明细弹窗 =====
const recordsDialogVisible = ref(false)
const recordsLoading = ref(false)
const currentTask = ref<InventoryTaskItem | null>(null)
const recordList = ref<InventoryRecordItem[]>([])

async function openRecordsDialog(task: InventoryTaskItem) {
  currentTask.value = task
  recordsDialogVisible.value = true
  recordsLoading.value = true
  try {
    const res = await getInventoryRecords(task.id)
    recordList.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '加载盘点明细失败')
  } finally {
    recordsLoading.value = false
  }
}

// ===== 编辑明细 =====
const editRecordDialogVisible = ref(false)
const updating = ref(false)
const recordFormRef = ref<FormInstance>()
const currentRecord = ref<InventoryRecordItem | null>(null)
const recordForm = reactive({
  actualLocation: '',
  actualKeeper: '',
  result: '',
  remark: ''
})
const recordRules = {
  result: [{ required: true, message: '请选择盘点结果', trigger: 'change' }]
}

function openEditRecordDialog(record: InventoryRecordItem) {
  currentRecord.value = record
  recordForm.actualLocation = record.actualLocation || ''
  recordForm.actualKeeper = record.actualKeeper || ''
  recordForm.result = record.result || ''
  recordForm.remark = record.remark || ''
  editRecordDialogVisible.value = true
}

async function handleUpdateRecord() {
  if (!recordFormRef.value || !currentRecord.value) return
  await recordFormRef.value.validate(async (valid) => {
    if (!valid) return
    updating.value = true
    try {
      await updateInventoryRecord(currentRecord.value!.id, {
        actualLocation: recordForm.actualLocation,
        actualKeeper: recordForm.actualKeeper,
        result: recordForm.result,
        remark: recordForm.remark
      })
      ElMessage.success('盘点结果保存成功')
      editRecordDialogVisible.value = false
      // 刷新明细
      if (currentTask.value) {
        const res = await getInventoryRecords(currentTask.value.id)
        recordList.value = res.data
        // 刷新任务列表中的进度
        await loadTasks()
      }
    } catch (e: any) {
      ElMessage.error(e?.message || '保存失败')
    } finally {
      updating.value = false
    }
  })
}

// ===== 完成任务 =====
async function handleComplete(task: InventoryTaskItem) {
  try {
    await ElMessageBox.confirm(
      `确认完成盘点任务"${task.taskName}"？完成后将无法再修改明细。`,
      '确认完成',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await completeInventoryTask(task.id)
    ElMessage.success('盘点任务已完成')
    // 刷新任务列表
    await loadTasks()
    // 刷新当前任务详情
    if (recordsDialogVisible.value && currentTask.value) {
      // 重新加载明细弹窗中的任务状态
      currentTask.value = taskList.value.find(t => t.id === task.id) || currentTask.value
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '完成失败')
  }
}

// ===== 工具函数 =====
function scopeTypeText(type: string): string {
  const map: Record<string, string> = {
    ALL: '全部资产',
    DEPARTMENT: '按部门',
    LOCATION: '按地点'
  }
  return map[type] || type || '-'
}

function scopeDescription(row: InventoryTaskItem): string {
  if (row.scopeType === 'DEPARTMENT') return row.department || '-'
  if (row.scopeType === 'LOCATION') return row.location || '-'
  return '全部资产'
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成'
  }
  return map[status] || status || '-'
}

function resultText(result: string): string {
  const map: Record<string, string> = {
    NORMAL: '正常',
    LOCATION_MISMATCH: '地点不符',
    KEEPER_MISMATCH: '保管人不符',
    LOST: '丢失',
    EXTRA: '账外资产'
  }
  return map[result] || result
}

function resultTagType(result: string): string {
  const map: Record<string, string> = {
    NORMAL: 'success',
    LOCATION_MISMATCH: 'warning',
    KEEPER_MISMATCH: 'warning',
    LOST: 'danger',
    EXTRA: 'info'
  }
  return map[result] || 'info'
}

// 初始化加载
loadTasks()
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
