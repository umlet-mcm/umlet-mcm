import { createApp } from 'vue'
import './assets/index.css'
import 'floating-vue/dist/style.css'
import App from './App.vue'
import {router} from "@/router.ts";
import FloatingVue from "floating-vue";

createApp(App)
    .use(router)
    .use(FloatingVue)
    .mount('#app')
