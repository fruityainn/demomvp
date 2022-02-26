package com.hide.videophoto.common.ext

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.hide.videophoto.BuildConfig

inline val Fragment.ctx: Context?
    get() = context

inline val Fragment.TAG: String
    get() = this::class.java.simpleName

fun Fragment.addFragment(containerId: Int, fragment: Fragment) {
    childFragmentManager.beginTransaction().add(containerId, fragment, fragment.TAG).commit()
}

fun Fragment.replaceFragment(containerId: Int, fragment: Fragment) {
    childFragmentManager.beginTransaction().replace(containerId, fragment, fragment.TAG).commit()
}

fun Fragment.openActivity(
    clz: Class<*>,
    bundle: Bundle? = null,
    enterAnim: Int? = null,
    exitAnim: Int? = null,
    flags: IntArray? = null
) {
    val intent = Intent(ctx, clz)
    if (flags?.isNotEmpty() == true) {
        for (flag in flags) {
            intent.addFlags(flag)
        }
    }
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
    enterAnim?.also { enter ->
        exitAnim?.also { exit ->
            activity?.overridePendingTransition(enter, exit)
        }
    }
}

fun Fragment.openActivityForResult(clz: Class<*>, requestCode: Int, bundle: Bundle? = null) {
    val intent = Intent(ctx, clz)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivityForResult(intent, requestCode)
}

fun Fragment.logE(msg: Any?) {
    if (BuildConfig.DEBUG) {
        val strMsg = when (msg) {
            is String -> msg
            else -> msg.toString()
        }
        Log.e(javaClass.simpleName, strMsg)
    }
}