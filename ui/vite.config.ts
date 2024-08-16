import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vuetify from "vite-plugin-vuetify";

// https://vitejs.dev/config/
export default defineConfig({
  base: "/assets/webview/",
  build: {
    outDir: "../app/src/main/assets/webview",
    emptyOutDir: true,
  },
  esbuild: {
    legalComments: "none",
  },
  plugins: [vue(), vuetify({ autoImport: true })],
  server: {
    host: false,
  },
});
