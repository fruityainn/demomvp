package com.hide.videophoto.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.data.entity.FileEntity
import com.hide.videophoto.data.model.FileModel
import java.io.File
import java.lang.reflect.Type

fun FileEntity.convertToModel(): FileModel {
    val entity = this
    return FileModel().apply {
        rowId = entity.rowId
        name = entity.name ?: ""
        mimeType = entity.mimeType
        encryptedName = entity.encryptedName
        parentId = entity.parentId
        originalFolder = entity.originalFolder
        cachePath = entity.cachePath
        size = entity.size ?: 0
        addedDate = entity.addedDate
        modifiedDate = entity.modifiedDate
        duration = entity.duration ?: 0
        title = entity.title
        artist = entity.artist
        content = entity.content
        orientation = entity.orientation
        resolution = entity.resolution
        encryptedType = entity.encryptedType
        isDirectory = entity.directory == 1
        isFavorite = entity.favorite == 1
        isNote = entity.note == 1
    }
}

fun List<FileEntity>.convertToModels(): ArrayList<FileModel> {
    return ArrayList(
        map {
            it.convertToModel()
        }
    )
}

fun FileModel.convertToEntity(): FileEntity {
    val model = this
    return FileEntity().apply {
        rowId = model.rowId
        name = model.name
        mimeType = model.mimeType
        encryptedName = model.encryptedName
        parentId = model.parentId
        originalFolder = model.originalFolder
        cachePath = model.cachePath
        size = model.size
        addedDate = model.addedDate
        modifiedDate = model.modifiedDate
        duration = model.duration
        title = model.title
        artist = model.artist
        content = model.content
        orientation = model.orientation
        resolution = model.resolution
        encryptedType = model.encryptedType
        directory = if (model.isDirectory) {
            1
        } else {
            0
        }
        favorite = if (model.isFavorite) {
            1
        } else {
            0
        }
        note = if (model.isNote) {
            1
        } else {
            0
        }
    }
}

fun List<FileModel>.convertToEntities(): ArrayList<FileEntity> {
    return ArrayList(
        map {
            it.convertToEntity()
        }
    )
}

fun FileModel.mergeWith(model: FileModel?) {
    model?.also {
        name = model.name
        parentId = model.parentId
        modifiedDate = model.modifiedDate
    }
}

fun FileModel.cacheFileExists(): Boolean {
    return cachePath?.let {
        File(it).exists()
    } ?: false
}

fun List<FileModel>.toJson(): String {
    return Gson().toJson(this)
}

fun String.convertToFileModel(): FileModel {
    return Gson().fromJson(this, FileModel::class.java)
}

fun String.convertToFileModels(): List<FileModel> {
    val type: Type = object : TypeToken<List<FileModel>>() {}.type
    return Gson().fromJson(this, type)
}

fun FileModel.isVideo(): Boolean {
    return mimeType?.startsWith(Constants.DataType.VIDEO, true) ?: false
}

fun FileModel.isAudio(): Boolean {
    return mimeType?.startsWith(Constants.DataType.AUDIO, true) ?: false
}

fun FileModel.isImage(): Boolean {
    return mimeType?.startsWith(Constants.DataType.IMAGE, true) ?: false
}

fun FileModel.isPdf(): Boolean {
    return getExtension()?.equals(".pdf", true) ?: false
}

fun FileModel.isDoc(): Boolean {
    return getExtension()?.equals(".doc", true) ?: getExtension()?.equals(".docx", true)
    ?: false
}

fun FileModel.isXls(): Boolean {
    return getExtension()?.equals(".xls", true) ?: getExtension()?.equals(".xlsx", true)
    ?: getExtension()?.equals(".xlt", true) ?: getExtension()?.equals(".xlsb", true)
    ?: getExtension()?.equals(".xlsm", true) ?: false
}

fun FileModel.isPpt(): Boolean {
    return getExtension()?.equals(".ppt", true) ?: getExtension()?.equals(".pptx", true)
    ?: getExtension()?.equals(".pptm", true) ?: false
}

fun FileModel.isTxt(): Boolean {
    return getExtension()?.equals(".txt", true) ?: false
}

fun FileModel.isCsv(): Boolean {
    return getExtension()?.equals(".csv", true) ?: false
}

fun FileModel.isZip(): Boolean {
    return getExtension()?.equals(".zip", true) ?: getExtension()?.equals(".rar", true)
    ?: getExtension()?.equals(".zipx", true) ?: false
}

fun FileModel.isMediaSupported(): Boolean {
    return Constants.mediaSupportedFormats.contains(getExtension()?.lowercase())
}

fun FileModel.isMediaFile(): Boolean {
    return isVideo() || isAudio() || isImage()
}

fun FileModel.getThumbnail(): Any {
    return when {
        isVideo() || isImage() -> getEncryptedPath()
        isAudio() -> R.drawable.ic_headphones
        isPdf() -> R.drawable.ic_pdf
        isDoc() -> R.drawable.ic_doc
        isXls() -> R.drawable.ic_xls
        isPpt() -> R.drawable.ic_ppt
        isTxt() -> R.drawable.ic_txt
        isCsv() -> R.drawable.ic_csv
        isZip() -> R.drawable.ic_zip
        else -> R.drawable.ic_file
    }
}
