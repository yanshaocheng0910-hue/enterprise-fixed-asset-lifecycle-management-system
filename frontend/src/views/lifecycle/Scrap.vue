<template>
  <div>
    <PageHeader title="报废管理" description="资产报废申请、审批、回收处置全流程管理。报废完成后资产状态变更为已报废。" />
    <EmptyState text="该模块将在第二阶段接入完整业务流程" subText="当前暂不开放操作功能" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
