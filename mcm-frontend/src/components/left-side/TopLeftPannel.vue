<script setup lang="ts">

import {Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Settings} from "lucide-vue-next";
import {Button} from "@/components/ui/button";
import {Configuration, Version} from "@/types/Configuration.ts";
import {Model} from "@/types/Model.ts";
import {ref, watch} from "vue";
import AlertConfirmation from "@/components/left-side/AlertConfirmation.vue";
import DialogSettings from "@/components/left-side/DialogSettings.vue";
import {checkoutConfiguration} from "@/api/configuration.ts";
import { useToast } from '@/components/ui/toast/use-toast'

// props related
const props = defineProps({
  selectedConfiguration: {
    type: Object as () => Configuration,
    required: true
  },
  selectedModel: {
    type: Object as () => Model,
    required: false
  },
  versionList: {
    type: Array as () => Version[],
    required: true
  }
});

/**
 * @emits {Model} update:selectedModel, selected model
 * @emits {Configuration} update:selectedConfiguration, selected configuration
 */
const emit = defineEmits<{
  'update:selectedModel': [value: Model | undefined],
  'update:selectedConfiguration': [value: Configuration]
}>()

// variables
const isDialogOpen = ref({settings: false, confirmation: false})
const selectedVersion = ref<string | undefined>(props.selectedConfiguration.version.hash)
const { toast } = useToast()

// functions
async function confirmLoadVersion() {
  if(!selectedVersion.value) return
  try{
    const newConfiguration = await checkoutConfiguration(props.selectedConfiguration.name, selectedVersion.value)
    isDialogOpen.value.confirmation = false
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
      title: 'Successfully loaded the new version',
      duration: 3000,
    });
  } catch(error: any) {
    console.log(error.response?.data?.message || error.message)
  }
}

const rejectLoadVersion = () => {
  selectedVersion.value = props.selectedConfiguration.version.hash
}

/*
  * Watchers
  * When the version change it means either the user selected a new version or a new configuration was loaded
 */
watch(() => props.selectedConfiguration.version, async (newVersion, oldVersion) => {
  if (newVersion !== oldVersion) {
    selectedVersion.value = props.selectedConfiguration.version.hash
  }
})

/**
 * When the selected version changes, open the confirmation dialog
 * the versions are in hash form
 */
watch(() => selectedVersion.value, (newVersion: any, oldVersion: any) => {
  if (newVersion !== oldVersion && newVersion !== props.selectedConfiguration.version.hash) {
    isDialogOpen.value.confirmation = true
  }
})
</script>

<template>
  <div class="w-full">
    <div class="flex items-center gap-2 justify-between">
      <h1 class="text-xl font-bold truncate">{{ selectedConfiguration.name }}</h1>
      <Button variant="ghost" size="icon" class="p-2" @click="isDialogOpen.settings = true">
        <Settings />
      </Button>
    </div>
    <div class="flex gap-2 items-center justify-between">
      <Select v-model="selectedVersion">
        <SelectTrigger class="truncate">
          <SelectValue placeholder="Select a version" />
        </SelectTrigger>
        <SelectContent>
          <SelectGroup>
            <div v-for="version in versionList.values()" :key="version.hash">
              <SelectItem :value="version.hash">
                {{ version.customName ?? version.name }}
              </SelectItem>
            </div>
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  </div>

  <!-- Dialog to change the configuration settings -->
  <DialogSettings
      v-model:isOpen="isDialogOpen.settings"
      :currentConfiguration="selectedConfiguration"
      @update:currentConfiguration="emit('update:selectedConfiguration', $event)"
  />
  <!-- Alert dialog to load a new configuration version -->
  <AlertConfirmation
      :on-confirm="confirmLoadVersion"
      :on-reject="rejectLoadVersion"
      dialog-title="Load this version?"
      dialog-description=""
      dialog-content="All of your unsaved modifications will be deleted."
      v-model:isOpen="isDialogOpen.confirmation"/>
</template>