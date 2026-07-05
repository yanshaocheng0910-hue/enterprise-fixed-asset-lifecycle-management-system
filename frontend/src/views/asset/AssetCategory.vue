<template>
  <div>
    <PageHeader title="资产分类" description="管理固定资产分类体系，配置默认折旧年限。" />

    <div style="display:flex;gap:16px;">
      <div style="width:260px;flex-shrink:0;background:#fff;border-radius:6px;border:1px solid var(--color-border);padding:12px;">
        <h3 style="font-size:14px;font-weight:600;margin-bottom:12px;padding-left:4px;">分类结构</h3>
        <el-tree
          :data="treeData"
          :props="{ label: 'categoryName', children: 'children' }"
          default-expand-all
          highlight-current
          node-key="id"
        />
      </div>
      <div style="flex:1;background:#fff;border-radius:6px;border:1px solid var(--color-border);padding:12px;">
        <el-table :data="listData" border stripe>
          <el-table-column prop="categoryCode" label="分类编码" width="140" />
          <el-table-column prop="categoryName" label="分类名称" width="160" />
          <el-table-column prop="depreciationYears" label="默认折旧年限" width="120" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { getCategoryTree, getCategoryList } from '@/api/asset'

const treeData = ref<any[]>([])
const listData = ref<any[]>([])

async function fetchData() {
  try {
    const [tr, lr] = await Promise.all([getCategoryTree(), getCategoryList()])
    if (tr.code === 200) treeData.value = tr.data
    if (lr.code === 200) listData.value = lr.data
  } catch {}
}

onMounted(() => fetchData())
</script>
