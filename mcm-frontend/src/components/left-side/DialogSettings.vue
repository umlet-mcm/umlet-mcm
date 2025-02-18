<script setup lang="ts">
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from "@/components/ui/dialog";
import {deleteConfiguration, renameConfiguration} from "@/api/configuration.ts";
import {Card, CardContent} from "@/components/ui/card";
import {Configuration} from "@/types/Configuration.ts";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {useRouter} from "vue-router";
import {ref} from "vue";
import AlertConfirmation from "@/components/left-side/AlertConfirmation.vue";
import {LoaderCircleIcon} from 'lucide-vue-next'
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
});

/**
 * @emits {Boolean} update:isOpen, dialog visibility
 */
const emit = defineEmits<{
  'update:isOpen': [value: boolean],
  'update:currentConfiguration': [value: Configuration]
}>()

//variables
const nameInput = ref(props.currentConfiguration.name)
const errorMessage = ref<string | undefined>(undefined)
const router = useRouter()
const isLoadingValidate = ref(false)
const isDialogOpen = ref({confirmation: false}) // confirmation dialog for deletion
const { toast } = useToast()

//functions
const closeDialog = () => {
  nameInput.value = props.currentConfiguration.name
  isDialogOpen.value.confirmation = false
  errorMessage.value = undefined
  emit('update:isOpen', false)
}

/**
 * Confirm the deletion of the current configuration
 * Called when the user confirms the deletion in the confirmation dialog
 * Uses the deleteConfiguration API call
 */
const confirmDeletion = async () => {
  try {
    // the name of a configuration is it's id
    await deleteConfiguration({name: props.currentConfiguration.name})
    emit('update:isOpen', false)
    // redirect to home to select a new configuration
    await router.push({name: 'home'})

    toast({
      title: 'Configuration has been deleted',
      duration: 3000,
    });
  }
  catch (error : any) {
    errorMessage.value = error.response?.data?.message || error.message
  }
}

/**
 * Save the changes made to the current configuration
 * Called when the user clicks the save button
 * Uses the renameConfiguration API call
 */
const saveChanges = async () => {
  isLoadingValidate.value = true
  try {
    const newConfig = await renameConfiguration(props.currentConfiguration.name, nameInput.value)
    await router.push({name: 'mainview', params: {id: newConfig.name}})
    emit('update:currentConfiguration', newConfig)
    emit('update:isOpen', false)

  } catch (error : any) {
    errorMessage.value = error.response?.data?.message || error.message
  }
  isLoadingValidate.value = false
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog" >
    <DialogContent class="sm:max-w-[600px]">
      <DialogHeader>
        <DialogTitle>Model configuration Settings</DialogTitle>
        <DialogDescription>
          Manage the current model configuration.
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-4 py-4">
        <Card>
          <CardContent class="p-4 space-y-4">
            <div class="space-y-2">
              <label class="text-sm font-medium">Model Configuration Name</label>
              <Input placeholder="Enter name" v-model="nameInput"/>
            </div>
          </CardContent>
        </Card>
        <div class="flex flex-col items-center">
          <Button variant="destructive" @click="isDialogOpen.confirmation = true">Delete Model Configuration</Button>
          <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
        </div>
      </div>
      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="saveChanges" :disabled="!nameInput.length || isLoadingValidate || nameInput === currentConfiguration.name">
          <LoaderCircleIcon v-if="isLoadingValidate" class="animate-spin"/>
          Save Changes
        </Button>
      </DialogFooter>
    </DialogContent>

    <!-- Alert dialog to delete a configuration -->
    <AlertConfirmation
        :on-confirm="confirmDeletion"
        dialog-title="Delete this model configuration?"
        dialog-description=""
        dialog-content="Do you want to delete this model configuration? This action cannot be undone."
        v-model:isOpen="isDialogOpen.confirmation"/>
  </Dialog>
</template>