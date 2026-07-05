<template>
  <div class="page-header">
    <h2 class="page-title">{{ title }}</h2>
    <p v-if="description" class="page-desc">{{ description }}</p>
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
  margin-bottom: 20px;
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
}
</style>
