<script setup lang="ts">
import {ref, onMounted, watch, PropType} from 'vue'
import { Network, Edge } from 'vis-network'
import {Model} from "@/types/Model.ts";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Relation, Node} from "@/types/Node.ts";
import {parseResponseGraph} from "@/components/main-content/responseGraphVisualization.ts";

// props related
const emit = defineEmits(["update:selectedEntity"]);
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

// variables
const container = ref<HTMLElement | null>(null)
const active = ref('full')
const activeModel = ref<Model>(props.selectedModel);
const queryGraph = ref<Model | null>(null);
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
    entity = activeModel.value.nodes.flatMap(node => node.relations).find(relation => relation.id === id) as Relation;
  } else {
    entity = activeModel.value.nodes.find(node => node.id === id) as Node;
  }
  if(entity) emit("update:selectedEntity", entity);
};

const initializeGraph = () => {
  if (!container.value) return;

  const nodes = activeModel.value.nodes.map((node) => ({
    id: node.id,
    label: node.title.replace("\n"," ").trim(),
    color: node.umletAttributes.bg || generatePaleColorFromText(node.elementType),
    x: node.umletPosition.x,
    y: node.umletPosition.y,
    widthConstraint: {
      minimum: node.umletPosition.width - 10,
      maximum: node.umletPosition.width - 10,
    }
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
        roundness: 0.1
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
