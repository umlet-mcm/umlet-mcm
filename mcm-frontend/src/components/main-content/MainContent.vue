<script setup lang="ts">
import { Button } from '@/components/ui/button'
import QueryEditor from "@/components/main-content/QueryEditor.vue"
import GraphVisualisation from "@/components/main-content/GraphVisualisation.vue"
import { ref } from "vue"
import { Play, Eye, ArrowLeft, Save } from 'lucide-vue-next'
import {Model, models_data} from "@/datamodel/Model.ts";

const query = ref('')
const models = ref<Model[]>(models_data)

defineProps({
  selectedModel: {
    type: Number,
    required: false
  }
});

const selectedNode = ref<string | undefined>()
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
        Current model : {{ models.find(m => m.id === selectedModel)?.name }}
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
      <GraphVisualisation class="h-full w-full" v-model:selected-node="selectedNode" />
    </div>
  </div>
</template>