<script setup lang="ts">

import { ref } from "vue"
import { AppConfig } from "@/config.ts";
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { Model, models_data } from '@/datamodel/Model.ts'
import ModelList from "@/components/left-side/ModelList.vue"
import { FileUp, Save, FileOutput, FileStack, Settings } from 'lucide-vue-next'

const projectName = AppConfig.projectName
const version = AppConfig.version
const models = ref<Model[]>(models_data)

const selectedModel = ref<number | undefined>()

const placeholder = () => {
  console.log('Placeholder')
}
</script>

<template>
  <div class="w-64 border-r border-border p-4 flex flex-col gap-4 bg-primary-foreground">
    <div class="flex justify-between items-start">
      <div>
        <h1 class="text-xl font-bold">{{ projectName }}</h1>
        <p class="text-sm text-muted-foreground">{{ version }}</p>
      </div>
      <Button variant="ghost" size="icon" @click="placeholder">
        <Settings />
      </Button>
    </div>
    <Separator />
    <div class="space-y-4">
      <div>
        <h2 class="text-sm font-semibold mb-2">Project Operations</h2>
        <div class="space-y-2">
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
            <FileUp class="mr-2" />
            Open new project
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
            <Save class="mr-2" />
            Save project
          </Button>
          <Button variant="outline" class="w-full justify-start" @click="placeholder">
            <FileOutput class="mr-2" />
            Export project to UXF
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
      <ModelList v-model:selected-model="selectedModel" :items="models" />
    </div>
  </div>
</template>