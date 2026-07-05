<template>
  <div>
    <PageHeader title="财务同步" description="将资产折旧数据同步至财务系统（现阶段为模拟对接，暂不接入真实用友/金蝶/SAP）。" />
    <div style="background:#fff;border:1px solid var(--color-border);border-radius:6px;padding:20px;">
      <el-alert title="当前为模拟对接状态" type="info" show-icon :closable="false" description="本模块将在第三阶段接入真实财务系统对接能力。" style="margin-bottom:16px;" />
      <el-button type="primary" disabled>同步本月折旧</el-button>
      <el-divider />
      <el-table :data="[]" border stripe>
        <el-table-column label="同步时间" width="180" />
        <el-table-column label="同步类型" width="120" />
        <el-table-column label="折旧月份" width="100" />
        <el-table-column label="总金额" width="120" />
        <el-table-column label="状态" width="100" />
        <el-table-column label="备注" />
      </el-table>
      <EmptyState text="暂无同步记录" subText="财务系统对接将在后续阶段启用" />
    </div>
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
