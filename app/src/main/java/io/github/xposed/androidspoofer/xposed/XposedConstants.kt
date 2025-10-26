package io.github.xposed.androidspoofer.xposed

import android.content.ContentResolver
import android.media.MediaDrm
import android.provider.Settings
import com.google.android.gms.appset.AppSetIdInfo
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.xposed.handlers.AppsetIdReplacement
import io.github.xposed.androidspoofer.xposed.handlers.MediaDrmHook
import io.github.xposed.androidspoofer.xposed.handlers.SecureGetString
import io.github.xposed.androidspoofer.xposed.handlers.SecureGetStringForUser


object XposedConstants {
    object SecureSettings {
        val CLASS = Settings.Secure::class.java

        object FUN {
            const val getString = "getString"
            const val getStringForUser = "getStringForUser"
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

        fun hookGetStringForUser(lpparam: LoadPackageParam, replacementString: String, newValue: String) {
            XposedHelpers.findAndHookMethod(
                CLASS,
                FUN.getStringForUser,
                ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType,  // userId
                SecureGetStringForUser(lpparam, replacementString, newValue)
            )
        }
    }
    object MediaDrmHook {
        val CLASS = MediaDrm::class.java

        object FUN {
            const val getPropertyByteArray = "getPropertyByteArray"
        }

        fun hookGetPropertyByteArray(lpparam: LoadPackageParam, replacementString: String, newValue: String) {
            XposedHelpers.findAndHookMethod(
                CLASS,
                FUN.getPropertyByteArray,
                String::class.java,
                MediaDrmHook(lpparam, replacementString, newValue)
            )
        }
    }
    object AppsetIdHook {
        val CLASS = AppSetIdInfo::class.java

        object FUN {
            const val getId = "getId"
        }

        fun hookGetPropertyByteArray(lpparam: LoadPackageParam, newValue: String) {
            XposedHelpers.findAndHookMethod(
                CLASS,
                FUN.getId,
                AppsetIdReplacement(lpparam, newValue)
            )
        }
    }
}