package com.hide.videophoto.common.util

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.os.Environment
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.hide.videophoto.BuildConfig
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.sharedPref
import com.hide.videophoto.common.ext.toast
import com.hide.videophoto.common.util.SharedPreferencesUtil.get
import com.hide.videophoto.common.util.SharedPreferencesUtil.set
import com.hide.videophoto.data.model.AppSettingsModel
import java.util.*

object CommonUtil {

    private const val APP_SETTINGS_MODEL = "app_settings_model"

    fun getAppID(): String {
        return BuildConfig.APPLICATION_ID
    }

    fun isStoragePermissionGranted(ctx: Context): Boolean {
        return if (PermissionUtil.isApi30orHigher()) {
            Environment.isExternalStorageManager()
        } else {
            PermissionUtil.isGranted(
                ctx,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    fun showKeyboard(ctx: Context) {
        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    fun closeKeyboard(activity: AppCompatActivity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    fun closeKeyboardWhileClickOutSide(activity: AppCompatActivity, view: View?) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view?.setOnTouchListener { _, _ ->
                closeKeyboard(activity)
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                closeKeyboardWhileClickOutSide(activity, innerView)
            }
        }
    }

    fun getHeightOfStatusBar(activity: AppCompatActivity): Int {
        val resId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) {
            activity.resources.getDimensionPixelSize(resId)
        } else {
            0
        }
    }

    fun getHeightOfNavigationBar(activity: AppCompatActivity): Int {
        val resId = activity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resId > 0) {
            activity.resources.getDimensionPixelSize(resId)
        } else {
            0
        }
    }

    fun getRealScreenSizeAsPixels(activity: AppCompatActivity): Point {
        val display = if (PermissionUtil.isApi30orHigher()) {
            activity.display
        } else {
            activity.windowManager.defaultDisplay
        }

        val outPoint = Point()
        display?.getRealSize(outPoint)
        return outPoint
    }

    fun getRealScreenWidthAsPixel(activity: AppCompatActivity) : Int {
        return if (PermissionUtil.isApi31orHigher()) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun getRealScreenHeightAsPixel(activity: AppCompatActivity) : Int {
        return if (PermissionUtil.isApi31orHigher()) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    fun convertDpToPixel(ctx: Context?, vararg dimensionIds: Int): Int {
        var result = 0
        ctx?.run {
            for (id in dimensionIds) {
                result += resources.getDimension(id).toInt()
            }
        }

        return result
    }

    /**
     * If you want to call this method on api 23 or higher then you have to check permission in runtime
     * (use PermissionUtil class for reference)
     *
     * @param act
     * @param phoneNumber
     */
    fun call(act: AppCompatActivity, phoneNumber: String) {
        val number = "tel:$phoneNumber"
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse(number))
        if (callIntent.resolveActivity(act.packageManager) != null) {
            act.startActivity(callIntent)
        } else {
            act.toast("No call service found")
        }
    }

    fun sendEmail(
        ctx: Context?,
        email: String,
        subject: String,
        content: String,
        bccEmail: String? = null
    ) {
        ctx?.also {
            if (email.isNotEmpty() && email != "null") {
                Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    bccEmail?.run {
                        putExtra(Intent.EXTRA_BCC, arrayOf(bccEmail))
                    }
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, content)
                }.run {
                    try {
                        ctx.startActivity(
                            Intent.createChooser(
                                this,
                                ctx.getString(R.string.title_choose_email_composer)
                            )
                        )
                    } catch (ex: ActivityNotFoundException) {
                        ctx.toast("Error, no email composer found")
                    }
                }
            } else {
                ctx.toast("Invalid email")
            }
        }
    }

    fun shareText(ctx: Context?, body: String) {
        ctx?.also {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, ctx?.getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, body)
            }.run {
                ctx.startActivity(Intent.createChooser(this, ctx.getString(R.string.share_app)))
            }
        }
    }

    fun openBrowser(ctx: Context?, url: String) {
        ctx?.run {
            val callIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (callIntent.resolveActivity(ctx.packageManager) != null) {
                ctx.startActivity(callIntent)
            } else {
                ctx.toast("No browser found")
            }
        }
    }

    fun setDefaultLanguage(ctx: Context) {
        with(ctx.resources) {
            configuration.setLocale(Locale.getDefault())
            ctx.createConfigurationContext(configuration)
        }
    }

    fun openAppInPlayStore(ctx: Context) {
        val uri = Uri.parse("market://details?id=${getAppID()}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )

        if (intent.resolveActivity(ctx.packageManager) != null) {
            ctx.startActivity(intent)
        } else {
            openBrowser(ctx, Constants.LINK_APP)
        }
    }

    fun saveAppSettingsModel(ctx: Context?, model: AppSettingsModel) {
        ctx?.run {
            sharedPref[APP_SETTINGS_MODEL] = model.toJson()
        }
    }

    fun getAppSettingsModel(ctx: Context): AppSettingsModel {
        val json: String? = ctx.sharedPref[APP_SETTINGS_MODEL]
        return json?.run { Gson().fromJson(json, AppSettingsModel::class.java) }
            ?: AppSettingsModel()
    }
}