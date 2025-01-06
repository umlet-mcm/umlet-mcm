<script setup lang="ts">

import {Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {GitGraph, Settings} from "lucide-vue-next";
import {Button} from "@/components/ui/button";
import {Configuration} from "@/types/Configuration.ts";
import {onMounted, ref} from "vue";
import {getConfigurationVersions} from "@/api/configuration.ts";
import AlertConfirmation from "@/components/left-side/AlertConfirmation.vue";
import DialogSettings from "@/components/left-side/DialogSettings.vue";

// props related
const props = defineProps({
  selectedConfiguration: {
    type: Object as () => Configuration,
    required: true
  }
});

// variables
const isDialogOpen = ref({settings: false, confirmation: false})
const versionList = ref<string[]>([])
const selectedVersion = ref<string | undefined>(undefined)

// functions
function confirmLoadVersion() {
  isDialogOpen.value.confirmation = false
}

onMounted(async () => {
  try {
    // todo the second argument will be deleted when the backend is ready
    versionList.value = await getConfigurationVersions(props.selectedConfiguration.name, props.selectedConfiguration.version)
    selectedVersion.value = props.selectedConfiguration.version
  } catch (e) {
    console.error(e)
  }
})
</script>

<template>
  <div class="flex justify-between items-start">
    <div class="flex-1 w-full">
      <div class="flex items-center gap-2 w-full">
        <h1 class="text-xl font-bold truncate">{{ selectedConfiguration.name }}</h1>
        <Button variant="ghost" size="icon" class="w-1/4" @click="isDialogOpen.settings = true">
          <Settings />
        </Button>
      </div>
      <div class="flex items-center gap-2">
        <Select v-model="selectedVersion">
          <SelectTrigger>
            <SelectValue placeholder="Select a version" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <div v-for="version in versionList.values()" :key="version">
                <SelectItem :value="version">
                  {{ version }}
                </SelectItem>
              </div>
            </SelectGroup>
          </SelectContent>
        </Select>
        <Button v-if="selectedVersion !== selectedConfiguration.version" size="icon" class="w-1/4" v-tooltip="'Checkout'" @click="isDialogOpen.confirmation = true">
          <GitGraph />
        </Button>
      </div>
    </div>
  </div>

  <!-- Dialog to change the configuration settings -->
  <DialogSettings
      v-model:isOpen="isDialogOpen.settings"
      :currentConfiguration="selectedConfiguration"
  />
  <!-- Alert dialog to load a new configuration version -->
  <AlertConfirmation
      :on-confirm="confirmLoadVersion"
      dialog-title="Load this version?"
      dialog-description=""
      dialog-content="All of your unsaved modifications will be deleted."
      v-model:isOpen="isDialogOpen.confirmation"/>
</template>