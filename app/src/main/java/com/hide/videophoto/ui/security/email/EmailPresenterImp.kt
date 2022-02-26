package com.hide.videophoto.ui.security.email

import android.content.Context
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.common.ext.authModel
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.ui.base.BasePresenterImp

class EmailPresenterImp(private val ctx: Context) : BasePresenterImp<EmailView>(ctx) {

    private val dbInteractor by lazy { DBInteractor(ctx) }

    fun setRecoveryEmail(email: String) {
        view?.also { v ->
            ctx.authModel.email = email.lowercase()
            dbInteractor.updateUser(ctx.authModel)
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        if (it > 0) {
                            v.onSettingEmailSuccess()
                        } else {
                            ctx.authModel.email = null

                            v.onQueryDbError()
                        }
                    },
                    {
                        ctx.authModel.email = null

                        v.onQueryDbError()
                    }
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }
}