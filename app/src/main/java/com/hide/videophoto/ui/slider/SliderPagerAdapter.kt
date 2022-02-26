package com.hide.videophoto.ui.slider

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SliderPagerAdapter(frg: FragmentActivity, private val fragments: List<Fragment>) :
    FragmentStateAdapter(frg) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}