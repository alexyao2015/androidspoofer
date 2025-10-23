<script setup lang="ts">
import { mdiShieldKey } from "@mdi/js";
import pref from "../plugins/store";
</script>

<template>
  <v-container>
    <v-card class="mb-4">
      <v-card-title class="d-flex align-center">
        <v-icon :icon="mdiShieldKey" class="me-2"></v-icon>
        Device DRM IDs
      </v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12">
            <v-textarea label="Widevine ID" :model-value="pref.roPreferences.uniqueIds.widevineId" readonly
              variant="outlined" density="comfortable" class="mb-3" rows="1" auto-grow>
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea label="PlayReady ID" :model-value="pref.roPreferences.uniqueIds.playReadyId" readonly
              variant="outlined" density="comfortable" rows="1" auto-grow>
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea label="Android ID" :model-value="pref.roPreferences.uniqueIds.androidId" readonly
              variant="outlined" density="comfortable" rows="1" auto-grow>
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea label="GSF ID" :model-value="pref.roPreferences.uniqueIds.gsfId" readonly variant="outlined"
              density="comfortable" rows="1" auto-grow>
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea label="Appset ID" :model-value="pref.roPreferences.uniqueIds.appsetId" readonly
              variant="outlined" density="comfortable" rows="1" auto-grow>
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea label="Ad ID" :model-value="pref.roPreferences.uniqueIds.adId" readonly variant="outlined"
              density="comfortable" rows="1" auto-grow>
            </v-textarea>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-container>
</template>
