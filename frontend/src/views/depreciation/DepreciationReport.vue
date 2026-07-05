<template>
  <div>
    <PageHeader title="折旧报表" description="查看固定资产月度折旧明细与汇总报表。支持按分类、部门查看折旧数据。" />
    <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:16px;">
      <DataCard label="本月折旧总额" :value="'--'" sub="元" />
      <DataCard label="累计折旧总额" :value="'--'" sub="元" />
      <DataCard label="资产总数" :value="'--'" sub="件" />
      <DataCard label="计提资产数" :value="'--'" sub="件" />
    </div>
    <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:12px;">
      <el-table :data="[]" border stripe>
        <el-table-column prop="assetCode" label="资产编号" width="140" />
        <el-table-column prop="assetName" label="资产名称" width="140" />
        <el-table-column label="上月折旧" width="120" />
        <el-table-column label="本月折旧" width="120" />
        <el-table-column label="累计折旧" width="120" />
        <el-table-column label="当前净值" width="120" />
        <el-table-column label="折旧月份" width="100" />
      </el-table>
      <EmptyState text="折旧报表将在第二阶段接入完整数据" subText="届时展示月度折旧明细与汇总统计" />
    </div>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import DataCard from '@/components/DataCard.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
