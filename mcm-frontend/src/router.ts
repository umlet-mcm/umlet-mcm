import OpenCreateProjectView from "@/views/OpenCreateProjectView.vue";
import MainView from "@/views/MainView.vue";
import { createRouter, createWebHashHistory, createWebHistory } from "vue-router";
import isElectron from 'is-electron';
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
    // history only works in hash mode in electron
    // cf {https://nklayman.github.io/vue-cli-plugin-electron-builder/guide/commonIssues.html#blank-screen-on-builds-but-works-fine-on-serve}
    history: isElectron() ? createWebHashHistory() : createWebHistory(),
    routes,
})