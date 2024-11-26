<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Network, Edge } from 'vis-network'
import {Node} from "@/datamodel/Node.ts";
import {Model} from "@/datamodel/Model.ts";

const container = ref<HTMLElement | null>(null)

const props = defineProps({
  selectedNode: {
    type: Object as () => Node,
    required: false
  },
  selectedModel: {
    type: Object as () => Model,
    required: false
  }
})

const emit = defineEmits(["update:selectedNode"]);

const selectNode = (nodeId: string) => {
  const node = props.selectedModel?.nodes.find((node) => node.id === nodeId);
  emit("update:selectedNode", node);
};

let network: Network | null = null;

const initializeGraph = () => {
  if (!container.value || !props.selectedModel) return;

  const nodes = props.selectedModel.nodes.map((node) => ({
    id: node.id,
    label: node.id,
  }));

  const edges: Edge[] = []
  props.selectedModel.nodes.forEach((node) => {
    node.relations.forEach((relation) => {
      edges.push({
        from: relation.source,
        to: relation.target,
        label: relation.text,
        arrows: 'to',
      });
    });
  })

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
      improvedLayout: true
    },
  };

  if (network) {
    network.destroy();
  }

  network = new Network(container.value, { nodes, edges }, options);

  network.on('click', (params) => {
    if (params.nodes.length > 0) {
      const nodeId = params.nodes[0];
      if (nodeId) selectNode(nodeId);
    }
  });
};

watch(() => props.selectedModel, (newValue, oldValue) => {
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
