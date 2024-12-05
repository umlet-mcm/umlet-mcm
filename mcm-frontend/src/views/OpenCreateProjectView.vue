<script setup lang="ts">

import NewProjectForm from "@/components/open-create-configuration/NewProjectForm.vue";
import ProjectList from "@/components/open-create-configuration/ProjectList.vue";
import {onMounted, ref} from "vue";
import {Configuration} from "@/types/Configuration.ts";
import {getAllConfigurations} from "@/api/configuration.ts";

const configurations = ref<Configuration[]>([]);

const fetchConfigurations = async () => {
  try {
    configurations.value = await getAllConfigurations();
  } catch (error) {
    console.error('Error fetching configuration', error);
  }
};

onMounted(() => {
  fetchConfigurations();
});
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4">
    <h1 class="text-4xl font-semibold text-gray-800 mb-4">
      UMLet MCM
    </h1>
    <div class="bg-white rounded-lg shadow-lg w-full max-w-4xl overflow-hidden">
      <div class="flex">
        <ProjectList :configurations="configurations"/>
        <NewProjectForm/>
      </div>
    </div>
  </div>
</template>