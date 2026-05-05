<template>
  <el-tag :type="tagType" :effect="effect" :size="size" disable-transitions>
    {{ label }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { getStatusLabel, getStatusType } from '@/utils/format'

const props = defineProps({
  status: {
    type: [Number, String],
    required: true
  },
  size: {
    type: String,
    default: 'default'
  },
  effect: {
    type: String,
    default: 'light'
  }
})

const label = computed(() => getStatusLabel(Number(props.status)))
const tagType = computed(() => getStatusType(Number(props.status)))
</script>
