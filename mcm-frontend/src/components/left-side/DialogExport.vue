<script setup lang="ts">
import {computed, ref} from "vue";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from "@/components/ui/dialog";
import {Card, CardContent} from "@/components/ui/card";
import {Input} from "@/components/ui/input";
import {Button} from "@/components/ui/button";
import {exportToUxf} from "@/api/files.ts";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import {exportToCsv} from "@/api/graphDB.ts";
import {Label} from "@/components/ui/label";

/**
 * @param {Boolean} isOpen, dialog visibility
 * @param {String} configurationName, configuration name
 */
const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  configurationName: {
    type: String,
    required: true
  }
});
const emit = defineEmits<{
  'update:isOpen': [value: boolean]
}>()

// variables
const exportName = ref(props.configurationName + '_MCM')
const exportType = ref('uxf')
const errorMessage = ref<string | undefined>(undefined)

// functions
const closeDialog = () => {
  errorMessage.value = undefined
  emit('update:isOpen', false)
}

/**
 * Check if the export can be done
 */
const canExport = computed(() => {
  return exportName.value.trim().length > 0 && exportType.value.trim().length > 0
})

/**
 * Handle the export of the configuration in the selected format (UXF or CSV)
 * Uses the exportToUxf or exportToCsv functions from the api
 */
const handleExport = async () => {
  if(canExport.value) {
    try {
      switch(exportType.value) {
        case 'uxf':
          await exportToUxf(props.configurationName, exportName.value, "configuration")
          break
        case 'csv':
          await exportToCsv(exportName.value)
          break
      }
      emit('update:isOpen', false)
    } catch (error: any) {
      errorMessage.value = 'An error occurred while exporting the configuration: ' + error.message
    }
  }
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog">
    <DialogContent class="sm:max-w-[800px]">
      <DialogHeader>
        <DialogTitle>Export Model Configuration</DialogTitle>
        <DialogDescription>
          Export this model configuration to a file.
        </DialogDescription>
      </DialogHeader>

      <div class="grid grid-cols-1 gap-4 py-4">
        <Card>
          <CardContent class="grid p-4">
            <h3 class="font-bold mb-4">Settings</h3>
            <div class="space-y-4">
              <div class="grid gap-2">
                <Label for="exportName" class="text-sm font-medium">Export name</Label>
                <Input
                    id="exportName"
                    v-model="exportName"
                    placeholder="Export name"/>
              </div>
              <div class="grid gap-2">
                <Label for="type" class="text-sm font-medium">Export type</Label>
                <RadioGroup v-model="exportType" default-value="uxf">
                  <div class="flex items-center space-x-2">
                    <RadioGroupItem id="uxf" value="uxf" />
                    <label for="uxf" class="text-sm">UXF</label>
                  </div>
                  <div class="flex items-center space-x-2">
                    <RadioGroupItem id="csv" value="csv" />
                    <label for="csv" class="text-sm">CSV</label>
                  </div>
                </RadioGroup>
              </div>
            </div>
        </CardContent>
        </Card>
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
      </div>
      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="handleExport" :disabled="!canExport">Export</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>