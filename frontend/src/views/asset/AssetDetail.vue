<template>
  <div>
    <PageHeader :title="'资产详情 - ' + (asset?.assetName || '')" />
    <div style="max-width:1100px;">
      <div v-loading="loading" style="background:#fff;border-radius:6px;border:1px solid var(--color-border);padding:24px;">
        <el-descriptions border :column="2" size="default" v-if="asset">
          <el-descriptions-item label="资产编号" :span="1">{{ asset.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="资产名称" :span="1">{{ asset.assetName }}</el-descriptions-item>
          <el-descriptions-item label="资产分类" :span="1">{{ asset.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="品牌" :span="1">{{ asset.brand || '-' }}</el-descriptions-item>
          <el-descriptions-item label="规格型号" :span="2">{{ asset.specification || '-' }}</el-descriptions-item>
          <el-descriptions-item label="原值" :span="1">{{ formatMoney(asset.originalValue) }} 元</el-descriptions-item>
          <el-descriptions-item label="净值" :span="1">{{ formatMoney(asset.netValue) }} 元</el-descriptions-item>
          <el-descriptions-item label="累计折旧" :span="1">{{ formatMoney(asset.accumulatedDepreciation) }} 元</el-descriptions-item>
          <el-descriptions-item label="残值率" :span="1">{{ asset.residualRate != null ? (asset.residualRate * 100).toFixed(2) + '%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="使用年限" :span="1">{{ asset.usefulLife }} 年</el-descriptions-item>
          <el-descriptions-item label="折旧方法" :span="1">{{ asset.depreciationMethod === 'straight_line' ? '平均年限法' : asset.depreciationMethod }}</el-descriptions-item>
          <el-descriptions-item label="购置日期" :span="1">{{ formatDate(asset.purchaseDate) }}</el-descriptions-item>
          <el-descriptions-item label="所属部门" :span="1">{{ asset.department || '-' }}</el-descriptions-item>
          <el-descriptions-item label="使用人" :span="1">{{ asset.keeper || '-' }}</el-descriptions-item>
          <el-descriptions-item label="存放地点" :span="1">{{ asset.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="资产状态" :span="1"><AssetStatusTag :status="asset.status" /></el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ asset.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:16px;">
          <el-button @click="goBack">返回</el-button>
        </div>
      </div>
      <div v-if="asset" style="display:flex;justify-content:flex-end;margin-top:16px;">
        <el-button size="small" type="success" :loading="exporting" @click="handleExportTimeline">
          <el-icon><Download /></el-icon>导出时间线
        </el-button>
      </div>
      <AssetTimeline v-if="asset" :asset-id="Number(route.params.id)" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import AssetStatusTag from '@/components/AssetStatusTag.vue'
import AssetTimeline from './components/AssetTimeline.vue'
import { getAssetDetail } from '@/api/asset'
import { Download } from '@element-plus/icons-vue'
import { exportAssetTimeline } from '@/api/export'
import { formatMoney, formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const asset = ref<any>(null)
const loading = ref(true)
const exporting = ref(false)

async function fetchDetail() {
  try {
    const id = Number(route.params.id)
    const r = await getAssetDetail(id)
    if (r.code === 200) asset.value = r.data
  } catch {} finally { loading.value = false }
}

function goBack() { router.push('/assets') }

async function handleExportTimeline() {
  exporting.value = true
  try {
    await exportAssetTimeline(Number(route.params.id))
    ElMessage.success('导出成功')
  } finally {
    exporting.value = false
  }
}

onMounted(() => fetchDetail())
</script>
