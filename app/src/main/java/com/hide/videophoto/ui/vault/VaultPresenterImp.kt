package com.hide.videophoto.ui.vault

import android.content.Context
import com.hide.videophoto.common.ext.activityModel
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.appSettingsModel
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.data.interactor.FileInteractor
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BasePresenterImp
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class VaultPresenterImp(private val ctx: Context) : BasePresenterImp<VaultView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }
    private val fileInteractor by lazy { FileInteractor(ctx) }

    fun getFilesByFolderFromDb(folderId: Long?) {
        view?.also { v ->
            dbInteractor.getFilesByFolder(folderId)
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFilesLoadedSuccess(it)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getFilesByTypeFromDb(type: String) {
        view?.also { v ->
            dbInteractor.getFilesByType(type)
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFilesLoadedSuccess(it)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getOtherFilesFromDb() {
        view?.also { v ->
            dbInteractor.getOtherFiles()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFilesLoadedSuccess(it)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getVideosForHiding() {
        view?.also { v ->
            showProgressDialog()

            Single.fromCallable {
                fileInteractor.loadVideos()
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFilesLoadedSuccess(it)

                        dismissProgressDialog()
                    },
                    {
                        dismissProgressDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getPhotosForHiding() {
        view?.also { v ->
            showProgressDialog()

            Single.fromCallable {
                fileInteractor.loadImages()
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFilesLoadedSuccess(it)

                        dismissProgressDialog()
                    },
                    {
                        dismissProgressDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getAudiosForHiding() {
        view?.also { v ->
            showProgressDialog()

            Single.fromCallable {
                fileInteractor.loadAudios()
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFilesLoadedSuccess(it)

                        dismissProgressDialog()
                    },
                    {
                        dismissProgressDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun encryptFile(models: List<FileModel>) {
        view?.also { v ->
            val totalSize = models.sumOf { it.size }
            showProgressPercentageDialog(getProgressPercentageDuration(totalSize))

            Single.fromCallable {
                fileInteractor.encryptFiles(models)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Count adding
                        ctx.appSettingsModel.apply {
                            countAddingFile += 1
                        }.run {
                            CommonUtil.saveAppSettingsModel(ctx, this)
                        }

                        v.onFilesEncryptedSuccess(it)

                        dismissProgressPercentageDialog()
                    },
                    {
                        dismissProgressPercentageDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getFolders(filesToMove: List<FileModel>) {
        view?.also { v ->
            dbInteractor.getFolders()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    { folders ->
                        v.onFoldersLoadedSuccess(folders, filesToMove)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun addNewFolder(folder: FileModel) {
        view?.also { v ->
            Single.fromCallable {
                // Insert into DB
                dbInteractor.insertFiles(listOf(folder))
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        v.onFolderAddedSuccess(folder)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getFolderDetail(file: FileModel) {
        view?.also { v ->
            dbInteractor.getFileDetail(file.parentId)
                .applyIOWithAndroidMainThread()
                .subscribe(
                    { folder ->
                        v.onFolderDetailLoadedSuccess(file, folder)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun saveLastAddedContent(content: String) {
        view?.also {
            ctx.activityModel.lastAddedContent = content
            dbInteractor.updateActivity(ctx.activityModel)
                .subscribeOn(Schedulers.io())
                .subscribe()
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun moveFiles(models: List<FileModel>) {
        view?.also { v ->
            Single.fromCallable {
                dbInteractor.updateFiles(models)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        if (it > 0) {
                            v.onFilesMovedSuccess(models)
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun rename(model: FileModel) {
        view?.also { v ->
            Single.fromCallable {
                dbInteractor.updateFiles(listOf(model))
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        if (it > 0) {
                            v.onFileRenamedSuccess(model)
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun deleteFiles(models: List<FileModel>) {
        view?.also { v ->
            Single.fromCallable {
                fileInteractor.deleteFiles(models)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        v.onFilesDeletedSuccess(models)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun unhideFiles(models: List<FileModel>, isOriginalPath: Boolean) {
        view?.also { v ->
            val totalSize = models.sumOf { it.size }
            showProgressPercentageDialog(getProgressPercentageDuration(totalSize))

            Single.fromCallable {
                fileInteractor.decryptFiles(models, isOriginalPath)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        v.onFilesUnhiddenSuccess(models)

                        dismissProgressPercentageDialog()
                    },
                    {
                        dismissProgressPercentageDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun doingInBackground(doing: () -> Unit, done: (() -> Unit)? = null) {
        view?.also {
            Single.fromCallable {
                doing()
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        done?.invoke()
                    },
                    {
                        it.printStackTrace()
                    }
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }
}