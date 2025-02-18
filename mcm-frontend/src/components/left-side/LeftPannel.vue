<script setup lang="ts">

import {Button} from '@/components/ui/button'
import {Separator} from '@/components/ui/separator'
import {Configuration, Version} from '@/types/Configuration.ts'
import ModelList from "@/components/left-side/ModelList.vue"
import {Diff, FileInput, FileOutput, FilePenLine, FileStack, FileUp, HelpCircle} from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import DialogMerge from "@/components/left-side/DialogMerge.vue";
import {onMounted, ref, watch} from "vue";
import {exportToUxf} from "@/api/files.ts";
import DialogExport from "@/components/left-side/DialogExport.vue";
import DialogUploadUXF from "@/components/left-side/DialogUploadUXF.vue";
import TopLeftPannel from "@/components/left-side/TopLeftPannel.vue";
import DialogVersionDiff from "@/components/left-side/DialogVersionDiff.vue";
import {getLastCreatedConfiguration, listConfigurationVersions} from "@/api/configuration.ts";
import AlertConfirmation from "@/components/left-side/AlertConfirmation.vue";
import {deleteModelFromConfig} from "@/api/model.ts";
import SaveButton from "@/components/left-side/SaveButton.vue";
import {useToast} from '@/components/ui/toast/use-toast'
import DialogUpdateUXF from "@/components/left-side/DialogUpdateUXF.vue";

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
const isDialogOpen = ref({merge: false, export: false, upload: false, confirmation: false, versionDiff:false, update:false})
const errorDelete = ref<string | undefined>(undefined)
const versionList = ref<Version[]>([])
const { toast } = useToast()

// functions
const placeholder = () => {
  console.log('Placeholder');
  //todo: replace all usages with functional code
};

/**
 * Export the current model to UXF
 * Called when the user clicks on the export button
 * Uses the exportToUxf function from the api
 */
const exportCurrentModel = async () => {
  if(!props.selectedModel) return;
  try {
    await exportToUxf(props.selectedConfiguration.name, props.selectedModel.id, "model");
  } catch (e) {
    console.error(e)
  }
}

/**
 * Confirm the deletion of the selected model
 * Called when the user confirms the deletion in the confirmation dialog
 * Uses the deleteModelFromConfig function from the api
 */
const confirmDeletion = async () => {
  const index = props.selectedConfiguration.models.findIndex(model => model.id === props.selectedModel!.id)
  if (index !== -1) {
    try {
      let updatedConfig = await deleteModelFromConfig(props.selectedModel!.id)

      if(updatedConfig.version.hash === props.selectedConfiguration.version.hash) {
        // if the configuration has the same version, get the last created configuration
        updatedConfig = await getLastCreatedConfiguration(props.selectedConfiguration.name, props.selectedConfiguration.version)
      }

      emit('update:selectedConfiguration', updatedConfig)
      emit('update:selectedModel', undefined)
      errorDelete.value = undefined
      toast({
        title: 'New version has been created',
        duration: 3000,
      });
    } catch (error: any) {
      errorDelete.value = error.response?.data?.message || error.message
    }
  }
}

/*
  * Watchers
  * When the version changes it means either the user selected a new version or a new configuration was loaded
 */
watch(() => props.selectedConfiguration.version.hash, async (newVersion, oldVersion) => {
  if (newVersion !== oldVersion) {
    try {
      versionList.value = await listConfigurationVersions(props.selectedConfiguration.name)
    } catch (e) {
      versionList.value = [props.selectedConfiguration.version]
      console.error(e)
    }
  }
})

/*
  * Lifecycle
  * When the component is mounted, we fetch the list of versions for the selected configuration
 */
onMounted(async () => {
  try {
    versionList.value = await listConfigurationVersions(props.selectedConfiguration.name)
  } catch (e) {
    versionList.value = [props.selectedConfiguration.version]
    console.error(e)
  }
})
</script>

<template>
  <div class="w-64 border-r border-border p-4 flex flex-col gap-4 bg-primary-foreground">
    <TopLeftPannel
        :selectedConfiguration="selectedConfiguration"
        :selected-model="selectedModel"
        @update:selectedConfiguration="emit('update:selectedConfiguration', $event)"
        @update:selectedModel="emit('update:selectedModel', $event)"
        :versionList="versionList"
    />
    <Separator />
    <div class="space-y-2">
      <div>
        <div class="flex justify-between items-center ">
          <h2 class="text-sm font-semibold">Configuration Operations</h2>
          <Button variant="ghost" size="icon" @click="placeholder">
            <HelpCircle />
          </Button>
        </div>
        <div class="space-y-2">
          <Button variant="outline" class="w-full justify-start" @click="$router.push({name:'home'})">
            <FileUp class="mr-2" />
            Open configuration
          </Button>
          <SaveButton
              :selectedConfiguration="selectedConfiguration"
              @update:selectedConfiguration="emit('update:selectedConfiguration', $event)"
              :selected-model="selectedModel"
              @update:selectedModel="emit('update:selectedModel', $event)"
          />
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.update = true">
            <FilePenLine class="mr-2" />
            Update from UXF file
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.upload = true">
            <FileInput class="mr-2" />
            Import UXF file
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.export = true">
            <FileOutput class="mr-2" />
            Export to UXF / CSV
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.versionDiff = true">
            <Diff class="mr-2" />
            Version diff
          </Button>
        </div>
      </div>
      <div>
        <div class="flex justify-between items-center ">
          <h2 class="text-sm font-semibold">Model Operations</h2>
          <Button variant="ghost" size="icon" @click="placeholder">
            <HelpCircle />
          </Button>
        </div>
        <div class="space-y-2">
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.merge = true">
            <FileStack class="mr-2" />
            Combine Models
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
      :configuration="selectedConfiguration"
      @update:configuration="emit('update:selectedConfiguration', $event)"
      @update:model="emit('update:selectedModel', $event)"
  />
  <DialogExport
      v-model:isOpen="isDialogOpen.export"
      :configuration-name="selectedConfiguration.name"
  />
  <DialogUploadUXF
      v-model:isOpen="isDialogOpen.upload"
      :currentConfiguration="selectedConfiguration"
      @update:currentConfiguration="emit('update:selectedConfiguration', $event)"
      @update:currentModel="emit('update:selectedModel', $event)"
  />
  <DialogUpdateUXF
      v-model:isOpen="isDialogOpen.update"
      :currentConfiguration="selectedConfiguration"
      :selected-model="selectedModel"
      @update:currentConfiguration="emit('update:selectedConfiguration', $event)"
      @update:currentModel="emit('update:selectedModel', $event)"
  />
  <DialogVersionDiff
      v-model:isOpen="isDialogOpen.versionDiff"
      :currentConfiguration="selectedConfiguration"
      :versionList="versionList"
  />
  <!-- Alert dialog to delete a model from configuration -->
  <AlertConfirmation
      :on-confirm="confirmDeletion"
      :on-reject="() => {errorDelete = undefined}"
      dialog-title="Delete this model?"
      dialog-description=""
      dialog-content="Do you want to delete this model? This action cannot be undone."
      :error-content="errorDelete"
      v-model:isOpen="isDialogOpen.confirmation"/>
</template>