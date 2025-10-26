<script setup lang="ts">
import Navbar from "./components/Navbar.vue";

const isCheckFlavor = __APP_FLAVOR__ === "check";
</script>

<template>
  <v-app>
    <v-main>
      <v-container fluid height="100%" class="pa-3 pt-14">
        <Suspense>
          <router-view />
          <template #fallback>
            <v-progress-circular indeterminate color="primary" />
          </template>
        </Suspense>
      </v-container>
    </v-main>
    <Navbar v-if="!isCheckFlavor" />
  </v-app>
</template>
