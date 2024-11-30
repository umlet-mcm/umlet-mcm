<script setup lang="ts">
import { Separator } from '@/components/ui/separator'
import {Model} from "@/types/Model.ts";
import {Node} from "@/types/Node.ts";

defineProps({
  selectedModel: {
    type: Object as () => Model,
    required: false
  },
  selectedNode: {
    type: Object as () => Node,
    required: false
  }
});

</script>

<template>
  <div class="w-96 bg-card border-l border-border p-4 flex flex-col gap-4">
    <h2 class="text-lg font-semibold mb-4">Query Results</h2>
    <div class="flex justify-center items-center h-full">
      <p class="text-muted-foreground">No results to display</p>
    </div>
    <Separator />
    <h2 class="text-lg font-semibold mb-4">
      Node attributes of : {{ selectedNode?.id }}
    </h2>
    <div class="flex justify-center h-full">
      <p v-if="selectedNode === undefined" class="text-muted-foreground flex h-full items-center">
        No attributes to display
      </p>
      <table v-else class="table-auto border-collapse border border-gray-200 w-full">
        <thead class="bg-secondary">
        <tr>
          <th class="border border-gray-300 px-4 py-2 text-left">Attribute</th>
          <th class="border border-gray-300 px-4 py-2 text-left">Value</th>
        </tr>
        </thead>
        <tbody>
        <tr class="bg-primary-light" v-for="(attribute, index) in selectedNode?.properties" :key="index">
          <td class="border border-gray-300 px-4 py-2">{{ attribute[0] }}</td>
          <td class="border border-gray-300 px-4 py-2">{{ attribute[1] }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>