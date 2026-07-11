<template>
  <div>
    <PageHeader title="资产分类" description="按分类浏览资产，管理分类体系。" />

    <div style="display:flex;gap:16px;align-items:flex-start;">
      <!-- 左侧：分类树 -->
      <div style="width:340px;flex-shrink:0;background:#fff;border-radius:6px;border:1px solid var(--color-border);padding:16px;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
          <h3 style="font-size:14px;font-weight:600;">分类结构</h3>
          <el-button type="primary" size="small" @click="openCreate()">
            <el-icon><Plus /></el-icon>新增
          </el-button>
        </div>
        <el-tree
          :data="treeData"
          :props="{ label: 'categoryName', children: 'children' }"
          default-expand-all
          highlight-current
          node-key="id"
          @node-click="onTreeNodeClick"
        >
          <template #default="{ node, data }">
            <span style="display:flex;align-items:center;justify-content:space-between;width:100%;padding-right:8px;">
              <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">
                {{ data.categoryName }}
                <span style="color:#999;font-size:12px;margin-left:4px;">({{ data.categoryCode }})</span>
              </span>
              <span style="display:none;gap:4px;" @click.stop>
                <el-button link type="primary" size="small" @click="openEdit(data)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDelete(data)">删除</el-button>
              </span>
            </span>
          </template>
        </el-tree>
      </div>

      <!-- 右侧：分类下的资产列表 -->
      <div style="flex:1;background:#fff;border-radius:6px;border:1px solid var(--color-border);padding:16px;">
        <div v-if="selectedCategory" style="margin-bottom:16px;display:flex;justify-content:space-between;align-items:center;">
          <div>
            <span style="font-size:16px;font-weight:600;">{{ selectedCategory.categoryName }}</span>
            <span style="color:#999;font-size:13px;margin-left:8px;">折旧年限 {{ selectedCategory.depreciationYears }} 年</span>
            <span v-if="selectedCategory.remark" style="color:#999;font-size:13px;margin-left:8px;">| {{ selectedCategory.remark }}</span>
          </div>
          <el-button type="success" :loading="exporting" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>

        <el-table v-if="selectedCategory" :data="assetList" border stripe v-loading="assetLoading" style="width:100%">
          <el-table-column prop="assetCode" label="资产编号" width="140" />
          <el-table-column prop="assetName" label="资产名称" width="160" show-overflow-tooltip />
          <el-table-column prop="specification" label="规格型号" width="140" show-overflow-tooltip />
          <el-table-column prop="department" label="使用部门" width="120" />
          <el-table-column prop="keeper" label="保管人" width="100" />
          <el-table-column prop="location" label="存放地点" width="120" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="originalValue" label="原值(元)" width="120" align="right">
            <template #default="{ row }">{{ formatAmount(row.originalValue) }}</template>
          </el-table-column>
          <el-table-column prop="netValue" label="净值(元)" width="120" align="right">
            <template #default="{ row }">{{ formatAmount(row.netValue) }}</template>
          </el-table-column>
          <template #empty>
            <el-empty description="该分类下暂无资产" :image-size="80" />
          </template>
        </el-table>

        <div v-if="selectedCategory && assetTotal > 0" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="assetTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="fetchAssets"
            @size-change="fetchAssets"
          />
        </div>

        <el-empty v-if="!selectedCategory" description="请选择左侧分类查看资产" :image-size="120" style="padding:80px 0;" />
      </div>
    </div>

    <!-- 新增/编辑分类弹窗 -->
    <el-dialog v-model="formVisible" :title="formTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="如 OFFICE" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="如 办公设备" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="不选则为顶级分类"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="折旧年限" prop="depreciationYears">
          <el-input-number v-model="form.depreciationYears" :min="1" :max="50" controls-position="right" style="width:180px" />
          <span style="margin-left:8px;color:#999;font-size:12px;">年</span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { getCategoryTree, createCategory, updateCategory, deleteCategory, getAssetPage } from '@/api/asset'
import { exportAssets } from '@/api/export'

// 分类树
const treeData = ref<any[]>([])
const selectedCategory = ref<any>(null)

// 资产列表
const assetLoading = ref(false)
const assetList = ref<any[]>([])
const assetTotal = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const exporting = ref(false)

// 表单
const formVisible = ref(false)
const formRef = ref<FormInstance>()
const saving = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)

const form = reactive({
  categoryCode: '',
  categoryName: '',
  parentId: 0 as number,
  depreciationYears: 5,
  remark: ''
})

const rules: FormRules = {
  categoryCode: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  depreciationYears: [{ required: true, message: '请输入折旧年限', trigger: 'blur' }]
}

const formTitle = computed(() => isEdit.value ? '编辑分类' : '新增分类')

const parentOptions = computed(() => {
  const buildOptions = (nodes: any[]): any[] => {
    return nodes.map(n => ({
      id: n.id,
      categoryName: n.categoryName,
      children: n.children && n.children.length > 0 ? buildOptions(n.children) : undefined
    }))
  }
  return buildOptions(treeData.value)
})

async function fetchTree() {
  try {
    const tr = await getCategoryTree()
    if (tr.code === 200) treeData.value = tr.data
  } catch {}
}

function onTreeNodeClick(node: any) {
  selectedCategory.value = node
  pageNum.value = 1
  fetchAssets()
}

async function fetchAssets() {
  if (!selectedCategory.value) return
  assetLoading.value = true
  try {
    const r = await getAssetPage({
      categoryId: selectedCategory.value.id,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    if (r.code === 200) {
      assetList.value = r.data.records
      assetTotal.value = r.data.total
    }
  } catch {} finally {
    assetLoading.value = false
  }
}

function statusLabel(s: string): string {
  const map: Record<string, string> = { IDLE: '闲置', IN_USE: '在用', REPAIRING: '维修中', WAITING_SCRAP: '待报废', SCRAPPED: '已报废' }
  return map[s] || s
}

function statusTagType(s: string): 'info' | 'success' | 'warning' | 'danger' | '' {
  const map: Record<string, 'info' | 'success' | 'warning' | 'danger' | ''> = { IDLE: 'info', IN_USE: 'success', REPAIRING: 'warning', WAITING_SCRAP: 'warning', SCRAPPED: 'danger' }
  return map[s] || 'info'
}

function formatAmount(v: number | string | null | undefined): string {
  if (v == null) return '-'
  const n = Number(v)
  if (isNaN(n)) return '-'
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function handleExport() {
  if (!selectedCategory.value) return
  exporting.value = true
  try {
    await exportAssets({ categoryId: selectedCategory.value.id })
    ElMessage.success('导出成功')
  } catch {} finally {
    exporting.value = false
  }
}

function resetForm() {
  form.categoryCode = ''
  form.categoryName = ''
  form.parentId = 0
  form.depreciationYears = 5
  form.remark = ''
  formRef.value?.clearValidate()
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  resetForm()
  formVisible.value = true
}

function openEdit(data: any) {
  isEdit.value = true
  editId.value = data.id
  form.categoryCode = data.categoryCode
  form.categoryName = data.categoryName
  form.parentId = data.parentId || 0
  form.depreciationYears = data.depreciationYears
  form.remark = data.remark || ''
  formVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        categoryCode: form.categoryCode,
        categoryName: form.categoryName,
        parentId: form.parentId || 0,
        depreciationYears: form.depreciationYears,
        remark: form.remark
      }
      if (isEdit.value && editId.value) {
        const r = await updateCategory(editId.value, payload)
        if (r.code === 200) {
          ElMessage.success('分类更新成功')
          formVisible.value = false
          fetchTree()
        }
      } else {
        const r = await createCategory(payload)
        if (r.code === 200) {
          ElMessage.success('分类创建成功')
          formVisible.value = false
          fetchTree()
        }
      }
    } catch {} finally {
      saving.value = false
    }
  })
}

async function handleDelete(data: any) {
  try {
    await ElMessageBox.confirm(`确认删除分类「${data.categoryName}」？删除后不可恢复。`, '提示', { type: 'warning' })
  } catch { return }
  try {
    const r = await deleteCategory(data.id)
    if (r.code === 200) {
      ElMessage.success('分类已删除')
      if (selectedCategory.value && selectedCategory.value.id === data.id) {
        selectedCategory.value = null
        assetList.value = []
      }
      fetchTree()
    }
  } catch {}
}

onMounted(() => fetchTree())
</script>

<style scoped>
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
:deep(.el-tree-node__content:hover) span span:last-child {
  display: flex !important;
}
</style>
