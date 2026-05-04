import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { Tenant, Workspace } from '@/domain/types'

export const useTenantWorkspaceStore = defineStore('tenant-workspace', () => {
  const tenants = ref<Tenant[]>([])
  const workspaces = ref<Workspace[]>([])
  const tenantId = ref('')
  const workspaceId = ref('')

  const selectedTenant = computed(() => tenants.value.find((item) => item.id === tenantId.value))
  const selectedWorkspace = computed(() => workspaces.value.find((item) => item.id === workspaceId.value))
  const selectionReady = computed(() => !!tenantId.value && !!workspaceId.value)

  function setTenants(items: Tenant[]) {
    tenants.value = items
  }

  function setWorkspaces(items: Workspace[]) {
    workspaces.value = items
  }

  function selectTenant(id: string) {
    tenantId.value = id
  }

  function selectWorkspace(id: string) {
    workspaceId.value = id
  }

  function clearSelection() {
    tenantId.value = ''
    workspaceId.value = ''
  }

  return {
    tenants,
    workspaces,
    tenantId,
    workspaceId,
    selectedTenant,
    selectedWorkspace,
    selectionReady,
    setTenants,
    setWorkspaces,
    selectTenant,
    selectWorkspace,
    clearSelection,
  }
})
