<script setup lang="ts">
import { mdiContentSave, mdiDelete, mdiDownload } from "@mdi/js";
import { computed, ref } from "vue";
import pref from "../plugins/store";
import { AppConfigType } from "../util/app_config";
import { IAppProfile, IAppsConfig } from "../util/types";

// Define props
const props = defineProps<{
    appId: string;
}>();

// Define emits
const emit = defineEmits<{
    profileLoaded: [];
}>();

// Initialize profiles structure if it doesn't exist
if (!pref.rwPreferences.config.profiles) {
    pref.rwPreferences.config.profiles = {};
}

// Get profiles for current app
const appProfiles = computed(() => {
    return pref.rwPreferences.config.profiles?.[props.appId] || [];
});

// UI state
const showSaveDialog = ref(false);
const newProfileName = ref("");
const selectedProfile = ref<string | null>(null);

// Generate default profile name with date and time
const generateDefaultProfileName = () => {
    const now = new Date();
    return now.toISOString().slice(0, 19).replace('T', ' ');
};

// Open save dialog with default name
const openSaveDialog = () => {
    newProfileName.value = generateDefaultProfileName();
    showSaveDialog.value = true;
};

// Get current config values
const getCurrentConfigs = () => {
    const configs: { [key: string]: string } = {};

    Object.values(AppConfigType).forEach(type => {
        const config = pref.rwPreferences.config.apps.find(
            (c: IAppsConfig) => c.key === props.appId && c.type === type
        );
        if (config) {
            configs[type] = config.value;
        }
    });

    return configs;
};

// Save current config as a profile
const saveProfile = () => {
    if (!newProfileName.value.trim()) return;

    const currentConfigs = getCurrentConfigs();

    const newProfile: IAppProfile = {
        name: newProfileName.value.trim(),
        configs: {
            android_id: currentConfigs.android_id,
            drm_id: currentConfigs.drm_id,
            appset_id: currentConfigs.appset_id,
        }
    };

    if (!pref.rwPreferences.config.profiles) {
        pref.rwPreferences.config.profiles = {};
    }

    if (!pref.rwPreferences.config.profiles[props.appId]) {
        pref.rwPreferences.config.profiles[props.appId] = [];
    }

    pref.rwPreferences.config.profiles[props.appId].push(newProfile);

    // Reset dialog
    newProfileName.value = "";
    showSaveDialog.value = false;
};

// Load a profile
const loadProfile = (profile: IAppProfile) => {
    // Clear existing configs for this app
    pref.rwPreferences.config.apps = pref.rwPreferences.config.apps.filter(
        (c: IAppsConfig) => c.key !== props.appId
    );

    // Add configs from profile
    Object.entries(profile.configs).forEach(([type, value]) => {
        if (value) {
            pref.rwPreferences.config.apps.push({
                key: props.appId,
                type: type as AppConfigType,
                value: value
            });
        }
    });

    selectedProfile.value = profile.name;
    emit("profileLoaded");
};

// Delete a profile
const deleteProfile = (profile: IAppProfile) => {
    if (!pref.rwPreferences.config.profiles?.[props.appId]) return;

    const index = pref.rwPreferences.config.profiles[props.appId].indexOf(profile);
    if (index > -1) {
        pref.rwPreferences.config.profiles[props.appId].splice(index, 1);
    }

    if (selectedProfile.value === profile.name) {
        selectedProfile.value = null;
    }
};
</script>

<template>
    <div>
        <v-row>
            <v-col>
                <h3 class="text-h6 mb-2">Profiles</h3>
                <p class="text-caption mb-3">Save and load different configuration sets</p>
            </v-col>
        </v-row>

        <v-row v-if="appProfiles.length > 0">
            <v-col>
                <v-list class="mb-3">
                    <v-list-item v-for="profile in appProfiles" :key="profile.name" class="mb-1"
                        style="border: 1px solid rgba(255, 255, 255, 0.12); border-radius: 4px;">
                        <v-list-item-title>{{ profile.name }}</v-list-item-title>
                        <v-list-item-subtitle>
                            {{Object.keys(profile.configs).filter(k => profile.configs[k as keyof typeof
                                profile.configs]).length}} config(s)
                        </v-list-item-subtitle>

                        <template v-slot:append>
                            <v-btn @click="loadProfile(profile)" :prepend-icon="mdiDownload" size="small" variant="text"
                                class="mr-2">
                                Load
                            </v-btn>
                            <v-btn @click="deleteProfile(profile)" :icon="mdiDelete" size="small" variant="text"
                                color="error">
                            </v-btn>
                        </template>
                    </v-list-item>
                </v-list>
            </v-col>
        </v-row>

        <v-row v-else>
            <v-col>
                <v-alert type="info" variant="tonal" class="mb-3">
                    No profiles saved yet. Save your current configuration as a profile.
                </v-alert>
            </v-col>
        </v-row>

        <v-row>
            <v-col>
                <v-btn @click="openSaveDialog" :prepend-icon="mdiContentSave" color="primary">
                    Save Current as Profile
                </v-btn>
            </v-col>
        </v-row>

        <!-- Save Profile Dialog -->
        <v-dialog v-model="showSaveDialog" max-width="500px">
            <v-card>
                <v-card-title>Save Profile</v-card-title>
                <v-card-text>
                    <v-text-field v-model="newProfileName" label="Profile Name" autofocus
                        @keyup.enter="saveProfile"></v-text-field>
                </v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn @click="showSaveDialog = false">Cancel</v-btn>
                    <v-btn @click="saveProfile" color="primary" :disabled="!newProfileName.trim()">
                        Save
                    </v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </div>
</template>
