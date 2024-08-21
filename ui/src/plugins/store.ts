import { defineStore } from "pinia";
import { IROPreferences, IRWPreferences } from "../util/types";
import {
  getROPreferences,
  getRWPreferences,
  setRWPreferences,
} from "./android";
import pinia from "./pinia";

interface PreferencesStore {
  rwPreferences: IRWPreferences;
  roPreferences: IROPreferences;
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
