<script setup lang="ts">

import { AppConfig } from "@/config.ts";
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { Configuration } from '@/types/Configuration.ts'
import ModelList from "@/components/left-side/ModelList.vue"
import { FileUp, Save, FileOutput, FileStack, Settings } from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";

const version = AppConfig.version

defineProps({
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

const placeholder = () => {
  console.log('Placeholder')
}

</script>

<template>
  <div class="w-64 border-r border-border p-4 flex flex-col gap-4 bg-primary-foreground">
    <div class="flex justify-between items-start">
      <div>
        <h1 class="text-xl font-bold">{{ selectedConfiguration.name }}</h1>
        <p class="text-sm text-muted-foreground">{{ version }}</p>
      </div>
      <Button variant="ghost" size="icon" @click="placeholder">
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
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
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
    <div>
      <h2 class="text-sm font-semibold mb-2">Models within configuration</h2>
      <ModelList
          :selected-model="selectedModel"
          :items="selectedConfiguration.models"
          @update:selectedModel="emit('update:selectedModel', $event)"/>
    </div>
  </div>
</template>