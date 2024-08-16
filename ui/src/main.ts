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
