<template>
  <el-tag :type="tagType" :size="size" disable-transitions>
    <el-icon v-if="showIcon" style="margin-right: 4px; vertical-align: middle;">
      <component :is="iconName" />
    </el-icon>
    <span style="vertical-align: middle;">{{ label }}</span>
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { getPayMethodLabel, getPayMethodType, PAY_METHOD_MAP } from '@/utils/format'

const props = defineProps({
  method: {
    type: [Number, String],
    required: true
  },
  size: {
    type: String,
    default: 'default'
  },
  showIcon: {
    type: Boolean,
    default: true
  }
})

const label = computed(() => getPayMethodLabel(Number(props.method)))
const tagType = computed(() => getPayMethodType(Number(props.method)))
const iconName = computed(() => PAY_METHOD_MAP[Number(props.method)]?.icon || 'Money')
</script>
