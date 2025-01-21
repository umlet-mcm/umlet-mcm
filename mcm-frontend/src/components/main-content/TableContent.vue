<script setup lang="ts">

import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {PropType} from "vue";

defineProps({
  queryResponse: {
    type: Array as PropType<Record<string, any>[]>,
    required: false
  },
  queryNum: {
    type: Number,
    required: true
  },
  queryTimestamp: {
    type: String,
    required: false
  }
});

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
</script>

<template>
  <Table v-if="queryResponse?.[0] && getColumns(queryResponse).length !== 0" ref="responseTable">
    <TableHeader>
      <TableRow>
        <TableHead v-for="columnName in getColumns(queryResponse)" :key="columnName">{{ columnName }}</TableHead>
      </TableRow>
    </TableHeader>
    <TableBody>
      <TableRow v-for="(value, index) in queryResponse" :key="index" class="even:bg-gray-300">

        <TableCell v-for="columnName in getColumns(queryResponse)">
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
      <p class="text-muted-foreground self-center" v-if="queryTimestamp">
        Query {{ queryNum ? "[" + queryNum + "]" : "" }} response was empty {{"(executed at " + queryTimestamp + ")"}}
      </p>
      <p class="text-muted-foreground self-center" v-else>
        No query has been executed yet
      </p>
    </div>
  </div>
</template>