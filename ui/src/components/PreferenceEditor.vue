<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, Ref } from "vue";
import { onBeforeRouteLeave } from "vue-router";
import pref from "../plugins/store";
const Snackbar = defineAsyncComponent(
  () => import("../components/Snackbar.vue")
);

const form: Ref<HTMLFormElement | null> = ref(null);
const valid = ref(true);

onMounted(() => {
  form.value?.validate();
});

onBeforeRouteLeave((_to, _from, next) => {
  pref.reset();
  next();
});

const snackbarMessage = ref("");
const showSnackbar = ref(false);
const snackbarColor = ref("");
const save = () => {
  if (form.value?.validate()) {
    pref.save();
    snackbarColor.value = "success";
    snackbarMessage.value = "Preferences saved!";
  } else {
    snackbarColor.value = "error";
    snackbarMessage.value = "Some fields are not valid";
  }
  showSnackbar.value = true;
};

const reset = () => {
  pref.reset();
  snackbarColor.value = "success";
  snackbarMessage.value = "Preferences reset!";
  showSnackbar.value = true;
};
</script>

<template>
  <v-form ref="form" v-model="valid" lazy-validation>
    <slot></slot>
  </v-form>

  <v-list-item class="d-flex flex-column" min-height="20px"></v-list-item>
  <v-divider />
  <v-list-item class="d-flex flex-column" min-height="20px"></v-list-item>

  <v-row>
    <v-col>
      <v-btn :disabled="!valid" @click="save()" class="mr-4">Save</v-btn>
      <v-btn @click="reset()">Reset</v-btn>
    </v-col>
  </v-row>
  <Snackbar
    class="pb-3"
    @display="(val) => (showSnackbar = val)"
    :color="snackbarColor"
    :display="showSnackbar"
    :timeout="3000"
    >{{ snackbarMessage }}</Snackbar
  >
</template>
