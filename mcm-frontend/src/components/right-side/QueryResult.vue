<script setup lang="ts">

import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {onMounted, PropType, ref, watch} from "vue";
import {ScrollArea} from "@/components/ui/scroll-area";
import VueJsonPretty from 'vue-json-pretty';
import 'vue-json-pretty/lib/styles.css';

//variables
const keys = ref<string[]>([]);

//props
const props = defineProps({
  queryResponse: {
    type: Array as PropType<Record<string, any>[]>,
    required: false
  }
});

watch(() => props.queryResponse, (newValue, oldValue) => {
  console.log('Query response changed', newValue, oldValue);
  if (newValue !== oldValue && newValue !== undefined) {
    //todo should react to a request
    keys.value = Object.keys(props.queryResponse![0]);
  }
});

onMounted(() => {
  //todo to remove, should be handled by watch
  keys.value = Object.keys(props.queryResponse![0])
});
</script>

<template>
  <div class="flex flex-col h-full">
    <h2 class="text-lg font-semibold mb-4">Query Results</h2>
    <div v-if="queryResponse === undefined" class="flex-1 flex items-center justify-center">
      <p class="text-muted-foreground">
        No results to display
      </p>
    </div>
    <div v-else class="flex-1 min-h-0">
      <Tabs :default-value="keys[0]" class="flex flex-col h-full">
        <TabsList>
          <TabsTrigger v-for="key in keys" :key="key" :value="key">{{key}}</TabsTrigger>
        </TabsList>
        <div class="flex-1 min-h-0">
          <TabsContent v-for="key in keys" :key="key" :value="key" class="h-full mt-0 data-[state=active]:h-full">
            <ScrollArea class="h-full rounded-md">
              <div class="p-2">
                <div v-for="(obj, index) in queryResponse" :key="index">
                  <vue-json-pretty class="bg-muted rounded-md p-2 mb-2 overflow-x-auto" :data="obj[key]"/>
                </div>
              </div>
            </ScrollArea>
          </TabsContent>
        </div>
      </Tabs>
    </div>
  </div>
</template>