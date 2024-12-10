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
import {exportToUxf, uploadUxfToConfiguration, uploadUxfToModel} from "@/api/files.ts";
import {useRouter} from "vue-router";
import DialogExport from "@/components/left-side/DialogExport.vue";

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
const router = useRouter()
const emit = defineEmits(["update:selectedModel", "update:selectedConfiguration"]);
const isDialogOpen = ref({merge: false, settings: false, export: false})

// functions
const handleMerge = (mergedModel: Model) => {
  props.selectedConfiguration.models.push(mergedModel)
  emit('update:selectedModel', mergedModel)
}

const placeholder = () => {
  console.log('Placeholder');
  //todo: replace all usages with functional code
};

const redirectToConfigInput = async () => {
  const inputTag = document.getElementById("inputUxfForConfiguration");
  if (inputTag) {
    inputTag.click();
  }
}

const redirectToModelInput = async () => {
  const inputTag = document.getElementById("inputUxfForModel");
  if (inputTag) {
    inputTag.click();
  }
}

// todo create a dialog for this
const uploadUxfConfig = async (event: any) => {
  try {
    const newConfig = await uploadUxfToConfiguration(event.target.files[0])
    if (confirm('Do you want to load this new configuration ?')) {
      await router.push({name: 'mainview', params: {id: newConfig.name}})
      emit('update:selectedConfiguration', newConfig)
      emit('update:selectedModel', newConfig.models[0])
    }
  } catch (e) {
    console.error(e)
  }
}

const uploadUxfModel = async (event: any, modelName: string) => {
  try {
    const newConfig = await uploadUxfToModel(event.target.files[0], modelName);
    emit('update:selectedConfiguration', newConfig)
  } catch (e) {
    console.error(e)
  }
}

const exportCurrentModel = async () => {
  if(!props.selectedModel) return;
  try {
    await exportToUxf(props.selectedModel.id, props.selectedModel.id, "model");
  } catch (e) {
    console.error(e)
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
          <Input id="inputUxfForConfiguration" type="file" @change="uploadUxfConfig" style="display: none"/>
          <Button variant="outline" class="w-full justify-start" @click="redirectToConfigInput">
            <FileInput class="mr-2" />
            Import from UXF
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
          <Input id="inputUxfForModel" type="file" @change="uploadUxfModel($event, selectedConfiguration.name)" style="display: none"/>
          <Button variant="outline" class="w-full justify-start" @click="redirectToModelInput">
            <FileUp class="mr-2" />
            Add Model in project
          </Button>
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
          @update:selectedModel="emit('update:selectedModel', $event)"/>
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
</template>