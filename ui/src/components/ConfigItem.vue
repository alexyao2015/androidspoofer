<script setup lang="ts">
import { mdiReload, mdiPlus, mdiClose } from "@mdi/js";
import { AppConfigType, appConfigTypeMetadata } from "../util/app_config";
import { IAppsConfig } from "../util/types";

// Define props
const props = defineProps<{
    type: AppConfigType;
    config: IAppsConfig | null;
}>();

// Define emits
const emit = defineEmits<{
    add: [];
    clear: [];
    regenerate: [];
}>();

const notNull = (value: any) => !!value || "Required";

const handleAdd = () => {
    emit("add");
};

const handleClear = () => {
    emit("clear");
};

const handleRegenerate = () => {
    emit("regenerate");
};
</script>

<template>
    <v-row class="mb-2">
        <v-col cols="7">
            <v-text-field v-if="config" v-model="config.value" :label="appConfigTypeMetadata[type].friendly"
                validate-on="eager" clearable hide-details="auto" :rules="[notNull]" variant="filled">
            </v-text-field>
            <v-text-field v-else :label="appConfigTypeMetadata[type].friendly" :model-value="''"
                placeholder="Not configured" disabled hide-details="auto" variant="outlined">
            </v-text-field>
        </v-col>
        <v-col cols="4">
            <!-- If config exists: show clear and regenerate buttons side by side -->
            <template v-if="config">
                <div class="d-flex ga-1">
                    <v-btn @click="handleClear" color="warning" style="height: 56px; min-width: 48px; flex: 1"
                        title="Clear configuration" size="small">
                        <v-icon :icon="mdiClose"></v-icon>
                    </v-btn>
                    <v-btn @click="handleRegenerate" style="height: 56px; min-width: 48px; flex: 1"
                        title="Regenerate value" size="small">
                        <v-icon :icon="mdiReload"></v-icon>
                    </v-btn>
                </div>
            </template>
            <!-- If config doesn't exist: show add button -->
            <template v-else>
                <v-btn @click="handleAdd" color="primary" style="height: 56px; min-width: 48px; width: 100%"
                    title="Add configuration" size="small">
                    <v-icon :icon="mdiPlus"></v-icon>
                </v-btn>
            </template>
        </v-col>
    </v-row>
</template>
