<script setup lang="ts">
import { Codemirror } from 'vue-codemirror'
import { oneDark } from '@codemirror/theme-one-dark'
import { cypher } from '@codemirror/legacy-modes/mode/cypher'
import {StreamLanguage} from "@codemirror/language"
import { autocompletion } from '@codemirror/autocomplete'
import {cypherCompletion} from "@/components/main-content/cypherCompletion.ts";

defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const extensions = [StreamLanguage.define(cypher), oneDark, autocompletion({override: [cypherCompletion]})]

const onChange = (value: string) => {
  emit('update:modelValue', value)
}
</script>

<template>
  <div class="border border-border rounded-md overflow-hidden">
    <Codemirror
        :extensions="extensions"
        @update:modelValue="onChange"
        placeholder="Enter your Neo4j query here..."
        :indent-with-tab="true"
        :tab-size="2"
        class="h-32"
    />
  </div>
</template>