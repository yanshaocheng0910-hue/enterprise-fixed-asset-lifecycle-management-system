<template>
  <div>
    <el-select
      :model-value="modelValue"
      filterable
      :disabled="disabled"
      placeholder="请选择资产"
      style="width:100%"
      @change="onChange"
    >
      <el-option
        v-for="item in options"
        :key="item.id"
        :label="`${item.assetCode} - ${item.assetName} (${item.department || '-'} / ${statusLabel(item.status)})`"
        :value="item.id"
      />
    </el-select>
    <div v-if="selectedAsset" style="margin-top:6px;font-size:12px;color:var(--color-text-secondary);line-height:1.6">
      已选：{{ selectedAsset.assetCode }} - {{ selectedAsset.assetName }}<br>
      部门：{{ selectedAsset.department || '-' }} | 存放地：{{ selectedAsset.location || '-' }} | 使用人：{{ selectedAsset.keeper || '-' }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { getAssetSelect } from '@/api/lifecycle'
import { STATUS_MAP } from '@/utils/dict'

const props = defineProps<{
  modelValue: number | undefined
  status?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number | undefined]
  change: [row: any]
}>()

const options = ref<any[]>([])
const selectedAsset = ref<any>(null)

function statusLabel(s: string) {
  return STATUS_MAP[s] || s
}

async function fetchOptions() {
  try {
    const r = await getAssetSelect(props.status)
    if (r.code === 200) {
      options.value = r.data
    }
  } catch {}
}

function onChange(val: number | undefined) {
  const found = options.value.find((o: any) => o.id === val) || null
  selectedAsset.value = found
  emit('update:modelValue', val)
  if (found) {
    emit('change', found)
  }
}

watch(() => props.modelValue, (val) => {
  if (val && !selectedAsset.value) {
    const found = options.value.find((o: any) => o.id === val)
    if (found) selectedAsset.value = found
  }
  if (!val) selectedAsset.value = null
}, { immediate: true })

onMounted(() => { fetchOptions() })
</script>
