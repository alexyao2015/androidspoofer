import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vuetify from "vite-plugin-vuetify";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // Get APP_FLAVOR from environment variable, default to 'full'
  // @ts-ignore - process is available in Node environment during build
  const appFlavor = process.env["APP_FLAVOR"] || "full";
  const outDir =
    appFlavor === "check"
      ? "../app/src/check/assets/webview"
      : "../app/src/full/assets/webview";

  return {
    base: "/assets/webview/",
    build: {
      outDir,
      emptyOutDir: true,
    },
    esbuild: {
      legalComments: "none",
    },
    define: {
      __APP_FLAVOR__: JSON.stringify(appFlavor),
    },
    plugins: [vue(), vuetify({ autoImport: true })],
    server: {
      host: false,
    },
  };
});
