<script setup lang="ts">
import { Button } from '@/components/ui/button'
import QueryEditor from "@/components/main-content/QueryEditor.vue"
import GraphVisualisation from "@/components/main-content/GraphVisualisation.vue"
import { ref } from "vue"
import { Play } from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import {Node} from "@/types/Node.ts";

const query = ref('')

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

const emit = defineEmits(["update:selectedNode"]);

const executeQuery = () => {
  console.log('Executing query:', query.value)
}
</script>

<template>
  <div class="flex-1 flex flex-col">
    <div class="p-4 space-y-4">
      <h1 class="text-xl font-bold">Neo4J Query</h1>
      <QueryEditor v-model="query" />
      <div class="flex gap-2">
        <Button @click="executeQuery" class="flex items-center">
          <Play class="mr-2 h-4 w-4" />
          Execute Query
        </Button>
      </div>
    </div>
    <div class="flex items-center justify-between">
      <h1 class="text-lg font-bold p-4">
        Current model : {{ selectedModel?.id }}
      </h1>
<!--      <div class="flex p-4">-->
<!--        <Button class="p-2 rounded-full" variant="destructive">-->
<!--          <ArrowLeft class="h-5 w-5"/>-->
<!--          Previous-->
<!--        </Button>-->
<!--      </div>-->
    </div>

    <div class="flex-1 p-2 overflow-hidden">
      <div v-if="selectedModel" class="h-full w-full">
        <GraphVisualisation
            class="h-full w-full"
            :selected-model="selectedModel"
            @update:selectedNode="emit('update:selectedNode', $event)"/>
      </div>
      <div v-else class="h-full w-full">
        <div class="flex-1 flex justify-center">
          <p class="text-muted-foreground">No model selected</p>
        </div>
      </div>
    </div>
  </div>
</template>