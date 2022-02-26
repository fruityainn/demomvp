package com.hide.videophoto.data.interactor

import android.content.Context
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.currentTimeInSecond
import com.hide.videophoto.data.mapper.convertToEntities
import com.hide.videophoto.data.mapper.convertToEntity
import com.hide.videophoto.data.mapper.convertToModel
import com.hide.videophoto.data.mapper.convertToModels
import com.hide.videophoto.data.model.ActivityModel
import com.hide.videophoto.data.model.AuthModel
import com.hide.videophoto.data.model.FileModel
import io.reactivex.Single

class DBInteractor(val ctx: Context) : BaseInteractor(ctx) {

    private val fileDao by lazy { roomDB.fileDao() }
    private val authDao by lazy { roomDB.authDao() }
    private val activityDao by lazy { roomDB.activityDao() }

    fun getFilesByFolder(folderId: Long?): Single<List<FileModel>> {
        return fileDao.getFilesByFolder(folderId ?: -1).map {
            it.convertToModels()
        }
    }

    fun getFilesByType(type: String): Single<List<FileModel>> {
        return fileDao.getFilesByType(type).map {
            it.convertToModels()
        }
    }

    fun getOtherFiles(): Single<List<FileModel>> {
        return fileDao.getOtherFiles().map {
            it.convertToModels()
        }
    }

    fun getNotes(): Single<List<FileModel>> {
        return fileDao.getNotes().map {
            it.convertToModels()
        }
    }

    fun getFolders(): Single<List<FileModel>> {
        return fileDao.getFolders().map {
            it.convertToModels()
        }
    }

    fun getFileDetail(rowId: Long?): Single<FileModel> {
        return fileDao.getFileDetail(rowId ?: -1).map {
            it.convertToModel()
        }
    }

    fun getFolderSize(rowId: Long?): Single<Long> {
        return fileDao.getFolderSize(rowId ?: -1)
    }

    fun countVideo(): Single<Int> {
        return fileDao.countFileByCategory(Constants.DataType.VIDEO)
    }

    fun countAudio(): Single<Int> {
        return fileDao.countFileByCategory(Constants.DataType.AUDIO)
    }

    fun countImage(): Single<Int> {
        return fileDao.countFileByCategory(Constants.DataType.IMAGE)
    }

    fun countOtherFiles(): Single<Int> {
        return fileDao.countOtherFiles()
    }

    fun countFileByFolder(folderId: Long?): Single<Int> {
        return fileDao.countFileByFolder(folderId ?: -1)
    }

    fun insertFiles(files: List<FileModel>) {
        files.map { file ->
            if (file.addedDate == null || file.addedDate == 0) {
                file.addedDate = ctx.currentTimeInSecond
            }
            if (file.modifiedDate == null || file.modifiedDate == 0) {
                file.modifiedDate = ctx.currentTimeInSecond
            }
        }
        fileDao.insert(*files.convertToEntities().toTypedArray()).zip(files).map {
            it.second.rowId = it.first
        }
    }

    fun updateFiles(files: List<FileModel>): Int {
        files.map { file ->
            file.modifiedDate = ctx.currentTimeInSecond
        }
        return fileDao.update(*files.convertToEntities().toTypedArray())
    }

    fun deleteFiles(files: List<FileModel>): Int {
        return fileDao.delete(*files.convertToEntities().toTypedArray())
    }

    fun login(password: String): Single<AuthModel> {
        return authDao.login(password).map {
            it.convertToModel()
        }
    }

    fun getBasicUserInfo(): Single<AuthModel> {
        return authDao.getBasicUserInfo().map {
            it.convertToModel()
        }
    }

    fun countUser(): Single<Int> {
        return authDao.countUser()
    }

    fun createUser(model: AuthModel): Single<Long> {
        return authDao.createUser(model.convertToEntity())
    }

    fun updateUser(model: AuthModel): Single<Int> {
        return authDao.updateUser(model.convertToEntity())
    }

    fun getActivity(): Single<ActivityModel> {
        return activityDao.getActivity().map {
            it.convertToModel()
        }
    }

    fun createActivity(model: ActivityModel): Single<Long> {
        return activityDao.createActivity(model.convertToEntity())
    }

    fun updateActivity(model: ActivityModel): Single<Int> {
        return activityDao.updateActivity(model.convertToEntity())
    }
}