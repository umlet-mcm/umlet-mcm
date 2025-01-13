<script setup lang="ts">

import {Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {GitGraph, Settings} from "lucide-vue-next";
import {Button} from "@/components/ui/button";
import {Configuration} from "@/types/Configuration.ts";
import {ref, watch} from "vue";
import AlertConfirmation from "@/components/left-side/AlertConfirmation.vue";
import DialogSettings from "@/components/left-side/DialogSettings.vue";

// props related
const props = defineProps({
  selectedConfiguration: {
    type: Object as () => Configuration,
    required: true
  },
  versionList: {
    type: Array as () => string[],
    required: true
  }
});
const emit = defineEmits(["update:selectedConfiguration"]);

// variables
const isDialogOpen = ref({settings: false, confirmation: false})
const selectedVersion = ref<string | undefined>(props.selectedConfiguration.version)

// functions
function confirmLoadVersion() {
  isDialogOpen.value.confirmation = false
  //todo load the selected version
  // emit('update:selectedConfiguration', newConfiguration)
}

/*
  * Watchers
  * When the version change it means either the user selected a new version or a new configuration was loaded
 */
watch(() => props.selectedConfiguration.version, async (newVersion, oldVersion) => {
  if (newVersion !== oldVersion) {
    selectedVersion.value = props.selectedConfiguration.version
  }
})
</script>

<template>
  <div class="w-full">
    <div class="flex items-center gap-2 justify-between">
      <h1 class="text-xl font-bold truncate">{{ selectedConfiguration.name }}</h1>
      <Button variant="ghost" size="icon" class="p-2" @click="isDialogOpen.settings = true">
        <Settings />
      </Button>
    </div>
    <div class="flex gap-2 items-center justify-between">
      <Select v-model="selectedVersion">
        <SelectTrigger class="truncate">
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
      <Button v-if="selectedVersion !== props.selectedConfiguration.version" class="p-2" size="icon" v-tooltip="'Checkout'" @click="isDialogOpen.confirmation = true">
        <GitGraph />
      </Button>
    </div>
  </div>

  <!-- Dialog to change the configuration settings -->
  <DialogSettings
      v-model:isOpen="isDialogOpen.settings"
      :currentConfiguration="selectedConfiguration"
      @update:currentConfiguration="emit('update:selectedConfiguration', $event)"
  />
  <!-- Alert dialog to load a new configuration version -->
  <AlertConfirmation
      :on-confirm="confirmLoadVersion"
      dialog-title="Load this version?"
      dialog-description=""
      dialog-content="All of your unsaved modifications will be deleted."
      v-model:isOpen="isDialogOpen.confirmation"/>
</template>