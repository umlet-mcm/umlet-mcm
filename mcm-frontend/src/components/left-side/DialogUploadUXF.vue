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
import {useRouter} from "vue-router";
import {ref} from "vue";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import {uploadUxfToConfiguration, uploadUxfToModel} from "@/api/files.ts";
import AlertConfirmation from "@/components/left-side/AlertConfirmation.vue";
import {LoaderCircleIcon} from 'lucide-vue-next'

//props related
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
const emit = defineEmits(['update:isOpen', 'update:currentConfiguration', 'update:currentModel']);

//variables
const router = useRouter()

const errorMessage = ref<string | undefined>(undefined)
const selectedFile = ref(undefined)
const uploadLocation = ref("model")
const newConfig = ref<Configuration | undefined>(undefined)
const uploadedName = ref("")
const isLoadingValidate = ref(false)
// confirmation dialog
const isDialogOpen = ref({confirmation: false})

//functions
const closeDialog = () => {
  errorMessage.value = undefined
  selectedFile.value = undefined
  isLoadingValidate.value = false
  uploadLocation.value = "model"
  uploadedName.value = ""
  emit('update:isOpen', false)
}

const fileSelected = (event: any) => {
  const file = event.target.files[0]
  selectedFile.value = (file && file.name.endsWith('.uxf')) ? file : undefined
}

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
      emit('update:currentModel', newModel)
    }
    closeDialog()
  } catch (error: any) {
    errorMessage.value = "ERROR : " + (error.response?.data?.message ?? error.message)
  }
  isLoadingValidate.value = false
}

const loadNewConfiguration = () => {
  // called when the user confirms the alert dialog
  if(!newConfig.value) {
    errorMessage.value = "ERROR : No configuration to load"
    return
  }
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
                  <label class="text-sm font-medium">Model name</label>
                  <Input id="uploadedName" type="text" placeholder="Name" v-model="uploadedName"/>
                </div>
                <label class="text-sm font-medium">Upload as?</label>
                  <RadioGroup v-model="uploadLocation">
                    <div class="flex items-center space-x-2">
                      <RadioGroupItem id="model" value="model" />
                      <label for="model" class="text-sm">New model within current configuration</label>
                    </div>
                    <div class="flex items-center space-x-2">
                      <RadioGroupItem id="configuration" value="configuration" />
                      <label for="configuration" class="text-sm">New model configuration</label>
                    </div>
                  </RadioGroup>
                </div>
            </CardContent>
          </Card>
        </div>
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
      </div>
      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="validateButton" :disabled="!uploadLocation || uploadedName.trim().length === 0 || isLoadingValidate">
          <LoaderCircleIcon v-if="isLoadingValidate" class="animate-spin"/>
          Upload
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