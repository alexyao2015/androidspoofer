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
  },
  appset_id: {
    key: "appset_id",
    friendly: "Appset ID",
    generate: () => {
      return crypto.randomUUID();
    },
  },
});

export enum AppConfigType {
  android_id = "android_id",
  drm_id = "drm_id",
  appset_id = "appset_id",
}
