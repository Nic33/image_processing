import { createWebHistory, createRouter } from "vue-router";
import { RouteRecordRaw } from "vue-router";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: () => import("./components/Home.vue"),
    props: true
  },

  {
    path: "/gallery",
    name: "gallery",
    component: () => import("./components/Gallery.vue"),
    props: true
  },

  {
    path: "/image/:id",
    name: "image",
    component: () => import("./components/Image.vue"),
    props: ({ params }) => ({ id: Number(params.id) || 0 })
  },

  {
    path: "/upload",
    name: "upload",
    component: () => import("./components/V1/Upload.vue"),
    props: true
  },
  {
    path: "/download",
    name: "download",
    component: () => import("./components/V1/Download.vue"),
    props: true
  },

  {
    path: "/modify",
    name: "modify",
    component: () => import("./components/V1/Modify.vue"),
    props: true
  },
  {
    path: "/modify/:id",
    name: "modify_id",
    component: () => import("./components/V1/Modify_Image.vue"),
    props: ({ params }) => ({ id: Number(params.id) || 0 })
  },

/*   {
    path: "/bottom",
    name: "bottom",
    component: () => import("./components/V2/Bottom.vue"),
    props: true
  }, */
  {
    path: "/contour",
    name: "contour",
    component: () => import("./components/V2/Contour.vue"),
    props: true
  },
  {
    path: "/editor",
    name: "editor",
    component: () => import("./components/V2/Editor.vue"),
    props: true
  },
  {
    path: "/paint",
    name: "paint",
    component: () => import("./components/V2/Paint.vue"),
    props: true
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
