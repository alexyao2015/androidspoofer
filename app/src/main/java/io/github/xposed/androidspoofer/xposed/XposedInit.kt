package io.github.xposed.androidspoofer.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.xposed.androidspoofer.BuildConfig
import io.github.xposed.androidspoofer.Constants
import io.github.xposed.androidspoofer.PreferencesManager
import io.github.xposed.androidspoofer.Utils
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util
import io.github.xposed.androidspoofer.xposed.handlers.AppSetIdHook
import io.github.xposed.androidspoofer.xposed.handlers.IpAddressHook
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

            when (conf.type) {
                Utils.ConfigAppsType.ANDROID_ID -> {
                    SecureSettingsHook.hook(lpparam, conf.value)
                }

                Utils.ConfigAppsType.DRM_ID -> {
                    MediaDrmHook.hook(lpparam, conf.value)
                }

                Utils.ConfigAppsType.APPSET_ID -> {
                    AppSetIdHook.hook(lpparam, conf.value)
                }

                Utils.ConfigAppsType.TIMEZONE -> {
                    TimeZoneHook.hook(lpparam, conf.value)
                }

                Utils.ConfigAppsType.IP_ADDRESS -> {
                    IpAddressHook.hook(lpparam, conf.value)
                }
            }
        }
    }
}