<template>
  <div class="data-card">
    <div class="card-label">{{ label }}</div>
    <div class="card-value" :style="{ color: valueColor }">{{ displayValue }}</div>
    <div v-if="sub" class="card-sub">{{ sub }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  value: string | number
  sub?: string
  color?: string
  type?: 'amount' | 'count'
}>()

const displayValue = computed(() => {
  return props.value ?? '-'
})

const valueColor = computed(() => props.color || 'var(--color-text)')
</script>

<style scoped>
.data-card {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  border: 1px solid var(--color-border);
}
.card-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}
.card-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}
.card-sub {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 6px;
}
</style>
