package com.liveo.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(
    fm: FragmentManager,
    private val allChannels: List<Channel>,
    private val prefsManager: PreferencesManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    
    private val categories = listOf("الكل", "قنوات مباشرة", "أفلام", "مسلسلات", "موسيقى", "المفضلة", "الأخيرة")
    
    override fun getItem(position: Int): Fragment {
        val filteredChannels = if (prefsManager.isParentalUnlocked() || !prefsManager.hasAdultAccess()) {
            allChannels
        } else {
            allChannels.filter { !it.isAdult }
        }
        
        return when (position) {
            0 -> AllChannelsFragment.newInstance(filteredChannels, prefsManager)
            1 -> CategoryWithSubsFragment.newInstance(filteredChannels, prefsManager, "قنوات")
            2 -> CategoryWithSubsFragment.newInstance(filteredChannels, prefsManager, "أفلام")
            3 -> CategoryWithSubsFragment.newInstance(filteredChannels, prefsManager, "مسلسلات")
            4 -> CategoryWithSubsFragment.newInstance(filteredChannels, prefsManager, "موسيقى")
            5 -> FavoritesFragment.newInstance(prefsManager)
            6 -> RecentFragment.newInstance(prefsManager)
            else -> AllChannelsFragment.newInstance(filteredChannels, prefsManager)
        }
    }
    
    override fun getCount(): Int = categories.size
    
    override fun getPageTitle(position: Int): CharSequence = categories[position]
}
	