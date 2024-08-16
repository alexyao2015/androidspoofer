package io.github.xposed.androidspoofer.xposed.handlers

import android.content.ContentResolver
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util


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
        util.log(tag, "Changed $replacementKey from $original -> $newValue")
    }
}
