<script setup lang="ts">
import { defineAsyncComponent, ref } from "vue";
import { AppConfigTypes } from "../plugins/android";
import pref from "../plugins/store";
const PreferenceEditor = defineAsyncComponent(
  () => import("../components/PreferenceEditor.vue")
);

const notNull = (value: any) => !!value || "Required";
const TypeFilter = Object.values(AppConfigTypes);
const selectedType = ref(TypeFilter[0]);
const searchField = ref("");
const searchFieldAppsList = ref("");

const selectedConfigs = () => {
  let app_configs = pref.rwPreferences.config.apps;
  if (selectedType.value !== AppConfigTypes.any) {
    app_configs = app_configs.filter(
      (config) => config.type === selectedType.value
    );
  }
  if (searchField.value !== null) {
    app_configs = app_configs.filter(
      (config) =>
        config.key.includes(searchField.value) ||
        config.value.includes(searchField.value) ||
        config.value === ""
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
    return app.includes(searchFieldAppsList.value);
  });
};

const addConfig = () => {
  const selected_configs = selectedConfigs();
  if (selected_configs.length === 0) {
    const new_type =
      selectedType.value !== AppConfigTypes.any
        ? selectedType.value
        : AppConfigTypes.android_id;
    pref.rwPreferences.config.apps.push({
      key: "",
      value: "",
      type: new_type,
    });
    return;
  }
  const last_config = selected_configs[selected_configs.length - 1];
  pref.rwPreferences.config.apps.push({ ...last_config });
};

const removeConfig = (index: number) => {
  const selected_configs = selectedConfigs();
  const remove_config = selected_configs[index];
  const idx_to_remove = pref.rwPreferences.config.apps.indexOf(remove_config);
  pref.rwPreferences.config.apps.splice(idx_to_remove, 1);
};
</script>

<template>
  <PreferenceEditor>
    <v-select
      v-model="selectedType"
      label="Type"
      :items="TypeFilter"
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
    <v-row v-for="(config, index) in selectedConfigs()">
      <v-container class="pa-0 fill-height">
        <v-col cols="9" class="pb-0">
          <!-- <v-text-field
            v-model="config.key"
            :rules="[notNull]"
            clearable
            hide-details="auto"
            label="App ID"
          ></v-text-field> -->
          <v-select
            v-model="config.key"
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
            clearable
            hide-details="auto"
            label="Replacement Value"
          ></v-text-field>
          <div v-if="selectedType === AppConfigTypes.any">
            <v-list-item
              class="d-flex flex-column"
              min-height="10px"
            ></v-list-item>
            <v-select
              v-model="config.type"
              label="Type"
              hide-details="auto"
              :items="TypeFilter.filter((type) => type !== AppConfigTypes.any) as Array<AppConfigTypes>"
            ></v-select>
          </div>
        </v-col>
        <v-col cols="3">
          <v-btn @click="removeConfig(index)" style="height: 56px">X</v-btn>
        </v-col>
        <v-list-item class="d-flex flex-column" min-height="10px"></v-list-item>
      </v-container>

      <!-- <v-row>
        <v-col>
          <v-list-item
            class="d-flex flex-column"
            min-height="10px"
          ></v-list-item>
          <v-divider />
          <v-list-item
            class="d-flex flex-column"
            min-height="20px"
          ></v-list-item>
        </v-col>
      </v-row> -->
    </v-row>
    <v-row>
      <v-col>
        <v-col class="text-right">
          <v-btn @click="addConfig()" style="height: 56px">Add Config</v-btn>
        </v-col>
      </v-col>
    </v-row>
  </PreferenceEditor>
</template>
