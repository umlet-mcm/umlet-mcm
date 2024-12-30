<script setup lang="ts">

import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { Configuration } from '@/types/Configuration.ts'
import ModelList from "@/components/left-side/ModelList.vue"
import { FileUp, Save, FileOutput, FileInput, FileStack, Settings } from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import DialogMerge from "@/components/left-side/DialogMerge.vue";
import {ref} from "vue";
import DialogSettings from "@/components/left-side/DialogSettings.vue";
import {exportToUxf} from "@/api/files.ts";
import DialogExport from "@/components/left-side/DialogExport.vue";
import DialogUploadUXF from "@/components/left-side/DialogUploadUXF.vue";
import AlertConfirmation from "@/components/left-side/AlertConfirmation.vue";
import {deleteModelFromConfig} from "@/api/model.ts";

// props related
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

// variables
const emit = defineEmits(["update:selectedModel", "update:selectedConfiguration"]);
const isDialogOpen = ref({merge: false, settings: false, export: false, upload: false, confirmation: false})

// functions
const handleMerge = (mergedModel: Model) => {
  props.selectedConfiguration.models.push(mergedModel)
  emit('update:selectedModel', mergedModel)
}

const placeholder = () => {
  console.log('Placeholder');
  //todo: replace all usages with functional code
};

const exportCurrentModel = async () => {
  if(!props.selectedModel) return;
  try {
    await exportToUxf(props.selectedModel.id, props.selectedModel.id, "model");
  } catch (e) {
    console.error(e)
  }
}

const confirmDeletion = async () => {
  const index = props.selectedConfiguration.models.findIndex(model => model.id === props.selectedModel!.id)
  if (index !== -1) {
    try {
      await deleteModelFromConfig(props.selectedModel!.id, props.selectedConfiguration.name)
      props.selectedConfiguration.models.splice(index, 1)
      emit('update:selectedModel', undefined)
      isDialogOpen.value.confirmation = false
    } catch (error: any) {
      console.error(error)
      isDialogOpen.value.confirmation = false
    }
  }
}

</script>

<template>
  <div class="w-64 border-r border-border p-4 flex flex-col gap-4 bg-primary-foreground">
    <div class="flex justify-between items-start">
      <div class="max-w-[80%]">
        <h1 class="text-xl font-bold truncate ">{{ selectedConfiguration.name }}</h1>
        <p class="text-sm text-muted-foreground truncate">{{ selectedConfiguration.version }}</p>
      </div>
      <Button variant="ghost" size="icon" @click="isDialogOpen.settings = true">
        <Settings />
      </Button>
    </div>
    <Separator />
    <div class="space-y-4">
      <div>
        <h2 class="text-sm font-semibold mb-2">Configuration Operations</h2>
        <div class="space-y-2">
          <Button variant="outline" class="w-full justify-start" @click="$router.push({name:'home'})">
            <FileUp class="mr-2" />
            Open new configuration
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
            <Save class="mr-2" />
            Save configuration
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.upload = true">
            <FileInput class="mr-2" />
            Import UXF file
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.export = true">
            <FileOutput class="mr-2" />
            Export to UXF / CSV
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
            <FileOutput class="mr-2" />
            Version history
          </Button>
        </div>
      </div>
      <div>
        <h2 class="text-sm font-semibold mb-2">Model Operations</h2>
        <div class="space-y-2">
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.merge = true">
            <FileStack class="mr-2" />
            Merge Models
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="exportCurrentModel()">
            <FileOutput class="mr-2" />
            Export to UXF
          </Button>
        </div>
      </div>
    </div>
    <Separator />
    <div class="flex-1 overflow-auto">
      <h2 class="text-sm font-semibold mb-2">Model to display</h2>
      <ModelList
          :selected-model="selectedModel"
          :items="selectedConfiguration.models"
          @update:selectedModel="emit('update:selectedModel', $event)"
          @deleteModel="isDialogOpen.confirmation = true"
      />
    </div>
  </div>

  <!-- dialogs -->
  <DialogMerge
      v-model:isOpen="isDialogOpen.merge"
      :models="selectedConfiguration.models"
      @merge="handleMerge"
  />
  <DialogExport
      v-model:isOpen="isDialogOpen.export"
      :configuration-name="selectedConfiguration.name"
  />
  <DialogSettings
      v-model:isOpen="isDialogOpen.settings"
      :currentConfiguration="selectedConfiguration"
  />
  <DialogUploadUXF
      v-model:isOpen="isDialogOpen.upload"
      :currentConfiguration="selectedConfiguration"
      @update:currentConfiguration="emit('update:selectedConfiguration', $event)"
      @update:currentModel="emit('update:selectedModel', $event)"
  />
  <!-- Alert dialog to delete a model from configuration -->
  <AlertConfirmation
      :on-confirm="confirmDeletion"
      dialog-title="Delete this model?"
      dialog-description=""
      dialog-content="Do you want to delete this model? This action cannot be undone."
      v-model:isOpen="isDialogOpen.confirmation"/>
</template>