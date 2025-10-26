/// <reference types="vite/client" />

declare const __APP_FLAVOR__: "full" | "check";

declare module "*.vue" {
  import type { DefineComponent } from "vue";
  const component: DefineComponent<{}, {}, any>;
  export default component;
}
