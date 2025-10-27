<script setup lang="ts">
import { mdiReload, mdiPlus, mdiClose } from "@mdi/js";
import { AppConfigType, appConfigTypeMetadata } from "../util/app_config";
import { IAppsConfig } from "../util/types";
import pref from "../plugins/store";

// Define props
const props = defineProps<{
  type: AppConfigType;
  config: IAppsConfig | null;
  appId: string;
}>();

const validateValue = (value: any) => {
  if (!value) return "Required";
  const isValid = appConfigTypeMetadata[props.type].validate(value);
  return (
    isValid || `Invalid ${appConfigTypeMetadata[props.type].friendly} format`
  );
};

const handleAdd = () => {
  // Add new config to the apps array
  pref.rwPreferences.config.apps.push({
    key: props.appId,
    value: appConfigTypeMetadata[props.type].generate(),
    type: props.type,
  });
};

const handleClear = () => {
  // Remove the config from the apps array
  if (props.config) {
    const idx_to_remove = pref.rwPreferences.config.apps.indexOf(props.config);
    if (idx_to_remove !== -1) {
      pref.rwPreferences.config.apps.splice(idx_to_remove, 1);
    }
  }
};

const handleRegenerate = () => {
  // Regenerate the value for the existing config
  if (props.config) {
    props.config.value = appConfigTypeMetadata[props.type].generate();
  }
};
</script>

<template>
  <v-row class="mb-2">
    <v-col cols="7">
      <v-text-field
        v-if="config"
        v-model="config.value"
        :label="appConfigTypeMetadata[type].friendly"
        validate-on="eager"
        clearable
        hide-details="auto"
        :rules="[validateValue]"
        variant="filled"
      >
      </v-text-field>
      <v-text-field
        v-else
        :label="appConfigTypeMetadata[type].friendly"
        :model-value="''"
        placeholder="Not configured"
        disabled
        hide-details="auto"
        variant="outlined"
      >
      </v-text-field>
    </v-col>
    <v-col cols="4">
      <!-- If config exists: show clear and regenerate buttons side by side -->
      <template v-if="config">
        <div class="d-flex ga-1">
          <v-btn
            @click="handleClear"
            color="warning"
            style="height: 56px; min-width: 48px; flex: 1"
            title="Clear configuration"
            size="small"
          >
            <v-icon :icon="mdiClose"></v-icon>
          </v-btn>
          <v-btn
            @click="handleRegenerate"
            style="height: 56px; min-width: 48px; flex: 1"
            title="Regenerate value"
            size="small"
          >
            <v-icon :icon="mdiReload"></v-icon>
          </v-btn>
        </div>
      </template>
      <!-- If config doesn't exist: show add button -->
      <template v-else>
        <v-btn
          @click="handleAdd"
          color="primary"
          style="height: 56px; min-width: 48px; width: 100%"
          title="Add configuration"
          size="small"
        >
          <v-icon :icon="mdiPlus"></v-icon>
        </v-btn>
      </template>
    </v-col>
  </v-row>
</template>
