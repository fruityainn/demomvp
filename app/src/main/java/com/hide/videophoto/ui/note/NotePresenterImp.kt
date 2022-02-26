package com.hide.videophoto.ui.note

import android.content.Context
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.common.ext.logE
import com.hide.videophoto.common.util.RxBus
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.data.interactor.FileInteractor
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BasePresenterImp
import io.reactivex.Single
import java.io.File

class NotePresenterImp(private val ctx: Context) : BasePresenterImp<NoteView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }
    private val fileInteractor by lazy { FileInteractor(ctx) }

    fun getNotesFromDb() {
        view?.also { v ->
            dbInteractor.getNotes()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onNotesLoadedSuccess(it)
                    },
                    {
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun deleteNotes(models: List<FileModel>) {
        view?.also { v ->
            showProgressDialog()

            Single.fromCallable {
                fileInteractor.deleteFiles(models)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        v.onNotesDeletedSuccess(models)

                        dismissProgressDialog()
                    },
                    {
                        dismissProgressDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun exportNotes(models: List<FileModel>, outputFile: File) {
        view?.also { v ->
            showProgressDialog()

            Single.fromCallable {
                fileInteractor.exportNotes(models, outputFile)
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        ctx.logE(it)
                        // Notify to view
                        v.onNotesExportedSuccess(models)

                        dismissProgressDialog()
                    },
                    {
                        dismissProgressDialog()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun listenNoteChangedEvent() {
        view?.also { v ->
            RxBus.listenNoteChanged()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onNoteChangedEvent(it)
                    },
                    {
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }
}