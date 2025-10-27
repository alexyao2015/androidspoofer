package io.github.xposed.androidspoofer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaDrm
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.appset.AppSet
import com.google.android.gms.tasks.Tasks
import io.github.xposed.androidspoofer.Constants.PLAYREADY_UUID
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RO
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_TYPE_ANDROID_ID
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_TYPE_APPSET_ID
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_TYPE_DRM_ID
import io.github.xposed.androidspoofer.Constants.WIDEVINE_UUID
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

    /**
     * Get the Advertising ID from Google Play Services
     */
    fun getAdId(context: Context): String {
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            val adId = adInfo.id
            if (adId != null) {
                return adId
            }
        } catch (e: Exception) {
            return "exception"
        }
        return "null_id"
    }

    /**
     * Get the AppSet ID from Google Play Services
     */
    fun getAppsetId(context: Context): String {
        val client = AppSet.getClient(context)
        val task = client.appSetIdInfo

        val info = Tasks.await(task)
        return info.id
    }

    /**
     * Get the Google Services Framework ID
     */
    fun getGsfId(context: Context): String {
        fun hasPermission(context: Context, permission: String): Boolean {
            return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }

        val uri = "content://com.google.android.gsf.gservices".toUri()
        // Check permission
        if (!hasPermission(context, "com.google.android.providers.gsf.permission.READ_GSERVICES")) {
            return "no_permission"
        }
        // Query the GSF provider with "android_id" key
        val query = context.contentResolver.query(
            uri, null, null,
            arrayOf("android_id"),
            null
        )
        if (query!!.moveToFirst() && query!!.columnCount >= 2) {
            return java.lang.Long.toHexString(query!!.getString(1).toLong())
        }
        return "not_found"
    }

    fun getWidevineId(): String {
        // Get Widevine ID
        try {
            val widevineMediaDrm = MediaDrm(WIDEVINE_UUID)
            try {
                val widevine_id =
                    widevineMediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                return bytesToHex(widevine_id)
            } finally {
                widevineMediaDrm.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        return ""
    }

    fun getPlayreadyId(): String {
        try {
            val playreadyMediaDrm = MediaDrm(PLAYREADY_UUID)
            try {
                val playready_id =
                    playreadyMediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                return bytesToHex(playready_id)
            } finally {
                playreadyMediaDrm.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        return ""
    }

}