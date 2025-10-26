package io.github.xposed.androidspoofer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.MediaDrm
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewClientCompat
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdClient
import com.google.android.gms.tasks.Tasks
import io.github.xposed.androidspoofer.BuildConfig
import io.github.xposed.androidspoofer.Constants
import io.github.xposed.androidspoofer.Constants.CONF_EXPORT_NAME
import io.github.xposed.androidspoofer.Constants.PLAYREADY_UUID
import io.github.xposed.androidspoofer.Constants.WIDEVINE_UUID
import io.github.xposed.androidspoofer.PreferencesManager
import io.github.xposed.androidspoofer.R
import io.github.xposed.androidspoofer.Utils
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.net.toUri
import com.google.android.gms.ads.identifier.AdvertisingIdClient


class ActivityWebview : AppCompatActivity() {
    private lateinit var webview: WebView

    private val pref by lazy {
        try {
            getSharedPreferences(Constants.SHARED_PREF_FILE_NAME, MODE_WORLD_READABLE)
        } catch (_: Exception) {
            // For check variant, fall back to MODE_PRIVATE to allow viewing device IDs
            // even when Xposed module is not enabled
            if (BuildConfig.IS_CHECK_VARIANT) {
                getSharedPreferences(Constants.SHARED_PREF_FILE_NAME, MODE_PRIVATE)
            } else {
                null
            }
        }
    }

    override fun onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_main)

        // Only show module not enabled alert for full variant
        if (pref == null && !BuildConfig.IS_CHECK_VARIANT) {
            AlertDialog.Builder(this).setMessage(R.string.module_not_enabled)
                .setPositiveButton(R.string.close) { _, _ ->
                    finish()
                }.setCancelable(false).show()
            return
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(this))
            .build()

        webview = findViewById(R.id.webview)
        webview.webViewClient = LocalContentWebViewClient(assetLoader)
        webview.settings.javaScriptEnabled = true
        webview.settings.loadsImagesAutomatically = true
        webview.addJavascriptInterface(WebAppInterface(this), "Android")
        webview.loadUrl("https://appassets.androidplatform.net/assets/webview/index.html")
    }

    /**
     * Open a storage location on the device to export the configuration as a document.
     * Uses intent with action [Intent.ACTION_CREATE_DOCUMENT]
     * Also see [configCreateLauncher].
     *
     * Derived from https://gist.github.com/neonankiti/05922cf0a44108a2e2732671ed9ef386
     */
    private fun saveConfFile() {
        val openIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // filter to only show openable items.
            addCategory(Intent.CATEGORY_OPENABLE)

            // Create a file with the requested Mime type
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, CONF_EXPORT_NAME)
        }
        Toast.makeText(this, R.string.select_a_location, Toast.LENGTH_SHORT).show()
        configCreateLauncher.launch(openIntent)
    }

    /**
     * Intent launcher to start system file picker UI to select location of export.
     * The Uri of the location is present in result.
     * Then call [Utils.writeConfigFile] using that Uri.
     */
    private val configCreateLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (it.resultCode == Activity.RESULT_OK) {
                    Utils.writeConfigFile(this, it.data!!.data!!, pref!!)
                    Toast.makeText(this, R.string.export_complete, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this, "${getString(R.string.share_error)}: ${e.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }


    /**
     * Read a JSON file to get the configurations.
     * Opens system file picker to select the file.
     *
     * https://developer.android.com/training/data-storage/shared/documents-files#open-file
     */
    private fun importConfFile() {
        val openIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        configOpenLauncher.launch(openIntent)
    }

    /**
     * Close and reopen the activity.
     * For some reason, invalidate or recreate() does not refresh the switches.
     */
    private fun restartActivity() {
        finish()
        startActivity(intent)
    }

    private val configOpenLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (it.resultCode == Activity.RESULT_OK) {
                    Utils.readConfigFile(this, it.data!!.data!!, pref!!)
                    Toast.makeText(this, R.string.import_complete, Toast.LENGTH_SHORT).show()
                    restartActivity()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this, "${getString(R.string.read_error)}: ${e.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }

    private class LocalContentWebViewClient(private val assetLoader: WebViewAssetLoader) :
        WebViewClientCompat() {

        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return assetLoader.shouldInterceptRequest(request.url)
        }
    }

    private inner class WebAppInterface(private val context: Context) {
        private val prefManager by lazy {
            PreferencesManager(pref ?: getSharedPreferences(Constants.SHARED_PREF_FILE_NAME, MODE_PRIVATE))
        }
        private val pm by lazy {
            context.packageManager
        }

        private fun updateAppList() {
            val appInfos: List<ApplicationInfo>
            val flags =
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            appInfos = pm.getInstalledApplications(flags)
            prefManager.ro_applist = JSONArray().apply {
                for (appInfo in appInfos) {
                    put(appInfo.packageName)
                }
            }
        }

        private fun updateUniqueIds() {
            try {
                val widevineMediaDrm = MediaDrm(WIDEVINE_UUID)
                try {
                    val widevine_id = widevineMediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                    prefManager.ro_unique_id_widevine_drm = Utils.bytesToHex(widevine_id)
                } finally {
                    widevineMediaDrm.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                prefManager.ro_unique_id_widevine_drm = ""
            }

            try {
                val playreadyMediaDrm = MediaDrm(PLAYREADY_UUID)
                try {
                    val playready_id = playreadyMediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
                    prefManager.ro_unique_id_playready_drm = Utils.bytesToHex(playready_id)
                } finally {
                    playreadyMediaDrm.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                prefManager.ro_unique_id_playready_drm = ""
            }
            prefManager.ro_unique_id_android_id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            prefManager.ro_unique_id_gsf_id = getGsfId(context)
            prefManager.ro_unique_id_appset_id = getAppsetId()
            prefManager.ro_unique_id_ad_id = getAdId()
        }
        fun getAdId(): String {
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
        fun getAppsetId(): String {
            val client = AppSet.getClient(applicationContext)
            val task = client.appSetIdInfo

            val info = Tasks.await(task)
            return info.id
        }
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

        @JavascriptInterface
        fun exportPreferences() {
            saveConfFile()
        }

        /**
         * Returns true if importing preferences was successful
         */
        @JavascriptInterface
        fun importPreferences(): Boolean {
            importConfFile()
            return false
        }

        @JavascriptInterface
        fun getROPreferences(): String {
            updateAppList()
            updateUniqueIds()
            return prefManager.ro.toString()
        }

        @JavascriptInterface
        fun getRWPreferences(): String {
            return prefManager.rw.toString()
        }

        @JavascriptInterface
        fun setRWPreferences(value: String) {
            prefManager.rw = JSONObject(value)
        }
    }
}