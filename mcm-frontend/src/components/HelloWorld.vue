<script setup lang="ts">
import { ref } from 'vue'
import {Button} from "@/components/ui/button"

defineProps<{ msg: string }>()

const count = ref(0)
</script>

<template>
  <h1 class="scroll-m-20 text-4xl font-extrabold tracking-tight lg:text-5xl">{{ msg }}</h1>
  <Button @click="count++">Shadcn Button style</Button>
  <p>count is: {{ count }}</p>
</template>