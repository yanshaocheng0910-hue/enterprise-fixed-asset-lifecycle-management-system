<template>
  <div>
    <PageHeader title="资产领用" description="记录资产领用申请、审批、出库全流程。领用后资产状态自动变更为使用中。" />
    <EmptyState text="该模块将在第二阶段接入完整业务流程" subText="当前暂不开放操作功能" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
