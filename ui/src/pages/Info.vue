<script setup lang="ts">
import { mdiShieldKey } from "@mdi/js";
import { computed, onMounted, nextTick } from "vue";
import pref from "../plugins/store";
import { IUniqueIds } from "../util/types";

// Fetch unique IDs on mount
onMounted(async () => {
  // Allow component to render first so loading spinner shows
  await nextTick();

  if (pref.uniqueIds === null) {
    // First time: wait for the data
    await pref.fetchUniqueIds();
  } else {
    // Already have data: refresh in background without blocking UI
    void pref.fetchUniqueIds();
  }
});

// Use cached unique IDs from store
const uniqueIds = computed(() => {
  // Return empty strings while loading or if data is null
  if (pref.uniqueIds === null || pref.uniqueIdsLoading) {
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
        <!-- Loading spinner for first load -->
        <div v-if="pref.uniqueIdsLoading" class="text-center py-8">
          <v-progress-circular
            indeterminate
            color="primary"
            size="64"
          ></v-progress-circular>
          <p class="mt-4 text-medium-emphasis">Loading device IDs...</p>
        </div>

        <!-- Unique IDs content -->
        <v-row v-else>
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
