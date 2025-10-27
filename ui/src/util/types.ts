import { AppConfigType } from "./app_config";

export interface IAppConfigTypeMeta {
  key: string;
  friendly: string;
  generate: () => string;
  validate: (value: string) => boolean;
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

// Unique IDs interface
export interface IUniqueIds {
  widevineId: string;
  playReadyId: string;
  androidId: string;
  gsfId: string;
  appsetId: string;
  adId: string;
}

// rw preferences
export interface IRWPreferences {
  appPref: IAppPreferences;
  config: IAppConfig;
}

export interface IAndroidInterface {
  getAppsList(): string;
  getUniqueIds(): string;
  getRWPreferences(): string;
  setRWPreferences(preferences: string): void;
  exportPreferences(): void;
  importPreferences(): void;
  getAppIcon(packageName: string): string;
}
