<script setup lang="ts">
import {computed, ref} from 'vue'
import {Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle} from '@/components/ui/dialog'
import {Card, CardContent} from '@/components/ui/card'
import {ScrollArea} from '@/components/ui/scroll-area'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Checkbox} from '@/components/ui/checkbox'
import {Configuration} from '@/types/Configuration.ts'
import {getLastCreatedConfiguration, updateConfiguration} from "@/api/configuration.ts";
import {Model} from "@/types/Model.ts";
import {LoaderCircleIcon} from "lucide-vue-next";
import {alignModels} from "@/api/model.ts";

/**
 * @param {Boolean} isOpen, dialog visibility
 * @param {Model[]} models, list of models to merge
 */
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

/**
 * @emits {Boolean} update:isOpen, dialog visibility
 * @emits {Model} merge, merged model newly created
 */
const emit = defineEmits<{
  'update:isOpen': [value: boolean],
  'update:configuration': [value: Configuration],
  'update:model': [value: Model]
}>()

// variables
const selectedModelsId = ref<string[]>([])
const newModelName = ref('')
const newVersionName = ref('')
const errorMessage = ref<string | undefined>(undefined)
const isLoadingValidate = ref(false)

/* functions */
const canMerge = computed(() => {
  return selectedModelsId.value.length >= 2 && newModelName.value.trim().length > 0
})

/**
 * Toggle the selection of a model
 * @param modelId
 */
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
  //todo GENERATE ID OR NOT ?
  const generatedUUID = crypto.randomUUID()
  const nodesNewId = selectedModels.flatMap(model => model.nodes).map(node => ({
    oldId: node.id,
    newId: crypto.randomUUID()
  }));

  const nodes = selectedModels.flatMap(model => model.nodes).map(node => ({
    // get all the properties of the node and add the new mcmModelId and id
    ...node,
    mcmModelId: generatedUUID,
    id: nodesNewId.find(n => n.oldId === node.id)!.newId,
    relations: node.relations.map(relation => ({
      // get all the properties of the relation and add the new mcmModelId, id and updated target
      ...relation,
      id: crypto.randomUUID(),
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

/**
 * Merge the selected models together and emit the new model
 * @emits {Model} merge, merged model newly created
 */
const handleMerge = async () => {
  if (canMerge.value) {
    isLoadingValidate.value = true
    try {
      // get the selected models
      const selectedModels: Model[] = selectedModelsId.value.map(id => props.configuration.models.find(m => m.id === id) as Model)
      // merge the models together
      const alignedModels = await alignModels(selectedModels)
      const mergedModel = combine(JSON.parse(JSON.stringify(alignedModels)))

      let newConfig = await updateConfiguration({
        ...props.configuration,
        version: {
          ...props.configuration.version,
          customName: newVersionName.value || props.configuration.version.customName
        },
        models: [...props.configuration.models, mergedModel]
      })

      if(newConfig.version.hash === props.configuration.version.hash) {
        // if the configuration has the same version, get the last created configuration
        newConfig = await getLastCreatedConfiguration(props.configuration.name, props.configuration.version)
      }

      emit('update:configuration', newConfig)
      emit("update:model", mergedModel)
      closeDialog()
    } catch (error:any) {
      errorMessage.value = 'An error occurred while combining the models: ' + (error.response?.data?.message || error.message)
    }
    isLoadingValidate.value = false
  }
}

const closeDialog = () => {
  selectedModelsId.value = []
  newModelName.value = ''
  newVersionName.value = ''
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
                    {{ model.title || "Untitled" }}
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
              <div class="space-y-2">
                <label for="newModelName" class="text-sm font-medium">
                  New version name (optional)
                </label>
                <Input
                    v-model="newVersionName"
                    placeholder="Enter new version name"/>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
      <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>

      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="handleMerge" :disabled="!canMerge || isLoadingValidate">
          <LoaderCircleIcon v-if="isLoadingValidate" class="w-5 h-5" />
          Combine Models
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>