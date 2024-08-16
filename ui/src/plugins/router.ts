import { createRouter, createWebHashHistory, type Router } from "vue-router";
import AppsConfig from "../pages/AppsConfig.vue";
import Options from "../pages/Options.vue";

const routes = [
  { name: "home", path: "/", component: AppsConfig, props: true },
  {
    name: "options",
    path: "/options",
    component: Options,
    props: true,
  },
];

const router: Router = createRouter({
  history: createWebHashHistory(),
  routes,
});

// router.beforeEach((to, from) => {
//   if (!from.name) return;
//   if (to.params["backnav"]) return;
//   from.meta.scrollPos = window.scrollY;
//   store.prevRoutes.push({
//     name: from.name as string,
//     query: from.query,
//     params: from.params,
//     meta: from.meta,
//   });
// });

export default router;
