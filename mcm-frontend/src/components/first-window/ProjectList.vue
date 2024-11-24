<script setup lang="ts">
import { FolderIcon } from 'lucide-vue-next'
import {Configuration} from "@/datamodel/Configuration";
import ProjectListView from "@/components/first-window/ProjectListView.vue";
import {ScrollArea} from "@/components/ui/scroll-area";

defineProps<{
  projects: Configuration[]
}>()
</script>

<template>
  <div class="w-1/2 border-r border-gray-200 p-6">
    <h2 class="text-2xl font-semibold mb-4 flex items-center gap-2">
      <FolderIcon class="w-6 h-6" />
      Open recent
    </h2>
    <div class="space-y-2">
      <ScrollArea class="h-52 rounded-md border">
      <ProjectListView
          v-for="project in projects"
          :key="project.id"
          :project="project"
          @select="$emit('select', project)"
      />
      </ScrollArea>
    </div>
  </div>
</template>