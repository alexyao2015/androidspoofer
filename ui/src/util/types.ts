import { AppConfigType } from "./app_config";

export interface IAppConfigTypeMeta {
  key: string;
  friendly: string;
  generate: () => string;
}
export interface IAppConfigTypeMetadata {
  android_id: IAppConfigTypeMeta;
  test: IAppConfigTypeMeta;
}
export interface IAppsConfig {
  key: string;
  value: string;
  type: AppConfigType;
}

export interface IAppConfig {
  apps: Array<IAppsConfig>;
}

export interface IAppPreferences {
  loggingEnabled: boolean;
}

// ro preferences
export interface IROPreferences {
  appsList: Array<string>;
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
  importPreferences(): boolean;
}
