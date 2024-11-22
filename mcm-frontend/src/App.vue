<script setup lang="ts">
import LeftPannel from "@/components/left-side/LeftPannel.vue";
import MainContent from "@/components/main-content/MainContent.vue";
import RightPannel from "@/components/right-side/RightPannel.vue";
import {ref} from "vue";

const selectedModel = ref<number | undefined>()

</script>

<template>
  <div class="flex h-screen">
    <LeftPannel v-model:selectedModel="selectedModel"/>
    <MainContent :selectedModel="selectedModel"/>
    <RightPannel/>
  </div>
</template>