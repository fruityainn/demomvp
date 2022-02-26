package com.hide.videophoto.ui.settings

import android.widget.TextView
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.ctx
import com.hide.videophoto.common.ext.setOnSafeClickListener
import com.hide.videophoto.ui.base.BaseActivity

class TipsActivity : BaseActivity<TipsView, TipsPresenterImp>(), TipsView {

    private val btnConfirm by lazy { findViewById<TextView>(R.id.btn_confirm) }

    override fun initView(): TipsView {
        return this
    }

    override fun initPresenter(): TipsPresenterImp {
        return TipsPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_tips
    }

    override fun initWidgets() {
        // Init toolbar
        enableHomeAsUp {
            finish()
        }
        showTitle(R.string.files_anti_lost)

        // Listener
        btnConfirm.setOnSafeClickListener {
            finish()
        }
    }
}
