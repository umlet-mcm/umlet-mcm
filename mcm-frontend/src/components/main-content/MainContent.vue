<script setup lang="ts">
import { Button } from '@/components/ui/button'
import QueryEditor from "@/components/main-content/QueryEditor.vue"
import GraphVisualisation from "@/components/main-content/GraphVisualisation.vue"
import {PropType, ref} from "vue"
import {HelpCircle, Play} from 'lucide-vue-next'
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

// execute multiple queries
const executeMultipleQuery = async (queries: string[]) => {
  let nbOk = 0, totalTime = 0, response
  errorMessage.value = undefined
  queryMessage.value = undefined

  try {
    for (const query of queries.filter(Boolean)) {
      // for all queries, measure the time it takes to execute
      const startTime = performance.now()
      response = await sendRequest(query)
      totalTime += performance.now() - startTime
      nbOk++
      queryNum.value++
    }
  } catch (error: any) {
    // if an error occurs, display the error message and the number of queries executed successfully
    queryMessage.value = nbOk ? `${nbOk} ${nbOk === 1 ? "query" : "queries"} executed successfully in ${totalTime} ms. Error on query ${nbOk + 1}` : undefined
    errorMessage.value = error.response?.data?.Message || error.message
  }

  // display the last response
  if (response) emit('update:response', response)
  if (!errorMessage.value) {
    queryMessage.value = `${nbOk} ${nbOk === 1 ? "query" : "queries"} executed successfully in ${totalTime} ms.`
  }
};

// execute the query field
const executeQuery = async () => {
  if (!query.value?.trim()) return
  query.value = query.value.trim().replace("/\n/g", "")
  const formattedQuery = query.value.endsWith(";") ? query.value : `${query.value};`
  await executeMultipleQuery(formattedQuery.split(";").filter(Boolean))
};
</script>

<template>
  <div class="flex-1 flex flex-col">
    <div class="p-4 space-y-4">
      <div class="flex items-center ">
        <h2 class="text-xl font-semibold">Neo4J Query</h2>
        <Button variant="ghost" size="icon" @click="console.log('help query')">
          <HelpCircle />
        </Button>
      </div>
      <QueryEditor v-model="query" />
      <div class="flex gap-2">
        <Button @click="executeQuery" class="flex items-center">
          <Play class="mr-2 h-4 w-4" />
          Execute Query
        </Button>
        <label v-if="queryMessage" class="text-sm text-green-500 content-center">{{ "["+queryNum+"] " + queryMessage }}</label>
        <label v-if="errorMessage" class="text-sm text-red-500 content-center">{{ "["+queryNum+"] " + errorMessage }}</label>
      </div>
    </div>
    <div class="flex items-center justify-between">
      <h1 class="text-lg font-bold p-4">
        Current model : {{ selectedModel?.id }}
      </h1>
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