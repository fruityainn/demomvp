package com.hide.videophoto.ui.note

import android.content.Context
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BasePresenterImp
import io.reactivex.Single

class AddingNotePresenterImp(private val ctx: Context) : BasePresenterImp<AddingNoteView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }

    fun addNote(model: FileModel) {
        view?.also { v ->
            Single.fromCallable {
                // Insert into DB
                dbInteractor.insertFiles(listOf(model))
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        v.onNoteAddedSuccess(model)
                    },
                    {
                        v.onQueryDbError()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun updateNote(model: FileModel) {
        view?.also { v ->
            Single.fromCallable {
                dbInteractor.updateFiles(listOf(model))
            }.applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        // Notify to view
                        v.onNoteEditedSuccess(model)
                    },
                    {
                        v.onQueryDbError()
                        it.printStackTrace()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }
}