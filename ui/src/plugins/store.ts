import pinia from "./pinia";
import { defineStore } from "pinia";
import {
  RWPreferences,
  getRWPreferences,
  setRWPreferences,
  getROPreferences,
  ROPreferences,
} from "./android";

interface PreferencesStore {
  rwPreferences: RWPreferences;
  roPreferences: ROPreferences;
}

const usePreferences = defineStore("page", {
  state: (): PreferencesStore => {
    return {
      rwPreferences: getRWPreferences(),
      roPreferences: getROPreferences(),
    };
  },
  actions: {
    save() {
      setRWPreferences(this.rwPreferences);
    },
    reset() {
      this.rwPreferences = getRWPreferences();
      this.roPreferences = getROPreferences();
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
