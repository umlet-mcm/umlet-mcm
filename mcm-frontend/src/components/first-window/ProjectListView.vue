<script setup lang="ts">
import { FileIcon } from 'lucide-vue-next'
import {Configuration} from "@/datamodel/Configuration";

defineProps<{
  project: Configuration
}>()

defineEmits<{
  (e: 'select', project: Configuration): void
}>()
</script>

<template>
  <button
      @click="$emit('select', project)"
      class="w-full text-left p-3 rounded-lg hover:bg-gray-100 transition-colors flex items-center gap-3"
  >
    <FileIcon class="w-5 h-5 text-gray-500" />
    <div>
      <p class="font-medium">{{ project.name }}</p>
      <p class="text-sm text-gray-500">{{ project.path }}</p>
    </div>
  </button>
</template>