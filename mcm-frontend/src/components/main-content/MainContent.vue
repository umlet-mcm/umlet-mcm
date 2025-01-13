<script setup lang="ts">
import {Button} from '@/components/ui/button'
import QueryEditor from "@/components/main-content/QueryEditor.vue"
import GraphVisualisation from "@/components/main-content/GraphVisualisation.vue"
import {PropType, ref, watch} from "vue"
import {HelpCircle, Play} from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import {Node, Relation} from "@/types/Node.ts";
import {sendRequest} from "@/api/graphDB.ts";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import QueryResult from "@/components/right-side/QueryResult.vue";
import {parseResponseGraph} from "@/components/main-content/responseGraphVisualization.ts";

// props related
const props = defineProps({
  selectedModel: {
    type: Object as () => Model | undefined,
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

// variables
const query = ref('')
const errorMessage = ref<string | undefined>(undefined)
const queryMessage = ref<string | undefined>(undefined)
const queryGraph = ref<Model | undefined>(undefined);
const queryNum = ref(0)
const activeTab = ref('full')
const messageGraph = ref<string | undefined>("Query result cannot be displayed as a Model")

const emit = defineEmits(["update:selectedEntity", "update:response"]);
let queryExecutionTimestamp: string | undefined;

//functions

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
    queryExecutionTimestamp = new Date().toLocaleString("de-AT");
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

const getColumns = (queryResponse: Record<string, any>[]): string[] => {
  const keys:string[] = []
  for(let i = 0; i < queryResponse.length; i++) {
    for (const key in queryResponse[i]) {
      if (!keys.includes(key)) {
        keys.push(key)
      }
    }
  }
  return keys;
}

const removeLabel = (json: any) => {
  const { labels, ...withoutLabels } = json;
  return withoutLabels;
}

// when the response changes, parse it to a graph
watch(() => props.response, async (newValue) => {
  if (newValue?.length) {
    if(props.selectedModel) {
      queryGraph.value = await parseResponseGraph(newValue, props.selectedModel);
      if(queryGraph.value.nodes.length === 0) {
        queryGraph.value = undefined
        messageGraph.value = "Response model is empty"
      }
    }
  } else {
    queryGraph.value = undefined;
  }
});
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

    <Tabs default-value="full" v-model:model-value="activeTab" class="h-full w-full overflow-hidden p-2">
      <TabsList>
        <TabsTrigger value="full">Current Model</TabsTrigger>
        <TabsTrigger value="request">Response as model</TabsTrigger>
        <TabsTrigger value="table">Response as table</TabsTrigger>
        <TabsTrigger value="json">Response as JSON</TabsTrigger>
      </TabsList>

      <TabsContent value="full" class="h-[95%]">
        <div v-if="selectedModel" class="h-full w-full">
            <GraphVisualisation
                :model-to-display="selectedModel"
                @update:selectedEntity="emit('update:selectedEntity', $event)"/>
        </div>
        <div v-else class="h-full w-full">
          <div class="flex-1 flex justify-center h-full">
            <p class="text-muted-foreground self-center">No model selected</p>
          </div>
        </div>
      </TabsContent>
      <TabsContent value="request" class="h-[95%]">
        <div v-if="queryGraph" class="h-full w-full">
          <GraphVisualisation
              :model-to-display="queryGraph"
              @update:selectedEntity="emit('update:selectedEntity', $event)"/>
        </div>
        <div v-else class="h-full w-full">
          <div class="flex-1 flex justify-center h-full">
            <p class="text-muted-foreground self-center">{{messageGraph}}</p>
          </div>
        </div>
      </TabsContent>
      <TabsContent value="table" class="h-[95%] overflow-scroll">
          <Table v-if="props.response?.[0] && getColumns(props.response).length !== 0" ref="responseTable">
            <TableHeader>
              <TableRow>
                <TableHead v-for="columnName in getColumns(props.response)" :key="columnName">{{ columnName }}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="(value, index) in response" :key="index" class="even:bg-gray-300">

                <TableCell v-for="columnName in getColumns(props.response)">
                  <span v-if="value[columnName] && value[columnName].labels?.[0]">
                    {{ value[columnName].labels?.[0] }}&nbsp;{{ removeLabel(value[columnName]) }}
                  </span>
                  <span v-else>
                    {{ value[columnName] }}
                  </span>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        <div v-else class="h-full w-full">
          <div class="flex-1 flex justify-center h-full">
            <p class="text-muted-foreground self-center">
              Query {{ queryNum ? "[" + queryNum + "]" : "" }} response was empty
              {{
                queryExecutionTimestamp ?
                    "(executed at " + queryExecutionTimestamp + ")" :
                    "(no query has been executed yet)"
              }}
            </p>
          </div>
        </div>
      </TabsContent>
      <TabsContent value="json" class="h-[95%] overflow-scroll">
        <QueryResult :queryResponse="response"/>
      </TabsContent>
    </Tabs>
  </div>
</template>