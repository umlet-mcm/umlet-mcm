<script setup lang="ts">
import {Configuration} from "@/types/Configuration";

/**
 * @param {Configuration} configuration, configuration to display
 */
defineProps({
  configuration: {
    type: Object as () => Configuration,
    required: true
  }
})

/**
 * @emits {Configuration} select, selected configuration
 */
defineEmits<{
  'select': [configuration: Configuration]
}>()
</script>

<template>
  <button
      @click="$emit('select', configuration)"
      class="w-full text-left p-3 rounded-lg hover:bg-gray-100 transition-colors flex items-center gap-3"
  >
    <div class="space-y-1">
      <p class="font-medium">{{ configuration.name }} <span class="text-gray-500">@{{configuration.version.customName ? configuration.version.customName : configuration.version.name}}</span></p>
      <p class="text-sm text-gray-500">{{ configuration.models.length }} model{{configuration.models.length > 1 ? 's' : ''}}</p>
    </div>
  </button>
</template>