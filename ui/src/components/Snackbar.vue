<script setup lang="ts">
import { computed, withDefaults } from "vue";

const props = withDefaults(
  defineProps<{ color?: string; display: boolean; timeout: number }>(),
  { color: "accent" }
);
const emit = defineEmits<{ (e: "display", value: boolean): void }>();

const showBar = computed({
  get: () => props.display,
  set: (value) => {
    emit("display", value);
  },
});
</script>

<template>
  <v-snackbar
    v-model="showBar"
    close-on-content-click
    content-class="mb-12"
    :color="color"
    :timeout="timeout"
  >
    <slot></slot>
  </v-snackbar>
</template>

<style scoped lang="scss">
:deep(.v-snackbar__content) {
  text-align: center;
}
</style>
