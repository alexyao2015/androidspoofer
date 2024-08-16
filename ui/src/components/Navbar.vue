<script setup lang="ts">
import { mdiHome, mdiWrench, mdiTicket, mdiCog } from "@mdi/js";
</script>

<template>
  <v-bottom-navigation>
    <v-tabs>
      <v-tab to="/">
        <v-icon :icon="mdiHome"></v-icon>
      </v-tab>
      <v-tab to="/options">
        <v-icon :icon="mdiCog"></v-icon>
      </v-tab>
    </v-tabs>
  </v-bottom-navigation>
</template>
