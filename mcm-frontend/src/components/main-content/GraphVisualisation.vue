<script setup lang="ts">
import {onMounted, ref, watch} from 'vue'
import {Edge, Network} from 'vis-network'
import {Model} from "@/types/Model.ts";
import {Node, Relation} from "@/types/Node.ts";

// props related
const emit = defineEmits(["update:selectedEntity"]);
const props = defineProps({
  modelToDisplay: {
    type: Object as () => Model,
    required: true
  },
})

// variables
const container = ref<HTMLElement | null>(null)
let network: Network | null = null;

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
    entity = props.modelToDisplay.nodes.flatMap(node => node.relations).find(relation => relation.id === id) as Relation;
  } else {
    entity = props.modelToDisplay.nodes.find(node => node.id === id) as Node;
  }
  if(entity) emit("update:selectedEntity", entity);
};

const initializeGraph = () => {
  if (!container.value) return;

  const nodes = props.modelToDisplay.nodes.map((node) => ({
    id: node.id,
    label: node.title.replace("\n"," ").trim(),
    color: generatePaleColorFromText(node.elementType),
  }));

  const edges: Edge[] = []
  props.modelToDisplay.nodes.forEach((node) => {
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
      smooth: {
        enabled: true,
        type: 'curvedCW',
        roundness: 0.1,
      },
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

// on change of selected model (left side)
watch(() => props.modelToDisplay, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    initializeGraph();
  }
});

// on mounted
onMounted(() => {
  initializeGraph();
});
</script>

<template>
  <div ref="container" class="h-full border border-border rounded-md bg-background"/>
</template>
