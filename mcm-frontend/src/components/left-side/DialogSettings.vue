<script setup lang="ts">
import {Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import {deleteConfiguration, updateConfiguration} from "@/api/configuration.ts";
import { Card, CardContent } from "@/components/ui/card";
import {Configuration} from "@/types/Configuration.ts";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {useRouter} from "vue-router";
import {ref} from "vue";

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
const emit = defineEmits<{
  'update:isOpen': [value: boolean]
}>()

//variables
const nameInput = ref(props.currentConfiguration.name)
const errorMessage = ref<string | undefined>(undefined)
const router = useRouter()

//functions
const closeDialog = () => {
  nameInput.value = props.currentConfiguration.name
  errorMessage.value = undefined
  emit('update:isOpen', false)
}

const deletePressed = async () => {
  if (confirm('Are you sure you want to delete this configuration?\nThis action cannot be undone.')) {
    try {
      // the name of a configuration is it's id
      await deleteConfiguration({name: props.currentConfiguration.name})
      emit('update:isOpen', false)
      // redirect to home to select a new configuration
      await router.push({name: 'home'})
    }
    catch (error : any) {
      errorMessage.value = error.message
    }
  }
}

const saveChanges = async () => {
  try {
    const newConfig = await updateConfiguration({
      name: nameInput.value,
      models: props.currentConfiguration.models
    })
    //todo may be a better idea to emit the new configuration and update it in the parent component
    props.currentConfiguration.name = newConfig.name
    props.currentConfiguration.models = newConfig.models
    emit('update:isOpen', false)
  } catch (error : any) {
    errorMessage.value = error.message
  }
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog" >
    <DialogContent class="sm:max-w-[600px]">
      <DialogHeader>
        <DialogTitle>Configuration Settings</DialogTitle>
        <DialogDescription>
          Manage the current configuration.
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-4 py-4">
        <Card>
          <CardContent class="p-4 space-y-4">
            <div class="space-y-2">
              <label class="text-sm font-medium">Configuration Name</label>
              <Input placeholder="Enter configuration name" v-model="nameInput"/>
            </div>
          </CardContent>
        </Card>
        <div class="flex flex-col items-center">
          <Button variant="destructive" @click="deletePressed">Delete Configuration</Button>
          <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
        </div>
      </div>
      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Cancel</Button>
        <Button @click="saveChanges" :disabled="!nameInput.length">Save Changes</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>