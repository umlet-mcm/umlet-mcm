<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Network } from 'vis-network'
import {nodes_data, edges_data} from "@/datamodel/Node.ts"

const container = ref<HTMLElement>()

const selectedNode = defineModel<string>('selectedNode', { required: false })

onMounted(() => {
  if (!container.value) return

  // Sample data - replace with actual Neo4j data

  const nodes = nodes_data.map((node) => {
    return { id: node.id, label: node.name }
  })

  const edges = edges_data.map((edge) => {
    return { from: edge.from, to: edge.to, label: edge.name, arrows: 'to' }
  })

  const options = {
    nodes: {
      shape: 'square',
      size: 16,
    },
    physics: {
      enabled: true,
      solver: 'forceAtlas2Based',
    },
  }

  const network = new Network(container.value, { nodes, edges }, options)
  network.on('click', (params) => {
    if (params.nodes.length > 0) {
      const nodeId = params.nodes[0]
      const node = nodes.find(n => n.id === nodeId)
      if (node) selectedNode.value = node.id
    }})
})
</script>

<template>
  <div ref="container" class="h-[500px] border border-border rounded-md bg-background"></div>
</template>