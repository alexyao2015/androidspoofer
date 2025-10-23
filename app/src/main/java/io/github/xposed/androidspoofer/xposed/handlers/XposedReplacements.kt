package io.github.xposed.androidspoofer.xposed.handlers

import android.content.ContentResolver
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.Utils.bytesToHex
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util
import io.github.xposed.androidspoofer.Utils.hexToBytes

class SecureGetString(
    private val lpparam: LoadPackageParam,
    private val replacementKey: String,
    private val newValue: String,
) :
    XC_MethodHook() {

    private val tag = this::class.simpleName

    override fun afterHookedMethod(param: MethodHookParam) {
        val contextResolver = param.args[0] as ContentResolver
        val wantedKey = param.args[1] as String
        // only change if we are checking for the specific key
        if (wantedKey !== replacementKey) return

        val original = param.result
        param.result = newValue
        util.log(tag, "${lpparam.packageName} ${tag}: Changed $replacementKey from $original -> $newValue")
    }
}

class MediaDrmHook(
    private val lpparam: LoadPackageParam,
    private val replacementKey: String,
    private val newValue: String,
) :
    XC_MethodHook() {

    private val tag = this::class.simpleName
    private val replacementBytes: ByteArray = hexToBytes(newValue)

    override fun afterHookedMethod(param: MethodHookParam) {
        val wantedKey = param.args[0] as String
        // only change if we are checking for the specific key
        if (wantedKey !== replacementKey) return

        // convert new value to hex
        val original = bytesToHex(param.result as ByteArray)

        param.result = replacementBytes
        util.log(tag, "${lpparam.packageName} ${tag}: Changed $replacementKey from $original -> $newValue")
    }
}

class AppsetIdReplacement(
    private val lpparam: LoadPackageParam,
    private val newValue: String,
) :
    XC_MethodHook() {

    private val tag = this::class.simpleName

    override fun afterHookedMethod(param: MethodHookParam) {
        val original = param.result as String

        param.result = newValue
        util.log(tag, "${lpparam.packageName} ${tag}: Changed from $original -> $newValue")
    }
}

