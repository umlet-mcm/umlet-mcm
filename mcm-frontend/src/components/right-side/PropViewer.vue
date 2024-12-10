<script setup lang="ts">
import {Node, Relation} from "@/types/Node.ts";
import {TableBody, TableCell, TableHead, TableHeader, TableRow, Table} from "@/components/ui/table";

defineProps({
  selectedEntity: {
    type: Object as () => Node | Relation,
    required: false
  }
});
</script>

<template>
  <div class="space-y-2 p-2 h-full flex flex-col">
    <div class="flex items-center justify-between space-y-2">
      <h2 class="text-lg font-semibold">
        Properties {{ selectedEntity?.title ? `: ${selectedEntity.title}` : '' }}
      </h2>
    </div>
    <div v-if="selectedEntity" class="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="w-full">Attribute</TableHead>
            <TableHead>Value</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow v-for="[key, value] in Object.entries({...selectedEntity.mcmAttributes, ...selectedEntity.umletAttributes})">
            <TableCell class="font-medium w-full">{{ key }}</TableCell>
            <TableCell>
              <span>{{ value }}</span>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>
    <div v-else class="h-full">
        <div class="flex-1 flex items-center justify-center h-full">
          <p class="text-muted-foreground">
            No results to display
          </p>
        </div>
    </div>
  </div>

</template>