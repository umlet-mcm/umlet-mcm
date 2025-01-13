<script setup lang="ts">

import {PropType, ref, watch} from "vue";
import VueJsonPretty from 'vue-json-pretty';
import 'vue-json-pretty/lib/styles.css';

//variables
const keys = ref<string[] | undefined>(undefined);
const message = ref<string | undefined>("No result to display");

//props
const props = defineProps({
  queryResponse: {
    type: Array as PropType<Record<string, any>[]>,
    required: false
  }
});

const getKeys = (queryResponse: Record<string, any>[]): string[] => {
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

//functions
watch(() => props.queryResponse, (newValue, oldValue) => {
  // update keys when queryResponse changes
  if (newValue !== oldValue && newValue !== undefined) {
    keys.value = getKeys(newValue)
    if(keys.value.length === 0) {
      message.value = "Query result is empty"
    } else {
      message.value = undefined
    }
  }
});
</script>

<template>
  <div class="flex flex-col h-full">
    <div v-if="queryResponse?.length" class="flex-1 min-h-0">
      <div class="p-2">
        <div v-for="(obj, index) in queryResponse" :key="index">
          <vue-json-pretty class="bg-muted rounded-md p-2 mb-2 overflow-x-auto" :data="obj"/>
        </div>
      </div>
    </div>
    <div v-else class="flex-1 flex items-center justify-center">
      <p class="text-muted-foreground">
        {{message}}
      </p>
    </div>
  </div>
</template>