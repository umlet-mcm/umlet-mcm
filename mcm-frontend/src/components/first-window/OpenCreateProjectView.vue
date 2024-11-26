<script setup lang="ts">

import NewProjectForm from "@/components/first-window/NewProjectForm.vue";
import ProjectList from "@/components/first-window/ProjectList.vue";
import {onMounted, ref} from "vue";
import {Configuration} from "@/datamodel/Configuration.ts";
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
  <div class="min-h-screen bg-gray-100 flex items-center justify-center p-4">
    <div class="bg-white rounded-lg shadow-lg w-full max-w-4xl overflow-hidden">
      <div class="flex">
        <ProjectList :configurations="configurations"/>
        <NewProjectForm/>
      </div>
    </div>
  </div>
</template>