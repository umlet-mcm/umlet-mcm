<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Network, Edge } from 'vis-network'
import {Model} from "@/types/Model.ts";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs";

// variables
const container = ref<HTMLElement | null>(null)
const active = ref('full')
let network: Network | null = null;

// props related
const emit = defineEmits(["update:selectedNode"]);
const props = defineProps({
  selectedModel: {
    type: Object as () => Model,
    required: true
  }
})

// functions
const generatePaleColorFromText = (text: string) => {
  const hash = [...text].reduce((acc, char) => acc + char.charCodeAt(0), 0);
  const red = (hash * 137) % 128 + 127;
  const green = (hash * 233) % 128 + 127;
  const blue = (hash * 97) % 128 + 127;
  return `rgb(${red}, ${green}, ${blue})`;
}

const selectNode = (nodeId: string) => {
  const node = props.selectedModel.nodes.find((node) => node.id === nodeId);
  emit("update:selectedNode", node);
};

const initializeGraph = () => {
  if (!container.value) return;

  const nodes = props.selectedModel.nodes.map((node) => ({
    id: node.id,
    label: node.title.replace("\n"," ").trim(),
    color: generatePaleColorFromText(node.elementType),
  }));

  const edges: Edge[] = []
  props.selectedModel.nodes.forEach((node) => {
    node.relations.forEach((relation) => {
      edges.push({
        from: node.id,
        to: relation.target,
        label: relation.title.replace("\n"," ").trim(),
        arrows: 'to',
      });
    });
  })

  const options = {
    nodes: {
      shape: 'box',
      font: {
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
        align: 'middle',
      }
    },
    physics: {
      enabled: true,
      solver: 'barnesHut',
      barnesHut: {
        avoidOverlap: 1,
      },
    }
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

// graph initialization
watch(() => props.selectedModel, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    initializeGraph();
  }
});

watch(active, (newValue) => {
  if (newValue === 'full') {
    initializeGraph();
  } else {
    initializeGraph();
  }
});

onMounted(() => {
  initializeGraph();
});
</script>

<template>
  <Tabs default-value="full" v-model:model-value="active">
    <TabsList class="h-[5%]">
      <TabsTrigger value="full">Full Model</TabsTrigger>
      <TabsTrigger value="request">Request Model</TabsTrigger>
    </TabsList>
    <div ref="container" class="h-[95%] border border-border rounded-md bg-background"/>
  </Tabs>
</template>
