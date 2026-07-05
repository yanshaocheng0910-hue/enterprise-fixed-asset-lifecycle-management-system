<template>
  <div>
    <PageHeader title="资产入库" description="新增资产入库单，记录资产来源、入库日期、经办人、原始凭证等信息，完成资产从采购到入库的流程。" />
    <EmptyState text="该模块将在第二阶段接入完整业务流程" subText="当前暂不开放操作功能" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
