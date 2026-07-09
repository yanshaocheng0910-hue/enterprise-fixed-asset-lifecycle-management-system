<template>
  <div class="page-header">
    <div class="page-header-left">
      <h2 class="page-title">{{ title }}</h2>
      <p v-if="description" class="page-desc">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-header-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: var(--space-lg);
  gap: var(--space-base);
}
.page-header-left {
  flex: 1;
  min-width: 0;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.4;
}
.page-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
  line-height: 1.5;
}
.page-header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-shrink: 0;
}
</style>
