package com.hide.videophoto.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class DashboardPagerAdapter(frg: FragmentActivity) : FragmentStateAdapter(frg) {

    private val fragments by lazy {
        arrayListOf<Fragment>().apply {
            add(HomeFragment())
            add(FolderFragment())
        }
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getFragment(position: Int): Fragment {
        return fragments[position]
    }
}