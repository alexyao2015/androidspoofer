<script setup lang="ts">
import { mdiArrowLeft, mdiReload } from "@mdi/js";
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

// Track which config types are selected
const selectedTypes = ref<Set<AppConfigType>>(new Set());

// Computed property for "select all" checkbox state
const selectAllState = computed({
  get: () => {
    const configuredTypes = allConfigTypes.value.filter(
      (ct) => ct.config !== null
    );
    if (configuredTypes.length === 0) return false;
    return configuredTypes.every((ct) => selectedTypes.value.has(ct.type));
  },
  set: (value: boolean) => {
    if (value) {
      // Select all configured types
      allConfigTypes.value.forEach((ct) => {
        if (ct.config !== null) {
          selectedTypes.value.add(ct.type);
        }
      });
    } else {
      // Deselect all
      selectedTypes.value.clear();
    }
  },
});

// Check if a type is selected
const isTypeSelected = (type: AppConfigType) => {
  return selectedTypes.value.has(type);
};

// Toggle selection for a type
const toggleTypeSelection = (type: AppConfigType) => {
  if (selectedTypes.value.has(type)) {
    selectedTypes.value.delete(type);
  } else {
    selectedTypes.value.add(type);
  }
};

// Count of selected configured items
const selectedCount = computed(() => {
  return allConfigTypes.value.filter(
    (ct) => ct.config !== null && selectedTypes.value.has(ct.type)
  ).length;
});

const handleBack = () => {
  router.push({ name: "home" });
};

const handleRegenerateSelected = () => {
  // Regenerate only selected configured values for this app
  pref.rwPreferences.config.apps.forEach((config) => {
    if (
      config.key === props.appId &&
      config.type in appConfigTypeMetadata &&
      selectedTypes.value.has(config.type as AppConfigType)
    ) {
      config.value = appConfigTypeMetadata[config.type].generate();
    }
  });
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

    <!-- Select All Checkbox -->
    <v-row class="mb-2" align="center">
      <v-col cols="auto">
        <v-btn
          @click="handleRegenerateSelected"
          color="primary"
          variant="tonal"
          :prepend-icon="mdiReload"
          :disabled="selectedCount === 0"
        >
          Regenerate Selected ({{ selectedCount }})
        </v-btn>
      </v-col>
      <v-col></v-col>
      <v-col cols="auto" class="d-flex align-center">
        <span class="mr-2" style="user-select: none">Select All</span>
        <v-checkbox v-model="selectAllState" hide-details density="compact" />
      </v-col>
    </v-row>

    <!-- Config Editor Section -->
    <PreferenceEditor>
      <ConfigItem
        v-for="configTypeInfo in allConfigTypes"
        :key="configTypeInfo.type"
        :type="configTypeInfo.type"
        :config="configTypeInfo.config"
        :app-id="appId"
        :is-selected="isTypeSelected(configTypeInfo.type)"
        @toggle-selection="toggleTypeSelection(configTypeInfo.type)"
      />

      <v-divider class="my-4" />

      <ProfileManager :app-id="appId" />
    </PreferenceEditor>
  </div>
</template>
