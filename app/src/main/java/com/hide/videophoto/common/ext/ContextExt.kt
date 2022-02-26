package com.hide.videophoto.common.ext

import android.content.*
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.text.format.DateFormat
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.hide.videophoto.BuildConfig
import com.hide.videophoto.R
import com.hide.videophoto.common.MyApplication
import com.hide.videophoto.common.util.AdsManager
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.PermissionUtil
import com.hide.videophoto.data.model.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

inline val Context.ctx: Context
    get() = this

inline val Context.sharedPref: SharedPreferences
    get() = MyApplication.instance.sharedPref

inline val Context.appSettingsModel: AppSettingsModel
    get() = MyApplication.instance.appSettingsModel

inline val Context.adsConfigModel: AdsConfigModel
    get() = MyApplication.instance.adsConfigModel

inline val Context.authModel: AuthModel
    get() = MyApplication.instance.authModel

inline val Context.activityModel: ActivityModel
    get() = MyApplication.instance.activityModel

inline val Context.eventTracking: EventTrackingManager
    get() = MyApplication.instance.eventTracking

inline val Context.adsManager: AdsManager
    get() = MyApplication.instance.adsManager

inline val Context.currentTimeInSecond: Int
    get() = (System.currentTimeMillis() / 1000).toInt()

inline val Context.dateFormat: String
    get() = (DateFormat.getDateFormat(this) as SimpleDateFormat).toLocalizedPattern()

inline val Context.timeFormat: String
    get() = (DateFormat.getTimeFormat(this) as SimpleDateFormat).toLocalizedPattern()

inline val Context.dateTimeFormat: String
    get() = "$dateFormat $timeFormat"

inline val Context.storageRootFolder: String
    get() = getExternalFilesDir(null)?.absolutePath ?: ""

inline val Context.manufacturer: String
    get() = Build.MANUFACTURER

inline val Context.isXiaomiDevice: Boolean
    get() = manufacturer.equals("Xiaomi", true)

inline val Context.isSamsungDevice: Boolean
    get() = manufacturer.equals("Samsung", true)

inline val Context.isOppoDevice: Boolean
    get() = manufacturer.equals("Oppo", true)

inline val Context.isVivoDevice: Boolean
    get() = manufacturer.equals("Vivo", true)

fun Context.isAdClickedTooMuch(): Boolean {
    return ctx.currentTimeInSecond < activityModel.lastTimeAdClicked
}

fun Context.shouldShowAds(): Boolean {
    return adsConfigModel.isAdsEnabled && !appSettingsModel.didRemoveAds && !isAdClickedTooMuch()
}

fun Context.getMimeTypeFromUri(uri: Uri): String? {
    return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        contentResolver.getType(uri)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
    }?.lowercase()
}

fun Context.getIdFromUri(uri: Uri): String {
    val decodedUri = Uri.decode(uri.toString())
    return if (decodedUri.contains("com.hide.videophoto.fileprovider")) {
        if (decodedUri.contains("/")) {
            decodedUri.substringAfterLast("/")
        } else {
            decodedUri
        }
    } else if (decodedUri.startsWith("file")) {
        if (decodedUri.contains("/")) {
            decodedUri.substringAfterLast("/")
        } else {
            decodedUri
        }
    } else {
        if (decodedUri.contains(":")) {
            decodedUri.substringAfterLast(":")
        } else {
            decodedUri
        }
    }
}

fun Context.getUriFromString(uri: String?): Uri? {
    return uri?.let {
        if (uri.startsWith("content")) {
            Uri.parse(uri)
        } else {
            Uri.fromFile(File(uri))
        }
    }
}

fun Context.networkIsConnected(): Boolean {
    try {
        val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return conMgr?.let {
            return if (PermissionUtil.isApi29orHigher()) {
                val capabilities = it.getNetworkCapabilities(it.activeNetwork)
                capabilities?.run {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } ?: false
            } else {
                it.activeNetworkInfo?.isConnected ?: false
            }
        } ?: false
    } catch (e: Exception) {
        logE("$e")
    }

    return false
}

fun Context.createFileChooser(fileModel: FileModel) {
    Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(fileModel.getEncryptedPath()), fileModel.mimeType)
    }.run {
        try {
            Intent.createChooser(this, getString(R.string.app_name))
                .run {
                    startActivity(this)
                }
        } catch (e: ActivityNotFoundException) {
            logE(e.message)
        }
    }
}

fun Context.shouldShowRatingDialog(): Boolean {
    return appSettingsModel.let {
        !it.dontShowRateDialogAgain
                && (it.countAddingFile == 3 || it.countAddingFile == 6 || (it.countAddingFile > 6 && it.countAddingFile % 5 == 0))
                && (currentTimeInSecond - adsConfigModel.lastTimeInterstitialAdShown >= 3)
    }
}

fun Context.updateLocale(context: Context, languageCode: String?): ContextWrapper {
    var ctx = context
    languageCode?.run {
        val resources: Resources? = ctx.resources
        val configuration: Configuration? = resources?.configuration
        val localeToSwitchTo = if (languageCode != "sys") {
            if (languageCode == "zh-rTW") {
                Locale.TAIWAN
            } else {
                Locale(languageCode)
            }
        } else {
            Locale(appSettingsModel.defaultLanguage ?: Locale.getDefault().language)
        }
        if (PermissionUtil.isApi24orHigher()) {
            val localeList = LocaleList(localeToSwitchTo)
            LocaleList.setDefault(localeList)
            configuration?.setLocales(localeList)
        } else {
            configuration?.locale = localeToSwitchTo
        }

        configuration?.run {
            configuration.setLayoutDirection(localeToSwitchTo)
            if (PermissionUtil.isApi25orHigher()) {
                ctx = ctx.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
        }
    }
    return ContextWrapper(ctx)
}

fun Context.openOtherPermissionsPageOnXiaomiDevice() {
    try {
        Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            putExtra("extra_pkgname", packageName)
        }.run {
            startActivity(this)
        }
    } catch (e: Exception) {
    }
}

fun Context.logE(msg: Any?) {
    if (BuildConfig.DEBUG) {
        val strMsg = when (msg) {
            is String -> msg
            else -> msg.toString()
        }
        Log.e(javaClass.simpleName, strMsg)
    }
}

fun Context.toast(msg: Any?, isLongToast: Boolean = false) {
    val message = when (msg) {
        is Int -> getString(msg)
        is Char -> msg.toString()
        is CharSequence -> msg.toString()
        is String -> msg
        else -> "Error: message type is not supported"
    }
    val length = if (isLongToast) {
        Toast.LENGTH_LONG
    } else {
        Toast.LENGTH_SHORT
    }
    Toast.makeText(this, message, length).show()
}