package com.liveo.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(
    fm: FragmentManager,
    private val allChannels: List<Channel>,
    private val prefsManager: PreferencesManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    
    private val tabs = listOf(
        "الكل",
        "المفضلة",
        "الأخيرة"
    )
    
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                // الكل
                AllChannelsFragment.newInstance(allChannels, prefsManager)
            }
            1 -> {
                // المفضلة
                FavoritesFragment.newInstance(prefsManager)
            }
            2 -> {
                // الأخيرة
                RecentFragment.newInstance(prefsManager)
            }
            else -> AllChannelsFragment.newInstance(allChannels, prefsManager)
        }
    }
    
    override fun getCount(): Int = tabs.size
    
    override fun getPageTitle(position: Int): CharSequence = tabs[position]
}
