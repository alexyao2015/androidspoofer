<script setup lang="ts">
import { mdiReload } from "@mdi/js";
import { defineAsyncComponent, ref } from "vue";
import pref from "../plugins/store";
import { AppConfigType, appConfigTypeMetadata } from "../util/app_config";
import { IAppsConfig } from "../util/types";
const PreferenceEditor = defineAsyncComponent(
  () => import("../components/PreferenceEditor.vue")
);

const selectedType = ref(Object.values(AppConfigType));
const searchField = ref(null);
const searchFieldAppsList = ref(null);

const selectedConfigs = () => {
  let app_configs = pref.rwPreferences.config.apps;
  if (selectedType.value.length > 1) {
    app_configs = app_configs.filter((config) =>
      selectedType.value.includes(config.type)
    );
  }
  if (searchField.value !== null) {
    app_configs = app_configs.filter(
      (config) =>
        // These properties may be null if the form is partially cleared
        config.key?.toLowerCase().includes(searchField.value.toLowerCase()) ||
        config.value?.toLowerCase().includes(searchField.value.toLowerCase())
    );
  }
  return app_configs;
};

const filteredAppList = () => {
  const apps_list = pref.roPreferences.appsList;
  if (searchFieldAppsList.value === null) {
    return apps_list;
  }
  return apps_list.filter((app) => {
    return app.toLowerCase().includes(searchFieldAppsList.value.toLowerCase());
  });
};

const addConfig = () => {
  const selected_configs = selectedConfigs();
  if (selected_configs.length === 0) {
    const new_type =
      selectedType.value.length > 1
        ? selectedType.value[0]
        : AppConfigType.android_id;
    pref.rwPreferences.config.apps.push({
      key: "",
      value: "",
      type: new_type,
    });
    return;
  }
  const last_config = selected_configs[selected_configs.length - 1];
  const config_copy = { ...last_config };
  config_copy.key = "";
  pref.rwPreferences.config.apps.push(config_copy);
};

const removeConfig = (config: IAppsConfig) => {
  const idx_to_remove = pref.rwPreferences.config.apps.indexOf(config);
  pref.rwPreferences.config.apps.splice(idx_to_remove, 1);
};

const regenerateConfigValue = (config: IAppsConfig) => {
  config.value = appConfigTypeMetadata[config.type].generate();
};

const notNull = (value: any) => !!value || "Required";
const noAppDuplicates = (value: any) => {
  const duplicates = pref.rwPreferences.config.apps.filter(
    (config) => config.key === value
  );
  return duplicates.length <= 1 || "Duplicate";
};
</script>

<template>
  <v-select
    v-model="selectedType"
    label="Type"
    multiple
    clearable
    :items="Object.values(appConfigTypeMetadata)"
    item-title="friendly"
    item-value="key"
  ></v-select>
  <v-text-field
    v-model="searchField"
    clearable
    hide-details="auto"
    label="Search"
  ></v-text-field>
  <v-list-item class="d-flex flex-column" min-height="10px"></v-list-item>
  <v-text-field
    v-model="searchFieldAppsList"
    clearable
    hide-details="auto"
    label="Search app list"
  ></v-text-field>
  <v-row>
    <v-col>
      <v-list-item class="d-flex flex-column" min-height="10px"></v-list-item>
      <v-divider />
    </v-col>
  </v-row>
  <PreferenceEditor>
    <v-row v-for="(config, index) in selectedConfigs()">
      <v-container class="pa-0 fill-height">
        <v-col cols="9" class="pb-0">
          <v-select
            v-model="config.key"
            validate-on="eager"
            :rules="[noAppDuplicates, notNull]"
            label="App ID"
            hide-details="auto"
            :items="filteredAppList()"
          ></v-select>
          <v-list-item
            class="d-flex flex-column"
            min-height="10px"
          ></v-list-item>
          <v-text-field
            v-model="config.value"
            validate-on="eager"
            clearable
            hide-details="auto"
            :rules="[notNull]"
            label="Replacement Value"
          ></v-text-field>
          <div v-if="selectedType.length !== 1">
            <v-list-item
              class="d-flex flex-column"
              min-height="10px"
            ></v-list-item>
            <v-select
              v-model="config.type"
              label="Type"
              hide-details="auto"
              :items="Object.values(appConfigTypeMetadata)"
              item-title="friendly"
              item-value="key"
            ></v-select>
          </div>
        </v-col>
        <v-col cols="3" class="pb-0">
          <v-btn @click="removeConfig(config)" style="height: 56px">X</v-btn>
          <div>
            <v-list-item
              class="d-flex flex-column"
              min-height="10px"
            ></v-list-item>
            <v-btn @click="regenerateConfigValue(config)" style="height: 56px">
              <v-icon :icon="mdiReload"> </v-icon>
            </v-btn>
          </div>
        </v-col>
        <v-list-item class="d-flex flex-column" min-height="10px"></v-list-item>
      </v-container>
    </v-row>
    <v-row>
      <v-col>
        <v-col class="text-right">
          <v-btn
            v-if="searchField === null"
            @click="addConfig()"
            style="height: 56px"
            >Add Config</v-btn
          >
        </v-col>
      </v-col>
    </v-row>
  </PreferenceEditor>
</template>
