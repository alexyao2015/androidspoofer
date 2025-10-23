package io.github.xposed.androidspoofer

import java.util.UUID



object Constants {
    val CONF_EXPORT_NAME = "spoofer_conf.json"

    val SHARED_PREF_FILE_NAME = "prefs"

    var WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
    var PLAYREADY_UUID = UUID(-2129748144642739255L, 8654423357094679310L);

    // full spec in ui/src/plugins/android.ts
    val PREF_JSON_RW = "rw"
    val PREF_JSON_RW_APPPREF = "appPref"
    val PREF_JSON_RW_APPPREF_LOGGING_ENABLED = "loggingEnabled"

    val PREF_JSON_RW_CONFIG = "config"
    val PREF_JSON_RW_CONFIG_APPS = "apps"
    val PREF_JSON_RW_CONFIG_APPS_KEY = "key"
    val PREF_JSON_RW_CONFIG_APPS_VALUE = "value"
    val PREF_JSON_RW_CONFIG_APPS_TYPE = "type"

    val PREF_JSON_RW_CONFIG_APPS_TYPE_ANDROID_ID = "android_id"
    val PREF_JSON_RW_CONFIG_APPS_TYPE_DRM_ID = "drm_id"
    val PREF_JSON_RW_CONFIG_APPS_TYPE_APPSET_ID = "appset_id"

    // ro prefs
    val PREF_JSON_RO = "ro"
    val PREF_JSON_RO_APPSLIST = "appsList"
    val PREF_JSON_RO_UNIQUE_IDS = "uniqueIds"
    val PREF_JSON_RO_UNIQUE_IDS_WIDEVINE_DRM = "widevineId"
    val PREF_JSON_RO_UNIQUE_IDS_PLAYREADY_DRM = "playReadyId"
    val PREF_JSON_RO_UNIQUE_IDS_ANDROID_ID = "androidId"
    val PREF_JSON_RO_UNIQUE_IDS_GSF_ID = "gsfId"
    val PREF_JSON_RO_UNIQUE_IDS_APPSET_ID = "appsetId"
    val PREF_JSON_RO_UNIQUE_IDS_AD_ID = "adId"
}