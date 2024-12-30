<script setup lang="ts">
import {LoaderCircleIcon, PlusCircleIcon, PlusIcon} from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {FormControl, FormField, FormItem, FormLabel, FormMessage} from '@/components/ui/form'
import { toTypedSchema } from '@vee-validate/zod'
import { useForm } from 'vee-validate'
import * as z from 'zod'
import {createConfiguration} from "@/api/configuration.ts";
import { useRouter } from 'vue-router'
import {ref} from "vue";
import {uploadUxfToModel} from "@/api/files.ts";

const router = useRouter()
const configFile = ref<File>()
const isLoadingValidate = ref(false)

const formSchema = toTypedSchema(z.object({
  configName: z.string().min(2).max(50),
  configFile: z.string().optional()
}))

const form = useForm({
  validationSchema: formSchema,
})

const createProject = async (data: { name: string }) => {
  try {
    isLoadingValidate.value = true
    const createdConfig = await createConfiguration(data);
    if(configFile.value) {
      await uploadUxfToModel(configFile.value, createdConfig.name)
    }
    isLoadingValidate.value = false
    await router.push({name: 'mainview', params: {id: createdConfig.name}})
  } catch (error) {
    form.setFieldError('configName', 'Error creating project');
  }
}

const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && input.files.length) {
    configFile.value = input.files[0]
  }
}

const onSubmit = form.handleSubmit((values) => {
  createProject({ name: values.configName })
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
            <Input type="text" required placeholder="Name" v-bind="componentField" />
          </FormControl>
        </FormItem>
        <FormMessage v-if="errorMessage">{{ errorMessage }}</FormMessage>
      </FormField>

      <FormField name="configFile">
        <FormItem>
          <FormLabel>Optional: Import from UXF file</FormLabel>
          <FormControl>
            <Input
                ref="fileInputRef"
                type="file"
                accept=".uxf"
                class="cursor-pointer"
                @change="handleFileChange"
            />
          </FormControl>
        </FormItem>
      </FormField>

      <Button type="submit" class="w-full flex items-center gap-2">
        <PlusIcon v-if="!isLoadingValidate" class="w-5 h-5" />
        <LoaderCircleIcon v-else class="animate-spin"/>
        Create
      </Button>
    </form>
  </div>
</template>