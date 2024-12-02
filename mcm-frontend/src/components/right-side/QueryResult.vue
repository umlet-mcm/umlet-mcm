<script setup lang="ts">

import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {onMounted, ref, watch} from "vue";
import {Neo4jResponse} from "@/types/Neo4jResponse.ts";

//variables
const keys = ref<string[]>([]);

//props
const props = defineProps({
  queryResponse: {
    type: Object as () => Neo4jResponse,
    required: false
  }
});

function formatValue(value: any): string {
  return JSON.stringify(value, null, 2)
}

watch(() => props.queryResponse, (newValue, oldValue) => {
  console.log('Query response changed', newValue, oldValue);
  if (newValue !== oldValue && newValue !== undefined) {
    const response = newValue as Neo4jResponse;
    //todo should react to a request
    keys.value = Object.values(response.results[0].columns);
  }
});

onMounted(() => {
  //todo to remove, should be handled by watch
  keys.value = Object.values(props.queryResponse!.results[0].columns)
});

</script>

<template>
  <h2 class="text-lg font-semibold mb-4">Query Results</h2>
  <div v-if="queryResponse === undefined" class="flex h-full items-center justify-center">
    <p class="text-muted-foreground">
      No results to display
    </p>
  </div>
  <div v-else class="h-full flex flex-col">
    <Tabs :default-value="keys[0]" class="w-full flex-1 overflow-auto">
      <TabsList>
        <TabsTrigger v-for="key in keys" :key="key" :value="key">{{key}}</TabsTrigger>
      </TabsList>
      <TabsContent class="" v-for="key in keys" :key="key" :value="key">
<!--        todo : to update in case of multiple response ? -->
        <pre class="bg-gray-100 rounded-md max-h-full">
          {{formatValue(queryResponse.results[0].data.map((d: any) => d.row[keys.indexOf(key)]))}}
        </pre>
      </TabsContent>
    </Tabs>
  </div>
</template>
