/**
 * 快速应用美化效果的脚本
 * 用于在现有页面中快速集成美化组件和效果
 */

// 1. 在设置页面添加效果开关
export const addEffectToggleToSettings = `
<!-- 在 SettingsView.vue 中添加 -->
<template>
  <section class="settings-page glass-float">
    <!-- 其他设置内容 -->
    
    <article class="panel glass-effect">
      <h3>视觉效果设置</h3>
      <EffectToggle />
    </article>
  </section>
</template>

<script setup lang="ts">
import EffectToggle from '@/components/EffectToggle.vue'
</script>
`

// 2. 在布局组件中添加全局效果
export const addGlobalEffectsToLayout = `
<!-- 在 TenantLayout.vue 中添加 -->
<template>
  <div class="tenant-layout" :class="{ 'effects-disabled': !effectsEnabled }">
    <!-- 布局内容 -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const effectsEnabled = ref(true)

onMounted(() => {
  const saved = localStorage.getItem('effect-settings')
  if (saved) {
    const settings = JSON.parse(saved)
    effectsEnabled.value = settings.glass && settings.float
  }
})
</script>
`

// 3. 表单弹窗化改造模板
export const modalFormTemplate = `
<!-- 改造前 -->
<article v-if="showAddForm" class="panel form-panel">
  <div class="panel-header">
    <h3>添加配置</h3>
    <button class="ghost" @click="cancelAdd">取消</button>
  </div>
  <form @submit.prevent="handleAdd">
    <!-- 表单内容 -->
  </form>
</article>

<!-- 改造后 -->
<ModalDialog
  v-model:visible="showAddForm"
  title="添加配置"
  @confirm="handleAdd"
  :confirm-disabled="!isFormValid || loading"
  confirm-text="添加"
>
  <form>
    <!-- 表单内容 -->
  </form>
</ModalDialog>
`

// 4. 下拉框改造模板
export const selectComponentTemplate = `
<!-- 改造前 -->
<select v-model="selectedValue" @change="handleChange" class="custom-select">
  <option value="">请选择</option>
  <option v-for="item in items" :key="item.id" :value="item.id">
    {{ item.name }}
  </option>
</select>

<!-- 改造后 -->
<CustomSelect
  v-model="selectedValue"
  :options="itemOptions"
  placeholder="请选择"
  @change="handleChange"
/>

<!-- 在 script 中准备选项数据 -->
<script setup lang="ts">
import { computed } from 'vue'
import CustomSelect from '@/components/CustomSelect.vue'

const itemOptions = computed(() => 
  items.value.map(item => ({
    value: item.id,
    label: item.name
  }))
)
</script>
`

// 5. 按钮改造模板
export const buttonComponentTemplate = `
<!-- 改造前 -->
<button class="primary" @click="handleClick">确定</button>
<button class="ghost" @click="handleCancel">取消</button>
<button class="danger" @click="handleDelete">删除</button>

<!-- 改造后 -->
<CustomButton type="primary" @click="handleClick">确定</CustomButton>
<CustomButton type="ghost" @click="handleCancel">取消</CustomButton>
<CustomButton type="danger" @click="handleDelete">删除</CustomButton>

<!-- 在 script 中引入组件 -->
<script setup lang="ts">
import CustomButton from '@/components/CustomButton.vue'
</script>
`

// 6. 页面效果应用模板
export const pageEffectTemplate = `
<!-- 为页面容器添加效果 -->
<template>
  <section class="page-container glass-float">
    <!-- 页面内容 -->
  </section>
</template>

<!-- 为面板/卡片添加效果 -->
<template>
  <article class="panel glass-effect">
    <!-- 面板内容 -->
  </article>
  
  <div class="card float-effect">
    <!-- 卡片内容 -->
  </div>
</template>
`

// 7. 完整的组件引入列表
export const componentImports = `
// 在需要使用美化组件的页面中引入
import ModalDialog from '@/components/ModalDialog.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import CustomButton from '@/components/CustomButton.vue'
import EffectToggle from '@/components/EffectToggle.vue'

// 如果需要使用工具函数
import { 
  useEffects, 
  useModalForm,
  enhancePageContainer 
} from '@/utils/pageEnhancer'
`

// 8. 效果类说明
export const effectClassesInfo = `
可用的效果类：

1. glass-effect - 轻度毛玻璃效果
   - 半透明白色背景 (70% 不透明度)
   - 20px 模糊效果
   - 适合大多数面板和卡片

2. glass-effect-medium - 中等毛玻璃效果
   - 半透明白色背景 (50% 不透明度)
   - 20px 模糊效果
   - 适合需要更强透明感的元素

3. glass-effect-dark - 深色毛玻璃效果
   - 半透明白色背景 (30% 不透明度)
   - 30px 模糊效果
   - 适合需要很强透明感的元素

4. float-effect - 悬浮效果
   - 鼠标悬停时向上移动 8px
   - 阴影增强效果
   - 适合卡片、按钮等交互元素

5. glass-float - 组合效果
   - 毛玻璃 + 悬浮效果
   - 最常用的组合
   - 适合主要面板和卡片

使用示例：
<div class="panel glass-float">内容</div>
<article class="card glass-effect">内容</article>
<section class="container float-effect">内容</section>
`

// 导出所有模板
export const beautificationTemplates = {
  effectToggle: addEffectToggleToSettings,
  globalEffects: addGlobalEffectsToLayout,
  modalForm: modalFormTemplate,
  select: selectComponentTemplate,
  button: buttonComponentTemplate,
  pageEffect: pageEffectTemplate,
  imports: componentImports,
  effectClasses: effectClassesInfo
}

console.log('美化模板已加载，请参考 BEAUTIFICATION_GUIDE.md 进行改造')
