<script setup lang="ts">
import { Separator } from '@/components/ui/separator'
import {Model} from "@/types/Model.ts";
import {Node} from "@/types/Node.ts";
import QueryResult from "@/components/right-side/QueryResult.vue";
import NodeViewer from "@/components/right-side/NodeViewer.vue";
import {PropType} from "vue";

defineProps({
  selectedModel: {
    type: Object as () => Model,
    required: false
  },
  selectedNode: {
    type: Object as () => Node,
    required: false
  },
  queryResponse: {
    type: Array as PropType<Record<string, any>[]>,
    required: false
  }
});

</script>

<template>
  <div class="w-96 bg-card border-l border-border p-4 flex flex-col gap-4 h-full">
    <div class="flex-1 overflow-hidden">
      <QueryResult :queryResponse="queryResponse"/>
    </div>
    <Separator />
    <div class="flex-1 overflow-auto">
    <NodeViewer :selectedModel="selectedModel" :selectedNode="selectedNode" />
    </div>
  </div>
</template>