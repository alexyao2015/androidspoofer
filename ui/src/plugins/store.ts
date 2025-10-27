import { defineStore } from "pinia";
import { IRWPreferences, IUniqueIds } from "../util/types";
import {
  getRWPreferences,
  setRWPreferences,
  getAppsList,
  getUniqueIds,
} from "./android";
import pinia from "./pinia";
import { wrapInPromise } from "../util/async";

// Sentinel value to indicate loading state
const LOADING = Symbol("loading");

interface PreferencesStore {
  rwPreferences: IRWPreferences;
  appsList: { [appName: string]: string } | null | typeof LOADING;
  uniqueIds: IUniqueIds | null | typeof LOADING;
}

// Track ongoing fetches to prevent concurrent requests
let appsListPromise: Promise<void> | null = null;
let uniqueIdsPromise: Promise<void> | null = null;

const usePreferences = defineStore("page", {
  state: (): PreferencesStore => {
    return {
      rwPreferences: getRWPreferences(),
      appsList: null,
      uniqueIds: null,
    };
  },
  getters: {
    appsListLoading: (state) => state.appsList === LOADING,
    uniqueIdsLoading: (state) => state.uniqueIds === LOADING,
  },
  actions: {
    save() {
      setRWPreferences(this.rwPreferences);
    },
    reset() {
      this.rwPreferences = getRWPreferences();
    },
    async fetchAppsList() {
      // Don't fetch if already loading
      if (appsListPromise) {
        return appsListPromise;
      }

      // Only set to LOADING if we don't have data yet (first load)
      const isFirstLoad = this.appsList === null;
      if (isFirstLoad) {
        this.appsList = LOADING;
      }

      appsListPromise = wrapInPromise(getAppsList)
        .then((result) => {
          this.appsList = result;
        })
        .finally(() => {
          appsListPromise = null;
        });

      return appsListPromise;
    },
    async fetchUniqueIds() {
      // Don't fetch if already loading
      if (uniqueIdsPromise) {
        return uniqueIdsPromise;
      }

      // Only set to LOADING if we don't have data yet (first load)
      const isFirstLoad = this.uniqueIds === null;
      if (isFirstLoad) {
        this.uniqueIds = LOADING;
      }

      uniqueIdsPromise = wrapInPromise(getUniqueIds)
        .then((result) => {
          this.uniqueIds = result;
        })
        .finally(() => {
          uniqueIdsPromise = null;
        });

      return uniqueIdsPromise;
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
