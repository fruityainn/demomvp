package com.hide.videophoto.common.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class FilePicker(val ctx: Any) {

    companion object {
        const val TYPE_ALL = "*/*"
        const val TYPE_IMAGE = "image/*"
        const val TYPE_AUDIO = "audio/*"
        const val TYPE_VIDEO = "video/*"
    }

    private var listener: Listener? = null
    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(ctx)

    private val fileLauncher = when (ctx) {
        is AppCompatActivity -> {
            ctx.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                parseResult(it)
            }
        }
        is Fragment -> {
            ctx.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                parseResult(it)
            }
        }
        else -> null
    }

    fun pick(type: String = TYPE_ALL, listener: Listener) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                this@FilePicker.listener = listener

                fileLauncher?.run {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
//                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//                        putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        this.type = type
                    }
                    launch(intent)
                }
            }

            override fun onDenied() {
            }
        })
    }

    private fun parseResult(activityResult: ActivityResult) {
        with(activityResult) {
            if (resultCode == Activity.RESULT_OK) {
                data?.data?.also { uri ->
                    when (ctx) {
                        is AppCompatActivity -> {
                            val contentResolver = ctx.applicationContext.contentResolver
                            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            // Check for the freshest data.
                            contentResolver.takePersistableUriPermission(uri, takeFlags)
                        }
                        is Fragment -> {
                            val contentResolver =
                                ctx.requireActivity().applicationContext.contentResolver
                            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            // Check for the freshest data.
                            contentResolver.takePersistableUriPermission(uri, takeFlags)
                        }
                    }

                    listener?.onFilePicked(uri)
                }
            }
        }
    }

    interface Listener {
        fun onFilePicked(uri: Uri)
    }
}