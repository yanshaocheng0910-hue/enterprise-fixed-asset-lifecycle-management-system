<template>
  <div class="asset-list-page">
    <PageHeader title="资产台账" description="统一维护固定资产基础信息、状态、保管人、部门及存放位置，支持多条件检索与导出。">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增资产</el-button>
        <el-button type="success" :loading="exporting" @click="handleExport">
          <el-icon><Download /></el-icon>导出 Excel
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选区 -->
    <div class="filter-card">
      <el-form :model="query" :inline="true" size="default" label-width="80" class="filter-form">
        <el-form-item label="资产编号">
          <el-input v-model="query.assetCode" placeholder="请输入资产编号" clearable style="width:170px" />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="query.assetName" placeholder="请输入资产名称" clearable style="width:170px" />
        </el-form-item>
        <el-form-item label="资产分类">
          <el-select v-model="query.categoryId" placeholder="选择分类" clearable style="width:160px">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="资产状态">
          <el-select v-model="query.status" placeholder="选择状态" clearable style="width:150px">
            <el-option v-for="s in statusOptions" :key="s.code" :label="s.label" :value="s.code" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button text type="primary" @click="advancedVisible = !advancedVisible">
            {{ advancedVisible ? '收起高级筛选' : '高级筛选' }}
            <el-icon style="margin-left:2px"><ArrowDown v-if="!advancedVisible" /><ArrowUp v-else /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>

      <div v-show="advancedVisible" class="advanced-filter">
        <el-form :model="query" :inline="true" size="default" label-width="80" class="filter-form">
          <el-form-item label="所属部门">
            <el-select v-model="query.department" placeholder="选择部门" clearable filterable style="width:150px">
              <el-option v-for="d in departmentOptions" :key="d.id" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="使用人">
            <el-select v-model="query.keeper" placeholder="选择使用人" clearable filterable style="width:150px">
              <el-option v-for="k in keeperOptions" :key="k.id" :label="k.label" :value="k.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="存放地点">
            <el-select v-model="query.location" placeholder="选择地点" clearable filterable style="width:150px">
              <el-option v-for="l in locationOptions" :key="l.id" :label="l.label" :value="l.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="购置日期">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:260px" />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 表格区 -->
    <div class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%" header-row-class-name="table-header">
        <el-table-column prop="assetCode" label="资产编号" width="140" />
        <el-table-column prop="assetName" label="资产名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="brand" label="品牌" width="90" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格型号" width="120" show-overflow-tooltip />
        <el-table-column prop="department" label="所属部门" width="100" show-overflow-tooltip />
        <el-table-column prop="keeper" label="使用人" width="80" />
        <el-table-column prop="location" label="存放地点" width="120" show-overflow-tooltip />
        <el-table-column prop="originalValue" label="原值" width="120" align="right" header-align="right" class-name="gx-amount">
          <template #default="{ row }">{{ formatMoney(row.originalValue) }}</template>
        </el-table-column>
        <el-table-column prop="netValue" label="净值" width="120" align="right" header-align="right" class-name="gx-amount">
          <template #default="{ row }">{{ formatMoney(row.netValue) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }"><StatusTag :value="row.status" category="asset" effect="light" /></template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="购置日期" width="110" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="goDetail(row.id)">查看</el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => { if (cmd === 'edit') openEdit(row); else if (cmd === 'delete') handleDelete(row) }">
              <el-button link type="primary" size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无资产数据" :image-size="80" />
        </template>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </div>

    <!-- Create / Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑资产' : '新增资产'" width="640px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90" size="default">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产名称" prop="assetName">
              <el-input v-model="form.assetName" placeholder="请输入资产名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择资产分类" style="width:100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌"><el-input v-model="form.brand" placeholder="品牌" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号"><el-input v-model="form.specification" placeholder="规格型号" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="购置日期" prop="purchaseDate">
              <el-date-picker v-model="form.purchaseDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原值(元)" prop="originalValue">
              <el-input-number v-model="form.originalValue" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="使用年限" prop="usefulLife">
              <el-input-number v-model="form.usefulLife" :min="1" :max="50" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="残值率" prop="residualRate">
              <el-input-number v-model="form.residualRate" :min="0" :max="1" :step="0.01" :precision="4" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部门" prop="department">
              <el-select v-model="form.department" placeholder="选择部门" filterable allow-create clearable style="width:100%">
                <el-option v-for="d in departmentOptions" :key="d.id" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用人" prop="keeper">
              <el-select v-model="form.keeper" placeholder="选择使用人" filterable allow-create clearable style="width:100%">
                <el-option v-for="k in keeperOptions" :key="k.id" :label="k.label" :value="k.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="存放地点">
              <el-select v-model="form.location" placeholder="选择存放地点" filterable allow-create clearable style="width:100%">
                <el-option v-for="l in locationOptions" :key="l.id" :label="l.label" :value="l.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="isEdit" label="资产状态" prop="status">
              <el-select v-model="form.status" placeholder="选择状态" style="width:100%">
                <el-option v-for="s in statusOptions" :key="s.code" :label="s.label" :value="s.code" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div v-if="!isEdit" class="form-tip">资产编号由系统自动生成，折旧与净值由系统自动计算。</div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { getAssetPage, createAsset, updateAsset, deleteAsset, getStatusOptions, getCategoryList } from '@/api/asset'
import { Download, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { exportAssets } from '@/api/export'
import { formatMoney } from '@/utils/format'
import { useMasterDataOptions } from '@/composables/useMasterDataOptions'

const { departmentOptions, locationOptions, keeperOptions, loadAll: loadMasterData } = useMasterDataOptions()

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const exporting = ref(false)
const advancedVisible = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statusOptions = ref<any[]>([])
const categories = ref<any[]>([])
const dateRange = ref<any>(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref()

const query = reactive({
  assetCode: '',
  assetName: '',
  categoryId: undefined as number | undefined,
  department: '',
  keeper: '',
  location: '',
  status: '',
  purchaseDateStart: '',
  purchaseDateEnd: ''
})

const form = reactive<any>({
  assetName: '',
  categoryId: undefined,
  specification: '',
  brand: '',
  purchaseDate: '',
  originalValue: undefined,
  usefulLife: 5,
  residualRate: 0.05,
  department: '',
  keeper: '',
  location: '',
  status: 'IDLE',
  qrCode: '',
  rfidCode: '',
  photoUrl: '',
  remark: ''
})

const formRules = {
  assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择资产分类', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购置日期', trigger: 'change' }],
  originalValue: [{ required: true, message: '请输入原值', trigger: 'blur' }],
  usefulLife: [{ required: true, message: '请输入使用年限', trigger: 'blur' }],
  residualRate: [{ required: true, message: '请输入残值率', trigger: 'blur' }],
  department: [{ required: true, message: '请输入所属部门', trigger: 'blur' }],
  keeper: [{ required: true, message: '请输入使用人', trigger: 'blur' }]
}

watch(dateRange, (val) => {
  if (val) {
    query.purchaseDateStart = val[0]
    query.purchaseDateEnd = val[1]
  } else {
    query.purchaseDateStart = ''
    query.purchaseDateEnd = ''
  }
})

async function fetchData() {
  loading.value = true
  try {
    const params = { ...query, pageNum: pageNum.value, pageSize: pageSize.value }
    const r = await getAssetPage(params)
    if (r.code === 200) {
      tableData.value = r.data.records
      total.value = r.data.total
    }
  } catch {} finally {
    loading.value = false
  }
}

async function handleExport() {
  exporting.value = true
  try {
    await exportAssets({ status: query.status, department: query.department, keyword: query.assetName })
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

async function fetchOptions() {
  try {
    const [sr, cr] = await Promise.all([getStatusOptions(), getCategoryList()])
    if (sr.code === 200) statusOptions.value = sr.data
    if (cr.code === 200) categories.value = cr.data
  } catch {}
}

function search() { pageNum.value = 1; fetchData() }
function resetQuery() {
  Object.assign(query, { assetCode: '', assetName: '', categoryId: undefined, department: '', keeper: '', location: '', status: '', purchaseDateStart: '', purchaseDateEnd: '' })
  dateRange.value = null
  pageNum.value = 1
  fetchData()
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, { assetName: '', categoryId: undefined, specification: '', brand: '', purchaseDate: '', originalValue: undefined, usefulLife: 5, residualRate: 0.05, department: '', keeper: '', location: '', status: 'IDLE', qrCode: '', rfidCode: '', photoUrl: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    assetName: row.assetName,
    categoryId: row.categoryId,
    specification: row.specification,
    brand: row.brand,
    purchaseDate: row.purchaseDate,
    originalValue: row.originalValue,
    usefulLife: row.usefulLife,
    residualRate: row.residualRate,
    department: row.department,
    keeper: row.keeper,
    location: row.location,
    status: row.status,
    qrCode: row.qrCode,
    rfidCode: row.rfidCode,
    photoUrl: row.photoUrl,
    remark: row.remark
  })
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateAsset(editId.value, form)
      ElMessage.success('编辑成功')
    } else {
      await createAsset(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {} finally {
    submitLoading.value = false
  }
}

function handleDelete(row: any) {
  ElMessageBox.confirm(`确认删除资产「${row.assetName}」？删除后可在数据中标记为已删除状态。`, '确认删除', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteAsset(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

function goDetail(id: number) { router.push(`/assets/${id}`) }

onMounted(() => { fetchData(); fetchOptions(); loadMasterData() })
</script>

<style scoped>
/* 筛选卡片：白色卡片，紧凑稳重 */
.filter-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-lg) var(--space-lg) var(--space-md);
  margin-bottom: var(--space-base);
}
.filter-form :deep(.el-form-item) {
  margin-bottom: var(--space-md);
  margin-right: var(--space-md);
}
.filter-form :deep(.el-form-item:last-child) {
  margin-right: 0;
}
.advanced-filter {
  margin-top: var(--space-sm);
  padding-top: var(--space-md);
  border-top: 1px dashed var(--color-border);
}

/* 表格卡片 */
.table-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-lg);
}
/* 表头：浅灰底，加粗，稳重表样 */
.table-card :deep(.table-header th) {
  background: var(--color-bg-soft) !important;
  color: var(--color-text);
  font-weight: 600;
}
/* 金额列：等宽数字，便于对账 */
.table-card :deep(.gx-amount .cell) {
  font-variant-numeric: tabular-nums;
  font-weight: 500;
  color: var(--color-text);
  letter-spacing: 0.2px;
}

/* 分页：右对齐，与表格间距统一 */
.table-card :deep(.el-pagination) {
  margin-top: var(--space-base);
  justify-content: flex-end;
}

/* 弹窗提示 */
.form-tip {
  font-size: 12px;
  color: var(--color-text-secondary);
  padding: 0 var(--space-md) var(--space-md);
}
</style>
