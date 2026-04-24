package com.liveo.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    activity: FragmentActivity,
    private val channels: List<Channel>,
    private val prefsManager: PreferencesManager
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllChannelsFragment.newInstance(channels, prefsManager)
            1 -> FavoritesFragment.newInstance(prefsManager)
            2 -> RecentFragment.newInstance(prefsManager)
            else -> AllChannelsFragment.newInstance(channels, prefsManager)
        }
    }
}
