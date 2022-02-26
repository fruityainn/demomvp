package com.hide.videophoto.ui.onboard

import android.os.Bundle
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.DialogUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.ExternalStoragePermissionUtil
import com.hide.videophoto.common.util.PermissionUtil
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.ui.security.password.PasswordActivity
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class OnboardActivity : BaseActivity<OnboardView, OnboardPresenterImp>(), OnboardView {

    private val viewPager by lazy { findViewById<ViewPager2>(R.id.pager_onboard) }
    private val indicator by lazy { findViewById<DotsIndicator>(R.id.indicator) }
    private val btnStart by lazy { findViewById<TextView>(R.id.btn_start) }

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)

    override fun onCreate(savedInstanceState: Bundle?) {
        hideNavigationBar()
        super.onCreate(savedInstanceState)
    }

    override fun initView(): OnboardView {
        return this
    }

    override fun initPresenter(): OnboardPresenterImp {
        return OnboardPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_onboard
    }

    override fun initWidgets() {
        // Init toolbar
        hideToolbarBase()

        // Init slider
        viewPager.adapter = OnboardPagerAdapter(self)
        indicator.setViewPager2(viewPager)

        // Listener
        btnStart.setOnSafeClickListener {
            if (PermissionUtil.isApi30orHigher()) {
                DialogUtil.showConfirmationDialog(
                    ctx,
                    R.string.title_grant_permission,
                    R.string.msg_permission_storage,
                    textOk = R.string.ok,
                    textCancel = R.string.cancel,
                    okListener = {
                        requestStoragePermission()
                    }
                )
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun requestStoragePermission() {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                // Event tracking
                if (viewPager.currentItem == 0) {
                    eventTracking.logEvent(EventTrackingManager.SKIP_ONBOARD)
                }

                // Go to Password page
                PasswordActivity.start(self)

                // Finish this activity
                finish()
            }

            override fun onDenied() {
                toast(R.string.msg_permission_storage, true)
            }
        })
    }
}
