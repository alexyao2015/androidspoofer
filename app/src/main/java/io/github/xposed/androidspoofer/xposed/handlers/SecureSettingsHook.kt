package io.github.xposed.androidspoofer.xposed.handlers

import android.content.ContentResolver
import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util

/**
 * Unified hook handler for Settings.Secure methods
 */
object SecureSettingsHook {
    private val TAG = this.javaClass.simpleName

    /**
     * Hooks Settings.Secure.getString to replace specific keys with custom values
     */
    fun hookGetString(lpparam: LoadPackageParam, replacementKey: String, newValue: String) {
        try {
            XposedHelpers.findAndHookMethod(
                Settings.Secure::class.java,
                "getString",
                ContentResolver::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val wantedKey = param.args[1] as String
                        // only change if we are checking for the specific key
                        if (wantedKey !== replacementKey) {
                            util.log(
                                TAG,
                                "${lpparam.packageName}: Skipped changing $wantedKey because it does not match $replacementKey"
                            )
                            return
                        }

                        val original = param.result
                        param.result = newValue
                        util.log(
                            TAG,
                            "${lpparam.packageName}: Changed $replacementKey from $original -> $newValue"
                        )
                    }
                }
            )
            util.log(TAG, "Successfully hooked Settings.Secure.getString for ${lpparam.packageName}")
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook Settings.Secure.getString: ${t.message}")
        }
    }

    /**
     * Hooks Settings.Secure.getStringForUser to replace specific keys with custom values
     */
    fun hookGetStringForUser(lpparam: LoadPackageParam, replacementKey: String, newValue: String) {
        try {
            XposedHelpers.findAndHookMethod(
                Settings.Secure::class.java,
                "getStringForUser",
                ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,  // userId
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val wantedKey = param.args[1] as String
                        // only change if we are checking for the specific key
                        if (wantedKey !== replacementKey) {
                            util.log(
                                TAG,
                                "${lpparam.packageName}: Skipped changing $wantedKey because it does not match $replacementKey"
                            )
                            return
                        }

                        val original = param.result
                        param.result = newValue
                        util.log(
                            TAG,
                            "${lpparam.packageName}: Changed $replacementKey from $original -> $newValue"
                        )
                    }
                }
            )
            util.log(TAG, "Successfully hooked Settings.Secure.getStringForUser for ${lpparam.packageName}")
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook Settings.Secure.getStringForUser: ${t.message}")
        }
    }
}

