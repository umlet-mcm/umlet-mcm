<script setup lang="ts">
import { Button } from '@/components/ui/button'
import QueryEditor from "@/components/main-content/QueryEditor.vue"
import GraphVisualisation from "@/components/main-content/GraphVisualisation.vue"
import { ref } from "vue"
import { Play, Eye, ArrowLeft, Save } from 'lucide-vue-next'
import {Model} from "@/datamodel/Model.ts";
import {Node} from "@/datamodel/Node.ts";

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

const previewQuery = () => {
  console.log('Preview query:', query.value)
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
        <Button @click="previewQuery" variant="secondary" class="flex items-center">
          <Eye class="mr-2 h-4 w-4" />
          Preview Query
        </Button>
      </div>
    </div>
    <div class="flex items-center justify-between p-4">
      <h1 class="text-xl font-bold p-4">
        Current model : {{ selectedModel?.name }}
      </h1>
      <div class="flex gap-4">
        <Button class="p-2 rounded-full" variant="outline">
          <ArrowLeft class="h-5 w-5" />
        </Button>
        <Button class="p-2 rounded-full" variant="outline">
          <Save class="h-5 w-5" />
        </Button>
      </div>
    </div>
    <div class="flex-1 p-2 overflow-hidden">
      <GraphVisualisation
          class="h-full w-full"
          :selected-node="selectedNode"
          :selected-model-id="selectedModel?.id"
          @update:selectedNode="emit('update:selectedNode', $event)"/>
    </div>
  </div>
</template>