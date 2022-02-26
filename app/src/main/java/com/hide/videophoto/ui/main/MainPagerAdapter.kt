package com.hide.videophoto.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hide.videophoto.ui.dashboard.DashboardFragment
import com.hide.videophoto.ui.settings.SettingsFragment

class MainPagerAdapter(frg: FragmentActivity) : FragmentStateAdapter(frg) {

    private val fragments by lazy {
        arrayListOf<Fragment>().apply {
            add(DashboardFragment())
            add(SettingsFragment())
        }
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}