<script setup lang="ts">
import { cn } from '@/lib/utils'
import { Model } from '@/datamodel/Model.ts'
import {Button} from "@/components/ui/button";
import {Trash} from "lucide-vue-next";

defineProps({
  items: {
    type: Array as () => Model[],
    required: true
  },
  selectedModel: {
    type: Object as () => Model,
    required: false
  }
});

const emit = defineEmits(["update:selectedModel"]);
const selectModel = (model: Model) => {
  emit("update:selectedModel", model);
};

const placeholder = () => {
  console.log('Placeholder')
}

</script>

<template>
    <div class="flex-1 flex flex-col gap-1">
      <TransitionGroup name="list" appear>
        <button
            v-for="item of items"
            :key="item.id"
            :class="cn(
            'flex flex-col items-start gap-2 rounded-lg border p-3 text-left text-sm transition-all hover:bg-accent bg-primary-light', selectedModel?.id === item.id && 'bg-muted')"
            @click="selectModel(item)">
          <div class="flex w-full flex-col gap-1">
            <div class="flex items-center">
              <div class="flex items-center gap-2">
                <div class="font-semibold">
                  {{ item.id }}
                </div>
              </div>
              <div :class="cn('ml-auto', selectedModel?.id === item.id ? 'visible' : 'invisible')">
                <Button class="rounded-full" variant="destructive" size="icon" @click="placeholder">
                  <Trash />
                </Button>
              </div>
            </div>
          </div>
        </button>
      </TransitionGroup>
    </div>
</template>