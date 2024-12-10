<script setup lang="ts">
import { FileIcon } from 'lucide-vue-next'
import {Configuration} from "@/types/Configuration";

defineProps<{
  configuration: Configuration
}>()

defineEmits<{
  (e: 'select', configuration: Configuration): void
}>()
</script>

<template>
  <button
      @click="$emit('select', configuration)"
      class="w-full text-left p-3 rounded-lg hover:bg-gray-100 transition-colors flex items-center gap-3"
  >
    <FileIcon class="w-5 h-5 text-gray-500" />
    <div>
      <p class="font-medium">{{ configuration.name }} <span class="text-gray-500">@{{configuration.version}}</span></p>
      <p class="text-sm text-gray-500">{{ configuration.models.length }} models</p>
    </div>
  </button>
</template>