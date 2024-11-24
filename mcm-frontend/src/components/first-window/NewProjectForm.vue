<script setup lang="ts">
import { PlusCircleIcon, PlusIcon } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import { toTypedSchema } from '@vee-validate/zod'
import { useForm } from 'vee-validate'
import * as z from 'zod'
import {createConfiguration} from "@/api/configuration.ts";

defineProps({
  selectedConfiguration: {
    type: Object,
    required: false
  }
});

const emit = defineEmits(["update:selectedConfiguration"]);

const formSchema = toTypedSchema(z.object({
  configName: z.string().min(2).max(50)
}))

const form = useForm({
  validationSchema: formSchema,
})

const createProject = async (data: { projectName: string }) => {
  try {
    const project = await createConfiguration(data);
    emit('update:selectedConfiguration', project);
  } catch (error) {
    form.setFieldError('configName', 'Error creating project');
  }
}

const onSubmit = form.handleSubmit((values) => {
  createProject({ projectName: values.configName })
})

</script>

<template>
  <div class="w-1/2 p-6">
    <h2 class="text-2xl font-semibold mb-4 flex items-center gap-2">
      <PlusCircleIcon class="w-6 h-6" />
      New Configuration
    </h2>
    <form @submit.prevent="onSubmit" class="space-y-4">
        <FormField v-slot="{ componentField, errorMessage }" name="configName">
          <FormItem>
            <FormLabel>Configuration name</FormLabel>
            <FormControl>
              <Input type="text" required placeholder="Configuration name" v-bind="componentField" />
            </FormControl>
          </FormItem>
          <FormMessage v-if="errorMessage">{{ errorMessage }}</FormMessage>
        </FormField>
      <Button type="submit" class="w-full">
        <PlusIcon class="w-5 h-5" />
        Create
      </Button>

    </form>
  </div>
</template>

