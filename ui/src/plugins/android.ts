export enum AppConfigTypes {
  android_id = "android_id",
  any = "any",
}
export interface AppsConfig {
  key: string;
  value: string;
  type: AppConfigTypes;
}

export interface AppConfig {
  apps: Array<AppsConfig>;
}

export interface AppPreferences {
  loggingEnabled: boolean;
}

// ro preferences
export interface ROPreferences {
  appsList: Array<string>;
}

// rw preferences
export interface RWPreferences {
  appPref: AppPreferences;
  config: AppConfig;
}

interface AndroidInterface {
  getROPreferences(): string;
  getRWPreferences(): string;
  setRWPreferences(preferences: string): void;
  exportPreferences(): void;
  importPreferences(): boolean;
}

let AndroidImpl: AndroidInterface;

try {
  // grab from global variable
  AndroidImpl = Function(`"use strict";return Android`)();
} catch (_error) {
  console.log("Android interface not found");
  let RWPrefs: RWPreferences;
  let ROPrefs: ROPreferences;
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
            value: "my_setting_value",
            type: AppConfigTypes.android_id,
          },
        ],
      },
    };
    ROPrefs = {
      appsList: ["my.app2.io", "my.app.io"],
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
export const getROPreferences = (): ROPreferences => {
  const roPref = JSON.parse(AndroidImpl.getROPreferences()) as ROPreferences;
  if (roPref.appsList === undefined) {
    roPref.appsList = [];
  }
  roPref.appsList.sort();
  return roPref;
};

export const getRWPreferences = (): RWPreferences => {
  const rwPref = JSON.parse(AndroidImpl.getRWPreferences()) as RWPreferences;
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

export const setRWPreferences = (preferences: RWPreferences): void => {
  AndroidImpl.setRWPreferences(JSON.stringify(preferences));
};

export const exportPreferences = () => {
  AndroidImpl.exportPreferences();
};

export const importPreferences = () => {
  return AndroidImpl.importPreferences();
};
