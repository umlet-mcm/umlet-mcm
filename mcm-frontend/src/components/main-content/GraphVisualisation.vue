<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Network } from 'vis-network'
import { nodes_data, edges_data } from "@/datamodel/Node.ts"
import {Node} from "@/datamodel/Node.ts";

const container = ref<HTMLElement | null>(null)

const props = defineProps({
  selectedNode: {
    type: Object as () => Node,
    required: false
  },
  selectedModelId: {
    type: String,
    required: false
  }
})

const emit = defineEmits(["update:selectedNode"]);

const selectNode = (node: Node) => {
  emit("update:selectedNode", node);
};

let network: Network | null = null;

const initializeGraph = () => {
  if (!container.value || !props.selectedModelId) return;

  const nodes = nodes_data.filter(value => value.model_id === props.selectedModelId).map((node) => ({
    id: node.id,
    label: node.name,
  }));

  const edges = edges_data.filter(value => value.model_id === props.selectedModelId).map((edge) => ({
    from: edge.from,
    to: edge.to,
    label: edge.name,
    arrows: 'to',
  }));

  const options = {
    nodes: {
      shape: 'box',
      color: {
        background: '#eaf3ff',
        border: '#007bff',
      },
      font: {
        color: '#000',
        size: 16,
        align: 'center'
      }
    },
    edges: {
      smooth: false,
      color: {
        color: '#848484',
        highlight: '#d9534f',
      },
      font: {
        size: 12,
        align: 'top',
      }
    },
    physics: {
      enabled: false,
      solver: 'barnesHut',
      barnesHut: {
        avoidOverlap: 1,
        damping: 0.09,
        springConstant: 0.04,
        springLength: 100,
      },

    },
    layout: {
      improvedLayout: true, // Algorithme d'agencement pour éviter les chevauchements
    },
  };

  if (network) {
    network.destroy();
  }

  network = new Network(container.value, { nodes, edges }, options);

  network.on('click', (params) => {
    if (params.nodes.length > 0) {
      const nodeId = params.nodes[0];
      const node = nodes.find((n) => n.id === nodeId);
      if (node) selectNode(nodes_data.find((n) => n.id === node.id) as Node);
    }
  });
};

watch(() => props.selectedModelId, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    //todo fetch nodes and edges based on selectedModelId (newValue)
    initializeGraph();
  }
});

onMounted(() => {
  initializeGraph();
});
</script>

<template>
  <div ref="container" class="h-[500px] border border-border rounded-md bg-background"></div>
</template>
