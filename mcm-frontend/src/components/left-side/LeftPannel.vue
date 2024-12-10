<script setup lang="ts">

import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { Configuration } from '@/types/Configuration.ts'
import ModelList from "@/components/left-side/ModelList.vue"
import { FileUp, Save, FileOutput, FileStack, Settings } from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import DialogMerge from "@/components/left-side/DialogMerge.vue";
import {ref} from "vue";
import DialogSettings from "@/components/left-side/DialogSettings.vue";

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

const emit = defineEmits(["update:selectedModel"]);
const isDialogOpen = ref({merge: false, settings: false})

const handleMerge = (mergedModel: Model) => {
  props.selectedConfiguration.models.push(mergedModel)
  emit('update:selectedModel', mergedModel)
}

const placeholder = () => {
  console.log('Placeholder')
}

</script>

<template>
  <div class="w-64 border-r border-border p-4 flex flex-col gap-4 bg-primary-foreground">
    <div class="flex justify-between items-start">
      <div>
        <h1 class="text-xl font-bold">{{ selectedConfiguration.name }}</h1>
        <p class="text-sm text-muted-foreground">{{ selectedConfiguration.version }}</p>
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
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
            <FileOutput class="mr-2" />
            Export to UXF
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
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
            <FileUp class="mr-2" />
            Add Model in project
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="isDialogOpen.merge = true">
            <FileStack class="mr-2" />
            Merge Models
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
            <FileOutput class="mr-2" />
            Export Model to UXF
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
  <DialogSettings
      v-model:isOpen="isDialogOpen.settings"
      :currentConfiguration="selectedConfiguration"
  />
</template>