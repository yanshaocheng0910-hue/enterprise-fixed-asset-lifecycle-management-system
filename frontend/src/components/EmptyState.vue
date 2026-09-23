<template>
  <div class="empty-state">
    <el-icon :size="48" color="#D1D5DB"><Folder /></el-icon>
    <p class="empty-text">{{ text || '暂无数据' }}</p>
    <p v-if="subText" class="empty-sub">{{ subText }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  text?: string
  subText?: string
}>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}
.empty-text {
  margin-top: 12px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
.empty-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #9CA3AF;
}
</style>
