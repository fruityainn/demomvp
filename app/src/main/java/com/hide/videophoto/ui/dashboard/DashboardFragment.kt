package com.hide.videophoto.ui.dashboard

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.ui.base.BaseFragment
import com.hide.videophoto.widget.DepthPageTransformer

class DashboardFragment : BaseFragment<DashboardView, DashboardPresenterImp>(), DashboardView {

    private lateinit var toolbarDashboard: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var imgLayout: ImageView
    private lateinit var imgNewFolder: ImageView

    private val dashboardAdapter by lazy { DashboardPagerAdapter(requireActivity()) }
    private val pagerListener by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 1) {
                    imgLayout.visible()
                    imgNewFolder.visible()
                } else {
                    imgLayout.gone()
                    imgNewFolder.gone()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPager.unregisterOnPageChangeCallback(pagerListener)
    }

    override fun initView(): DashboardView {
        return this
    }

    override fun initPresenter(): DashboardPresenterImp {
        return DashboardPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dashboard
    }

    override fun initWidgets(rootView: View) {
        // Find views
        rootView.run {
            toolbarDashboard = findViewById(R.id.toolbar_dashboard)
            tabLayout = findViewById(R.id.tabLayout_dashboard)
            viewPager = findViewById(R.id.pager_dashboard)
            imgLayout = findViewById(R.id.img_layout)
            imgNewFolder = findViewById(R.id.img_new_folder)
        }

        // Init pager
        viewPager.apply {
            adapter = dashboardAdapter
            setPageTransformer(DepthPageTransformer())
            registerOnPageChangeCallback(pagerListener)
            offscreenPageLimit = 2
        }
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val tabLabel = when (position) {
                0 -> getString(R.string.home)
                1 -> getString(R.string.folder)
                else -> ""
            }
            tab.text = tabLabel
        }.attach()

        // Set layout icon as list or grid
        updateLayoutIcon()

        // Listeners
        imgLayout.setOnSafeClickListener {
            (dashboardAdapter.getFragment(1) as FolderFragment).switchLayout()
            updateLayoutIcon()
        }

        imgNewFolder.setOnSafeClickListener {
            (dashboardAdapter.getFragment(1) as FolderFragment).showNewFolderDialog()
        }
    }

    private fun updateLayoutIcon() {
        if (Constants.Layout.GRID == ctx?.appSettingsModel?.layoutTypeFolder) {
            imgLayout.setImageResource(R.drawable.ic_menu_list)
        } else {
            imgLayout.setImageResource(R.drawable.ic_menu_grid)
        }
    }
}