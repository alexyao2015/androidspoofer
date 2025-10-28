package io.github.xposed.androidspoofer.xposed

import android.media.MediaDrm
import android.provider.Settings
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.xposed.androidspoofer.BuildConfig
import io.github.xposed.androidspoofer.Constants
import io.github.xposed.androidspoofer.PreferencesManager
import io.github.xposed.androidspoofer.Utils
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util
import io.github.xposed.androidspoofer.xposed.handlers.AppSetIdHook
import io.github.xposed.androidspoofer.xposed.handlers.MediaDrmHook
import io.github.xposed.androidspoofer.xposed.handlers.SecureSettingsHook
import io.github.xposed.androidspoofer.xposed.handlers.TimeZoneHook

class XposedInit : IXposedHookLoadPackage {
    private val tag = "XposedInit"

    private val pref by lazy {
        XSharedPreferences(BuildConfig.APPLICATION_ID, Constants.SHARED_PREF_FILE_NAME).apply {
            util.log(tag, "Preference location: ${file.canonicalPath}")
        }
    }

    private val prefManager by lazy {
        PreferencesManager(pref)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        for (conf in prefManager.rw_config_apps) {
            if (!lpparam.packageName.equals(conf.key)) continue

            if (conf.type == Utils.ConfigAppsType.ANDROID_ID) {
                SecureSettingsHook.hookGetString(
                    lpparam,
                    Settings.Secure.ANDROID_ID,
                    conf.value
                )
                SecureSettingsHook.hookGetStringForUser(
                    lpparam,
                    Settings.Secure.ANDROID_ID,
                    conf.value
                )
                util.log(tag, "${conf.type} hooked in ${lpparam.packageName}")
            }
            if (conf.type == Utils.ConfigAppsType.DRM_ID) {
                MediaDrmHook.hookGetPropertyByteArray(
                    lpparam,
                    MediaDrm.PROPERTY_DEVICE_UNIQUE_ID,
                    conf.value
                )
                util.log(tag, "${conf.type} hooked in ${lpparam.packageName}")
            }
            if (conf.type == Utils.ConfigAppsType.APPSET_ID) {
                AppSetIdHook.hookBinderTransact(
                    lpparam,
                    conf.value
                )
                util.log(tag, "${conf.type} hooked in ${lpparam.packageName}")
            }
            if (conf.type == Utils.ConfigAppsType.TIMEZONE) {
                TimeZoneHook.hookGetDefault(
                    lpparam,
                    conf.value
                )
                TimeZoneHook.hookGetID(
                    lpparam,
                    conf.value
                )
                util.log(tag, "${conf.type} hooked in ${lpparam.packageName}")
            }
        }
    }
}