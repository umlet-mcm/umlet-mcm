import OpenCreateProjectView from "@/views/OpenCreateProjectView.vue";
import MainView from "@/views/MainView.vue";
import {createRouter, createWebHistory} from "vue-router";

const routes = [
    { path: '/', name: "home", component: OpenCreateProjectView },
    {
        path: '/configuration/:id',
        name: "mainview",
        component: MainView,
        props: route => ({
            id: route.params.id
        }),
    }
]

export const router = createRouter({
    history: createWebHistory(),
    routes,
})