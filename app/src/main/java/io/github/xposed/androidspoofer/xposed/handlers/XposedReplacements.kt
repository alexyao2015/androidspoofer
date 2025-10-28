package io.github.xposed.androidspoofer.xposed.handlers

import android.content.ContentResolver
import android.media.MediaDrm
import android.os.Binder
import android.os.Parcel
import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.xposed.androidspoofer.Utils.bytesToHex
import io.github.xposed.androidspoofer.Utils.hexToBytes
import io.github.xposed.androidspoofer.xposed.XposedUtils.Factory.util
import java.nio.charset.StandardCharsets
import java.util.Arrays

/**
 * Unified hook handler for Settings.Secure methods
 */
object SecureSettingsHook {
    /**
     * Hooks Settings.Secure.getString to replace specific keys with custom values
     */
    fun hookGetString(lpparam: LoadPackageParam, replacementKey: String, newValue: String) {
        XposedHelpers.findAndHookMethod(
            Settings.Secure::class.java,
            "getString",
            ContentResolver::class.java,
            String::class.java,
            object : XC_MethodHook() {
                private val tag = "SecureGetString"

                override fun afterHookedMethod(param: MethodHookParam) {
                    val wantedKey = param.args[1] as String
                    // only change if we are checking for the specific key
                    if (wantedKey !== replacementKey) {
                        util.log(
                            tag,
                            "${lpparam.packageName}: Skipped changing $wantedKey because it does not match $replacementKey"
                        )
                        return
                    }

                    val original = param.result
                    param.result = newValue
                    util.log(
                        tag,
                        "${lpparam.packageName}: Changed $replacementKey from $original -> $newValue"
                    )
                }
            }
        )
    }

    /**
     * Hooks Settings.Secure.getStringForUser to replace specific keys with custom values
     */
    fun hookGetStringForUser(lpparam: LoadPackageParam, replacementKey: String, newValue: String) {
        XposedHelpers.findAndHookMethod(
            Settings.Secure::class.java,
            "getStringForUser",
            ContentResolver::class.java,
            String::class.java,
            Int::class.javaPrimitiveType,  // userId
            object : XC_MethodHook() {
                private val tag = "SecureGetStringForUser"

                override fun afterHookedMethod(param: MethodHookParam) {
                    val wantedKey = param.args[1] as String
                    // only change if we are checking for the specific key
                    if (wantedKey !== replacementKey) {
                        util.log(
                            tag,
                            "${lpparam.packageName}: Skipped changing $wantedKey because it does not match $replacementKey"
                        )
                        return
                    }

                    val original = param.result
                    param.result = newValue
                    util.log(
                        tag,
                        "${lpparam.packageName}: Changed $replacementKey from $original -> $newValue"
                    )
                }
            }
        )
    }
}

/**
 * Unified hook handler for MediaDrm methods
 */
object MediaDrmHook {
    /**
     * Hooks MediaDrm.getPropertyByteArray to replace specific properties with custom values
     */
    fun hookGetPropertyByteArray(
        lpparam: LoadPackageParam,
        replacementKey: String,
        newValue: String
    ) {
        XposedHelpers.findAndHookMethod(
            MediaDrm::class.java,
            "getPropertyByteArray",
            String::class.java,
            object : XC_MethodHook() {
                private val tag = "MediaDrmHook"
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
                        tag,
                        "${lpparam.packageName}: Changed $replacementKey from $original -> $newValue"
                    )
                }
            }
        )
    }
}

/**
 * Unified hook handler for AppSetId via Binder interception
 * This hooks at the Binder level to intercept AppSetId service calls
 */
object AppSetIdHook {
    private const val TAG = "AppSetIdHook"

    /**
     * Hooks Binder.execTransactInternal to intercept and replace AppSetId at the service level
     * This is a more robust approach than hooking AppSetIdInfo.getId()
     */
    fun hookBinderTransact(lpparam: LoadPackageParam, newValue: String) {
        util.log(TAG, "Initializing AppSetId Binder hook for: ${lpparam.packageName}")

        try {
            val binderClass = XposedHelpers.findClass("android.os.Binder", lpparam.classLoader)

            XposedHelpers.findAndHookMethod(
                binderClass,
                "execTransactInternal",
                Int::class.javaPrimitiveType,  // code
                Parcel::class.java,  // data
                Parcel::class.java,  // reply
                Int::class.javaPrimitiveType,  // flags
                Int::class.javaPrimitiveType,  // callingUid
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val data = param.args[1] as Parcel
                            val thisObj = param.thisObject as Binder

                            // Get the interface descriptor to identify the service
                            val descriptor = thisObj.interfaceDescriptor

                            // Only process IAppSetIdCallback interface
                            if (descriptor != null &&
                                descriptor == "com.google.android.gms.appset.internal.IAppSetIdCallback"
                            ) {

                                val dataParcel = data
                                if (dataParcel.dataSize() == 260) {
                                    try {
                                        interceptAppSetId(dataParcel, newValue, lpparam)
                                    } catch (e: Exception) {
                                        util.log(TAG, "Failed to intercept AppSetId: ${e.message}")
                                    }
                                }
                            }
                        } catch (t: Throwable) {
                            // Silently fail to avoid crashing the app
                        }
                    }
                }
            )

            util.log(TAG, "Successfully hooked Binder.execTransact for ${lpparam.packageName}")
        } catch (t: Throwable) {
            util.log(TAG, "Failed to hook Binder.execTransact: ${t}")
        }
    }

    /**
     * Intercepts and replaces the AppSetId in the Parcel data
     * Based on the implementation from BinderInterceptorBase
     */
    private fun interceptAppSetId(dataParcel: Parcel, newId: String, lpparam: LoadPackageParam) {
        val originalPosition = dataParcel.dataPosition()

        try {
            // Marshall the entire parcel to byte array
            val bytes = dataParcel.marshall()

            // The AppSetId is at a specific offset in the byte array
            // startIndex = 88 characters * 2 bytes per UTF-16LE character = byte offset 176
            val startIndex = 88
            val length = 36  // UUID length (36 characters)
            val byteStartIndex = startIndex * 2
            val byteLength = length * 2

            // Extract the original AppSetId bytes
            val originalBytes =
                Arrays.copyOfRange(bytes, byteStartIndex, byteStartIndex + byteLength)
            val originalId = String(originalBytes, StandardCharsets.UTF_16LE)

            // Validate fake ID length
            if (newId.length != originalId.length) {
                util.log(TAG, "Fake ID length mismatch: ${newId.length} != ${originalId.length}")
                return
            }

            // Convert fake ID to bytes
            val fakeBytes = newId.toByteArray(StandardCharsets.UTF_16LE)

            if (fakeBytes.size != originalBytes.size) {
                util.log(TAG, "Byte size mismatch: ${fakeBytes.size} != ${originalBytes.size}")
                return
            }

            // Replace the bytes in the array
            val newBytes = bytes.clone()
            System.arraycopy(fakeBytes, 0, newBytes, byteStartIndex, fakeBytes.size)

            // Unmarshall the modified bytes back into the parcel
            dataParcel.unmarshall(newBytes, 0, newBytes.size)
            dataParcel.setDataPosition(0)

            util.log(
                TAG,
                "${lpparam.packageName}: Successfully replaced AppSetId: $originalId -> $newId"
            )

        } catch (e: Exception) {
            util.log(TAG, "Error during AppSetId interception: ${e.message}")
            // Restore original position on error
            dataParcel.setDataPosition(originalPosition)
        }
    }
}

