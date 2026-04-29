<template>
  <section class="grid">
    <div class="toolbar">
      <button class="secondary" @click="loadBilling">刷新计费数据</button>
      <span class="status">{{ error }}</span>
    </div>
    <div class="stats">
      <article class="stat">
        <h3>当前租户</h3>
        <select v-model="selectedTenantId" @change="onTenantChange" class="tenant-select">
          <option value="">请选择租户</option>
          <option v-for="t in tenants" :key="t.id" :value="t.id">
            {{ t.name }} ({{ t.code }})
          </option>
        </select>
      </article>
      <article class="stat">
        <h3>累计 Tokens</h3>
        <strong>{{ usage?.totalTokens ?? 0 }}</strong>
      </article>
      <article class="stat">
        <h3>当前成本</h3>
        <strong>{{ usage ? formatCurrency(usage.currentCostCents, usage.currency) : '未加载' }}</strong>
      </article>
    </div>
    <article class="panel">
      <h2>预算策略</h2>
      <form class="form-grid" @submit.prevent="submitBudget">
        <label class="field">
          <span>租户 ID</span>
          <input v-model="budgetTenantId" placeholder="tenant-001" />
        </label>
        <label class="field">
          <span>预算金额（分）</span>
          <input v-model.number="limitCents" type="number" min="1" />
        </label>
        <label class="field">
          <span>币种</span>
          <input v-model="currency" />
        </label>
        <button class="primary" type="submit">更新预算</button>
      </form>
      <p class="muted" v-if="budget">
        当前预算：{{ budget.tenantId }} / {{ formatCurrency(budget.limitCents, budget.currency) }}
      </p>
    </article>
    <article class="panel">
      <h2>发票</h2>
      <table class="table">
        <thead>
          <tr>
            <th>发票 ID</th>
            <th>租户</th>
            <th>期间</th>
            <th>金额</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="invoice in invoices" :key="invoice.id">
            <td>{{ invoice.id }}</td>
            <td>{{ invoice.tenantId }}</td>
            <td>{{ invoice.periodStart }} - {{ invoice.periodEnd }}</td>
            <td>{{ formatCurrency(invoice.amountCents, invoice.currency) }}</td>
            <td><span class="tag">{{ invoice.status }}</span></td>
          </tr>
        </tbody>
      </table>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getUsage, listInvoices, listTenants, updateBudget } from '@/api/admin-api'
import { formatCurrency } from '@/common/format'
import type { Budget, Invoice, TenantInfo, Usage } from '@/domain/types'

const tenants = ref<TenantInfo[]>([])
const selectedTenantId = ref('')
const usage = ref<Usage | null>(null)
const invoices = ref<Invoice[]>([])
const budget = ref<Budget | null>(null)
const budgetTenantId = ref('tenant-001')
const limitCents = ref(100000)
const currency = ref('CNY')
const error = ref('')

onMounted(loadBilling)

async function loadBilling() {
  await execute(async () => {
    tenants.value = await listTenants()
    if (tenants.value.length > 0 && !selectedTenantId.value) {
      selectedTenantId.value = tenants.value[0].id
    }
    if (selectedTenantId.value) {
      usage.value = await getUsage(selectedTenantId.value)
    }
    invoices.value = await listInvoices()
  })
}

async function onTenantChange() {
  if (!selectedTenantId.value) return
  await execute(async () => {
    usage.value = await getUsage(selectedTenantId.value)
  })
}

async function submitBudget() {
  await execute(async () => {
    budget.value = await updateBudget(budgetTenantId.value.trim(), limitCents.value, currency.value.trim())
  })
}

async function execute(action: () => Promise<void>) {
  error.value = ''
  try {
    await action()
  } catch (reason) {
    error.value = reason instanceof Error ? reason.message : '请求失败'
  }
}
</script>

<style scoped>
.tenant-select {
  font-size: 16px;
  padding: 4px 8px;
  min-width: 200px;
}
</style>

