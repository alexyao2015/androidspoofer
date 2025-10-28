package io.github.xposed.androidspoofer.xposed.handlers

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util
import java.util.TimeZone

/**
 * Unified hook handler for TimeZone methods
 */
object TimeZoneHook {
    private val TAG = this.javaClass.simpleName

    /**
     * Hooks TimeZone.getDefault to replace the system timezone with a custom value
     */
    fun hookGetDefault(lpparam: LoadPackageParam, newTimeZoneId: String) {
        try {
            XposedHelpers.findAndHookMethod(
                TimeZone::class.java,
                "getDefault",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val original = param.result as? TimeZone
                            val originalId = original?.id ?: "unknown"

                            // Create new TimeZone with the custom ID
                            val newTimeZone = TimeZone.getTimeZone(newTimeZoneId)
                            param.result = newTimeZone

                            util.log(
                                TAG,
                                "${lpparam.packageName}: Changed timezone from $originalId -> ${newTimeZone.id}"
                            )
                        } catch (e: Exception) {
                            util.log(TAG, "Error in afterHookedMethod: ${e.message}")
                        }
                    }
                }
            )
            util.log(TAG, "Successfully hooked TimeZone.getDefault for ${lpparam.packageName}")
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook TimeZone.getDefault: ${t.message}")
        }
    }

    /**
     * Hooks TimeZone.getID to replace the timezone ID with a custom value
     */
    fun hookGetID(lpparam: LoadPackageParam, newTimeZoneId: String) {
        try {
            XposedHelpers.findAndHookMethod(
                TimeZone::class.java,
                "getID",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val original = param.result as? String
                        param.result = newTimeZoneId

                        util.log(
                            TAG,
                            "${lpparam.packageName}: Changed timezone ID from $original -> $newTimeZoneId"
                        )
                    }
                }
            )
            util.log(TAG, "Successfully hooked TimeZone.getID for ${lpparam.packageName}")
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook TimeZone.getID: ${t.message}")
        }
    }
}

