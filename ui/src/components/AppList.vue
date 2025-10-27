<script setup lang="ts">
import { mdiChevronRight } from "@mdi/js";
import { computed, defineAsyncComponent, ref, Ref } from "vue";
import { useRouter } from "vue-router";
import pref from "../plugins/store";
import { getAppIcon } from "../plugins/android";

const SaveResetButtons = defineAsyncComponent(
  () => import("./SaveResetButtons.vue")
);

const router = useRouter();

const searchFieldAppsList: Ref<null | string> = ref(null);
const showOnlyConfigured = ref(false);

// Get list of app IDs with existing configs count and icons
const appsWithConfigCounts = computed(() => {
  const apps_list = pref.roPreferences.appsList;
  const search = searchFieldAppsList.value;

  // Convert object to array of {appName, appId} entries
  let appEntries = Object.entries(apps_list).map(([appName, appId]) => ({
    appName,
    appId,
  }));

  // Filter by search
  if (search !== null && search !== "") {
    appEntries = appEntries.filter(
      (app) =>
        app.appName.toLowerCase().includes(search.toLowerCase()) ||
        app.appId.toLowerCase().includes(search.toLowerCase())
    );
  }

  // Sort by app name
  appEntries.sort((a, b) => a.appName.localeCompare(b.appName));

  // Map to include config counts and icons
  const appsWithCountsAndIcons = appEntries.map(({ appName, appId }) => {
    const count = pref.rwPreferences.config.apps.filter(
      (c) => c.key === appId
    ).length;

    // Fetch the app icon
    let iconDataUrl = "";
    try {
      const iconBase64 = getAppIcon(appId);
      if (iconBase64) {
        iconDataUrl = `data:image/png;base64,${iconBase64}`;
      }
    } catch (e) {
      console.error(`Failed to get icon for ${appId}:`, e);
    }

    return { appName, appId, count, iconDataUrl };
  });

  // Apply configured filter
  if (showOnlyConfigured.value) {
    return appsWithCountsAndIcons.filter((app) => app.count > 0);
  }

  return appsWithCountsAndIcons;
});

const handleSelectApp = (appId: string) => {
  router.push({ name: "appConfig", params: { appId } });
};
</script>

<template>
  <div>
    <v-text-field
      v-model="searchFieldAppsList"
      clearable
      hide-details="auto"
      label="Search apps"
      class="mb-4"
    ></v-text-field>

    <v-checkbox
      v-model="showOnlyConfigured"
      label="Show only apps with configurations"
      hide-details
      class="mb-4"
    ></v-checkbox>

    <v-list>
      <v-list-item
        v-for="appInfo in appsWithConfigCounts"
        :key="appInfo.appId"
        @click="handleSelectApp(appInfo.appId)"
        class="mb-2"
        style="
          cursor: pointer;
          border: 1px solid rgba(255, 255, 255, 0.12);
          border-radius: 4px;
        "
      >
        <template v-slot:prepend>
          <v-avatar v-if="appInfo.iconDataUrl" size="40" class="mr-2">
            <v-img :src="appInfo.iconDataUrl" :alt="appInfo.appName"></v-img>
          </v-avatar>
          <v-icon v-else :icon="mdiChevronRight" size="small"></v-icon>
        </template>
        <v-list-item-title>{{ appInfo.appName }}</v-list-item-title>
        <v-list-item-subtitle class="text-caption">{{
          appInfo.appId
        }}</v-list-item-subtitle>
        <template v-slot:append>
          <v-chip size="small" v-if="appInfo.count > 0">
            {{ appInfo.count }} config{{ appInfo.count > 1 ? "s" : "" }}
          </v-chip>
        </template>
      </v-list-item>
    </v-list>

    <v-alert v-if="appsWithConfigCounts.length === 0" type="info" class="mt-4">
      No apps found
    </v-alert>

    <SaveResetButtons />
  </div>
</template>
