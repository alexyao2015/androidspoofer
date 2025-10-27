import { defineStore } from "pinia";
import { IRWPreferences, IUniqueIds } from "../util/types";
import {
  getRWPreferences,
  setRWPreferences,
  getAppsList,
  getUniqueIds,
} from "./android";
import pinia from "./pinia";

interface PreferencesStore {
  rwPreferences: IRWPreferences;
  appsList: { [appName: string]: string } | null;
  uniqueIds: IUniqueIds | null;
}

const usePreferences = defineStore("page", {
  state: (): PreferencesStore => {
    return {
      rwPreferences: getRWPreferences(),
      appsList: null,
      uniqueIds: null,
    };
  },
  actions: {
    save() {
      setRWPreferences(this.rwPreferences);
    },
    reset() {
      this.rwPreferences = getRWPreferences();
    },
    fetchAppsList() {
      this.appsList = getAppsList();
    },
    fetchUniqueIds() {
      this.uniqueIds = getUniqueIds();
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
