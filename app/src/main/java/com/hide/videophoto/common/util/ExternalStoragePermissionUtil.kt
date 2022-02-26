package com.hide.videophoto.common.util

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class ExternalStoragePermissionUtil(private val ctx: Any?) {

    private var listener: Listener? = null
    private val intentManageAppAllFiles by lazy {
        Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse("package:${CommonUtil.getAppID()}")
        )
    }
    private val intentManageAllFiles by lazy {
        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    }

    private val externalStoragePermissionResultApi30 = when (ctx) {
        is AppCompatActivity -> {
            ctx.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                parsePermissionsApi30()
            }
        }
        is Fragment -> {
            ctx.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                parsePermissionsApi30()
            }
        }
        else -> null
    }

    private val externalStoragePermissionResult = when (ctx) {
        is AppCompatActivity -> {
            ctx.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                parsePermissions(it)
            }
        }
        is Fragment -> {
            ctx.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                parsePermissions(it)
            }
        }
        else -> null
    }

    private fun parsePermissions(maps: Map<String, Boolean?>) {
        val granted = maps.values.isNotEmpty() && maps.values.all { result ->
            result == true
        }
        if (granted) {
            listener?.onGranted()
        } else {
            listener?.onDenied()
        }
    }

    private fun parsePermissionsApi30() {
        if (isExternalStorageManager()) {
            listener?.onGranted()
        } else {
            listener?.onDenied()
        }
    }

    private fun isExternalStorageManager(): Boolean {
        return PermissionUtil.isApi30orHigher() && Environment.isExternalStorageManager()
    }

    fun requestPermission(listener: Listener) {
        externalStoragePermissionResultApi30?.also { resultLauncherApi30 ->
            externalStoragePermissionResult?.also { resultLauncher ->
                this@ExternalStoragePermissionUtil.listener = listener

                if (ctx is AppCompatActivity || ctx is Fragment) {
                    if (PermissionUtil.isApi30orHigher()) {
                        if (isExternalStorageManager()) {
                            listener.onGranted()
                        } else {
                            try {
                                resultLauncherApi30.launch(intentManageAppAllFiles)
                            } catch (e: Exception) {
                                resultLauncherApi30.launch(intentManageAllFiles)
                            }
                        }
                    } else {
                        if (PermissionUtil.isGranted(
                                ctx,
                                arrayOf(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ),
                                resultLauncher
                            )
                        ) {
                            listener.onGranted()
                        }
                    }
                }
            }
        }
    }

    interface Listener {
        fun onGranted()

        fun onDenied()
    }
}