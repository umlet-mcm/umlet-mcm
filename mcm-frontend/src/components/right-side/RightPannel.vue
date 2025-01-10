<script setup lang="ts">
import { Separator } from '@/components/ui/separator'
import {Node, Relation} from "@/types/Node.ts";
import QueryResult from "@/components/right-side/QueryResult.vue";
import PropViewer from "@/components/right-side/PropViewer.vue";
import {PropType} from "vue";

/**
 * @param {Node | Relation} selectedEntity, selected entity (optional)
 * @param {Record<string, any>[]} queryResponse, response from the query (optional)
 */
defineProps({
  selectedEntity: {
    type: Object as () => Node | Relation,
    required: false
  },
  queryResponse: {
    type: Array as PropType<Record<string, any>[]>,
    required: false
  }
});

</script>

<template>
  <div class="w-96 bg-card border-l border-border p-3 flex flex-col gap-4 h-full">
    <div class="flex-1 overflow-hidden">
      <QueryResult :queryResponse="queryResponse"/>
    </div>
    <Separator />
    <div class="flex-1 overflow-auto">
      <PropViewer :selectedEntity="selectedEntity" />
    </div>
  </div>
</template>