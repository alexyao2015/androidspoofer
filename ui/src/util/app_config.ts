import { buf2hex } from "../util/math";
import { IAppConfigTypeMetadata } from "./types";

export const appConfigTypeMetadata: IAppConfigTypeMetadata = Object.freeze({
  android_id: {
    key: "android_id",
    friendly: "Android ID",
    generate: () => {
      // need 8 bytes of random
      const rand = new Uint32Array(2);
      crypto.getRandomValues(rand);
      return buf2hex(rand.buffer);
    },
  },
  test: {
    key: "test",
    friendly: "Test",
    generate: () => {
      return "test";
    },
  },
});

export enum AppConfigType {
  android_id = "android_id",
  test = "test",
}
