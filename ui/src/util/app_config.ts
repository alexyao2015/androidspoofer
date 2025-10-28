import { buf2hex } from "../util/math";
import { IAppConfigTypeMetadata, TIMEZONES } from "./types";

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
  timezone: {
    key: "timezone",
    friendly: "Timezone",
    generate: () => {
      return TIMEZONES[Math.floor(Math.random() * TIMEZONES.length)];
    },
    validate: (value: string) => {
      // Only accept timezones that are in the TIMEZONES array
      return TIMEZONES.includes(value as any);
    },
    selectItems: TIMEZONES,
  },
  ip_address: {
    key: "ip_address",
    friendly: "IP Address",
    generate: () => {
      // Generate a random private IP address from one of the three private ranges
      const range = Math.floor(Math.random() * 3);
      const octet3 = Math.floor(Math.random() * 256);
      const octet4 = Math.floor(Math.random() * 254) + 1; // Avoid .0

      if (range === 0) {
        // 10.x.x.x (10.0.0.0/8)
        const octet2 = Math.floor(Math.random() * 256);
        return `10.${octet2}.${octet3}.${octet4}`;
      } else if (range === 1) {
        // 172.16.x.x to 172.31.x.x (172.16.0.0/12)
        const octet2 = Math.floor(Math.random() * 16) + 16; // 16-31
        return `172.${octet2}.${octet3}.${octet4}`;
      } else {
        // 192.168.x.x (192.168.0.0/16)
        return `192.168.${octet3}.${octet4}`;
      }
    },
    validate: (value: string) => {
      // Validate IPv4 address format
      return /^((25[0-5]|(2[0-4]|1\d|[1-9]|)\d)\.?\b){4}$/.test(value);
    },
  },
});

export enum AppConfigType {
  android_id = "android_id",
  drm_id = "drm_id",
  appset_id = "appset_id",
  timezone = "timezone",
  ip_address = "ip_address",
}
