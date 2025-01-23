<script setup lang="ts">
import {Node, Relation} from "@/types/Node.ts";
import {TableBody, TableCell, TableHead, TableRow, Table} from "@/components/ui/table";
import {HelpCircle} from "lucide-vue-next";
import {prettyPrint} from "@/lib/utils.ts";

/**
 * @param {Node | Relation} selectedEntity, selected entity (optional)
 */
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
        Properties {{ selectedEntity?.title ? `: ${prettyPrint(selectedEntity.title)}` : '' }}
      </h2>
    </div>
    <div v-if="selectedEntity" class="rounded-md border">
      <Table>
        <TableBody>
            <TableRow class="table-head" v-if="Object.keys(selectedEntity.mcmAttributes).length">
              <TableHead class="w-1/3">properties</TableHead>
              <TableHead class="w-2/3">value</TableHead>
            </TableRow>
            <TableRow v-for="[key, value] in Object.entries({...selectedEntity.mcmAttributes})">
              <TableCell class="font-medium flex items-center">
                <!--  TODO TOOLTIP BASED ON COMMENT -->
                <HelpCircle v-if="false" class="w-4 h-4 mr-2" v-tooltip="''"/>
                <span>{{ key }}</span>
              </TableCell>
              <TableCell class="w-2/3">{{ value }}</TableCell>
            </TableRow>
          <TableRow class="table-head" v-if="Object.keys(selectedEntity.umletAttributes).length">
            <TableHead class="w-1/3">umletProperties</TableHead>
            <TableHead class="w-2/3">value</TableHead>
          </TableRow>
          <TableRow v-for="[key, value] in Object.entries({...selectedEntity.umletAttributes})">
            <TableCell class="font-medium w-1/3">{{ key }}</TableCell>
            <TableCell class="w-2/3">{{ value }}</TableCell>
          </TableRow>
          <TableRow class="table-head" v-if="Object.keys(selectedEntity.mcmAttributes).length">
            <TableHead>Details</TableHead>
            <TableHead>value</TableHead>
          </TableRow>
          <TableRow>
            <TableCell class="font-medium w-1/3">&lt;Full name&gt;</TableCell>
            <TableCell class="w-2/3">{{ selectedEntity.title }}</TableCell>
          </TableRow>
          <TableRow>
            <TableCell class="font-medium w-1/3">&lt;Full description&gt;</TableCell>
            <TableCell class="w-2/3">{{ selectedEntity.description }}</TableCell>
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

<style scoped>
  .table-head {
    background-color: hsl(var(--accent));
  }
</style>