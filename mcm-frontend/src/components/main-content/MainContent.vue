<script setup lang="ts">
import { Button } from '@/components/ui/button'
import QueryEditor from "@/components/main-content/QueryEditor.vue"
import GraphVisualisation from "@/components/main-content/GraphVisualisation.vue"
import {PropType, ref} from "vue"
import { Play } from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import {Node, Relation} from "@/types/Node.ts";
import {sendRequest} from "@/api/graphDB.ts";

// variables
const query = ref('')
const errorMessage = ref<string | undefined>(undefined)
const queryMessage = ref<string | undefined>(undefined)
const queryNum = ref(0)

// props related
defineProps({
  selectedModel: {
    type: Object as () => Model,
    required: false
  },
  selectedEntity: {
    type: Object as () => Node | Relation,
    required: false
  },
  response: {
    type: Array as PropType<Record<string, any>[]>,
    required: false
  }
});
const emit = defineEmits(["update:selectedEntity", "update:response"]);

// functions
const executeQuery = async () => {
  if(!query.value) return
  queryNum.value += 1
  try {
    const startTime = performance.now();
    const response = await sendRequest(query.value)
    const endTime = performance.now();
    emit('update:response', response)
    errorMessage.value = undefined
    queryMessage.value = `Query executed successfully in ${(endTime - startTime)} ms`
  } catch (error: any) {
    errorMessage.value = error.response.data.Message ? error.response.data.Message : error.message
    queryMessage.value = undefined
  }
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
        <label v-if="errorMessage" class="text-sm text-red-500 content-center">{{ "["+queryNum+"] " + errorMessage }}</label>
        <label v-if="queryMessage" class="text-sm text-green-500 content-center">{{ "["+queryNum+"] " + queryMessage }}</label>
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
            @update:selectedEntity="emit('update:selectedEntity', $event)"/>
      </div>
      <div v-else class="h-full w-full">
        <div class="flex-1 flex justify-center">
          <p class="text-muted-foreground">No model selected</p>
        </div>
      </div>
    </div>
  </div>
</template>