import { buf2hex } from "../util/math";
import { IAppConfigTypeMetadata } from "./types";

export const appConfigTypeMetadata: IAppConfigTypeMetadata = Object.freeze({
  android_id: {
    key: "android_id",
    friendly: "Android ID",
    generate: () => {
      // need 8 bytes of random
      const rand = new Uint8Array(8);
      crypto.getRandomValues(rand);
      return buf2hex(rand.buffer);
    },
    validate: (value: string) => {
      // Android ID should be a 16-character hexadecimal string (8 bytes)
      return /^[0-9a-fA-F]{16}$/.test(value);
    },
  },
  drm_id: {
    key: "drm_id",
    friendly: "DRM ID",
    generate: () => {
      // need 32 bytes of random
      const rand = new Uint8Array(32);
      crypto.getRandomValues(rand);
      return buf2hex(rand.buffer);
    },
    validate: (value: string) => {
      // DRM ID should be a 64-character hexadecimal string (32 bytes)
      return /^[0-9a-fA-F]{64}$/.test(value);
    },
  },
  appset_id: {
    key: "appset_id",
    friendly: "Appset ID",
    generate: () => {
      return crypto.randomUUID();
    },
    validate: (value: string) => {
      // Appset ID should be a valid UUID format
      return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/i.test(
        value
      );
    },
  },
});

export enum AppConfigType {
  android_id = "android_id",
  drm_id = "drm_id",
  appset_id = "appset_id",
}
