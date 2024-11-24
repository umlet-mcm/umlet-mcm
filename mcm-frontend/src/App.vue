<script setup lang="ts">
import LeftPannel from "@/components/left-side/LeftPannel.vue";
import MainContent from "@/components/main-content/MainContent.vue";
import RightPannel from "@/components/right-side/RightPannel.vue";
import {ref, onMounted} from "vue";
import ProjectList from "@/components/first-window/ProjectList.vue";
import NewProjectForm from "@/components/first-window/NewProjectForm.vue";
import {Configuration} from "@/datamodel/Configuration";
import {getAllConfigurations} from "@/api/configuration.ts";
import {Model} from "@/datamodel/Model.ts";
import {Node} from "@/datamodel/Node.ts";

const selectedModel = ref<Model | undefined>()
const selectedNode = ref<Node | undefined>()
const selectedConfiguration = ref<Configuration | undefined>(undefined);
const projects = ref<Configuration[]>([]);

const getCookie = (name: string) => {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
  return match ? match[2] : null
}

const fetchConfigurations = async () => {
  try {
    projects.value = await getAllConfigurations();

    // todo don't like this, should be done in a better way
    const selectedConfigurationId = getCookie('selectedConfigurationId')
    if (selectedConfigurationId) {
      selectedConfiguration.value = projects.value.find(project => project.id === selectedConfigurationId)
    }
  } catch (error) {
    console.error('Error fetching configuration', error);
  }
};

const selectProject = (project: Configuration) => {
  document.cookie = `selectedConfigurationId=${project.id}; path=/; max-age=${60 * 60 * 24 * 7}; samesite=strict`
  selectedConfiguration.value = project
  console.log('Selected project', project)
}

onMounted(() => {
  fetchConfigurations();
});

</script>

<template>
  <div v-if="selectedConfiguration">
    <div class="flex h-screen">
      <LeftPannel v-model:selectedModel="selectedModel" :selectedConfiguration="selectedConfiguration"/>
      <MainContent :selectedModel="selectedModel" v-model:selectedNode="selectedNode"/>
      <RightPannel :selectedModel="selectedModel" :selectedNode="selectedNode"/>
    </div>
  </div>
  <div v-else>
    <div class="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <div class="bg-white rounded-lg shadow-lg w-full max-w-4xl overflow-hidden">
        <div class="flex">
          <ProjectList :projects="projects" @select="selectProject"/>
          <NewProjectForm v-model:selectedConfiguration="selectedConfiguration"/>
        </div>
      </div>
    </div>
  </div>
</template>