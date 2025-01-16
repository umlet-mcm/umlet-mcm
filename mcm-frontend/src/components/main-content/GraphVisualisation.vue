<script setup lang="ts">
import {onMounted, ref, watch} from 'vue'
import {Edge, Network} from 'vis-network'
import {Model} from "@/types/Model.ts";
import {Node, Relation} from "@/types/Node.ts";

/**
 * @param {Model} selectedModel, model to display
 * @param {Record<string, any>[]} queryResponse, response from the query, used to filter the model (optional)
 */
const props = defineProps({
  modelToDisplay: {
    type: Object as () => Model,
    required: true
  },
})

/**
 * @emits {Node | Relation} update:selectedEntity, selected entity
 */
const emit = defineEmits<{
  'update:selectedEntity': [entity: Node | Relation]
}>()

// variables
const container = ref<HTMLElement | null>(null)
let network: Network | null = null;

// functions
/**
 * Generate a pale color from a text
 * @param text to generate the color from
 * @returns rgb color
 */
const generatePaleColorFromText = (text: string) => {
  const hash = [...text].reduce((acc, char) => acc + char.charCodeAt(0), 0);
  const red = (hash * 137) % 128 + 127;
  const green = (hash * 233) % 128 + 127;
  const blue = (hash * 97) % 128 + 127;
  return `rgb(${red}, ${green}, ${blue})`;
}

/**
 * Select an entity on the graph
 * @param id of the entity
 * @param type of the entity (node or relation)
 */
const selectedEntity = (id: string, type: string) => {
  let entity;
  if (type === 'relation') {
    entity = props.modelToDisplay.nodes.flatMap(node => node.relations).find(relation => relation.id === id) as Relation;
  } else {
    entity = props.modelToDisplay.nodes.find(node => node.id === id) as Node;
  }
  if(entity) emit("update:selectedEntity", entity);
};

/**
 * Initialize the graph
 * Create the nodes and edges from the active model
 * Add the events on the graph
 */
const initializeGraph = () => {
  if (!container.value) return;

  const nodes = props.modelToDisplay.nodes.map((node) => ({
    id: node.id,
    label: node.title.replace("\n"," ").trim(),
    color: node.umletAttributes.bg || generatePaleColorFromText(node.elementType),
    x: node.umletPosition.x,
    y: node.umletPosition.y,
    widthConstraint: node.umletPosition.width - 10 // reducing overlap by minus 10
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
        dashes: relation.type.includes('.'),
      });
    });
  })

  const options = {
    nodes: {
      shape: 'box',
      font: {
        size: 12,
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
      }
    },
    physics: {
      enabled: false,
    },
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
