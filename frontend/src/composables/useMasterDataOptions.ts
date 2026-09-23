import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDepartments, getLocations, getKeepers, type MasterDataItem } from '@/api/masterData'

export function useMasterDataOptions() {
  const departmentOptions = ref<MasterDataItem[]>([])
  const locationOptions = ref<MasterDataItem[]>([])
  const keeperOptions = ref<MasterDataItem[]>([])

  async function loadDepartments() {
    try {
      const res = await getDepartments()
      departmentOptions.value = res.data || []
    } catch {
      departmentOptions.value = []
    }
  }

  async function loadLocations() {
    try {
      const res = await getLocations()
      locationOptions.value = res.data || []
    } catch {
      locationOptions.value = []
    }
  }

  async function loadKeepers() {
    try {
      const res = await getKeepers()
      keeperOptions.value = res.data || []
    } catch {
      keeperOptions.value = []
    }
  }

  async function loadAll() {
    await Promise.all([loadDepartments(), loadLocations(), loadKeepers()])
  }

  return {
    departmentOptions,
    locationOptions,
    keeperOptions,
    loadAll,
    loadDepartments,
    loadLocations,
    loadKeepers
  }
}
