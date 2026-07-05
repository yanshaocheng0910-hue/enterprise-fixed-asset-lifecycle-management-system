<template>
  <div>
    <PageHeader title="维修管理" description="记录资产报修、维修进度、维修费用和维修结果。维修期间资产状态自动变更为维修中。" />
    <EmptyState text="该模块将在第二阶段接入完整业务流程" subText="当前暂不开放操作功能" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
