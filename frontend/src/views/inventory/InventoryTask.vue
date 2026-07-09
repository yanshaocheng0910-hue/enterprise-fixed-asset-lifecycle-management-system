<template>
  <div>
    <PageHeader title="盘点任务" description="创建和管理资产盘点任务，支持按部门、地点、分类等维度进行盘点。" />
    <EmptyState text="盘点管理功能待开发" subText="后续可支持创建盘点任务、生成盘点报告" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
