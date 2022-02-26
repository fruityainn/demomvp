package com.hide.videophoto.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hide.videophoto.common.Constants

@Entity(tableName = Constants.DB.FILE_ENTITY)
class FileEntity : BaseEntity() {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Constants.DB.ROWID)
    var rowId: Long? = null

    @ColumnInfo(name = Constants.DB.NAME)
    var name: String? = null

    @ColumnInfo(name = Constants.DB.MIME_TYPE)
    var mimeType: String? = null

    @ColumnInfo(name = Constants.DB.ENCRYPTED_NAME)
    var encryptedName: String? = null

    @ColumnInfo(name = Constants.DB.PARENT_ID)
    var parentId: Long? = null

    @ColumnInfo(name = Constants.DB.ORIGINAL_FOLDER)
    var originalFolder: String? = null

    @ColumnInfo(name = Constants.DB.CACHE_PATH)
    var cachePath: String? = null

    @ColumnInfo(name = Constants.DB.SIZE)
    var size: Long? = null

    @ColumnInfo(name = Constants.DB.DURATION)
    var duration: Int? = null

    @ColumnInfo(name = Constants.DB.TITLE)
    var title: String? = null

    @ColumnInfo(name = Constants.DB.ARTIST)
    var artist: String? = null

    @ColumnInfo(name = Constants.DB.CONTENT)
    var content: String? = null

    @ColumnInfo(name = Constants.DB.ORIENTATION)
    var orientation: Int? = null

    @ColumnInfo(name = Constants.DB.RESOLUTION)
    var resolution: String? = null

    @ColumnInfo(name = Constants.DB.ADDED_DATE)
    var addedDate: Int? = null

    @ColumnInfo(name = Constants.DB.MODIFIED_DATE)
    var modifiedDate: Int? = null

    @ColumnInfo(name = Constants.DB.ENCRYPTED_TYPE)
    var encryptedType: String? = null

    @ColumnInfo(name = Constants.DB.DIRECTORY)
    var directory: Int = 0

    @ColumnInfo(name = Constants.DB.FAVORITE)
    var favorite: Int = 0

    @ColumnInfo(name = Constants.DB.NOTE)
    var note: Int = 0
}