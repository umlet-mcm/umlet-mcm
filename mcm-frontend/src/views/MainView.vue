<script setup lang="ts">

import LeftPannel from "@/components/left-side/LeftPannel.vue";
import RightPannel from "@/components/right-side/RightPannel.vue";
import MainContent from "@/components/main-content/MainContent.vue";
import { useRoute } from 'vue-router'
import {Model} from "@/types/Model.ts";
import {Node, Relation} from "@/types/Node.ts";
import {onMounted, ref} from "vue";
import {Configuration} from "@/types/Configuration.ts";
import {getConfigurationById} from "@/api/configuration.ts";

// variables
const route = useRoute()
const selectedConfiguration = ref<Configuration>();
const selectedModel = ref<Model | undefined>()
const selectedEntity = ref<Node | Relation | undefined>()
const queryResponse = ref<Record<string, any>[]>()

// functions
/**
 * Fetch the selected configuration
 * Uses the getConfigurationById function from the configuration API
 */
const getSelectedConfiguration = async () => {
  try {
    selectedConfiguration.value = await getConfigurationById({id: route.params.id as string});
    selectedModel.value = selectedConfiguration.value.models[0] || undefined;
  } catch (error) {
    console.error("Error fetching model configuration with id " + route.params.id, error);
  }
};

// lifecycle
/**
 * Fetch the selected configuration on mounted
 */
onMounted(() => {
  getSelectedConfiguration();
});
</script>

<template>
  <div v-if="selectedConfiguration">
    <div class="flex h-screen">
      <LeftPannel
          v-model:selectedModel="selectedModel"
          v-model:selectedConfiguration="selectedConfiguration"
      />
      <MainContent
          :selectedModel="selectedModel"
          @update:selectedEntity="selectedEntity = $event"
          @update:response="queryResponse = $event"
      />
      <RightPannel
          :selectedEntity="selectedEntity"
          :queryResponse="queryResponse"
      />
    </div>
  </div>
</template>