package com.hide.videophoto.data.interactor

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.DateTimeUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.PermissionUtil
import com.hide.videophoto.data.mapper.*
import com.hide.videophoto.data.model.FileModel
import java.io.File
import java.io.IOException
import java.util.*

class FileInteractor(private val ctx: Context) : BaseInteractor(ctx) {

    companion object {
        private const val SCHEME_CONTENT = "content"
        private const val BUFFERED_SIZE = 50 * 1024 * 1024 // 50 MB
        private const val SIZE_1MB = 1024 * 1024 // 1 MB
        private const val SIZE_2MB = 2 * SIZE_1MB // 2 MB
        private const val SIZE_5MB = 5 * SIZE_1MB // 5 MB
        private const val SIZE_10MB = 10 * SIZE_1MB // 10 MB
        private const val SIZE_20MB = 20 * SIZE_1MB // 20 MB
        private const val SIZE_50MB = 50 * SIZE_1MB // 50 MB
        private const val SIZE_100MB = 100 * SIZE_1MB // 100 MB
        private const val SIZE_200MB = 200 * SIZE_1MB // 200 MB
        private const val SIZE_500MB = 500 * SIZE_1MB // 500 MB
        private const val SIZE_1GB = 1024 * SIZE_1MB // 1 GB
        private const val SIZE_2GB = 2L * 1024 * SIZE_1MB // 2 GB
        private const val SIZE_5GB = 5L * 1024 * SIZE_1MB // 5 GB
        private const val SIZE_10GB = 10L * 1024 * SIZE_1MB // 10 GB
    }

    private val dbInteractor by lazy { DBInteractor(ctx) }

    fun loadImages(pickedUri: Uri? = null): List<FileModel> {
        val result = ArrayList<FileModel>()

        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.ORIENTATION
        ).let { arr ->
            var arrCol = arr
            if (PermissionUtil.isApi30orHigher()) {
                arrCol = arrCol.append(
                    MediaStore.Images.ImageColumns.IS_FAVORITE,
                    MediaStore.Images.ImageColumns.RESOLUTION
                )
            }

            arrCol
        }

        val selection = pickedUri?.let {
            if (isNotContentScheme(it)) {
                "${MediaStore.MediaColumns._ID} = ? OR ${MediaStore.MediaColumns.DATA} LIKE ?"
            } else {
                null
            }
        }
        val selectionArgs: Array<String>? = pickedUri?.let {
            if (isNotContentScheme(it)) {
                val id = ctx.getIdFromUri(it)
                arrayOf(id, "%$id")
            } else {
                null
            }
        }
        val sortOrder = "${MediaStore.Images.ImageColumns.DATE_MODIFIED} DESC"
        val baseUri = pickedUri?.let {
            if (isNotContentScheme(it)) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                it
            }
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ctx.applicationContext.contentResolver.query(
            baseUri, projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
            val pathCol = if (pickedUri == null) {
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)
            } else {
                null
            }
            val bucketNameCol =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME)
            val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE)
            val dateAddedCol =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_ADDED)
            val dateModifiedCol =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED)
            val orientationCol =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION)
            val resolutionCol = if (PermissionUtil.isApi30orHigher()) {
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.RESOLUTION)
            } else {
                null
            }
            val isFavoriteCol = if (PermissionUtil.isApi30orHigher()) {
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.IS_FAVORITE)
            } else {
                null
            }

            pickedUri?.run {
                if (cursor.moveToFirst()) {
                    result.add(
                        FileModel().apply {
                            fileId = cursor.getLong(idCol)
                            originalFolder =
                                pickedUri.getFilePath(ctx)?.substringBeforeLast(File.separator)
                            name = cursor.getString(nameCol)
                            mimeType = cursor.getString(mimeTypeCol).lowercase()
                            size = cursor.getLong(sizeCol)
                            addedDate = cursor.getInt(dateAddedCol)
                            modifiedDate = cursor.getInt(dateModifiedCol)
                            uri = pickedUri.toString()
                            orientationCol?.also { col ->
                                orientation = cursor.getInt(col)
                            }
                            resolutionCol?.also { col ->
                                resolution = cursor.getString(col)
                            }
                            isFavoriteCol?.also { col ->
                                isFavorite = cursor.getInt(col) == 1
                            }
                        }
                    )
                }
            } ?: run {
                while (cursor.moveToNext()) {
                    result.add(
                        FileModel().apply {
                            fileId = cursor.getLong(idCol)
                            originalFolder = pathCol?.let { col ->
                                cursor.getString(col).substringBeforeLast(File.separator)
                            }
                            name = cursor.getString(nameCol)
                            bucketName = cursor.getString(bucketNameCol)
                            mimeType = cursor.getString(mimeTypeCol).lowercase()
                            size = cursor.getLong(sizeCol)
                            addedDate = cursor.getInt(dateAddedCol)
                            modifiedDate = cursor.getInt(dateModifiedCol)
                            fileId?.also { id ->
                                uri = ContentUris.withAppendedId(baseUri, id).toString()
                            }

                            orientationCol?.also { col ->
                                orientation = cursor.getInt(col)
                            }
                            resolutionCol?.also { col ->
                                resolution = cursor.getString(col)
                            }
                            isFavoriteCol?.also { col ->
                                isFavorite = cursor.getInt(col) == 1
                            }
                        }
                    )
                }
            }
        }

        return result
    }

    fun loadVideos(pickedUri: Uri? = null): List<FileModel> {
        val result = ArrayList<FileModel>()

        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.MIME_TYPE,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DATE_MODIFIED,
            MediaStore.Video.VideoColumns.TITLE,
            MediaStore.Video.VideoColumns.DURATION
        ).let { arr ->
            if (PermissionUtil.isApi30orHigher()) {
                arr.append(
                    MediaStore.Video.VideoColumns.IS_FAVORITE,
                    MediaStore.Video.VideoColumns.RESOLUTION
                )
            } else {
                arr
            }
        }

        val selection = pickedUri?.let {
            if (isNotContentScheme(it)) {
                "${MediaStore.MediaColumns._ID} = ? OR ${MediaStore.MediaColumns.DATA} LIKE ?"
            } else {
                null
            }
        }
        val selectionArgs: Array<String>? = pickedUri?.let {
            if (isNotContentScheme(it)) {
                val id = ctx.getIdFromUri(it)
                arrayOf(id, "%$id")
            } else {
                null
            }
        }
        val sortOrder = "${MediaStore.Video.VideoColumns.DATE_MODIFIED} DESC"
        val baseUri = pickedUri?.let {
            if (isNotContentScheme(it)) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                it
            }
        } ?: MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ctx.applicationContext.contentResolver.query(
            baseUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA)
            val bucketNameCol =
                cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME)
            val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.MIME_TYPE)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TITLE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.SIZE)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
            val dateAddedCol =
                cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_ADDED)
            val dateModifiedCol =
                cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_MODIFIED)
            val resolutionCol = if (PermissionUtil.isApi30orHigher()) {
                cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.RESOLUTION)
            } else {
                null
            }
            val isFavoriteCol = if (PermissionUtil.isApi30orHigher()) {
                cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.IS_FAVORITE)
            } else {
                null
            }

            pickedUri?.run {
                if (cursor.moveToFirst()) {
                    result.add(
                        FileModel().apply {
                            fileId = cursor.getLong(idCol)
                            originalFolder =
                                cursor.getString(pathCol).substringBeforeLast(File.separator)
                            name = cursor.getString(nameCol)
                            mimeType = cursor.getString(mimeTypeCol).lowercase()
                            title = cursor.getString(titleCol)
                            size = cursor.getLong(sizeCol)
                            duration = cursor.getInt(durationCol)
                            addedDate = cursor.getInt(dateAddedCol)
                            modifiedDate = cursor.getInt(dateModifiedCol)
                            uri = pickedUri.toString()
                            resolutionCol?.also { col ->
                                resolution = cursor.getString(col)
                            }
                            isFavoriteCol?.also { col ->
                                isFavorite = cursor.getInt(col) == 1
                            }
                        }
                    )
                }
            } ?: run {
                while (cursor.moveToNext()) {
                    result.add(
                        FileModel().apply {
                            fileId = cursor.getLong(idCol)
                            originalFolder =
                                cursor.getString(pathCol).substringBeforeLast(File.separator)
                            name = cursor.getString(nameCol)
                            bucketName = cursor.getString(bucketNameCol)
                            mimeType = cursor.getString(mimeTypeCol).lowercase()
                            title = cursor.getString(titleCol)
                            size = cursor.getLong(sizeCol)
                            duration = cursor.getInt(durationCol)
                            addedDate = cursor.getInt(dateAddedCol)
                            modifiedDate = cursor.getInt(dateModifiedCol)
                            fileId?.also { id ->
                                uri = ContentUris.withAppendedId(baseUri, id).toString()
                            }
                            resolutionCol?.also { col ->
                                resolution = cursor.getString(col)
                            }
                            isFavoriteCol?.also { col ->
                                isFavorite = cursor.getInt(col) == 1
                            }
                        }
                    )
                }
            }
        }

        return result
    }

    fun loadAudios(pickedUri: Uri? = null): List<FileModel> {
        val result = ArrayList<FileModel>()

        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.DATE_MODIFIED,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.DURATION
        ).let { arr ->
            if (PermissionUtil.isApi30orHigher()) {
                arr.append(MediaStore.Audio.AudioColumns.IS_FAVORITE)
            } else {
                arr
            }
        }

        val selection = pickedUri?.let {
            if (isNotContentScheme(it)) {
                "${MediaStore.MediaColumns._ID} = ? OR ${MediaStore.MediaColumns.DATA} LIKE ?"
            } else {
                null
            }
        }
        val selectionArgs: Array<String>? = pickedUri?.let {
            if (isNotContentScheme(it)) {
                val id = ctx.getIdFromUri(it)
                arrayOf(id, "%$id")
            } else {
                null
            }
        }
        val sortOrder = "${MediaStore.Audio.AudioColumns.DATE_MODIFIED} DESC"
        val baseUri = pickedUri?.let {
            if (isNotContentScheme(it)) {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            } else {
                it
            }
        } ?: MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        ctx.applicationContext.contentResolver.query(
            baseUri, projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
            val bucketNameCol =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.BUCKET_DISPLAY_NAME)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.MIME_TYPE)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.SIZE)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val dateAddedCol =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_ADDED)
            val dateModifiedCol =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_MODIFIED)
            val isFavoriteCol = if (PermissionUtil.isApi30orHigher()) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.IS_FAVORITE)
            } else {
                null
            }

            pickedUri?.run {
                if (cursor.moveToFirst()) {
                    result.add(
                        FileModel().apply {
                            fileId = cursor.getLong(idCol)
                            originalFolder =
                                cursor.getString(pathCol).substringBeforeLast(File.separator)
                            name = cursor.getString(nameCol)
                            mimeType = cursor.getString(mimeTypeCol).lowercase()
                            title = cursor.getString(titleCol)
                            artist = cursor.getString(artistCol)
                            size = cursor.getLong(sizeCol)
                            duration = cursor.getInt(durationCol)
                            addedDate = cursor.getInt(dateAddedCol)
                            modifiedDate = cursor.getInt(dateModifiedCol)
                            uri = pickedUri.toString()
                            isFavoriteCol?.also { col ->
                                isFavorite = cursor.getInt(col) == 1
                            }
                        }
                    )
                }
            } ?: run {
                while (cursor.moveToNext()) {
                    result.add(
                        FileModel().apply {
                            fileId = cursor.getLong(idCol)
                            originalFolder =
                                cursor.getString(pathCol).substringBeforeLast(File.separator)
                            name = cursor.getString(nameCol)
                            bucketName = cursor.getString(bucketNameCol)
                            mimeType = cursor.getString(mimeTypeCol).lowercase()
                            title = cursor.getString(titleCol)
                            artist = cursor.getString(artistCol)
                            size = cursor.getLong(sizeCol)
                            duration = cursor.getInt(durationCol)
                            addedDate = cursor.getInt(dateAddedCol)
                            modifiedDate = cursor.getInt(dateModifiedCol)
                            fileId?.also { id ->
                                uri = ContentUris.withAppendedId(baseUri, id).toString()
                            }
                            isFavoriteCol?.also { col ->
                                isFavorite = cursor.getInt(col) == 1
                            }
                        }
                    )
                }
            }
        }

        return result
    }

    fun loadOtherFile(pickedUri: Uri? = null): FileModel? {
        var result: FileModel? = null
        pickedUri?.run {
            ctx.applicationContext.contentResolver.query(
                pickedUri, null, null, null, null
            )?.use { cursor ->
                val mimeTypeCol =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val modifiedCol =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                val sizeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val flagsCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_FLAGS)

                if (cursor.moveToFirst()) {
                    val path = pickedUri.getFilePath(ctx)

                    path?.also {
                        result = FileModel().apply {
                            originalFolder = path.substringBeforeLast("/")
                            name = path.substringAfterLast("/")
                            mimeType = cursor.getString(mimeTypeCol).lowercase()
                            size = cursor.getLong(sizeCol)
                            modifiedDate = (cursor.getLong(modifiedCol) / 1000).toInt()
                        }
                    }
                }
            }
        }

        return result
    }

    fun encryptFiles(fileModels: List<FileModel>, isOtherWay: Boolean = false): List<FileModel> {
        val filesToInsert = arrayListOf<FileModel>()

        for (model in fileModels) {
            // Assign model fields
            model.encryptedName = UUID.randomUUID().toString().replace(Regex("[-]"), "")
            model.encryptedType = Constants.EncryptedType.NONE

            // Save file into hidden folder
            val originalFile = File(model.getOriginalPath())
            val encryptFile = File(model.getEncryptedPath())
            if (encryptFile.exists()) {
                encryptFile.delete()
            }
            if (originalFile.exists()) {
                originalFile.copyTo(encryptFile, bufferSize = BUFFERED_SIZE)
                filesToInsert.add(model)

                // Delete original file on disk
                try {
                    if (model.isMediaFile()) {
                        /*if (isOtherWay) {
                            deleteFromMediaStore(model, true)
                        } else {
                            originalFile.delete()
                            deleteFromMediaStore(model, false)
                        }*/
                        deleteFromMediaStore(model, false)
                    } else {
                        originalFile.delete()
                    }

                    // Event tracking
                    val eventNameFileType = when {
                        model.isVideo() -> EventTrackingManager.HIDE_VIDEO_
                        model.isImage() -> EventTrackingManager.HIDE_PHOTO_
                        model.isAudio() -> EventTrackingManager.HIDE_AUDIO_
                        else -> EventTrackingManager.HIDE_OTHER_
                    } + (model.getExtension() ?: "")
                    ctx.eventTracking.logEvent(eventNameFileType)

                    val eventNameFileSize = when (model.size) {
                        in 0 until SIZE_1MB -> EventTrackingManager.FILE_SIZE_SMALL
                        in SIZE_1MB until SIZE_2MB -> EventTrackingManager.FILE_SIZE_1MB
                        in SIZE_2MB until SIZE_5MB -> EventTrackingManager.FILE_SIZE_2MB
                        in SIZE_5MB until SIZE_10MB -> EventTrackingManager.FILE_SIZE_5MB
                        in SIZE_10MB until SIZE_20MB -> EventTrackingManager.FILE_SIZE_10MB
                        in SIZE_20MB until SIZE_50MB -> EventTrackingManager.FILE_SIZE_20MB
                        in SIZE_50MB until SIZE_100MB -> EventTrackingManager.FILE_SIZE_50MB
                        in SIZE_100MB until SIZE_200MB -> EventTrackingManager.FILE_SIZE_100MB
                        in SIZE_200MB until SIZE_500MB -> EventTrackingManager.FILE_SIZE_200MB
                        in SIZE_500MB until SIZE_1GB -> EventTrackingManager.FILE_SIZE_500MB
                        in SIZE_1GB until SIZE_2GB -> EventTrackingManager.FILE_SIZE_1GB
                        in SIZE_2GB until SIZE_5GB -> EventTrackingManager.FILE_SIZE_2GB
                        in SIZE_5GB until SIZE_10GB -> EventTrackingManager.FILE_SIZE_5GB
                        else -> EventTrackingManager.FILE_SIZE_10GB
                    } + (model.getExtension() ?: "")
                    ctx.eventTracking.logEvent(eventNameFileSize)
                } catch (e: IOException) {
                    ctx.logE(e.message)
                }
            } else {
                // Refresh MediaStore immediately
                if (model.isMediaFile()) {
                    deleteFromMediaStore(model, false)
                }
            }
        }

        // Insert files to DB
        if (filesToInsert.isNotEmpty()) {
            dbInteractor.insertFiles(filesToInsert)
        }

        return filesToInsert
    }

    fun decryptFiles(fileModels: List<FileModel>, isOriginalPath: Boolean): List<FileModel> {
        for (model in fileModels) {
            val path = if (isOriginalPath) {
                model.getOriginalPath()
            } else {
                "${Constants.UNHIDDEN_FOLDER}${File.separator}${model.name}"
            }
            var targetFile = File(path)
            val encryptedFile = File(model.getEncryptedPath())
            if (encryptedFile.exists()) {
                // Move file to visible folder
                if (targetFile.exists()) {
                    val suffixTime =
                        DateTimeUtil.convertTimeStampToDate(
                            ctx.currentTimeInSecond, DateTimeUtil.YYYYMMDD_HHMMSS
                        )
                    val newPath = if (model.name.contains(".")) {
                        "${model.originalFolder}${File.separator}${model.name.substringBeforeLast(".")}_$suffixTime${model.getExtension()}"
                    } else {
                        "${model.originalFolder}${File.separator}${model.name}-$suffixTime"
                    }
                    targetFile = File(newPath)
                }
                encryptedFile.copyTo(targetFile, bufferSize = BUFFERED_SIZE)

                // Refresh MediaStore immediately
                if (model.isMediaFile()) {
                    val contentUri = Uri.fromFile(targetFile)
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri)
                    ctx.sendBroadcast(mediaScanIntent)
                }

                // Delete encrypted file on disk
                try {
                    encryptedFile.delete()
                } catch (e: IOException) {
                    ctx.logE(e.message)
                }
            }
        }

        // Delete files from DB
        dbInteractor.deleteFiles(fileModels)

        // Event tracking
        val eventName = if (isOriginalPath) {
            EventTrackingManager.UNHIDDEN_TO_ORIGINAL_FOLDER
        } else {
            EventTrackingManager.UNHIDDEN_TO_HIDE_S_FOLDER
        }
        ctx.eventTracking.logEvent(eventName)

        return fileModels
    }

    fun deleteFiles(fileModels: List<FileModel>): List<FileModel> {
        // Delete files from DB
        dbInteractor.deleteFiles(fileModels)

        // Delete encrypted file on disk
        for (model in fileModels) {
            val encryptedFile = File(model.getEncryptedPath())
            if (encryptedFile.exists()) {
                try {
                    encryptedFile.delete()
                } catch (e: IOException) {
                    ctx.logE(e.message)
                }
            }
        }

        return fileModels
    }

    fun exportNotes(notes: List<FileModel>, outputFile: File): String {
        // Concat notes
        val format = ctx.dateTimeFormat
        val content = StringBuilder()
        for (model in notes) {
            if (model.title?.isNotBlank() == true) {
                content.append("${model.title}\n")
            }
            if (model.content?.isNotBlank() == true) {
                content.append("${model.content}\n")
            }
            content.append(
                "${ctx.getString(R.string.last_modified)}: ${
                    DateTimeUtil.convertTimeStampToDate(model.modifiedDate, format)
                }\n"
            )
            content.append("** * **\n\n")
        }

        // Write text to file
        val folder = File(Constants.NOTE_FOLDER)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        outputFile.writeText(content.toString())

        return content.toString()
    }

    fun copyToCache(model: FileModel, isFromHiddenFolder: Boolean = false): FileModel {
        if (!model.isDirectory && !model.cacheFileExists()) {
            // Copy to cacheDir
            var sourcePath = model.getOriginalPath()
            var cachePath: String? =
                "${ctx.cacheDir}${File.separator}${model.encryptedName}${model.getExtension() ?: ""}"
            if (isFromHiddenFolder) {
                sourcePath = model.getEncryptedPath()
                cachePath = model.cachePath
            }
            cachePath?.run {
                val cacheFile = File(cachePath)
                File(sourcePath).copyTo(cacheFile).apply {
                    model.cachePath = absolutePath
                }
            }
        }

        return model
    }

    private fun deleteFromMediaStore(model: FileModel, isOtherWay: Boolean) {
        ctx.getUriFromString(model.getOriginalPath())?.run {
            /*if (isOtherWay) {
                try {
                    val documentFile = DocumentFile.fromSingleUri(ctx, this)
                    documentFile?.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val selection =
                    "${MediaStore.MediaColumns._ID} = ? OR ${MediaStore.MediaColumns.DATA} LIKE ?"
                val id = ctx.getIdFromUri(this)
                val selectionArgs: Array<String> = arrayOf(id, "%$id")
                val baseUri = when {
                    model.isImage() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    model.isVideo() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    model.isAudio() -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> null
                }
                baseUri?.also {
                    ctx.applicationContext.contentResolver.delete(baseUri, selection, selectionArgs)
                }
            }*/
            // Delete from disk
            val file = File(model.getOriginalPath())
            if (file.exists()) {
                file.delete()
            }

            // Delete from MediaStore
            val selection =
                "${MediaStore.MediaColumns._ID} = ? OR ${MediaStore.MediaColumns.DATA} LIKE ?"
            val id = ctx.getIdFromUri(this)
            val selectionArgs: Array<String> = arrayOf(id, "%$id")
            val baseUri = when {
                model.isImage() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                model.isVideo() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                model.isAudio() -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            }
            baseUri?.also {
                ctx.applicationContext.contentResolver.delete(baseUri, selection, selectionArgs)
            }
        }
    }

    private fun isNotContentScheme(uri: Uri): Boolean {
        return uri.run {
            !SCHEME_CONTENT.equals(scheme, true) || isExternalStorageDocument()
                    || isDownloadsDocument() || isMediaDocument() || isGooglePhotosUri()
        }
    }
}