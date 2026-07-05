<template>
  <div>
    <PageHeader title="资产调拨" description="资产在不同部门之间调拨，需要记录调出部门、调入部门、调拨原因和审批信息。" />
    <EmptyState text="该模块将在第二阶段接入完整业务流程" subText="当前暂不开放操作功能" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
