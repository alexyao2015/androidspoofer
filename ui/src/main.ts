import { createApp } from "vue";
import App from "./App.vue";
import vuetify from "./plugins/vuetify";
import pinia from "./plugins/pinia";
import router from "./plugins/router";

const app = createApp(App);

app.use(vuetify);
app.use(router);
app.use(pinia);

app.mount("#app");

// Handle Android back button
(window as any).handleAndroidBack = () => {
  // Check if we can go back in Vue Router history
  if (router.options.history.state.back) {
    router.back();
    return true;
  }
  // Let Android handle it (exit app)
  return false;
};
