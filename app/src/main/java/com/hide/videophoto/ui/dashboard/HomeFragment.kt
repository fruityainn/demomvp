package com.hide.videophoto.ui.dashboard

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.ExternalStoragePermissionUtil
import com.hide.videophoto.data.model.EventAppSettingsModel
import com.hide.videophoto.ui.base.BaseFragment
import com.hide.videophoto.ui.note.NoteActivity
import com.hide.videophoto.ui.vault.VaultActivity

class HomeFragment : BaseFragment<HomeView, HomePresenterImp>(), HomeView {

    private lateinit var frlVideo: FrameLayout
    private lateinit var lblVideo: TextView
    private lateinit var frlPhoto: FrameLayout
    private lateinit var lblPhoto: TextView
    private lateinit var frlNote: FrameLayout
    private lateinit var lblNote: TextView
    private lateinit var frlOther: FrameLayout
    private lateinit var lblOther: TextView
    private lateinit var frlNativeAdHome: FrameLayout

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)
    private val itemWidth by lazy {
        (CommonUtil.getRealScreenSizeAsPixels(parentActivity).x - CommonUtil.convertDpToPixel(
            ctx, R.dimen.dimen_24
        ) * 3) / 2
    }

    override fun initView(): HomeView {
        return this
    }

    override fun initPresenter(): HomePresenterImp {
        return HomePresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun initWidgets(rootView: View) {
        // Find views
        rootView.run {
            frlVideo = findViewById(R.id.frl_video)
            lblVideo = findViewById(R.id.lbl_video)
            frlPhoto = findViewById(R.id.frl_photo)
            lblPhoto = findViewById(R.id.lbl_photo)
            frlNote = findViewById(R.id.frl_note)
            lblNote = findViewById(R.id.lbl_note)
            frlOther = findViewById(R.id.frl_others)
            lblOther = findViewById(R.id.lbl_other)
            frlNativeAdHome = findViewById(R.id.frl_native_ad_home)

            frlVideo.calRatio(itemWidth, 0.75f)
            frlPhoto.calRatio(itemWidth, 0.75f)
            frlNote.calRatio(itemWidth, 0.75f)
            frlOther.calRatio(itemWidth, 0.75f)
        }

        // Show native ad
        ctx?.adsManager?.showNativeAdHome(frlNativeAdHome)

        // Listeners
        frlPhoto.setOnSafeClickListener {
            showAllFilesByCategory(Constants.DataType.IMAGE)

            // Event tracking
            ctx?.eventTracking?.logEvent(EventTrackingManager.CLICK_HOME_ITEM_PHOTO)
        }

        frlVideo.setOnSafeClickListener {
            showAllFilesByCategory(Constants.DataType.VIDEO)

            // Event tracking
            ctx?.eventTracking?.logEvent(EventTrackingManager.CLICK_HOME_ITEM_VIDEO)
        }

        frlNote.setOnSafeClickListener {
            openActivity(NoteActivity::class.java)

            // Event tracking
            ctx?.eventTracking?.logEvent(EventTrackingManager.CLICK_HOME_ITEM_NOTE)
        }

        frlOther.setOnSafeClickListener {
            showAllFilesByCategory(Constants.DataType.OTHER)

            // Event tracking
            ctx?.eventTracking?.logEvent(EventTrackingManager.CLICK_HOME_ITEM_OTHER)
        }

        presenter.listenAppSettingsChanged()
    }

    override fun onAppSettingsChanged(model: EventAppSettingsModel) {
        when (model.event) {
            Constants.Event.CHANGE_LANGUAGE -> {
                model.resources?.also { res ->
                    lblVideo.text = res.getString(R.string.video)
                    lblPhoto.text = res.getString(R.string.photo)
                    lblNote.text = res.getString(R.string.note)
                    lblOther.text = res.getString(R.string.other)
                }
            }
            Constants.Event.REMOVE_AD -> {
                frlNativeAdHome.gone()
            }
        }
    }

    override fun onDestroyAds() {
        ctx?.adsManager?.destroyNativeAdHome()
    }

    private fun showAllFilesByCategory(type: String) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                ctx?.run {
                    VaultActivity.start(ctx, isShowingFileByType = true, dataType = type)
                }
            }

            override fun onDenied() {
                ctx?.toast(R.string.msg_permission_storage)
            }
        })
    }
}