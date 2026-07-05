<template>
  <div>
    <PageHeader title="用户管理" description="管理系统用户账号、角色权限和部门信息。" />
    <EmptyState text="用户管理功能将在第二阶段完善" subText="届时支持新增用户、角色分配、权限配置等操作" />
  </div>
</template>

<script setup lang="ts">
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
</script>
