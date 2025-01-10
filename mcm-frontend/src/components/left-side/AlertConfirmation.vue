<script setup lang="ts">
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";

/**
 * @param {Boolean} isOpen, dialog visibility
 * @param {String} dialogTitle, dialog title
 * @param {String} dialogDescription, dialog description
 * @param {String} dialogContent, dialog content
 * @param {Function} onConfirm, function to be called when confirm button is clicked
 * @param {Function} onReject, function to be called when reject button is clicked (optional)
 */
const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  dialogTitle: {
    type: String,
    required: true
  },
  dialogDescription: {
    type: String,
    required: true
  },
  dialogContent: {
    type: String,
    required: true
  },
  onConfirm: {
    type: Function,
    required: true
  },
  onReject: {
    type: Function,
    required: false
  }
});

/**
 * @emits {Boolean} update:isOpen, dialog visibility
 */
const emit = defineEmits<{
  'update:isOpen': [value: boolean]
}>()

// functions
const confirm = () => {
  props.onConfirm()
  emit('update:isOpen', false)
}

const closeDialog = () => {
  if(props.onReject) props.onReject()
  emit('update:isOpen', false)
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog">
    <DialogContent class="sm:max-w-[600px]">
      <DialogHeader>
        <DialogTitle>{{dialogTitle}}</DialogTitle>
        <DialogDescription>{{dialogDescription}}</DialogDescription>
      </DialogHeader>
      <label class="text-sm">{{dialogContent}}</label>
      <DialogFooter>
        <Button variant="outline" @click="closeDialog">No</Button>
        <Button @click="confirm">Yes</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>