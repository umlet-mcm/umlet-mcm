<script setup lang="ts">

import NewProjectForm from "@/components/open-create-configuration/NewProjectForm.vue";
import ProjectList from "@/components/open-create-configuration/ProjectList.vue";
import {onMounted, ref} from "vue";
import {Configuration} from "@/types/Configuration.ts";
import {getAllConfigurations} from "@/api/configuration.ts";

const configurations = ref<Configuration[]>([]);
const errorMessage = ref<string | undefined>(undefined)

const fetchConfigurations = async () => {
  try {
    configurations.value = await getAllConfigurations();
    errorMessage.value = undefined
  } catch (error: any) {
    errorMessage.value = error.message
  }
};

onMounted(() => {
  errorMessage.value = undefined
  fetchConfigurations();
});
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4">
    <img src="/mcm.svg" alt="TU Wien Logo" class="mb-3 w-48"/>
    <h1 class="text-4xl font-semibold text-gray-800 mb-4">
      UMLet MCM
    </h1>
    <div class="bg-white rounded-lg shadow-lg w-full max-w-4xl overflow-hidden">
      <div class="flex justify-center p-2">
        <label v-if="errorMessage" class="text-sm font-medium text-red-500">Error : {{errorMessage}}</label>
      </div>
      <div class="flex">
        <ProjectList :configurations="configurations"/>
        <NewProjectForm/>
      </div>
    </div>
    <img src="/tu_logo.svg" alt="TU Wien Logo" class="mt-3 w-12"/>
  </div>
</template>