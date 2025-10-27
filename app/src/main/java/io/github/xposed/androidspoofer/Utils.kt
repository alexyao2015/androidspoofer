package io.github.xposed.androidspoofer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RO
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_TYPE_ANDROID_ID
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_TYPE_APPSET_ID
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_TYPE_DRM_ID
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.util.HexFormat


/**
 * Utilities class for various functions.
 */
object Utils {
    /**
     * Write all keys of shared preference in a file as a JSON string.
     *
     * @param context Activity context
     * @param uri Uri of file to write to.
     * Using uri as it can be used to write a file in internal cache directory,
     * as well as an external location opened using [Intent.ACTION_CREATE_DOCUMENT].
     * @param pref SharedPreference instance.
     */
    fun writeConfigFile(context: Context, uri: Uri, pref: SharedPreferences) {
        val prefManager = PreferencesManager(pref)

        val outputStream = context.contentResolver.openOutputStream(uri)
        val writer = BufferedWriter(OutputStreamWriter(outputStream))

        val jsonObject = JSONObject()
        jsonObject.put(PREF_JSON_RW, prefManager.rw)
        jsonObject.put(PREF_JSON_RO, prefManager.ro)

        writer.run {
            write(jsonObject.toString(4))
            close()
        }
    }

    /**
     * Read an exported JSON file and stores entries in shared preference.
     *
     * @param context Activity context
     * @param uri Uri of file to read from.
     * @param pref SharedPreference instance.
     */
    fun readConfigFile(context: Context, uri: Uri, pref: SharedPreferences) {
        val prefManager = PreferencesManager(pref)
        var jsonObject = JSONObject()
        val baos = ByteArrayOutputStream()

        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            baos.use { output ->
                input.copyTo(output)
            }
            jsonObject = JSONObject(baos.toString())
        }

        prefManager.rw = jsonObject.getJSONObject(PREF_JSON_RW)
        prefManager.ro = jsonObject.getJSONObject(PREF_JSON_RO)
    }

    enum class ConfigAppsType {
        ANDROID_ID,
        DRM_ID,
        APPSET_ID
    }

    data class AppConfig(val key: String, val value: String, val type: ConfigAppsType)

    fun stringToAppsType(typeString: String): ConfigAppsType {
        if (typeString == PREF_JSON_RW_CONFIG_APPS_TYPE_ANDROID_ID) {
            return ConfigAppsType.ANDROID_ID
        }
        if (typeString == PREF_JSON_RW_CONFIG_APPS_TYPE_DRM_ID) {
            return ConfigAppsType.DRM_ID
        }
        if (typeString == PREF_JSON_RW_CONFIG_APPS_TYPE_APPSET_ID) {
            return ConfigAppsType.APPSET_ID
        }
        throw UnsupportedOperationException("Unable to decode app type")
    }

    fun bytesToHex(buf: ByteArray): String {
        return buf.joinToString("") { "%02x".format(it) }
    }

    fun hexToBytes(hex: String): ByteArray {
        return HexFormat.of().parseHex(hex)
    }

    /**
     * Convert a Drawable to a Bitmap
     */
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}