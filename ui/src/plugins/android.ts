import { AppConfigType } from "../util/app_config";
import {
  IAndroidInterface,
  IROPreferences,
  IRWPreferences,
} from "../util/types";

let AndroidImpl: IAndroidInterface;

try {
  // grab from global variable
  AndroidImpl = Function(`"use strict";return Android`)();
} catch (_error) {
  console.log("Android interface not found");
  let RWPrefs: IRWPreferences;
  let ROPrefs: IROPreferences;
  // @ts-ignore For testing only
  if (1 == 0) {
    // @ts-ignore For testing only
    RWPrefs = {};
    // @ts-ignore For testing only
    ROPrefs = {};
  } else {
    RWPrefs = {
      appPref: {
        loggingEnabled: true,
      },
      config: {
        apps: [
          {
            key: "my.app.io",
            value: "android_setting_value",
            type: AppConfigType.android_id,
          },
          {
            key: "my.app.io",
            value: "drm_setting_value",
            type: AppConfigType.drm_id,
          },
        ],
      },
    };
    ROPrefs = {
      appsList: ["my.app2.io", "my.app.io"],
      uniqueIds: {
        widevineId: "sample_widevine_id_value",
        playReadyId: "sample_playready_id_value",
        androidId: "sample_android_id_value",
        gsfId: "sample_gsf_id_value",
        appsetId: "sample_appset_id_value",
        adId: "sample_ad_id_value",
      },
    };
  }

  AndroidImpl = {
    getROPreferences: () => JSON.stringify(ROPrefs),
    getRWPreferences: () => JSON.stringify(RWPrefs),
    setRWPreferences: (preferences: string) =>
      (RWPrefs = JSON.parse(preferences)),
    exportPreferences: () => {
      console.log("export preferences");
    },
    importPreferences: () => {
      console.log("import preferences");
      return true;
    },
  };
}

// one way read from app preferences
export const getROPreferences = (): IROPreferences => {
  const roPref = JSON.parse(AndroidImpl.getROPreferences()) as IROPreferences;
  if (roPref.appsList === undefined) {
    roPref.appsList = [];
  }
  roPref.appsList.sort();
  if (roPref.uniqueIds === undefined) {
    roPref.uniqueIds = {
      widevineId: "unknown",
      playReadyId: "unknown",
      androidId: "unknown",
      gsfId: "unknown",
      appsetId: "unknown",
      adId: "unknown",
    };
  }
  return roPref;
};

export const getRWPreferences = (): IRWPreferences => {
  const rwPref = JSON.parse(AndroidImpl.getRWPreferences()) as IRWPreferences;
  // bootstrap an initial config if it doesn't exist
  if (rwPref.appPref === undefined) {
    rwPref.appPref = {
      loggingEnabled: true,
    };
  }
  if (rwPref.config === undefined) {
    rwPref.config = {
      apps: [],
    };
  }
  return rwPref;
};

export const setRWPreferences = (preferences: IRWPreferences): void => {
  AndroidImpl.setRWPreferences(JSON.stringify(preferences));
};

export const exportPreferences = () => {
  AndroidImpl.exportPreferences();
};

export const importPreferences = () => {
  return AndroidImpl.importPreferences();
};
