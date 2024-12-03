<script setup lang="ts">
import { ref, computed } from 'vue'
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription} from '@/components/ui/dialog'
import { Card, CardContent } from '@/components/ui/card'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Checkbox } from '@/components/ui/checkbox'
import { Model } from '@/types/Model'
import {mergeModels} from "@/api/model.ts";

// variables
const selectedModelsId = ref<string[]>([])
const newModelName = ref('')
const errorMessage = ref<string | undefined>(undefined)

// props related
const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  models: {
    type: Array as () => Model[],
    required: true
  }
});
const emit = defineEmits<{
  'update:isOpen': [value: boolean],
  merge: [mergedModel: Model]
}>()

/* functions */
const canMerge = computed(() => {
  return selectedModelsId.value.length >= 2 && newModelName.value.trim().length > 0
})

const toggleModel = (modelId: string) => {
  // find the index of the model in the selectedModelsId array
  const index = selectedModelsId.value.indexOf(modelId)
  if (index === -1) {
    // if the model is not in the array, add it
    selectedModelsId.value.push(modelId)
  } else {
    // if the model is in the array, remove it
    selectedModelsId.value.splice(index, 1)
  }
}

const handleMerge = async () => {
  if (canMerge.value) {
    try {
      // get the selected models
      const selectedModels: Model[] = selectedModelsId.value.map(id => props.models.find(m => m.id === id) as Model)
      // merge the models together
      const newModel = await mergeModels(selectedModels, newModelName.value)
      emit('merge', newModel)
      closeDialog()
    } catch (error) {
      errorMessage.value = 'An error occurred while merging the models : ' + error
    }
  }
}

const closeDialog = () => {
  selectedModelsId.value = []
  newModelName.value = ''
  emit('update:isOpen', false)
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog">
    <DialogContent class="sm:max-w-[800px]">
      <DialogHeader>
        <DialogTitle>Merge Models</DialogTitle>
        <DialogDescription>
          Merge multiple models together in a new model within configuration.
        </DialogDescription>
      </DialogHeader>

      <div class="grid grid-cols-2 gap-4 py-4">
        <!-- Left side - Model Selection -->
        <Card>
          <CardContent class="p-4">
            <h3 class="font-bold mb-4">Models in configuration</h3>
            <ScrollArea class="h-[300px] w-full rounded-md border p-4">
              <div class="space-y-4">
                <div v-for="model in models" :key="model.id"
                     class="flex items-center space-x-2">
                  <Checkbox
                      :id="model.id"
                      :checked="selectedModelsId.includes(model.id)"
                      @update:checked="() => toggleModel(model.id)"/>
                  <label
                      :for="model.id"
                      class="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                    {{ model.id }}
                  </label>
                </div>
              </div>
            </ScrollArea>
          </CardContent>
        </Card>

        <!-- Right side - New Model Name -->
        <Card>
          <CardContent class="p-4">
            <h3 class="font-bold mb-4">Merged Model Output</h3>
            <div class="space-y-4">
              <div class="space-y-2">
                <label for="newModelName" class="text-sm font-medium">
                  Merged Model Name
                </label>
                <Input
                    id="newModelName"
                    v-model="newModelName"
                    placeholder="Enter new model name"/>
              </div>

              <div v-if="selectedModelsId.length > 0" class="text-sm">
                Selected models to merge:
                <ul class="list-disc list-inside mt-2">
                  <li v-for="modelId in selectedModelsId" :key="modelId">
                    {{ models.find(m => m.id === modelId)?.id }}
                  </li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
      <label v-if="errorMessage !== undefined" class="text-sm font-medium text-red-500">{{errorMessage}}</label>

      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="handleMerge" :disabled="!canMerge">Merge Models</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>