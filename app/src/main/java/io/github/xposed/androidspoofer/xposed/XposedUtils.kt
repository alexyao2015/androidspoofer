package io.github.xposed.androidspoofer.xposed

import android.util.Log
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import io.github.xposed.androidspoofer.BuildConfig
import io.github.xposed.androidspoofer.Constants
import io.github.xposed.androidspoofer.PreferencesManager

class XposedUtils(private val loggingEnabled: Boolean) {
    companion object Factory {
        val util: XposedUtils by lazy {
            val pref = XSharedPreferences(
                BuildConfig.APPLICATION_ID,
                Constants.SHARED_PREF_FILE_NAME
            )
            val prefManager = PreferencesManager(pref)
            val loggingEnabled = prefManager.rw_apppref_logging_enabled
            XposedUtils(loggingEnabled)
        }
    }

    fun log(tag: String?, message: String) {
        if (!loggingEnabled) return

        XposedBridge.log("${tag ?: "MissingTag"}: $message")
        Log.d(tag, message)
    }
}
