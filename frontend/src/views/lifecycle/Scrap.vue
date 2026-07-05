<template>
  <div>
    <PageHeader title="报废管理" description="管理资产报废流程，记录报废原因、报废日期和处置方式。报废完成后资产状态变更为已报废。" />

    <div class="search-form">
      <el-form :model="query" :inline="true" size="default" label-width="80">
        <el-form-item label="单据编号"><el-input v-model="query.orderCode" placeholder="单据编号" clearable style="width:150px" /></el-form-item>
        <el-form-item label="单据状态">
          <el-select v-model="query.status" placeholder="选择状态" clearable style="width:140px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="openCreate">新增报废</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-wrapper">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="orderCode" label="单据编号" width="160" />
        <el-table-column prop="assetCode" label="资产编号" width="140" />
        <el-table-column prop="assetName" label="资产名称" min-width="120" />
        <el-table-column prop="scrapReason" label="报废原因" width="140" show-overflow-tooltip />
        <el-table-column prop="scrapDate" label="报废日期" width="100" />
        <el-table-column prop="disposalMethod" label="处置方式" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'COMPLETED' ? 'success' : row.status === 'CANCELLED' ? 'danger' : 'info'" size="small">{{ row.status === 'DRAFT' ? '草稿' : row.status === 'COMPLETED' ? '已完成' : '已取消' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">查看</el-button>
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

    <el-dialog v-model="createVisible" title="新增报废" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90" size="default">
        <el-form-item label="选择资产" prop="assetId">
          <AssetSelect v-model="form.assetId" />
        </el-form-item>
        <el-form-item label="报废原因" prop="scrapReason">
          <el-input v-model="form.scrapReason" type="textarea" :rows="2" placeholder="请填写报废原因" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报废日期" prop="scrapDate">
              <el-date-picker v-model="form.scrapDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处置方式">
              <el-select v-model="form.disposalMethod" placeholder="请选择" style="width:100%">
                <el-option label="报废回收" value="报废回收" />
                <el-option label="捐赠" value="捐赠" />
                <el-option label="低价变卖" value="低价变卖" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="残值">
              <el-input-number v-model="form.residualValue" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注"><el-input v-model="form.remark" placeholder="备注" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">确认</el-button>
      </template>
    </el-dialog>

    <LifecycleDetailDialog v-model:visible="detailVisible" :data="detailData" title="报废详情" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import AssetSelect from '@/components/AssetSelect.vue'
import LifecycleDetailDialog from '@/components/LifecycleDetailDialog.vue'
import { getScrapPage, getScrapDetail, createScrap } from '@/api/lifecycle'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const formRef = ref()
const createVisible = ref(false)
const detailVisible = ref(false)
const detailData = ref<any>(null)

const query = reactive({
  orderCode: '',
  status: ''
})

const form = reactive<any>({
  assetId: undefined,
  scrapReason: '',
  scrapDate: '',
  disposalMethod: '',
  residualValue: undefined,
  remark: ''
})

const formRules = {
  assetId: [{ required: true, message: '请选择资产', trigger: 'change' }],
  scrapReason: [{ required: true, message: '请输入报废原因', trigger: 'blur' }],
  scrapDate: [{ required: true, message: '请选择报废日期', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params = { ...query, pageNum: pageNum.value, pageSize: pageSize.value }
    const r = await getScrapPage(params)
    if (r.code === 200) {
      tableData.value = r.data.records
      total.value = r.data.total
    }
  } catch {} finally {
    loading.value = false
  }
}

function search() { pageNum.value = 1; fetchData() }

function resetQuery() {
  Object.assign(query, { orderCode: '', status: '' })
  pageNum.value = 1
  fetchData()
}

function openCreate() {
  Object.assign(form, { assetId: undefined, scrapReason: '', scrapDate: '', disposalMethod: '', residualValue: undefined, remark: '' })
  createVisible.value = true
}

async function submitCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createScrap(form)
    ElMessage.success('新增成功')
    createVisible.value = false
    fetchData()
  } catch {} finally {
    submitLoading.value = false
  }
}

async function viewDetail(row: any) {
  try {
    const r = await getScrapDetail(row.id)
    if (r.code === 200) {
      detailData.value = r.data
      detailVisible.value = true
    }
  } catch {}
}

onMounted(() => { fetchData() })
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
