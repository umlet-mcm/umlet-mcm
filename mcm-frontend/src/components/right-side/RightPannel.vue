<script setup lang="ts">
import {Model} from "@/types/Model.ts";
import {Node, Relation} from "@/types/Node.ts";
import PropViewer from "@/components/right-side/PropViewer.vue";
import {PropType} from "vue";

defineProps({
  selectedModel: {
    type: Object as () => Model,
    required: false
  },
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
    <div class="flex-1 overflow-auto">
      <PropViewer :selectedEntity="selectedEntity" />
    </div>
  </div>
</template>