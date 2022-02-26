package com.hide.videophoto.ui.onboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hide.videophoto.R

class OnboardPagerAdapter(frg: FragmentActivity) : FragmentStateAdapter(frg) {

    private val fragments by lazy {
        arrayListOf<Fragment>().apply {
            add(
                OnboardFragment.newInstance(
                    R.drawable.img_onboard_1,
                    R.string.onboard_title_1,
                    R.string.onboard_desc_1
                )
            )
            add(
                OnboardFragment.newInstance(
                    R.drawable.img_onboard_2,
                    R.string.onboard_title_2,
                    R.string.onboard_desc_2
                )
            )
            add(
                OnboardFragment.newInstance(
                    R.drawable.img_onboard_3,
                    R.string.onboard_title_3,
                    R.string.onboard_desc_3
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}