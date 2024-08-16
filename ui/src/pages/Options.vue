<script setup lang="ts">
import { mdiExportVariant, mdiImport } from "@mdi/js";
import { defineAsyncComponent } from "vue";
import pref from "../plugins/store";
import { exportPreferences, importPreferences } from "../plugins/android";
const PreferenceEditor = defineAsyncComponent(
  () => import("../components/PreferenceEditor.vue")
);
</script>

<template>
  <v-container>
    <PreferenceEditor>
      <v-row>
        <v-col>
          <v-checkbox
            hide-details
            v-model="pref.rwPreferences.appPref.loggingEnabled"
            label="Enable logging"
          >
          </v-checkbox>
        </v-col>
      </v-row>
    </PreferenceEditor>
    <v-list-item class="d-flex flex-column" min-height="20px"></v-list-item>
    <v-divider></v-divider>
    <v-list-item class="d-flex flex-column" min-height="20px"></v-list-item>

    <v-row>
      <v-col>
        <v-btn @click="exportPreferences()">
          <v-icon>{{ mdiExportVariant }}</v-icon>
          Export Settings
        </v-btn>
      </v-col>
      <v-col>
        <v-btn @click="importPreferences() && pref.reset()">
          <v-icon>{{ mdiImport }}</v-icon>
          Import Settings
        </v-btn>
      </v-col>
      <v-spacer></v-spacer>
      <v-spacer></v-spacer>
      <v-spacer></v-spacer>
      <v-spacer></v-spacer>
    </v-row>
  </v-container>
</template>
