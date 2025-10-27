<script setup lang="ts">
import { mdiArrowLeft } from "@mdi/js";
import { computed, defineAsyncComponent, watch, ref } from "vue";
import { useRouter } from "vue-router";
import pref from "../plugins/store";
import { AppConfigType, appConfigTypeMetadata } from "../util/app_config";
import { IAppsConfig } from "../util/types";

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

// Get friendly name for the app
const appFriendlyName = computed(() => {
  const appsList = pref.roPreferences.appsList;
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

const addOrGenerateConfig = (type: AppConfigType) => {
  pref.rwPreferences.config.apps.push({
    key: props.appId,
    value: appConfigTypeMetadata[type].generate(),
    type: type,
  });
};

const clearConfig = (type: AppConfigType) => {
  const existingConfig = getConfigForType(type);
  if (existingConfig) {
    const idx_to_remove =
      pref.rwPreferences.config.apps.indexOf(existingConfig);
    pref.rwPreferences.config.apps.splice(idx_to_remove, 1);
  }
};

const regenerateConfigValue = (config: IAppsConfig) => {
  config.value = appConfigTypeMetadata[config.type].generate();
};

const handleRegenerate = (type: AppConfigType) => {
  const config = getConfigForType(type);
  if (config) {
    regenerateConfigValue(config);
  }
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

    <v-row>
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
        @add="addOrGenerateConfig(configTypeInfo.type)"
        @clear="clearConfig(configTypeInfo.type)"
        @regenerate="handleRegenerate(configTypeInfo.type)"
      />

      <v-divider class="my-4" />

      <ProfileManager :app-id="appId" />
    </PreferenceEditor>
  </div>
</template>
