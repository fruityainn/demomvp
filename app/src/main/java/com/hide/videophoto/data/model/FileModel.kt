package com.hide.videophoto.data.model

import com.hide.videophoto.common.Constants
import java.io.File

class FileModel : BaseModel() {
    var rowId: Long? = null

    var fileId: Long? = null

    var name: String = ""

    var mimeType: String? = null

    var encryptedName: String? = null

    var parentId: Long? = null

    var bucketName: String? = null

    var originalFolder: String? = null

    var cachePath: String? = null

    var size: Long = 0 // As byte

    var uri: String? = null

    var addedDate: Int? = null // As second

    var modifiedDate: Int? = null // As second

    var duration: Int = 0 // As millisecond

    var title: String? = null

    var artist: String? = null

    var content: String? = null

    var orientation: Int? = null

    var resolution: String? = null

    var encryptedType: String? = null // Encrypted algorithm

    var isDirectory: Boolean = false

    var isFavorite: Boolean = false

    var isNote: Boolean = false

    var isSelected: Boolean = false

    var itemQuantity: Int = 0

    fun getExtension(): String? {
        return if (name.contains(".")) {
            name.substring(name.lastIndexOf("."))
        } else {
            null
        }
    }

    fun getEncryptedPath(): String {
        return Constants.APP_ROOT_FOLDER + File.separator + encryptedName
    }

    fun getOriginalPath(): String {
        return originalFolder + File.separator + name
    }
}