<script setup lang="ts">
import {ref, onMounted, watch, PropType} from 'vue'
import { Network, Edge } from 'vis-network'
import {Model} from "@/types/Model.ts";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Relation, Node} from "@/types/Node.ts";
import {parseResponseGraph} from "@/components/main-content/responseGraphVisualization.ts";

/**
 * @param {Model} selectedModel, model to display
 * @param {Record<string, any>[]} queryResponse, response from the query, used to filter the model (optional)
 */
const props = defineProps({
  selectedModel: {
    type: Object as () => Model,
    required: true
  },
  queryResponse: {
    type: Array as PropType<Record<string, any>[]>,
    required: false
  }
})

/**
 * @emits {Node | Relation} update:selectedEntity, selected entity
 */
const emit = defineEmits<{
  'update:selectedEntity': [entity: Node | Relation]
}>()

// variables
const container = ref<HTMLElement | null>(null)
const active = ref('full')
const activeModel = ref<Model>(props.selectedModel);
const queryGraph = ref<Model | null>(null);
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
    entity = activeModel.value.nodes.flatMap(node => node.relations).find(relation => relation.id === id) as Relation;
  } else {
    entity = activeModel.value.nodes.find(node => node.id === id) as Node;
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

  const nodes = activeModel.value.nodes.map((node) => ({
    id: node.id,
    label: node.title.replace("\n"," ").trim(),
    color: generatePaleColorFromText(node.elementType),
  }));

  const edges: Edge[] = []
  activeModel.value.nodes.forEach((node) => {
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
        type: 'curvedCW',
        roundness: 0.1,
        enabled:true
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
watch(() => props.selectedModel, (newValue, oldValue) => {
  if (newValue !== oldValue) {
    activeModel.value = newValue;
    active.value = 'full';
    initializeGraph();
  }
});

// on change of active tab
watch(active, async (newValue) => {
  if (newValue === 'full') {
    activeModel.value = props.selectedModel
    initializeGraph();
  } else {
    if(queryGraph.value) {
      activeModel.value = queryGraph.value;
      initializeGraph();
    }
  }
});

// on change of query response (new request)
watch(() => props.queryResponse, async (newValue) => {
  if (newValue?.length) {
    queryGraph.value = await parseResponseGraph(newValue, props.selectedModel);
    // if queryGraph is empty, then set the selected model as active model
    activeModel.value = queryGraph.value?.nodes.length ? queryGraph.value : props.selectedModel;
    active.value = queryGraph.value?.nodes.length ? 'request' : 'full';
  } else {
    // if queryGraph is empty, then set the selected model as active model
    queryGraph.value = null;
    activeModel.value = props.selectedModel;
    active.value = 'full';
  }
  initializeGraph();
});

// on mounted
onMounted(() => {
  activeModel.value = props.selectedModel
  initializeGraph();
});
</script>

<template>
  <Tabs default-value="full" v-model:model-value="active">
    <TabsList class="h-[5%]">
      <TabsTrigger value="full">Full Model</TabsTrigger>
      <TabsTrigger v-if="queryGraph && queryGraph.nodes.length > 0" value="request">Request Model</TabsTrigger>
    </TabsList>
    <div ref="container" class="h-[95%] border border-border rounded-md bg-background"/>
  </Tabs>
</template>
