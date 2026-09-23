<template>
  <div class="data-card" :class="[`variant-${variant}`, { 'has-accent': accent }]">
    <div v-if="accent" class="card-accent-bar" :style="{ background: accentColor }"></div>
    <div class="card-main">
      <div class="card-header">
        <span class="card-label">{{ label }}</span>
        <el-icon v-if="icon" :size="16" class="card-icon" :style="{ color: iconColor }">
          <component :is="icon" />
        </el-icon>
      </div>
      <div class="card-value" :style="{ color: valueColor }">
        {{ displayValue }}
        <span v-if="unit" class="card-unit">{{ unit }}</span>
      </div>
      <div v-if="sub" class="card-sub">{{ sub }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label: string
  value: string | number
  sub?: string
  unit?: string
  color?: string
  icon?: any
  iconColor?: string
  accent?: boolean
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
}>()

const displayValue = computed(() => props.value ?? '-')

const variantColorMap: Record<string, string> = {
  default: 'var(--color-text)',
  primary: 'var(--color-primary)',
  success: 'var(--color-success)',
  warning: 'var(--color-warning)',
  danger: 'var(--color-danger)',
  info: 'var(--color-info)'
}

const valueColor = computed(() => props.color || variantColorMap[props.variant || 'default'])
const accentColor = computed(() => props.color || variantColorMap[props.variant || 'default'] || 'var(--color-primary)')
</script>

<style scoped>
.data-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--space-lg);
  position: relative;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.data-card:hover {
  box-shadow: var(--shadow-md);
}
.data-card.has-accent {
  padding-left: 24px;
}
.card-accent-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-md);
}
.card-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
}
.card-icon {
  opacity: 0.7;
}
.card-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
  font-feature-settings: 'tnum';
}
.card-unit {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-left: 4px;
}
.card-sub {
  font-size: 12px;
  color: var(--color-text-tertiary);
  margin-top: 8px;
}

/* 风险变体：浅色背景 */
.variant-danger {
  background: var(--color-danger-light);
  border-color: rgba(208, 48, 80, 0.2);
}
.variant-warning {
  background: var(--color-warning-light);
  border-color: rgba(240, 160, 32, 0.2);
}
.variant-success {
  background: var(--color-success-light);
  border-color: rgba(24, 160, 88, 0.2);
}
.variant-primary {
  background: var(--color-primary-light);
  border-color: rgba(31, 78, 121, 0.15);
}
</style>
