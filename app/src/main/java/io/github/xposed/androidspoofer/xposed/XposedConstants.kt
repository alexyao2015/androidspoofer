package io.github.xposed.androidspoofer.xposed

import android.content.ContentResolver
import android.provider.Settings
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.xposed.handlers.SecureGetString


object XposedConstants {
    object SecureSettings {
        val CLASS = Settings.Secure::class.java

        object FUN {
            const val getString = "getString"
        }

        fun hookGetString(lpparam: LoadPackageParam, replacementString: String, newValue: String) {
            XposedHelpers.findAndHookMethod(
                CLASS,
                FUN.getString,
                ContentResolver::class.java,
                String::class.java,
                SecureGetString(lpparam, replacementString, newValue)
            )
        }
    }

}