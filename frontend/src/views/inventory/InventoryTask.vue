<template>
  <div>
    <PageHeader title="盘点任务" description="创建和管理资产盘点任务，按部门、存放地点等维度进行盘点。" />

    <div class="search-form">
      <el-form :model="query" :inline="true" size="default" label-width="80">
        <el-form-item label="任务名称">
          <el-input v-model="query.keyword" placeholder="任务名称/编号" clearable style="width:160px" />
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:130px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="执行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="openCreate">新建盘点</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-wrapper">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="taskCode" label="任务编号" width="180" />
        <el-table-column prop="taskName" label="任务名称" min-width="130" />
        <el-table-column prop="scopeType" label="盘点范围" width="100">
          <template #default="{ row }">
            {{ scopeLabel(row.scopeType) }}
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="location" label="存放地点" width="120" show-overflow-tooltip />
        <el-table-column label="盘点进度" width="180">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:6px;">
              <el-progress :percentage="row.totalCount > 0 ? Math.round(row.scannedCount / row.totalCount * 100) : 0" :stroke-width="14" :text-inside="true" />
              <span style="font-size:12px;white-space:nowrap;color:var(--color-text-secondary);">{{ row.scannedCount }}/{{ row.totalCount }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'DRAFT'" link type="primary" size="small" @click="handleStart(row)">启动</el-button>
            <el-button v-if="row.status === 'IN_PROGRESS'" link type="primary" size="small" @click="handleExecute(row)">执行盘点</el-button>
            <el-button v-if="row.status === 'COMPLETED'" link type="primary" size="small" @click="handleReport(row)">查看报告</el-button>
            <el-button v-if="row.status === 'DRAFT'" link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </div>

    <el-dialog v-model="createVisible" title="新建盘点任务" width="480px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90" size="default">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="例如：2026年7月季度盘点" />
        </el-form-item>
        <el-form-item label="盘点范围" prop="scopeType">
          <el-radio-group v-model="form.scopeType">
            <el-radio value="ALL">全部资产</el-radio>
            <el-radio value="DEPARTMENT">按部门</el-radio>
            <el-radio value="LOCATION">按存放地点</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.scopeType === 'DEPARTMENT'" label="部门" prop="department">
          <el-select v-model="form.department" placeholder="选择部门" filterable style="width:100%">
            <el-option v-for="d in deptOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.scopeType === 'LOCATION'" label="存放地点" prop="location">
          <el-select v-model="form.location" placeholder="选择存放地点" filterable style="width:100%">
            <el-option v-for="l in locationOptions" :key="l" :label="l" :value="l" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getInventoryTaskPage, createInventoryTask, startInventoryTask, deleteInventoryTask, getDepartments, getLocations } from '@/api/inventory'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const formRef = ref()
const createVisible = ref(false)

const query = reactive({
  keyword: '',
  status: ''
})

const form = reactive<any>({
  taskName: '',
  scopeType: 'ALL',
  department: '',
  location: ''
})

const formRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }]
}

function scopeLabel(s: string) {
  const map: Record<string, string> = { ALL: '全部', DEPARTMENT: '按部门', LOCATION: '按存放地点' }
  return map[s] || s
}

function statusLabel(s: string) {
  const map: Record<string, string> = { DRAFT: '草稿', IN_PROGRESS: '执行中', COMPLETED: '已完成' }
  return map[s] || s
}

function statusType(s: string) {
  const map: Record<string, string> = { DRAFT: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success' }
  return map[s] || ''
}

async function fetchData() {
  loading.value = true
  try {
    const params: Record<string, any> = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.keyword) params.keyword = query.keyword
    if (query.status) params.status = query.status
    const r = await getInventoryTaskPage(params)
    if (r.code === 200) {
      tableData.value = r.data.records
      total.value = r.data.total
    }
  } catch {} finally {
    loading.value = false
  }
}

function search() { pageNum.value = 1; fetchData() }
function resetQuery() { Object.assign(query, { keyword: '', status: '' }); pageNum.value = 1; fetchData() }

function openCreate() {
  Object.assign(form, { taskName: '', scopeType: 'ALL', department: '', location: '' })
  createVisible.value = true
}

async function submitCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const r = await createInventoryTask({
      taskName: form.taskName,
      scopeType: form.scopeType === 'ALL' ? undefined : form.scopeType,
      department: form.department || undefined,
      location: form.location || undefined
    })
    if (r.code === 200) {
      ElMessage.success('创建成功')
      createVisible.value = false
      fetchData()
    }
  } catch {} finally {
    submitLoading.value = false
  }
}

async function handleStart(row: any) {
  try {
    await ElMessageBox.confirm(`确定启动盘点任务「」？启动后将自动加载盘点范围内的资产。`, '确认启动')
    const r = await startInventoryTask(row.id)
    if (r.code === 200) {
      ElMessage.success('已启动')
      fetchData()
    }
  } catch {}
}

async function handleExecute(row: any) {
  router.push(`/inventory/tasks//execute`)
}

function handleReport(row: any) {
  router.push(`/inventory/tasks//report`)
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除盘点任务「」？`, '确认删除', { type: 'warning' })
    const r = await deleteInventoryTask(row.id)
    if (r.code === 200) {
      ElMessage.success('已删除')
      fetchData()
    }
  } catch {}
}

const deptOptions = ref<string[]>([])
const locationOptions = ref<string[]>([])

onMounted(() => { 
  fetchData() 
  getDepartments().then((r: any) => { if (r.code === 200) deptOptions.value = r.data })
  getLocations().then((r: any) => { if (r.code === 200) locationOptions.value = r.data })
})
</script>

<style scoped>
.search-form {
  background: #fff;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  margin-bottom: 12px;
}
.table-wrapper {
  background: #fff;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
}
</style>
