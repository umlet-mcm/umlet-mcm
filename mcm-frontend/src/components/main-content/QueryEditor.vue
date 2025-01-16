<script setup lang="ts">
import { Codemirror } from 'vue-codemirror'
import { oneDark } from '@codemirror/theme-one-dark'
import { cypher } from '@codemirror/legacy-modes/mode/cypher'
import {StreamLanguage} from "@codemirror/language"
import { autocompletion } from '@codemirror/autocomplete'
import {cypherCompletion} from "@/components/main-content/cypherCompletion.ts";

/**
 * @param {string} query, the query string
 */
defineProps({
  query: {
    type: String,
    required: true
  }
})

/**
 * @emits {string} update:query, the query string
 */
defineEmits<{
  'update:query': [value: string]
}>()

const extensions = [StreamLanguage.define(cypher), oneDark, autocompletion({override: [cypherCompletion]})]
</script>

<template>
  <div class="border border-border rounded-md overflow-hidden">
    <Codemirror
        :extensions="extensions"
        @update:modelValue="$emit('update:query', $event)"
        placeholder="Enter your Neo4j query here..."
        :indent-with-tab="true"
        :tab-size="2"
        class="h-32"
    />
  </div>
</template>