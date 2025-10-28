package io.github.xposed.androidspoofer.xposed.handlers

import android.media.MediaDrm
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.Utils.bytesToHex
import io.github.xposed.androidspoofer.Utils.hexToBytes
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util

/**
 * Unified hook handler for MediaDrm methods
 */
object MediaDrmHook {
    private val TAG = this.javaClass.simpleName

    /**
     * Hooks MediaDrm.getPropertyByteArray to replace specific properties with custom values
     */
    fun hookGetPropertyByteArray(
        lpparam: LoadPackageParam,
        replacementKey: String,
        newValue: String
    ) {
        try {
            XposedHelpers.findAndHookMethod(
                MediaDrm::class.java,
                "getPropertyByteArray",
                String::class.java,
                object : XC_MethodHook() {
                    private val replacementBytes: ByteArray = hexToBytes(newValue)

                    override fun afterHookedMethod(param: MethodHookParam) {
                        val wantedKey = param.args[0] as String
                        // only change if we are checking for the specific key
                        if (wantedKey !== replacementKey) return

                        // only change if not null
                        if (param.result == null) return

                        // convert new value to hex
                        val original = bytesToHex(param.result as ByteArray)

                        param.result = replacementBytes
                        util.log(
                            TAG,
                            "${lpparam.packageName}: Changed $replacementKey from $original -> $newValue"
                        )
                    }
                }
            )
            util.log(TAG, "Successfully hooked MediaDrm.getPropertyByteArray for ${lpparam.packageName}")
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook MediaDrm.getPropertyByteArray: ${t.message}")
        }
    }
}

