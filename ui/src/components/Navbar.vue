<script setup lang="ts">
import { mdiHome, mdiWrench, mdiTicket, mdiCog, mdiInformation } from "@mdi/js";
</script>

<template>
  <v-bottom-navigation height="75">
    <v-tabs>
      <v-tab to="/">
        <v-icon :icon="mdiHome"></v-icon>
      </v-tab>
      <v-tab to="/info">
        <v-icon :icon="mdiInformation"></v-icon>
      </v-tab>
      <v-tab to="/options">
        <v-icon :icon="mdiCog"></v-icon>
      </v-tab>
    </v-tabs>
  </v-bottom-navigation>
</template>
