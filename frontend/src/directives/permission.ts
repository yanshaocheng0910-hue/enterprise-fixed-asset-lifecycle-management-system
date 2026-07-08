import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

const hasPermission = (permission: string): boolean => {
  const authStore = useAuthStore()
  return authStore.permissions.includes(permission)
}

export const vPermission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
    if (binding.value && !hasPermission(binding.value)) {
      el.parentNode?.removeChild(el)
    }
  }
}
