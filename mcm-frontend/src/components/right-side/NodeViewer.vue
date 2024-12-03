<script setup lang="ts">
import {Node} from "@/types/Node.ts";
import {TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";

defineProps({
  selectedNode: {
    type: Object as () => Node,
    required: false
  }
});
</script>

<template>
  <div class="space-y-2 p-2">
    <div class="flex items-center justify-between space-y-2">
      <h2 class="text-lg font-semibold">
        Node attributes of : {{ selectedNode?.text }}
      </h2>
    </div>
    <div class="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="w-full">Attribute</TableHead>
            <TableHead>Value</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="[key, value] in selectedNode?.properties" :key="key">
            <TableCell class="font-medium w-full">{{ key }}</TableCell>
            <TableCell>
              <span>{{ value }}</span>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
  </div>
</template>