<script setup lang="ts">

import {LoaderCircleIcon, Save} from "lucide-vue-next";
import {Button} from "@/components/ui/button";
import { Configuration } from '@/types/Configuration.ts'
import {Model} from "@/types/Model.ts";
import {ref} from "vue";
import {getConfigurationById} from "@/api/configuration.ts";
import {saveNeo4JToRepository} from "@/api/graphDB.ts";
import { useToast } from '@/components/ui/toast/use-toast'

/**
 * @param {Model} selectedModel, selected model to display (if any)
 * @param {Configuration} selectedConfiguration, selected configuration to display
 */
const props = defineProps({
  selectedModel: {
    type: Object as () => Model,
    required: false
  },
  selectedConfiguration: {
    type: Object as () => Configuration,
    required: true
  }
});

/**
 * @emits {Model} update:selectedModel, selected model to display, or undefined if none
 * @emits {Configuration} update:selectedConfiguration, selected configuration to display
 */
const emit = defineEmits<{
  'update:selectedModel': [value: Model | undefined],
  'update:selectedConfiguration': [value: Configuration]
}>()

// variables
const isLoading = ref(false)
const { toast } = useToast()

// functions
const save = async () => {
  isLoading.value = true
  try {
    // save the current neo4j state to the repository to create a new version
    await saveNeo4JToRepository()
    const newConfiguration = await getConfigurationById({id: props.selectedConfiguration.name})
    emit('update:selectedConfiguration', newConfiguration)
    if(props.selectedModel) {
      const model: Model|undefined = newConfiguration.models.find(model => model.id === props.selectedModel!.id)
      if(model) {
        // if the current model is in the loaded configuration, update it
        emit('update:selectedModel', model)
      } else {
        // if the current model is not in the configuration, deselect it
        emit('update:selectedModel', undefined)
      }
    }

    toast({
      title: 'New version has been created',
      duration: 3000,
    });

  } catch (error: any) {
    console.log(error.response?.data?.message || error.message)
  }
  isLoading.value = false
}
</script>

<template>
  <Button variant="outline" class="w-full justify-start" :disabled="isLoading" @click="save">
    <Save v-if="!isLoading" class="mr-2" />
    <LoaderCircleIcon v-else class="animate-spin"/>
    Save configuration
  </Button>
</template>