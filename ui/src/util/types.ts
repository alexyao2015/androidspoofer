import { AppConfigType } from "./app_config";

export interface IAppConfigTypeMeta {
  key: string;
  friendly: string;
  generate: () => string;
}
export interface IAppConfigTypeMetadata {
  android_id: IAppConfigTypeMeta;
  drm_id: IAppConfigTypeMeta;
  appset_id: IAppConfigTypeMeta;
}
export interface IAppsConfig {
  key: string;
  value: string;
  type: AppConfigType;
}

export interface IAppProfile {
  name: string;
  configs: {
    android_id?: string;
    drm_id?: string;
    appset_id?: string;
  };
}

export interface IAppConfig {
  apps: Array<IAppsConfig>;
  profiles?: { [appId: string]: IAppProfile[] };
}

export interface IAppPreferences {
  loggingEnabled: boolean;
}

// ro preferences
export interface IROPreferences {
  appsList: { [appName: string]: string };
  uniqueIds: {
    widevineId: string;
    playReadyId: string;
    androidId: string;
    gsfId: string;
    appsetId: string;
    adId: string;
  };
}

// rw preferences
export interface IRWPreferences {
  appPref: IAppPreferences;
  config: IAppConfig;
}

export interface IAndroidInterface {
  getROPreferences(): string;
  getRWPreferences(): string;
  setRWPreferences(preferences: string): void;
  exportPreferences(): void;
  importPreferences(): void;
}
