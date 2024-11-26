<script setup lang="ts">

import LeftPannel from "@/components/left-side/LeftPannel.vue";
import RightPannel from "@/components/right-side/RightPannel.vue";
import MainContent from "@/components/main-content/MainContent.vue";
import { useRoute } from 'vue-router'
import {Model} from "@/datamodel/Model.ts";
import {Node} from "@/datamodel/Node.ts";
import {onMounted, ref} from "vue";
import {Configuration} from "@/datamodel/Configuration.ts";
import {getConfigurationById} from "@/api/configuration.ts";
const route = useRoute()

const selectedConfiguration = ref<Configuration>();
const selectedModel = ref<Model | undefined>()
const selectedNode = ref<Node | undefined>()

const getSelectedConfiguration = async () => {
  try {
    selectedConfiguration.value = await getConfigurationById({id: route.params.id as string});
  } catch (error) {
    console.error("Error fetching configuration with id " + route.params.id, error);
  }
};

onMounted(() => {
  getSelectedConfiguration();
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
</template>