<script setup lang="ts">
import LeftPannel from "@/components/left-side/LeftPannel.vue";
import MainContent from "@/components/main-content/MainContent.vue";
import RightPannel from "@/components/right-side/RightPannel.vue";

</script>

<template>
  <div class="flex h-screen">
    <LeftPannel/>
    <MainContent/>
    <RightPannel/>
  </div>
</template>