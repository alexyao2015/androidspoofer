<script setup lang="ts">
import { onMounted, onUnmounted, ref, Ref } from "vue";
import { mdiAndroid } from "@mdi/js";
import { getAppIcon } from "../plugins/android";

const props = defineProps<{
  appId: string;
  appName: string;
}>();

const iconDataUrl: Ref<string> = ref("");
const elementRef: Ref<HTMLElement | null> = ref(null);
let observer: IntersectionObserver | null = null;

const loadIcon = () => {
  try {
    const iconBase64 = getAppIcon(props.appId);
    if (iconBase64) {
      iconDataUrl.value = `data:image/webp;base64,${iconBase64}`;
    }
  } catch (e) {
    console.error(`Failed to get icon for ${props.appId}:`, e);
  }
};

onMounted(() => {
  if (!elementRef.value) return;

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          loadIcon();
          // Stop observing once loaded
          if (observer) {
            observer.disconnect();
            observer = null;
          }
        }
      });
    },
    {
      root: null,
      rootMargin: "100px", // Start loading 100px before visible
      threshold: 0.01,
    }
  );

  observer.observe(elementRef.value);
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
    observer = null;
  }
});
</script>

<template>
  <div ref="elementRef">
    <v-avatar v-if="iconDataUrl" size="40" class="mr-2">
      <v-img :src="iconDataUrl" :alt="appName"></v-img>
    </v-avatar>
    <v-avatar v-else size="40" class="mr-2" color="grey-darken-3">
      <v-icon :icon="mdiAndroid" size="small"></v-icon>
    </v-avatar>
  </div>
</template>
