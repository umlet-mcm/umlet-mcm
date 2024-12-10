<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Network, Edge } from 'vis-network'
import {Model} from "@/types/Model.ts";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Relation, Node} from "@/types/Node.ts";

// variables
const container = ref<HTMLElement | null>(null)
const active = ref('full')
let network: Network | null = null;

// props related
const emit = defineEmits(["update:selectedEntity"]);
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

const selectedEntity = (id: string, type: string) => {
  let entity;
  if (type === 'relation') {
    entity = props.selectedModel.nodes.flatMap(node => node.relations).find(relation => relation.id === id) as Relation;
    console.log(props.selectedModel.nodes.flatMap(node => node.relations))
    console.log(id)
  } else {
    entity = props.selectedModel.nodes.find(node => node.id === id) as Node;
  }
  if(entity) emit("update:selectedEntity", entity);
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
        id: relation.id,
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
    },
    layout: {
      hierarchical: {
        direction: 'UD',
        sortMethod: 'hubsize',
        parentCentralization: true,
        edgeMinimization: true,
        blockShifting: true,
      }
    }
  };
  if (network) {
    network.destroy();
  }

  network = new Network(container.value, { nodes, edges }, options);
  network.on('click', (params) => {
    if(params.edges.length > 0 && !params.nodes.length) {
      selectedEntity(params.edges[0], 'relation');
    } else if (params.nodes.length > 0) {
      selectedEntity(params.nodes[0], 'node');
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
