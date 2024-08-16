package io.github.xposed.androidspoofer

object Constants {
    val CONF_EXPORT_NAME = "spoofer_conf.json"

    val SHARED_PREF_FILE_NAME = "prefs"

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

    // ro prefs
    val PREF_JSON_RO = "ro"
    val PREF_JSON_RO_APPSLIST = "appsList"
}