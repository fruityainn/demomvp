package com.hide.videophoto.ui.splash

import android.content.Context
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.ui.base.BasePresenterImp

class SplashPresenterImp(private val ctx: Context) : BasePresenterImp<SplashView>(ctx) {

    fun start() {
        view?.also { v ->
            if (CommonUtil.isStoragePermissionGranted(ctx)) {
                v.openPasswordPage()
            } else {
                v.openOnboardPage()
            }
        }
    }
}