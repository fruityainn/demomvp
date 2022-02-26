package com.hide.videophoto.ui.main

import android.content.Context
import android.net.Uri
import com.hide.videophoto.common.ext.activityModel
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.appSettingsModel
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.common.util.RxBus
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.data.interactor.FileInteractor
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BasePresenterImp
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class MainPresenterImp(private val ctx: Context) : BasePresenterImp<MainView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }
    private val fileInteractor by lazy { FileInteractor(ctx) }

    fun getVideoFileDetail(uri: Uri) {
        view?.also { v ->
            Single.fromCallable {
                fileInteractor.loadVideos(uri)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        if (it.isNotEmpty()) {
                            v.onGettingFileDetailSuccess(it[0])
                        } else {
                            v.onGettingFileDetailError()
                        }
                    },
                    {
                        v.onGettingFileDetailError()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getAudioFileDetail(uri: Uri) {
        view?.also { v ->
            Single.fromCallable {
                fileInteractor.loadAudios(uri)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        if (it.isNotEmpty()) {
                            v.onGettingFileDetailSuccess(it[0])
                        } else {
                            v.onGettingFileDetailError()
                        }
                    },
                    {
                        v.onGettingFileDetailError()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getImageFileDetail(uri: Uri) {
        view?.also { v ->
            Single.fromCallable {
                fileInteractor.loadImages(uri)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        if (it.isNotEmpty()) {
                            v.onGettingFileDetailSuccess(it[0])
                        } else {
                            v.onGettingFileDetailError()
                        }
                    },
                    {
                        v.onGettingFileDetailError()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getOtherFileDetail(uri: Uri) {
        view?.also { v ->
            Single.fromCallable {
                fileInteractor.loadOtherFile(uri)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        it?.run {
                            v.onGettingFileDetailSuccess(it)
                        } ?: run {
                            v.onGettingFileDetailError()
                        }
                    },
                    {
                        v.onGettingFileDetailError()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun encryptFile(models: ArrayList<FileModel>) {
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

                        v.onFileEncryptedSuccess(it)

                        dismissProgressPercentageDialog()
                    },
                    {
                        dismissProgressPercentageDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getFolders() {
        view?.also { v ->
            dbInteractor.getFolders()
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

    fun saveLastAddedContent(content: String) {
        view?.also {
            ctx.activityModel.lastAddedContent = content
            dbInteractor.updateActivity(ctx.activityModel)
                .subscribeOn(Schedulers.io())
                .subscribe()
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun listenAppStateChanged() {
        view?.also { v ->
            RxBus.listenAppStateChanged()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onAppStateChanged(it)
                    },
                    {}
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun delayBeforeDoing(task: () -> Unit) {
        delayBeforeDoSomething(100) {
            task()
        }
    }
}