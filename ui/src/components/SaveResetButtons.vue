<script setup lang="ts">
import { defineAsyncComponent, ref } from "vue";
import pref from "../plugins/store";

const Snackbar = defineAsyncComponent(
    () => import("../components/Snackbar.vue")
);

const snackbarMessage = ref("");
const showSnackbar = ref(false);
const snackbarColor = ref("");

const save = () => {
    pref.save();
    snackbarColor.value = "success";
    snackbarMessage.value = "Preferences saved!";
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
    <div>
        <v-list-item class="d-flex flex-column" min-height="20px"></v-list-item>
        <v-divider />
        <v-list-item class="d-flex flex-column" min-height="20px"></v-list-item>

        <v-row>
            <v-col>
                <v-btn @click="save()" class="mr-4">Save</v-btn>
                <v-btn @click="reset()">Reset</v-btn>
            </v-col>
        </v-row>

        <Snackbar class="pb-3" @display="(val) => (showSnackbar = val)" :color="snackbarColor" :display="showSnackbar"
            :timeout="3000">{{ snackbarMessage }}</Snackbar>
    </div>
</template>
