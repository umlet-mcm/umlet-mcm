<script setup lang="ts">

import NewProjectForm from "@/components/open-create-configuration/NewProjectForm.vue";
import ProjectList from "@/components/open-create-configuration/ProjectList.vue";
import {onMounted, ref} from "vue";
import {Configuration} from "@/types/Configuration.ts";
import {getAllConfigurations} from "@/api/configuration.ts";
import {HelpCircle} from 'lucide-vue-next'
import {Button} from "@/components/ui/button";

// variables
const configurations = ref<Configuration[]>([]);
const errorMessage = ref<string | undefined>(undefined)

// functions
/**
 * Fetch all configurations
 * Uses the getAllConfigurations function from the configuration API
 */
const fetchConfigurations = async () => {
  try {
    configurations.value = await getAllConfigurations();
    errorMessage.value = undefined
  } catch (error: any) {
    errorMessage.value = "Unable to fetch configurations: " + error.message
  }
};

// lifecycle
/**
 * Fetch all configurations on mounted
 */
onMounted(() => {
  errorMessage.value = undefined
  fetchConfigurations();
});
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4">
    <img src="/mcm.svg" alt="TU Wien Logo" class="mb-3 w-48"/>
    <h1 class="text-4xl font-semibold text-gray-800 mb-4">
      UMLet Model Change Management
    </h1>
    <div class="bg-white rounded-lg shadow-lg w-full max-w-4xl overflow-hidden">
      <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
        <label v-else class="text-sm font-medium text-green-500">Database connection OK</label>
      </div>
      <div class="flex">
        <ProjectList :configurations="configurations"/>
        <NewProjectForm/>
      </div>
    </div>
    <div class="flex items-center mt-3">
      <img src="/tu_logo.svg" alt="TU Wien Logo" class="w-12 m-2"/>
      <Button @click="$router.push({ name: 'help'})" class="w-full flex items-center gap-2" variant="outline">
        <HelpCircle/>
        How to use
      </Button>
    </div>
  </div>
</template>