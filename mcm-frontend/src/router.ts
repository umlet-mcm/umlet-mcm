import OpenCreateProjectView from "@/components/first-window/OpenCreateProjectView.vue";
import MainView from "@/components/main-content/MainView.vue";
import {createRouter, createWebHistory} from "vue-router";

const routes = [
    { path: '/', name: "home", component: OpenCreateProjectView },
    {
        path: '/configuration/:id',
        name: "configview",
        component: MainView,
        props: route => ({
            id: route.params.id,
            model: route.query.model
        }),
    }
]

export const router = createRouter({
    history: createWebHistory(),
    routes,
})