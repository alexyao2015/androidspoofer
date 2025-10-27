import { defineStore } from "pinia";
import { IRWPreferences } from "../util/types";
import { getRWPreferences, setRWPreferences, getAppsList } from "./android";
import pinia from "./pinia";

interface PreferencesStore {
  rwPreferences: IRWPreferences;
  appsList: { [appName: string]: string } | null;
  appsListLoading: boolean;
}

const usePreferences = defineStore("page", {
  state: (): PreferencesStore => {
    return {
      rwPreferences: getRWPreferences(),
      appsList: null,
      appsListLoading: false,
    };
  },
  actions: {
    save() {
      setRWPreferences(this.rwPreferences);
    },
    reset() {
      this.rwPreferences = getRWPreferences();
    },
    async fetchAppsList() {
      this.appsListLoading = true;
      try {
        this.appsList = await new Promise((resolve) => {
          // Execute the slow Android call asynchronously
          const result = getAppsList();
          resolve(result);
        });
      } finally {
        this.appsListLoading = false;
      }
    },
  },
});

const pref = usePreferences(pinia);
// if (!pref.initialized) {
//   pref.initialized = true;
//   pref.$subscribe((mutation, state) => {
//     setPreferences(state.preferences);
//   });
// }

export default pref;
