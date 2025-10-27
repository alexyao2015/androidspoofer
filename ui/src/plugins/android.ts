import { AppConfigType } from "../util/app_config";
import { IAndroidInterface, IUniqueIds, IRWPreferences } from "../util/types";

let AndroidImpl: IAndroidInterface;

try {
  // grab from global variable
  AndroidImpl = Function(`"use strict";return Android`)();
} catch (_error) {
  console.log("Android interface not found");
  let RWPrefs: IRWPreferences;
  let appsListData: { [appName: string]: string };
  let uniqueIdsData: IUniqueIds;

  // @ts-ignore For testing only
  if (1 == 0) {
    // @ts-ignore For testing only
    RWPrefs = {};
    // @ts-ignore For testing only
    appsListData = {};
    // @ts-ignore For testing only
    uniqueIdsData = {};
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
    appsListData = {
      "My App 2": "my.app2.io",
      "My App": "my.app.io",
    };
    uniqueIdsData = {
      widevineId: "sample_widevine_id_value",
      playReadyId: "sample_playready_id_value",
      androidId: "sample_android_id_value",
      gsfId: "sample_gsf_id_value",
      appsetId: "sample_appset_id_value",
      adId: "sample_ad_id_value",
    };
  }

  AndroidImpl = {
    getAppsList: () => JSON.stringify(appsListData),
    getUniqueIds: () => JSON.stringify(uniqueIdsData),
    getRWPreferences: () => JSON.stringify(RWPrefs),
    setRWPreferences: (preferences: string) =>
      (RWPrefs = JSON.parse(preferences)),
    exportPreferences: () => {
      console.log("export preferences");
    },
    importPreferences: () => {
      console.log("import preferences");
    },
    getAppIcon: (packageName: string) => {
      console.log("getAppIcon called for:", packageName);
      return ""; // Return empty string for web testing
    },
  };
}

// Get apps list from Android
export const getAppsList = (): { [appName: string]: string } => {
  const appsList = JSON.parse(AndroidImpl.getAppsList()) as {
    [appName: string]: string;
  };
  return appsList || {};
};

// Get unique IDs from Android
export const getUniqueIds = (): IUniqueIds => {
  return JSON.parse(AndroidImpl.getUniqueIds()) as IUniqueIds;
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
  AndroidImpl.importPreferences();
};

export const getAppIcon = (packageName: string): string => {
  return AndroidImpl.getAppIcon(packageName);
};
