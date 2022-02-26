package com.hide.videophoto.data.dao

import androidx.room.*
import com.hide.videophoto.common.Constants
import com.hide.videophoto.data.entity.FileEntity
import io.reactivex.Single

@Dao
interface FileDao {
    @Query("SELECT * FROM ${Constants.DB.FILE_ENTITY} ORDER BY ${Constants.DB.MODIFIED_DATE} DESC")
    fun getAllFile(): Single<List<FileEntity>>

    @Query("SELECT * FROM ${Constants.DB.FILE_ENTITY} WHERE ${Constants.DB.PARENT_ID} = :folderId ORDER BY ${Constants.DB.MODIFIED_DATE} DESC")
    fun getFilesByFolder(folderId: Long?): Single<List<FileEntity>>

    @Query("SELECT * FROM ${Constants.DB.FILE_ENTITY} WHERE ${Constants.DB.MIME_TYPE} LIKE :type||'%' ORDER BY ${Constants.DB.MODIFIED_DATE} DESC")
    fun getFilesByType(type: String): Single<List<FileEntity>>

    @Query(
        "SELECT * FROM ${Constants.DB.FILE_ENTITY}" +
                " WHERE ${Constants.DB.DIRECTORY} = 0" +
                " AND ${Constants.DB.MIME_TYPE} NOT LIKE '${Constants.DataType.VIDEO}%'" +
                " AND ${Constants.DB.MIME_TYPE} NOT LIKE '${Constants.DataType.IMAGE}%'" +
                " AND ${Constants.DB.NOTE} != 1 ORDER BY ${Constants.DB.MODIFIED_DATE} DESC"
    )
    fun getOtherFiles(): Single<List<FileEntity>>

    @Query("SELECT * FROM ${Constants.DB.FILE_ENTITY} WHERE ${Constants.DB.NOTE} = 1 ORDER BY ${Constants.DB.MODIFIED_DATE} DESC")
    fun getNotes(): Single<List<FileEntity>>

    @Query("SELECT * FROM ${Constants.DB.FILE_ENTITY} WHERE ${Constants.DB.DIRECTORY} = 1 ORDER BY ${Constants.DB.MODIFIED_DATE} DESC")
    fun getFolders(): Single<List<FileEntity>>

    @Query("SELECT * FROM ${Constants.DB.FILE_ENTITY} WHERE ${Constants.DB.ROWID} = :rowId")
    fun getFileDetail(rowId: Long): Single<FileEntity>

    @Query("SELECT SUM(${Constants.DB.SIZE}) FROM ${Constants.DB.FILE_ENTITY} WHERE ${Constants.DB.PARENT_ID} = :rowId")
    fun getFolderSize(rowId: Long): Single<Long>

    @Query("SELECT COUNT(*) FROM ${Constants.DB.FILE_ENTITY} WHERE ${Constants.DB.MIME_TYPE} LIKE :category||'%'")
    fun countFileByCategory(category: String): Single<Int>

    @Query(
        "SELECT COUNT(*) FROM ${Constants.DB.FILE_ENTITY}" +
                " WHERE ${Constants.DB.DIRECTORY} = 0" +
                " AND ${Constants.DB.MIME_TYPE} NOT LIKE '${Constants.DataType.IMAGE}%'" +
                " AND ${Constants.DB.MIME_TYPE} NOT LIKE '${Constants.DataType.AUDIO}%'" +
                " AND ${Constants.DB.MIME_TYPE} NOT LIKE '${Constants.DataType.VIDEO}%'"
    )
    fun countOtherFiles(): Single<Int>

    @Query("SELECT COUNT(*) FROM ${Constants.DB.FILE_ENTITY} WHERE ${Constants.DB.PARENT_ID} = :folderId")
    fun countFileByFolder(folderId: Long): Single<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg files: FileEntity): List<Long>

    @Delete
    fun delete(vararg files: FileEntity): Int

    @Update
    fun update(vararg files: FileEntity): Int
}