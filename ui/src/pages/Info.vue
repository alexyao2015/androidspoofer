<script setup lang="ts">
import { mdiShieldKey } from "@mdi/js";
import { computed } from "vue";
import pref from "../plugins/store";
import { IUniqueIds } from "../util/types";

// Use cached unique IDs from store
const uniqueIds = computed(() => {
  // Fetch data if not already loaded
  if (pref.uniqueIds === null) {
    pref.fetchUniqueIds();
  }

  // Return empty strings if data is null
  if (pref.uniqueIds === null) {
    return {
      widevineId: "",
      playReadyId: "",
      androidId: "",
      gsfId: "",
      appsetId: "",
      adId: "",
    };
  }
  return pref.uniqueIds as IUniqueIds;
});
</script>

<template>
  <v-container>
    <v-card class="mb-4">
      <v-card-title class="d-flex align-center">
        <v-icon :icon="mdiShieldKey" class="me-2"></v-icon>
        Device IDs
      </v-card-title>
      <v-card-text>
        <!-- Unique IDs content -->
        <v-row>
          <v-col cols="12">
            <v-textarea
              label="Widevine ID"
              :model-value="uniqueIds.widevineId"
              readonly
              variant="outlined"
              density="comfortable"
              class="mb-3"
              rows="1"
              auto-grow
            >
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea
              label="PlayReady ID"
              :model-value="uniqueIds.playReadyId"
              readonly
              variant="outlined"
              density="comfortable"
              rows="1"
              auto-grow
            >
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea
              label="Android ID"
              :model-value="uniqueIds.androidId"
              readonly
              variant="outlined"
              density="comfortable"
              rows="1"
              auto-grow
            >
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea
              label="GSF ID"
              :model-value="uniqueIds.gsfId"
              readonly
              variant="outlined"
              density="comfortable"
              rows="1"
              auto-grow
            >
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea
              label="Appset ID"
              :model-value="uniqueIds.appsetId"
              readonly
              variant="outlined"
              density="comfortable"
              rows="1"
              auto-grow
            >
            </v-textarea>
          </v-col>
          <v-col cols="12">
            <v-textarea
              label="Ad ID"
              :model-value="uniqueIds.adId"
              readonly
              variant="outlined"
              density="comfortable"
              rows="1"
              auto-grow
            >
            </v-textarea>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-container>
</template>
