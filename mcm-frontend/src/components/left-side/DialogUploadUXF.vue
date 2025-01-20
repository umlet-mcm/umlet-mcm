<script setup lang="ts">
import {Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {Card, CardContent} from "@/components/ui/card";
import {Configuration} from "@/types/Configuration.ts";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {useRouter} from "vue-router";
import {ref} from "vue";
import {Label} from "@/components/ui/label";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import {uploadUxfToConfiguration, uploadUxfToModel} from "@/api/files.ts";
import AlertConfirmation from "@/components/left-side/AlertConfirmation.vue";
import {ScrollArea} from "@/components/ui/scroll-area";
import { LoaderCircleIcon } from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import { useToast } from '@/components/ui/toast/use-toast'

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
const router = useRouter()
const errorMessage = ref<string | undefined>(undefined)
const selectedFile = ref(undefined)
const uploadLocation = ref(undefined)
const uploadedConfig = ref<Configuration | undefined>(undefined)
const newConfig = ref<Configuration | undefined>(undefined)
const uploadedName = ref("")
const warnings = ref<string[]>([])
const isLoadingUpload = ref(false)
const isLoadingValidate = ref(false)
const isDialogOpen = ref({confirmation: false}) // confirmation dialog for deletion
const { toast } = useToast()

//functions
/**
 * Upload the selected file to the server and retrieve the warnings
 * Called when the user clicks on the upload button after selecting a file
 */
const upload = async () => {
  try {
    //todo upload uxf without database storing and retrieve the warnings
    isLoadingUpload.value = true
    uploadedConfig.value = {name: "test", version: "1.0", models: []}
    uploadedName.value = uploadedConfig.value.name
    warnings.value = []
    isLoadingUpload.value = false
  } catch (error: any) {
    errorMessage.value = "ERROR : " + (error.response?.data?.message ?? error.message)
    isLoadingUpload.value = false
  }
}

/**
 * Close the dialog and reset all the values
 */
const closeDialog = () => {
  errorMessage.value = undefined
  selectedFile.value = undefined
  uploadLocation.value = undefined
  isLoadingUpload.value = false
  isLoadingValidate.value = false
  warnings.value = []
  uploadedName.value = ""
  uploadedConfig.value = undefined
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
    if(uploadLocation.value === "configuration") {
      // create a new configuration and open the alert dialog
      newConfig.value = await uploadUxfToConfiguration(selectedFile.value)
      isDialogOpen.value.confirmation = true
    } else {
      // upload uxf to current configuration
      newConfig.value = await uploadUxfToModel(selectedFile.value, props.currentConfiguration.name)
      // find the first model that is not in the current configuration (newly created model)
      const newModel = newConfig.value.models.find(m => !props.currentConfiguration.models.map(m => m.id).includes(m.id))
      emit('update:currentConfiguration', newConfig.value)
      if(newModel) emit('update:currentModel', newModel)

      toast({
        title: 'New version has been created',
        duration: 3000,
      });
    }
    closeDialog()
  } catch (error: any) {
    errorMessage.value = "ERROR : " + (error.response?.data?.message ?? error.message)
    isLoadingValidate.value = false
  }
}

/**
 * Load the newly created configuration
 * Called when the user confirms the alert dialog
 */
const loadNewConfiguration = () => {
  router.push({name: 'mainview', params: {id: newConfig.value!.name}})
  emit('update:currentConfiguration', newConfig.value!)
  emit('update:currentModel', newConfig.value!.models[0])
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog" >
    <DialogContent class="sm:max-w-[800px]">
      <DialogHeader>
        <DialogTitle>Upload a new UXF</DialogTitle>
        <DialogDescription>
          Upload a UXF file as a new model configuration or a model within the current one.
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
                <Button :disabled="!selectedFile" @click="upload">
                  <LoaderCircleIcon v-if="isLoadingUpload" class="animate-spin"/>
                  Upload
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- UXF overview and configuration -->
        <div v-if="uploadedConfig" class="grid grid-cols-2 gap-4">
          <Card>
            <CardContent class="p-4 space-y-4">
              <div class="space-y-4">
                <label class="text-sm font-medium">UXF overview</label>
                <div class="flex flex-col w-full space-y-2">
                  <div class="flex w-full items-center gap-1.5">
                    <Label class="text-sm font-medium">Name</Label>
                    <Input id="uploadedName" type="text" placeholder="Name" v-model="uploadedName"/>
                  </div>
                  <div class="flex flex-col">
                    <Label class="text-sm font-medium">Number of nodes : {{ uploadedConfig.models.flatMap(m => m.nodes).length }}</Label>
                  </div>
                  <div class="flex flex-col">
                    <Label class="text-sm font-medium">Number of relations : {{ uploadedConfig.models.flatMap(m => m.nodes).flatMap(n => n.relations).length }}</Label>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent class="p-4 space-y-4">
              <div class="space-y-2">
                <label class="text-sm font-medium">Upload as?</label>
                  <RadioGroup v-model="uploadLocation">
                    <div class="flex items-center space-x-2">
                      <RadioGroupItem id="configuration" value="configuration" />
                      <label for="configuration" class="text-sm">New model configuration</label>
                    </div>
                    <div class="flex items-center space-x-2">
                      <RadioGroupItem id="model" value="model" />
                      <label for="model" class="text-sm">New model within current configuration</label>
                    </div>
                  </RadioGroup>
                </div>
            </CardContent>
          </Card>
        </div>

        <!-- Warnings -->
        <div v-if="warnings.length" class="grid gap-4">
          <Card class="bg-orange-50">
            <CardContent class="p-4 space-y-4">
              <div class="space-y-2">
                <label class="text-sm font-medium">Warnings</label>
                <ScrollArea class="h-[50px]">
                  <div class="flex flex-col gap-2 text-orange-500">
                    <div v-for="warning of warnings" :key="warning">
                      <p class="text-sm">{{warning}}</p>
                    </div>
                  </div>
                </ScrollArea>
              </div>
            </CardContent>
          </Card>
        </div>
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
      </div>
      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="validateButton" :disabled="!uploadedConfig || !uploadLocation || uploadedName.trim().length === 0">
          <LoaderCircleIcon v-if="isLoadingValidate" class="animate-spin"/>
          OK
        </Button>
      </DialogFooter>
    </DialogContent>

    <!-- Alert dialog to load a configuration -->
    <AlertConfirmation
        :on-confirm="loadNewConfiguration"
        :on-reject="closeDialog"
        dialog-title="Load this configuration?"
        dialog-description=""
        dialog-content="Do you want to load this newly created configuration? All unsaved modifications will be erased."
        v-model:isOpen="isDialogOpen.confirmation"/>
  </Dialog>
</template>