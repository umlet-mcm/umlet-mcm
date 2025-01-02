import OpenCreateProjectView from "@/views/OpenCreateProjectView.vue";
import MainView from "@/views/MainView.vue";
import {createRouter, createWebHistory} from "vue-router";
import HelpView from "@/views/HelpView.vue";

const routes = [
    { path: '/', name: "home", component: OpenCreateProjectView },
    {
        path: '/configuration/:id',
        name: "mainview",
        component: MainView,
        props: (route:any) => ({
            id: route.params.id
        }),
    },
    { path: '/help', name: "help", component: HelpView },
    { path: '/:pathMatch(.*)*', redirect: { name: "home" } },
]

export const router = createRouter({
    history: createWebHistory(),
    routes,
})