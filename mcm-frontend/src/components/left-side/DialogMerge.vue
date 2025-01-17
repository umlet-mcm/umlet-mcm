<script setup lang="ts">
import {computed, ref} from 'vue'
import {Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle} from '@/components/ui/dialog'
import {Card, CardContent} from '@/components/ui/card'
import {ScrollArea} from '@/components/ui/scroll-area'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Checkbox} from '@/components/ui/checkbox'
import {Configuration} from '@/types/Configuration.ts'
import {updateConfiguration} from "@/api/configuration.ts";
import {Model} from "@/types/Model.ts";
import {v4 as uuidv4} from 'uuid'
import {PlusIcon} from "lucide-vue-next";

// variables
const selectedModelsId = ref<string[]>([])
const newModelName = ref('')
const errorMessage = ref<string | undefined>(undefined)
const isLoadingValidate = ref(false)

// props related
const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  configuration: {
    type: Object as () => Configuration,
    required: true
  }
});
const emit = defineEmits<{
  'update:isOpen': [value: boolean],
  'update:configuration': [value: Configuration],
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

/**
 * Combine the selected models into a new model
 * Update the ids of the nodes and relations to avoid conflicts
 * @param selectedModels
 */
function combine(selectedModels: Model[]): Model {
  const generatedUUID = uuidv4();
  const nodesNewId = selectedModels.flatMap(model => model.nodes).map(node => ({
    oldId: node.id,
    newId: uuidv4()
  }));

  const nodes = selectedModels.flatMap(model => model.nodes).map(node => ({
    // get all the properties of the node and add the new mcmModelId and id
    ...node,
    mcmModelId: generatedUUID,
    id: nodesNewId.find(n => n.oldId === node.id)!.newId,
    relations: node.relations.map(relation => ({
      // get all the properties of the relation and add the new mcmModelId, id and updated target
      ...relation,
      id: uuidv4(),
      mcmModelId: generatedUUID,
      target: nodesNewId.find(n => n.oldId === relation.target)?.newId || ""
    }))
  }));

  return {
    id: generatedUUID,
    description: "",
    mcmAttributes: {},
    originalText: "",
    title: newModelName.value,
    tags: [],
    nodes: nodes,
    zoomLevel: 10
  };
}

const handleMerge = async () => {
  if (canMerge.value) {
    isLoadingValidate.value = true
    try {
      // get the selected models
      const selectedModels: Model[] = selectedModelsId.value.map(id => props.configuration.models.find(m => m.id === id) as Model)
      // merge the models together
      const mergedModel = combine(JSON.parse(JSON.stringify(selectedModels)))

      const newConfig = await updateConfiguration({
        ...props.configuration,
        models: [...props.configuration.models, mergedModel]
      })
      emit('update:configuration', newConfig)
      closeDialog()
    } catch (error:any) {
      errorMessage.value = 'An error occurred while combining the models : ' + (error.response?.data?.message || error.message)
    }
    isLoadingValidate.value = false
  }
}

const closeDialog = () => {
  selectedModelsId.value = []
  newModelName.value = ''
  errorMessage.value = undefined
  isLoadingValidate.value = false
  emit('update:isOpen', false)
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog">
    <DialogContent class="sm:max-w-[800px]">
      <DialogHeader>
        <DialogTitle>Combine Models</DialogTitle>
        <DialogDescription>
          Combine multiple models together in a new model within this model configuration.
        </DialogDescription>
      </DialogHeader>

      <div class="grid grid-cols-2 gap-4 py-4">
        <!-- Left side - Model Selection -->
        <Card>
          <CardContent class="p-4">
            <h3 class="font-bold mb-4">Models in configuration</h3>
            <ScrollArea class="h-[300px] w-full rounded-md border p-4">
              <div class="space-y-4">
                <div v-for="model in configuration.models" :key="model.id"
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
            <h3 class="font-bold mb-4">Combined Model Output</h3>
            <div class="space-y-4">
              <div class="space-y-2">
                <label for="newModelName" class="text-sm font-medium">
                  Combined Model Name
                </label>
                <Input
                    id="newModelName"
                    v-model="newModelName"
                    placeholder="Enter new model name"/>
              </div>

              <div v-if="selectedModelsId.length > 0" class="text-sm">
                Selected models to combine:
                <ul class="list-disc list-inside mt-2">
                  <li v-for="modelId in selectedModelsId" :key="modelId">
                    {{ configuration.models.find(m => m.id === modelId)?.id }}
                  </li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
      <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>

      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="handleMerge" :disabled="!canMerge || isLoadingValidate">
          <PlusIcon v-if="!isLoadingValidate" class="w-5 h-5" />
          Combine Models
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>