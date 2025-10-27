<script setup lang="ts">
import {
  computed,
  defineAsyncComponent,
  onMounted,
  onUnmounted,
  ref,
  Ref,
  watch,
  nextTick,
} from "vue";
import { useRouter } from "vue-router";
import pref from "../plugins/store";
import AppIcon from "./AppIcon.vue";

const SaveResetButtons = defineAsyncComponent(
  () => import("./SaveResetButtons.vue")
);

const router = useRouter();

const searchFieldAppsList: Ref<null | string> = ref(null);
const showOnlyConfigured = ref(false);
const displayCount = ref(80); // Start with 80 apps
const LOAD_INCREMENT = 80; // Load 80 more each time
const loadMoreTrigger = ref<HTMLElement | null>(null);
let observer: IntersectionObserver | null = null;

// Setup intersection observer for infinite scroll
const setupObserver = () => {
  if (observer) {
    observer.disconnect();
  }

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          // Load more items when trigger comes into view
          if (displayCount.value < appsWithConfigCounts.value.length) {
            displayCount.value += LOAD_INCREMENT;
          }
        }
      });
    },
    { threshold: 0.1 }
  );

  if (loadMoreTrigger.value) {
    observer.observe(loadMoreTrigger.value);
  }
};

// Fetch apps list on mount
onMounted(async () => {
  // Allow component to render first so loading spinner shows
  await nextTick();

  if (pref.appsList === null) {
    // First time: wait for the data
    await pref.fetchAppsList();
  } else {
    // Already have data: refresh in background without blocking UI
    pref.fetchAppsList();
  }

  // Setup observer after data is loaded
  await nextTick();
  setupObserver();
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});

// Get full list of app IDs with existing configs count
const appsWithConfigCounts = computed(() => {
  // Return empty array while loading or not yet loaded
  if (pref.appsList === null || pref.appsListLoading) {
    return [];
  }

  const apps_list = pref.appsList as { [appName: string]: string };
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

  // Map to include config counts
  const appsWithCounts = appEntries.map(({ appName, appId }) => {
    const count = pref.rwPreferences.config.apps.filter(
      (c) => c.key === appId
    ).length;
    return { appName, appId, count };
  });

  // Apply configured filter
  if (showOnlyConfigured.value) {
    return appsWithCounts.filter((app) => app.count > 0);
  }

  return appsWithCounts;
});

// Displayed apps using infinite scroll
const displayedApps = computed(() => {
  return appsWithConfigCounts.value.slice(0, displayCount.value);
});

// Reset display count when search or filter changes, then re-setup observer
watch([searchFieldAppsList, showOnlyConfigured], async () => {
  displayCount.value = LOAD_INCREMENT;
  await nextTick();
  setupObserver();
});

// Watch for changes to displayedApps and re-setup observer
watch(displayedApps, async () => {
  await nextTick();
  setupObserver();
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

    <!-- Loading spinner for first load -->
    <div v-if="pref.appsListLoading" class="text-center py-8">
      <v-progress-circular
        indeterminate
        color="primary"
        size="64"
      ></v-progress-circular>
      <p class="mt-4 text-medium-emphasis">Loading apps list...</p>
    </div>

    <!-- Apps list -->
    <v-list v-else>
      <v-list-item
        v-for="appInfo in displayedApps"
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
          <AppIcon :app-id="appInfo.appId" :app-name="appInfo.appName" />
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

    <!-- Intersection observer target for infinite scroll -->
    <div
      v-if="
        !pref.appsListLoading &&
        displayedApps.length < appsWithConfigCounts.length
      "
      ref="loadMoreTrigger"
      class="py-4 text-center"
    >
      <v-progress-circular indeterminate size="32"></v-progress-circular>
    </div>

    <v-alert
      v-if="!pref.appsListLoading && appsWithConfigCounts.length === 0"
      type="info"
      class="mt-4"
    >
      No apps found
    </v-alert>

    <SaveResetButtons />
  </div>
</template>
