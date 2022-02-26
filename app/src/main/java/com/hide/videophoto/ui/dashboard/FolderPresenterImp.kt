package com.hide.videophoto.ui.dashboard

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.common.util.RxBus
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.data.interactor.FileInteractor
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BasePresenterImp
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class FolderPresenterImp(private val ctx: Context) : BasePresenterImp<FolderView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }
    private val fileInteractor by lazy { FileInteractor(ctx) }

    fun getFolders() {
        view?.also { v ->
            dbInteractor.getFolders()
                .flatMap { files ->
                    files.map { file ->
                        dbInteractor.countFileByFolder(file.rowId)
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                { qty ->
                                    file.itemQuantity = qty
                                },
                                {
                                    it.printStackTrace()
                                })
                    }
                    return@flatMap Single.just(files)
                }
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFoldersLoadedSuccess(it)
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

    fun rename(model: FileModel) {
        view?.also { v ->
            Single.fromCallable {
                dbInteractor.updateFiles(listOf(model))
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        if (it > 0) {
                            v.onFolderRenamedSuccess(model)
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun deleteFolder(folder: FileModel, isAlsoDeleteFolder: Boolean) {
        view?.also { v ->
            dbInteractor.getFilesByFolder(folder.rowId)
                .flatMap {
                    val filesToDelete = ArrayList(it)
                    if (isAlsoDeleteFolder) {
                        filesToDelete.add(folder)
                    }
                    val result = fileInteractor.deleteFiles(filesToDelete)

                    return@flatMap Single.just(result.size)
                }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFilesDeletedSuccess(folder)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun unhideFolder(folder: FileModel, isOriginalPath: Boolean) {
        view?.also { v ->
            dbInteractor.getFilesByFolder(folder.rowId)
                .flatMap { filesToUnhide ->
                    (ctx as AppCompatActivity).runOnUiThread {
                        val totalSize = filesToUnhide.sumOf { it.size }
                        showProgressPercentageDialog(getProgressPercentageDuration(totalSize))
                    }

                    val result = fileInteractor.decryptFiles(filesToUnhide, isOriginalPath)

                    return@flatMap Single.just(result.size)
                }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFilesUnhiddenSuccess(folder)

                        dismissProgressPercentageDialog()
                    },
                    {
                        dismissProgressPercentageDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getFolderSize(folder: FileModel) {
        view?.also { v ->
            dbInteractor.getFolderSize(folder.rowId)
                .applyIOWithAndroidMainThread()
                .subscribe(
                    { size ->
                        v.onGettingFolderSizeSuccess(folder, size)
                    },
                    {
                        v.onGettingFolderSizeSuccess(folder, 0)
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun listenFolderChangedEvent() {
        view?.also { v ->
            RxBus.listenFolderChanged()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onFolderChangedEvent()
                    },
                    {
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }
}