<script setup lang="ts">
import { mdiArrowLeft } from "@mdi/js";
import {
  computed,
  defineAsyncComponent,
  onMounted,
  nextTick,
  watch,
  ref,
} from "vue";
import { useRouter } from "vue-router";
import pref from "../plugins/store";
import { AppConfigType, appConfigTypeMetadata } from "../util/app_config";
import { IAppsConfig } from "../util/types";
import AppIcon from "./AppIcon.vue";

const ConfigItem = defineAsyncComponent(() => import("./ConfigItem.vue"));
const PreferenceEditor = defineAsyncComponent(
  () => import("./PreferenceEditor.vue")
);
const ProfileManager = defineAsyncComponent(
  () => import("./ProfileManager.vue")
);

const router = useRouter();

// Define props
const props = defineProps<{
  appId: string;
}>();

// Fetch apps list if not already cached
onMounted(async () => {
  // Allow component to render first
  await nextTick();

  if (pref.appsList === null) {
    await pref.fetchAppsList();
  }
});

// Get friendly name for the app
const appFriendlyName = computed(() => {
  if (pref.appsList === null) {
    return props.appId; // Fallback while loading
  }

  const appsList = pref.appsList;
  // Find the friendly name by searching for the appId in the values
  for (const [friendlyName, packageId] of Object.entries(appsList)) {
    if (packageId === props.appId) {
      return friendlyName;
    }
  }
  return props.appId; // Fallback to package ID if not found
});

// Get config state for each type
const getConfigForType = (type: AppConfigType): IAppsConfig | null => {
  return (
    pref.rwPreferences.config.apps.find(
      (config) => config.key === props.appId && config.type === type
    ) || null
  );
};

// Create a computed array of all config types with their current values
const allConfigTypes = computed(() => {
  return Object.values(AppConfigType).map((type) => ({
    type,
    config: getConfigForType(type),
  }));
});

const handleBack = () => {
  router.push({ name: "home" });
};
</script>

<template>
  <div>
    <v-row>
      <v-col>
        <v-btn @click="handleBack" variant="text" :prepend-icon="mdiArrowLeft">
          Back to apps
        </v-btn>
      </v-col>
    </v-row>

    <v-row align="center">
      <v-col cols="auto">
        <AppIcon :app-id="appId" :app-name="appFriendlyName" />
      </v-col>
      <v-col>
        <h2 class="text-h5 mb-2">{{ appFriendlyName }}</h2>
        <p class="text-caption text-medium-emphasis">{{ appId }}</p>
      </v-col>
    </v-row>

    <v-divider class="my-4" />

    <!-- Config Editor Section -->
    <PreferenceEditor>
      <ConfigItem
        v-for="configTypeInfo in allConfigTypes"
        :type="configTypeInfo.type"
        :config="configTypeInfo.config"
        :app-id="appId"
      />

      <v-divider class="my-4" />

      <ProfileManager :app-id="appId" />
    </PreferenceEditor>
  </div>
</template>
