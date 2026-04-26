package com.liveo.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    private val channels: List<Channel>,
    private val prefsManager: PreferencesManager
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AllChannelsFragment.newInstance(channels, prefsManager)
            1 -> FavoritesFragment.newInstance(prefsManager)
            2 -> RecentFragment.newInstance(prefsManager)
            else -> AllChannelsFragment.newInstance(channels, prefsManager)
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "الكل"
            1 -> "المفضلة"
            2 -> "الأخيرة"
            else -> ""
        }
    }
}
