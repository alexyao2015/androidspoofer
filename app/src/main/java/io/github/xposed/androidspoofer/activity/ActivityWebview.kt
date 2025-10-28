package io.github.xposed.androidspoofer.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewClientCompat
import io.github.xposed.androidspoofer.BuildConfig
import io.github.xposed.androidspoofer.Constants
import io.github.xposed.androidspoofer.Constants.CONF_EXPORT_NAME
import io.github.xposed.androidspoofer.PreferencesManager
import io.github.xposed.androidspoofer.R
import io.github.xposed.androidspoofer.Utils
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class ActivityWebview : AppCompatActivity() {
    private lateinit var webview: WebView

    private val pref by lazy {
        try {
            getSharedPreferences(Constants.SHARED_PREF_FILE_NAME, MODE_WORLD_READABLE)
        } catch (_: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_main)

        // Request location permission if not granted (needed for WiFi IP address)
        Utils.requestLocationPermissionIfNeeded(
            this,
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {})

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

        // Handle back button with modern API
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Try to let the Vue app handle the back navigation
                webview.evaluateJavascript(
                    "(function() { try { return window.handleAndroidBack ? window.handleAndroidBack() : false; } catch(e) { return false; } })()"
                ) { result ->
                    // If the Vue app didn't handle it (returned false/null), check WebView history or exit
                    if (result == "false" || result == "null") {
                        if (webview.canGoBack()) {
                            webview.goBack()
                        }
//                        } else {
//                            // No history left, so disable this callback and trigger back again to exit
//                            isEnabled = false
//                            onBackPressedDispatcher.onBackPressed()
//                        }
                    }
                    // If result is "true", the Vue app handled it, so do nothing
                }
            }
        })
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
                if (it.resultCode == RESULT_OK) {
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
                if (it.resultCode == RESULT_OK) {
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
            PreferencesManager(
                pref ?: getSharedPreferences(
                    Constants.SHARED_PREF_FILE_NAME,
                    MODE_PRIVATE
                )
            )
        }
        private val pm by lazy {
            context.packageManager
        }

        private fun updateAppList() {
            val appInfos: List<ApplicationInfo>
            val flags =
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            appInfos = pm.getInstalledApplications(flags)
            prefManager.ro_applist = JSONObject().apply {
                for (appInfo in appInfos) {
                    val appName = appInfo.loadLabel(pm).toString()
                    put(appName, appInfo.packageName)
                }
            }
        }


        @JavascriptInterface
        fun exportPreferences() {
            saveConfFile()
        }

        /**
         * Launches the file picker to import preferences.
         * The actual import result will be shown via Toast notification.
         */
        @JavascriptInterface
        fun importPreferences() {
            importConfFile()
        }

        @JavascriptInterface
        fun getAppsList(): String {
            updateAppList()
            return prefManager.ro_applist.toString()
        }

        @JavascriptInterface
        fun getUniqueIds(): String {
            return JSONObject().apply {
                put("widevineId", Utils.getWidevineId())
                put("playReadyId", Utils.getPlayreadyId())
                put(
                    "androidId",
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                )
                put("gsfId", Utils.getGsfId(context))
                put("appsetId", Utils.getAppsetId(context))
                put("adId", Utils.getAdId(context))
                put("ipAddress", Utils.getIpAddress(context))
                put("timeZone", Utils.getSystemTimeZone())
            }.toString()
        }

        @JavascriptInterface
        fun getRWPreferences(): String {
            return prefManager.rw.toString()
        }

        @JavascriptInterface
        fun setRWPreferences(value: String) {
            prefManager.rw = JSONObject(value)
        }

        /**
         * Get the app icon for a given package name as a base64 encoded string.
         * @param packageName The package name of the app
         * @return Base64 encoded WebP image string, or empty string if app not found or is default icon
         */
        @JavascriptInterface
        fun getAppIcon(packageName: String): String {
            return try {
                val icon: Drawable = pm.getApplicationIcon(packageName)

                // Convert to bitmap and scale down to 100x100 for performance
                val bitmap = Utils.drawableToBitmap(icon)
                val scaledBitmap = bitmap.scale(100, 100)

                val outputStream = ByteArrayOutputStream()
                // Use WebP with 80% quality for much better compression
                scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
                bitmap.recycle()
                scaledBitmap.recycle()

                val byteArray = outputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}