<script setup lang="ts">
import { defineAsyncComponent, Ref, ref } from "vue";

const AppList = defineAsyncComponent(
  () => import("../components/AppList.vue")
);
const AppConfigEditor = defineAsyncComponent(
  () => import("../components/AppConfigEditor.vue")
);

// Two-step flow: first select app, then configure
const selectedAppId: Ref<null | string> = ref(null);

const selectApp = (appId: string) => {
  selectedAppId.value = appId;
};

const goBackToAppList = () => {
  selectedAppId.value = null;
};
</script>

<template>
  <!-- Step 1: App Selection View -->
  <AppList v-if="!selectedAppId" @select-app="selectApp" />

  <!-- Step 2: Config Editor View -->
  <AppConfigEditor v-else :app-id="selectedAppId" @back="goBackToAppList" />
</template>
