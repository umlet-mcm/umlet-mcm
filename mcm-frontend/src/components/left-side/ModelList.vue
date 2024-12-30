<script setup lang="ts">
import { cn } from '@/lib/utils'
import { Model } from '@/types/Model.ts'
import {Button} from "@/components/ui/button";
import {Trash} from "lucide-vue-next";

defineProps({
  items: {
    type: Array as () => Model[],
    required: true,
  },
  selectedModel: {
    type: Object as () => Model,
    required: false
  }
});

defineEmits(["update:selectedModel", "deleteModel"]);
</script>

<template>
    <div class="flex-1 flex flex-col gap-1">
      <TransitionGroup name="list" appear>
        <button
            v-for="item of items.sort((a, b) => a.id.localeCompare(b.id))"
            :key="item.id"
            :class="cn(
            'flex flex-col items-start gap-2 rounded-lg border p-3 text-left text-sm transition-all hover:bg-accent bg-primary-light', selectedModel?.id === item.id && 'bg-muted')"
            @click="$emit('update:selectedModel', item)">
          <div class="flex w-full flex-col gap-1">
            <div class="flex items-center w-full">
              <div class="flex items-center gap-2 w-4/5">
                <div class="font-semibold w-4/5">
                  {{ item.id }}
                </div>
              </div>
              <div :class="cn('ml-auto', selectedModel?.id === item.id ? 'visible' : 'invisible')" class="w-1/4">
                <Button class="rounded-full" variant="destructive" size="icon" @click="$emit('deleteModel')">
                  <Trash />
                </Button>
              </div>
            </div>
          </div>
        </button>
      </TransitionGroup>
    </div>
</template>