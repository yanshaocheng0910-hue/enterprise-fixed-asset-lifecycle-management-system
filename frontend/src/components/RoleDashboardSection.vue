<template>
  <div class="role-section">
    <div class="role-section-header">
      <div class="role-section-title-area">
        <h3 class="role-section-title">{{ title }}</h3>
        <span v-if="desc" class="role-section-desc">{{ desc }}</span>
      </div>
      <el-button v-if="to" text type="primary" size="small" @click="$router.push(to)">
        查看全部
        <el-icon style="margin-left: 2px"><ArrowRight /></el-icon>
      </el-button>
    </div>
    <div class="role-section-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'

defineProps<{
  title: string
  desc?: string
  to?: string
}>()
</script>

<style scoped>
.role-section {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}
.role-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-bg-soft);
}
.role-section-title-area {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.role-section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}
.role-section-desc {
  font-size: 12px;
  color: var(--color-text-tertiary);
}
.role-section-body {
  padding: var(--space-lg);
}
</style>
