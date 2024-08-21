package io.github.xposed.androidspoofer

import android.content.SharedPreferences
import android.util.Log
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RO_APPSLIST
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_APPPREF
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_APPPREF_LOGGING_ENABLED
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_KEY
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_TYPE
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_VALUE
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PreferencesManager(private val pref: SharedPreferences) {
    private val tag = this::class.simpleName
    private val utils by lazy {
        Utils()
    }

    // used for some app specific things that shouldn't be modified by the user
    // this is currently the app list
    var ro: JSONObject
        get() {
            val savedPref = pref.getString(Constants.PREF_JSON_RO, null)
            return JSONObject(savedPref ?: "{}")
        }
        set(value) {
            pref.edit().apply {
                putString(Constants.PREF_JSON_RO, value.toString())
                apply()
            }
        }
    var ro_applist: JSONArray
        get() {
            try {
                return ro.getJSONArray(PREF_JSON_RO_APPSLIST)
            } catch (e: JSONException) {
                Log.e(tag, "Unable to retrieve ro_applist. ro: $ro")
                return JSONArray("[]")
            }
        }
        set(value) {
            ro = ro.put(PREF_JSON_RO_APPSLIST, value)
        }

    var rw: JSONObject
        get() {
            val savedPref = pref.getString(Constants.PREF_JSON_RW, null)
            return JSONObject(savedPref ?: "{}")
        }
        set(value) {
            pref.edit().apply {
                putString(Constants.PREF_JSON_RW, value.toString())
                apply()
            }
        }

    val rw_apppref: JSONObject
        get() {
            try {
                return rw.getJSONObject(PREF_JSON_RW_APPPREF)
            } catch (e: JSONException) {
                Log.e(tag, "Unable to retrieve rw_apppref. rw: $rw")
                return JSONObject("{}")
            }
        }

    val rw_apppref_logging_enabled: Boolean
        get() {
            try {
                return rw_apppref.getBoolean(PREF_JSON_RW_APPPREF_LOGGING_ENABLED)
            } catch (e: JSONException) {
                Log.e(tag, "Unable to retrieve rw_apppref_logging_enabled. rw_apppref: $rw_apppref")
                return false
            }
        }

    var rw_config: JSONObject
        get() {
            try {
                return rw.getJSONObject(PREF_JSON_RW_CONFIG)
            } catch (e: JSONException) {
                Log.e(tag, "Unable to retrieve rw_config. rw: $rw")
                return JSONObject("{}")
            }
        }
        set(value) {
            rw = rw.put(PREF_JSON_RW_CONFIG, value)
        }

    val rw_config_apps: List<Utils.AppConfig>
        get() {
            val savedPref: JSONArray
            try {
                savedPref = rw_config.getJSONArray(PREF_JSON_RW_CONFIG_APPS)
            } catch (e: JSONException) {
                Log.e(tag, "Unable to retrieve rw_config_apps. rw_config: $rw_config")
                return emptyList()
            }
            val confs = mutableListOf<Utils.AppConfig>()
            for (i in 0 until savedPref.length()) {
                val conf = savedPref.getJSONObject(i)
                confs.add(
                    Utils.AppConfig(
                        conf.getString(PREF_JSON_RW_CONFIG_APPS_KEY),
                        conf.getString(PREF_JSON_RW_CONFIG_APPS_VALUE),
                        utils.stringToAppsType(conf.getString(PREF_JSON_RW_CONFIG_APPS_TYPE))
                    )
                )
            }
            return confs
        }
}