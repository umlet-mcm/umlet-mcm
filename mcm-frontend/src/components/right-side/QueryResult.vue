<script setup lang="ts">

import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {PropType, ref, watch} from "vue";
import {ScrollArea} from "@/components/ui/scroll-area";
import VueJsonPretty from 'vue-json-pretty';
import 'vue-json-pretty/lib/styles.css';

/**
 * @param {Record<string, any>[]} queryResponse, response from the query (optional)
 */
const props = defineProps({
  queryResponse: {
    type: Array as PropType<Record<string, any>[]>,
    required: false
  }
});

//variables
const keys = ref<string[] | undefined>(undefined);
const message = ref<string | undefined>("No result to display");


//functions
/**
 * Watch the queryResponse and update when it changes
 */
watch(() => props.queryResponse, (newValue, oldValue) => {
  // update keys when queryResponse changes
  if (newValue !== oldValue && newValue !== undefined) {
    if(props.queryResponse!.length === 0 || Object.keys(props.queryResponse![0]).length === 0) {
      keys.value = undefined
      message.value = "Query result is empty"
    } else {
      keys.value = Object.keys(props.queryResponse![0]);
    }
  }
});
</script>

<template>
  <div class="flex flex-col h-full">
    <h2 class="text-lg font-semibold mb-4">Query Results</h2>
    <div v-if="queryResponse && keys" class="flex-1 min-h-0">
      <Tabs :default-value="keys[0]" class="flex flex-col h-full">
        <TabsList>
          <TabsTrigger v-for="key in keys" :key="key" :value="key">{{key}}</TabsTrigger>
        </TabsList>
        <div class="flex-1 min-h-0">
          <TabsContent v-for="key in keys" :key="key" :value="key" class="h-full">
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
    <div v-else class="flex-1 flex items-center justify-center">
      <p class="text-muted-foreground">
        {{ message }}
      </p>
    </div>
  </div>
</template>