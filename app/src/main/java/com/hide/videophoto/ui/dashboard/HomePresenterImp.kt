package com.hide.videophoto.ui.dashboard

import android.content.Context
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.common.util.RxBus
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.ui.base.BasePresenterImp
import io.reactivex.Single

class HomePresenterImp(private val ctx: Context) : BasePresenterImp<HomeView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }

    fun countFiles() {
        view?.also { v ->
            showProgressDialog()

            Single.zip(
                dbInteractor.countImage(),
                dbInteractor.countVideo(),
                dbInteractor.countAudio(),
                dbInteractor.countOtherFiles(),
                { images, videos, audios, others ->
                    listOf(images, videos, audios, others)
                }
            ).applyIOWithAndroidMainThread()
                .subscribe(
                    { results ->
                        // v.onCountingSuccess(results[0], results[1], results[2], results[3])

                        dismissProgressDialog()
                    },
                    {
                        dismissProgressDialog()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun listenAppSettingsChanged() {
        view?.also { v ->
            RxBus.listenAppSettingsChanged()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onAppSettingsChanged(it)
                    },
                    {}
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }
}