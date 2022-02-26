package com.hide.videophoto.common.util

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtil {

    fun isApi21orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun isApi23(): Boolean {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.M
    }

    fun isApi23orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isApi24orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    fun isApi25orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    }

    fun isApi26orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun isApi27orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    }

    fun isApi28orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun isApi29orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun isApi30orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    fun isApi31orHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    fun isGranted(
        ctx: Any?,
        permissions: Array<String>,
        resultLauncher: ActivityResultLauncher<Array<String>>? = null
    ): Boolean {
        var isGranted = true

        if (isApi23orHigher()) {
            for (permission in permissions) {
                when (ctx) {
                    is AppCompatActivity -> {
                        isGranted = ContextCompat.checkSelfPermission(
                            ctx,
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                    is Fragment -> {
                        isGranted = ContextCompat.checkSelfPermission(
                            ctx.requireContext(),
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                }
                if (!isGranted) {
                    break
                }
            }

            // Asking permissions
            if (!isGranted && resultLauncher != null) {
                if (ctx is AppCompatActivity || ctx is Fragment) {
                    resultLauncher.launch(permissions)
                }
            }
        }

        return isGranted
    }
}