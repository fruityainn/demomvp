package com.hide.videophoto.ui.slider

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.data.mapper.convertToFileModels
import com.hide.videophoto.data.mapper.toJson
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.widget.DepthPageTransformer

class SliderActivity : BaseActivity<SliderView, SliderPresenterImp>(), SliderView,
    PhotoFragment.Listener {

    private val toolbarSlider by lazy { findViewById<Toolbar>(R.id.toolbar_slider) }
    private val viewPager by lazy { findViewById<ViewPager2>(R.id.pager_slider) }

    private val fileModels by lazy { arrayListOf<FileModel>() }
    private var startPosition = 0
    private var menuItemRotatePortrait: MenuItem? = null
    private var menuItemRotateLandscape: MenuItem? = null

    companion object {
        fun start(ctx: Context, fileModels: List<FileModel>, startPosition: Int) {
            Intent(ctx, SliderActivity::class.java).apply {
                putExtra(Constants.Key.FILE_MODELS, fileModels.toJson())
                putExtra(Constants.Key.POSITION, startPosition)
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if device has navigation bar or not
        if (appSettingsModel.hasNavigationBar == null) {
            val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
            appSettingsModel.run {
                hasNavigationBar = id > 0 && resources.getBoolean(id)
                CommonUtil.saveAppSettingsModel(ctx, this)
            }
        }
    }

    override fun onBackPressed() {
        if (isSensorLandscapeOrientation()) {
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
            if (appSettingsModel.hasNavigationBar == true) {
                menuItemRotatePortrait?.setIcon(R.drawable.ic_menu_rotate_landscape)
            } else {
                menuItemRotatePortrait?.isVisible = true
            }
            menuItemRotateLandscape?.isVisible = false
        } else {
            adsManager.showInterstitialAd(self) {
                super.onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_slider, menu)
        menuItemRotatePortrait = menu?.findItem(R.id.menu_rotate_portrait)
        menuItemRotateLandscape = menu?.findItem(R.id.menu_rotate_landscape)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_rotate_portrait -> {
                rotateScreen()
                if (appSettingsModel.hasNavigationBar == true) {
                    menuItemRotatePortrait?.icon = null
                } else {
                    menuItemRotatePortrait?.isVisible = false
                }
                menuItemRotateLandscape?.isVisible = true
            }
            R.id.menu_rotate_landscape -> {
                rotateScreen()
                if (appSettingsModel.hasNavigationBar == true) {
                    menuItemRotatePortrait?.setIcon(R.drawable.ic_menu_rotate_landscape)
                } else {
                    menuItemRotatePortrait?.isVisible = true
                }
                menuItemRotateLandscape?.isVisible = false
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun initView(): SliderView {
        return this
    }

    override fun initPresenter(): SliderPresenterImp {
        return SliderPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_slider
    }

    override fun initWidgets() {
        // Get data from intent first
        getValuesFromExtras(intent)

        // Init toolbar
        applyToolbar(toolbarSlider)
        val marginLayoutParams = toolbarSlider.layoutParams as ViewGroup.MarginLayoutParams?
        marginLayoutParams?.topMargin = CommonUtil.getHeightOfStatusBar(self)
        toolbarSlider.requestLayout()

        enableHomeAsUp {
            onBackPressed()
        }

        // Init slider
        if (fileModels.isNotEmpty()) {
            val pages = arrayListOf<Fragment>()
            for (model in fileModels) {
                pages.add(PhotoFragment.newInstance(model))
            }

            viewPager.apply {
                adapter = SliderPagerAdapter(self, pages)
                setCurrentItem(startPosition, false)
                setPageTransformer(DepthPageTransformer())
            }
        }
    }

    override fun onPhotoClicked(model: FileModel) {
        if (toolbarSlider.isVisible) {
            toolbarSlider.gone()
            hideNavigationBar()
        } else {
            toolbarSlider.visible()
            showNavigationBar()
        }
    }

    private fun getValuesFromExtras(intent: Intent?) {
        intent?.extras?.run {
            if (containsKey(Constants.Key.FILE_MODELS)) {
                val json = getString(Constants.Key.FILE_MODELS)
                json?.run {
                    fileModels.addAll(convertToFileModels())
                }
            }
            if (containsKey(Constants.Key.POSITION)) {
                startPosition = getInt(Constants.Key.POSITION)
            }
        }
    }
}
