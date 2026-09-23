<template>
  <div class="quick-action-card" @click="$emit('navigate', to)">
    <div class="qa-icon" :style="{ background: color + '20', color: color }">
      <el-icon :size="20"><component :is="icon" /></el-icon>
    </div>
    <div class="qa-body">
      <div class="qa-title">{{ title }}</div>
      <div class="qa-desc">{{ desc }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  desc?: string
  icon: any
  color?: string
  to: string
}>()
defineEmits<{ navigate: [to: string] }>()
</script>

<style scoped>
.quick-action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid var(--color-border, #e4e7ed);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.quick-action-card:hover {
  border-color: var(--color-primary, #409eff);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.qa-icon {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.qa-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text, #303133);
}
.qa-desc {
  font-size: 12px;
  color: var(--color-text-secondary, #909399);
  margin-top: 2px;
}
</style>
