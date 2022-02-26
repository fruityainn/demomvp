package com.hide.videophoto.ui.security.other

import android.content.Context
import com.hide.videophoto.common.ext.activityModel
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.ui.base.BasePresenterImp

class OtherPresenterImp(private val ctx: Context) : BasePresenterImp<OtherView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }

    fun countFailTimes() {
        view?.also { v ->
            ctx.activityModel.failedResetTimes += 1
            dbInteractor.updateActivity(ctx.activityModel)
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        if (it > 0) {
                            v.onUpdateActivitySuccess()
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }
}