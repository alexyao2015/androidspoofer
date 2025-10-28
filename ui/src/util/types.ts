import { AppConfigType } from "./app_config";
import timezonesList from "timezones-list";

// Generate list of all valid timezones from timezones-list library
export const TIMEZONES = timezonesList
  .map((tz) => tz.tzCode)
  .sort() as readonly string[];

export type TimezoneType = (typeof TIMEZONES)[number];

export interface IAppConfigTypeMeta {
  key: string;
  friendly: string;
  generate: () => string;
  validate: (value: string) => boolean;
  selectItems?: readonly string[]; // Optional: if present, use dropdown with these items
}
export interface IAppConfigTypeMetadata {
  android_id: IAppConfigTypeMeta;
  drm_id: IAppConfigTypeMeta;
  appset_id: IAppConfigTypeMeta;
  timezone: IAppConfigTypeMeta;
  ip_address: IAppConfigTypeMeta;
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
    timezone?: TimezoneType;
    ip_address?: string;
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
  ipAddress: string;
  timeZone: string;
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
