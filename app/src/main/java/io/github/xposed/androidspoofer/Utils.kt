package io.github.xposed.androidspoofer

import android.Manifest
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
import android.net.wifi.WifiManager
import android.text.format.Formatter
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
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
import io.github.xposed.androidspoofer.Constants.PREF_JSON_RW_CONFIG_APPS_TYPE_TIMEZONE
import io.github.xposed.androidspoofer.Constants.WIDEVINE_UUID
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.HexFormat
import java.util.TimeZone


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
        APPSET_ID,
        TIMEZONE
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
        if (typeString == PREF_JSON_RW_CONFIG_APPS_TYPE_TIMEZONE) {
            return ConfigAppsType.TIMEZONE
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
            createBitmap(1, 1)
        } else {
            createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
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
        } catch (_: Exception) {
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
        val query = context.contentResolver.query(
            uri, null, null,
            arrayOf("android_id"),
            null
        )
        try {
            // Query the GSF provider with "android_id" key
            if (query!!.moveToFirst() && query.columnCount >= 2) {
                return java.lang.Long.toHexString(query.getString(1).toLong())
            }
        } finally {
            query?.close()
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

    /**
     * Get all device IP addresses
     * Collects both WiFi and network interface IPs
     *
     * @param context Application context
     * @return All IP addresses separated by newlines, or empty string if none available
     */
    @Suppress("DEPRECATION")
    fun getIpAddress(context: Context): String {
        val ipAddresses = mutableListOf<String>()

        try {
            // Try to get WiFi IP address (requires location permission)
            if (hasLocationPermission(context)) {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifiManager?.connectionInfo?.ipAddress?.let { ipInt ->
                    if (ipInt != 0) {
                        val wifiIp = Formatter.formatIpAddress(ipInt)
                        if (wifiIp.isNotEmpty()) {
                            ipAddresses.add(wifiIp)
                        }
                    }
                }
            }

            // Get all network interface IP addresses
            NetworkInterface.getNetworkInterfaces()?.toList()?.forEach { networkInterface ->
                networkInterface.inetAddresses?.toList()?.forEach { inetAddress ->
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        inetAddress.hostAddress?.let { address ->
                            // Avoid duplicates
                            if (!ipAddresses.contains(address)) {
                                ipAddresses.add(address)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ipAddresses.joinToString("\n")
    }

    /**
     * Get the system's default timezone
     *
     * @return Timezone ID as a string (e.g., "America/New_York", "Europe/London")
     */
    fun getSystemTimeZone(): String {
        return try {
            TimeZone.getDefault().id
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Check if location permission is granted
     *
     * @param context Application context
     * @return true if either FINE or COARSE location permission is granted
     */
    fun hasLocationPermission(context: Context): Boolean {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasFineLocation || hasCoarseLocation
    }

    /**
     * Request location permission if not already granted
     *
     * @param context Application context
     * @param launcher ActivityResultLauncher to trigger permission request
     */
    fun requestLocationPermissionIfNeeded(
        context: Context,
        launcher: ActivityResultLauncher<String>
    ) {
        if (!hasLocationPermission(context)) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

}