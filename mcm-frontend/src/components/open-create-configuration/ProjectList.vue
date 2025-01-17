<script setup lang="ts">
import { FolderIcon } from 'lucide-vue-next'
import {Configuration} from "@/types/Configuration";
import ProjectListView from "@/components/open-create-configuration/ProjectListView.vue";
import {ScrollArea} from "@/components/ui/scroll-area";

/**
 * @param {Configuration[]} configurations, list of configurations to display
 */
defineProps({
  configurations: {
    type: Array as () => Configuration[],
    required: true
  }
})
</script>

<template>
  <div class="w-1/2 border-r border-gray-200 p-6">
    <h2 class="text-2xl font-semibold mb-4 flex items-center gap-2">
      <FolderIcon class="w-6 h-6" />
      Model Configuration list
    </h2>
    <div class="space-y-2">
      <ScrollArea class="h-52 rounded-md border">
      <ProjectListView
          v-for="config in configurations"
          :key="config.name"
          :configuration="config"
          @select="$router.push({ name: 'mainview', params: { id: config.name } })"
      />
      </ScrollArea>
    </div>
  </div>
</template>