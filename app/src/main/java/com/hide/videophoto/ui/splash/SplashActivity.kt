package com.hide.videophoto.ui.splash

import android.os.Bundle
import com.hide.videophoto.common.ext.ctx
import com.hide.videophoto.common.ext.openActivity
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.ui.onboard.OnboardActivity
import com.hide.videophoto.ui.security.password.PasswordActivity

class SplashActivity : BaseActivity<SplashView, SplashPresenterImp>(), SplashView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.start()
    }

    override fun initView(): SplashView {
        return this
    }

    override fun initPresenter(): SplashPresenterImp {
        return SplashPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return null
    }

    override fun initWidgets() {
        hideNavigationBar()
    }

    override fun openOnboardPage() {
        openActivity(OnboardActivity::class.java)
    }

    override fun openPasswordPage() {
        PasswordActivity.start(self)
    }
}
