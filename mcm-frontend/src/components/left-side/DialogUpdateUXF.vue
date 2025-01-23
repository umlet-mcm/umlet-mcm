<script setup lang="ts">
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from "@/components/ui/dialog";
import {Card, CardContent} from "@/components/ui/card";
import {Configuration} from "@/types/Configuration.ts";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {ref} from "vue";
import {updateConfigurationUXF} from "@/api/files.ts";
import {LoaderCircleIcon} from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import {useToast} from '@/components/ui/toast/use-toast'

/**
 * @param {Boolean} isOpen, dialog visibility
 * @param {Configuration} currentConfiguration, current configuration to edit
 */
const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  currentConfiguration: {
    type: Object as () => Configuration,
    required: true
  },
  selectedModel: {
    type: Object as () => Model,
    required: false
  },
});

/**
 * @emits {Boolean} update:isOpen, dialog visibility
 * @emits {Configuration} update:currentConfiguration, configuration to display
 * @emits {Model} update:currentModel, model to display
 */
const emit = defineEmits<{
  'update:isOpen': [value: boolean],
  'update:currentConfiguration': [value: Configuration],
  'update:currentModel': [value: Model]
}>()

//variables
const errorMessage = ref<string | undefined>(undefined)
const selectedFile = ref(undefined)
const newConfig = ref<Configuration | undefined>(undefined)
const versionName = ref("")
const isLoadingValidate = ref(false)
const { toast } = useToast()

//functions
/**
 * Close the dialog and reset all the values
 */
const closeDialog = () => {
  errorMessage.value = undefined
  selectedFile.value = undefined
  isLoadingValidate.value = false
  versionName.value = ""
  emit('update:isOpen', false)
}

/**
 * Select the file to upload and check if it is a valid UXF file
 * @param event the event triggered by the file selection
 */
const fileSelected = (event: any) => {
  const file = event.target.files[0]
  selectedFile.value = (file && file.name.endsWith('.uxf')) ? file : undefined
}

/**
 * Validate the upload and create a new configuration or model
 * Called when the user clicks on the OK button
 * Uses the uploadUxfToConfiguration or uploadUxfToModel API call
 */
const validateButton = async () => {
  isLoadingValidate.value = true
  try {
    versionName.value = versionName.value.trim()
    newConfig.value = await updateConfigurationUXF(selectedFile.value, props.currentConfiguration.name, versionName.value.length > 0 ? versionName.value : undefined)
    emit('update:currentConfiguration', newConfig.value)
    if(props.selectedModel) {
      const updatedSelectedModel = newConfig.value.models.find(m => m.id === props.selectedModel?.id)
      if(updatedSelectedModel) emit('update:currentModel', updatedSelectedModel)
    }

    toast({
      title: 'New version has been created',
      duration: 3000,
    });
    closeDialog()
  } catch (error: any) {
    errorMessage.value = "ERROR: " + (error.response?.data?.message ?? error.message)
  }
  isLoadingValidate.value = false
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog" >
    <DialogContent class="sm:max-w-[800px]">
      <DialogHeader>
        <DialogTitle>Update Configuration by UXF</DialogTitle>
        <DialogDescription>
          Update the current configuration by a UXF file
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-4 py-4">
        <!-- UXF Selection card -->
        <Card>
          <CardContent class="p-4 space-y-4">
            <div class="space-y-2">
              <label class="text-sm font-medium">UXF selection</label>
              <div class="flex w-full items-center gap-1.5">
                <Input id="file" type="file" @change="fileSelected" accept=".uxf"/>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- UXF overview and configuration -->
        <div v-if="selectedFile" class="grid grid-cols gap-4">
          <Card>
            <CardContent class="p-4 space-y-4">
              <div class="space-y-2">
                <div class="mb-4">
                  <label class="text-sm font-medium">New version name (optional)</label>
                  <Input id="versionName" type="text" placeholder="Name" v-model="versionName"/>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
      </div>
      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="validateButton" :disabled="isLoadingValidate || !selectedFile">
          <LoaderCircleIcon v-if="isLoadingValidate" class="animate-spin"/>
          Update
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>