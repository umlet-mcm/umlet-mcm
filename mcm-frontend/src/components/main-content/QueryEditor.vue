<script setup lang="ts">
import { Codemirror } from 'vue-codemirror'
import { oneDark } from '@codemirror/theme-one-dark'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const extensions = [oneDark]

const onChange = (value: string) => {
  emit('update:modelValue', value)
}
</script>

<template>
  <div class="border border-border rounded-md overflow-hidden">
    <Codemirror
        :value="modelValue"
        :extensions="extensions"
        @change="onChange"
        placeholder="Enter your Neo4j query here..."
        :indent-with-tab="true"
        :tab-size="2"
        class="h-32"
    />
  </div>
</template>