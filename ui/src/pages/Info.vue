<script setup lang="ts">
import { mdiShieldKey } from "@mdi/js";
import { defineAsyncComponent } from "vue";
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
            <v-text-field label="Widevine ID" :model-value="pref.roPreferences.mediaDrmUniqueId.widevineId" readonly
              variant="outlined" density="comfortable" class="mb-3">
            </v-text-field>
          </v-col>
          <v-col cols="12">
            <v-text-field label="PlayReady ID" :model-value="pref.roPreferences.mediaDrmUniqueId.playReadyId" readonly
              variant="outlined" density="comfortable">
            </v-text-field>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-container>
</template>
