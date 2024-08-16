<script setup lang="ts">
import Navbar from "./components/Navbar.vue";
</script>

<template>
  <v-app>
    <v-main>
      <v-container fluid height="100%" class="pa-3">
        <Suspense>
          <router-view />
          <template #fallback>
            <v-progress-circular indeterminate color="primary" />
          </template>
        </Suspense>
      </v-container>
    </v-main>
    <Navbar />
  </v-app>
</template>
