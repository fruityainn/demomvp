package com.hide.videophoto.common.ext

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.hide.videophoto.common.Constants
import java.io.File

fun Uri.getFilePath(ctx: Context): String? {
    var result: String? = null
    when {
        DocumentsContract.isDocumentUri(ctx, this) -> {
            when {
                isExternalStorageDocument() -> {
                    val docId = DocumentsContract.getDocumentId(this)
                    val split = docId.split(":")
                    val type = split[0]

                    // This is for checking Main Memory
                    result = if ("primary".equals(type, true)) {
                        if (split.size > 1) {
                            Constants.STORAGE_ROOT_FOLDER + File.separator + split[1]
                        } else {
                            Constants.STORAGE_ROOT_FOLDER
                        }
                    } else { // This is for checking SD Card
                        "storage" + File.separator + docId.replace(":", File.separator)
                    }
                }
                isDownloadsDocument() -> {
                    try {
                        val docId = DocumentsContract.getDocumentId(this)
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/my_downloads"),
                            docId.toLong()
                        )
                        result = contentUri.getDataColumn(ctx)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isMediaDocument() -> {
                    try {
                        val docId = DocumentsContract.getDocumentId(this)
                        val split = docId.split(":")
                        val type = split[0]

                        when {
                            "image".equals(type, true) -> {
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            }
                            "video".equals(type, true) -> {
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            }
                            "audio".equals(type, true) -> {
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }
                            else -> {
                                null
                            }
                        }?.run {
                            val selection = "${MediaStore.MediaColumns._ID} = ?"
                            val selectionArgs = arrayOf(split[1])
                            result = getDataColumn(ctx, selection, selectionArgs)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        "content".equals(scheme, true) -> {
            result = if (isGooglePhotosUri()) {
                lastPathSegment
            } else {
                getDataColumn(ctx)
            }
        }
        "file".equals(scheme, true) -> {
            result = path
        }
    }

    return result
}

fun Uri.getDataColumn(
    ctx: Context, selection: String? = null, selectionArgs: Array<String>? = null
): String? {
    var path: String? = null

    val projection = arrayOf(MediaStore.MediaColumns.DATA)
    ctx.applicationContext.contentResolver.query(
        this,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            path = cursor.getString(pathCol)
        }
    }

    return path
}

fun Uri.isExternalStorageDocument(): Boolean {
    return "com.android.externalstorage.documents" == authority
}

fun Uri.isDownloadsDocument(): Boolean {
    return "com.android.providers.downloads.documents" == authority
}

fun Uri.isMediaDocument(): Boolean {
    return "com.android.providers.media.documents" == authority
}

fun Uri.isGooglePhotosUri(): Boolean {
    return "com.google.android.apps.photos.content" == authority
}